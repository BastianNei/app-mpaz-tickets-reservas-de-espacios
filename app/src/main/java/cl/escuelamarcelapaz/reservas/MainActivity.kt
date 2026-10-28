package cl.escuelamarcelapaz.reservas

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.escuelamarcelapaz.reservas.ui.components.SchoolBottomBar
import cl.escuelamarcelapaz.reservas.ui.components.SchoolTopBar
import cl.escuelamarcelapaz.reservas.ui.navigation.AppPage
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import cl.escuelamarcelapaz.reservas.ui.navigation.bottomDestinations
import cl.escuelamarcelapaz.reservas.ui.navigation.homePage
import cl.escuelamarcelapaz.reservas.ui.screens.*
import cl.escuelamarcelapaz.reservas.ui.theme.ReservaEspaciosColegioTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("app_session", Context.MODE_PRIVATE) }
            val savedThemeStr = prefs.getString("app_theme", AppThemeOption.SYSTEM.name) ?: AppThemeOption.SYSTEM.name
            var currentThemeOption by remember { mutableStateOf(AppThemeOption.valueOf(savedThemeStr)) }

            val isDark = when (currentThemeOption) {
                AppThemeOption.SYSTEM -> isSystemInDarkTheme()
                AppThemeOption.LIGHT -> false
                AppThemeOption.DARK -> true
            }

            ReservaEspaciosColegioTheme(darkTheme = isDark) {
                MainFlow(
                    currentTheme = currentThemeOption,
                    onThemeChanged = { newTheme ->
                        currentThemeOption = newTheme
                        prefs.edit().putString("app_theme", newTheme.name).apply()
                    }
                )
            }
        }
    }
}

// LIMPIEZA AUTOMÁTICA TRAS 2 DÍAS (48H) SOLO PARA SOLICITUDES APROBADAS O RECHAZADAS
private fun limpiarSolicitudesAntiguasGlobal() {
    val db = FirebaseFirestore.getInstance()
    // 2 días en milisegundos (48 horas)
    val hace48Horas = System.currentTimeMillis() - (48 * 60 * 60 * 1000)

    db.collection("solicitudes")
        .whereLessThan("timestamp", hace48Horas)
        .get()
        .addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                val estado = doc.getString("estado") ?: ""
                // Solo elimina si ya están resueltas (Aprobada o Rechazada), manteniendo las Pendientes
                if (estado == "Aprobada" || estado == "Rechazada") {
                    db.collection("solicitudes").document(doc.id).delete()
                }
            }
        }
}

@Composable
private fun MainFlow(
    currentTheme: AppThemeOption,
    onThemeChanged: (AppThemeOption) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_session", Context.MODE_PRIVATE) }

    var savedUsername by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var savedRoleStr by remember { mutableStateOf(prefs.getString("role", "") ?: "") }

    var isLoggedIn by rememberSaveable { mutableStateOf(savedUsername.isNotBlank()) }
    var currentUserRole by rememberSaveable {
        mutableStateOf(if (savedRoleStr == AppRole.INSPECTORIA.name) AppRole.INSPECTORIA else AppRole.SOLICITANTE)
    }
    var currentUsername by rememberSaveable { mutableStateOf(savedUsername) }

    if (!isLoggedIn) {
        AuthScreen(
            onLoginSuccess = { username, role ->
                try {
                    prefs.edit().putString("username", username).putString("role", role.name).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                currentUsername = username
                currentUserRole = role
                isLoggedIn = true
            }
        )
    } else {
        ReservationsApp(
            username = currentUsername,
            userRole = currentUserRole,
            currentTheme = currentTheme,
            onThemeChanged = onThemeChanged,
            onLogout = {
                try {
                    prefs.edit().clear().apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isLoggedIn = false
            }
        )
    }
}

@Composable
private fun ReservationsApp(
    username: String,
    userRole: AppRole,
    currentTheme: AppThemeOption,
    onThemeChanged: (AppThemeOption) -> Unit,
    onLogout: () -> Unit
) {
    var currentPage by rememberSaveable { mutableStateOf(homePage(userRole)) }
    var selectedTab by rememberSaveable { mutableStateOf(homePage(userRole)) }
    var selectedRequestId by rememberSaveable { mutableStateOf("") }
    var preselectedSpace by rememberSaveable { mutableStateOf("") }
    var pendingCount by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_session", Context.MODE_PRIVATE) }

    // LIMPIEZA AUTOMÁTICA (48H, SOLO APROBADAS/RECHAZADAS) Y CONTADOR DINÁMICO
    LaunchedEffect(username, userRole) {
        limpiarSolicitudesAntiguasGlobal()
        val db = FirebaseFirestore.getInstance()
        if (userRole == AppRole.SOLICITANTE) {
            db.collection("solicitudes")
                .whereEqualTo("usuarioId", username.lowercase())
                .whereEqualTo("vistoPorSolicitante", false)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        pendingCount = snapshot.size()
                    }
                }
        } else {
            db.collection("solicitudes")
                .whereEqualTo("estado", "Pendiente")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        pendingCount = snapshot.size()
                    }
                }
        }
    }

    val destinations = bottomDestinations(userRole)
    val startHome = homePage(userRole)

    BackHandler(enabled = currentPage != startHome) {
        currentPage = startHome
        selectedTab = startHome
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SchoolTopBar(
                title = currentPage.title,
                role = userRole,
                username = username
            )
        },
        bottomBar = {
            SchoolBottomBar(
                destinations = destinations,
                selectedPage = selectedTab,
                pendingCount = pendingCount,
                onDestinationSelected = { page: AppPage ->
                    currentPage = page
                    selectedTab = page
                }
            )
        },
        floatingActionButton = {
            if (userRole == AppRole.SOLICITANTE && currentPage == AppPage.INICIO_SOLICITANTE) {
                FloatingActionButton(
                    onClick = {
                        preselectedSpace = ""
                        currentPage = AppPage.NUEVA_SOLICITUD
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva Solicitud"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .padding(innerPadding)
                .imePadding(),
            label = "PageTransition"
        ) { page ->
            AppPageContent(
                page = page,
                username = username,
                userRole = userRole,
                selectedRequestId = selectedRequestId,
                preselectedSpace = preselectedSpace,
                currentTheme = currentTheme,
                onThemeChanged = onThemeChanged,
                onSelectRequestId = { selectedRequestId = it },
                onPreselectedSpaceChanged = { preselectedSpace = it },
                onNavigate = { newPage ->
                    currentPage = newPage
                },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun AppPageContent(
    page: AppPage,
    username: String,
    userRole: AppRole,
    selectedRequestId: String,
    preselectedSpace: String,
    currentTheme: AppThemeOption,
    onThemeChanged: (AppThemeOption) -> Unit,
    onSelectRequestId: (String) -> Unit,
    onPreselectedSpaceChanged: (String) -> Unit,
    onNavigate: (AppPage) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (page) {
        AppPage.INICIO_SOLICITANTE -> ApplicantHomeScreen(
            username = username,
            onNewRequest = {
                onPreselectedSpaceChanged("")
                onNavigate(AppPage.NUEVA_SOLICITUD)
            },
            onOpenDocuments = { onNavigate(AppPage.DOCUMENTOS_PDF) },
            modifier = modifier
        )

        AppPage.ESPACIOS -> SpacesScreen(
            onReserveSpace = { espacio ->
                onPreselectedSpaceChanged(espacio)
                onNavigate(AppPage.NUEVA_SOLICITUD)
            },
            modifier = modifier
        )

        AppPage.NUEVA_SOLICITUD -> NewRequestScreen(
            username = username,
            preselectedSpace = preselectedSpace,
            onSaveSuccess = {
                onPreselectedSpaceChanged("")
                onNavigate(AppPage.MIS_SOLICITUDES)
            },
            modifier = modifier
        )

        AppPage.MIS_SOLICITUDES -> MyRequestsScreen(
            username = username,
            onSelectRequest = { id ->
                onSelectRequestId(id)
                onNavigate(AppPage.DETALLE_SOLICITUD_SOLICITANTE)
            },
            modifier = modifier
        )

        AppPage.HISTORIAL -> HistoryScreen(
            username = username,
            onSelectRequest = { id ->
                onSelectRequestId(id)
                onNavigate(AppPage.DETALLE_SOLICITUD_SOLICITANTE)
            },
            modifier = modifier
        )

        AppPage.DETALLE_SOLICITUD_SOLICITANTE -> RequesterRequestDetailScreen(
            requestId = selectedRequestId,
            onBack = { onNavigate(AppPage.MIS_SOLICITUDES) },
            modifier = modifier
        )

        AppPage.INICIO_INSPECTORIA -> InspectorHomeScreen(
            username = username,
            onOpenDocuments = { onNavigate(AppPage.DOCUMENTOS_PDF) },
            modifier = modifier
        )

        AppPage.SOLICITUDES_PENDIENTES -> PendingRequestsScreen(
            onSelectRequest = { id ->
                onSelectRequestId(id)
                onNavigate(AppPage.DETALLE_SOLICITUD)
            },
            onOpenAllHistory = { onNavigate(AppPage.BLOQUEOS) },
            modifier = modifier
        )

        AppPage.DETALLE_SOLICITUD -> RequestReviewScreen(
            requestId = selectedRequestId,
            onBack = {
                onNavigate(AppPage.SOLICITUDES_PENDIENTES)
            },
            modifier = modifier
        )

        AppPage.GESTION_ESPACIOS -> SpaceManagementScreen(
            onManageBlocks = { onNavigate(AppPage.BLOQUEOS) },
            modifier = modifier
        )

        AppPage.BLOQUEOS -> BlockScheduleScreen(
            onSelectRequest = { id ->
                onSelectRequestId(id)
                onNavigate(AppPage.DETALLE_SOLICITUD)
            },
            onBack = { onNavigate(AppPage.SOLICITUDES_PENDIENTES) },
            modifier = modifier
        )

        AppPage.DOCUMENTOS_PDF -> PdfDocumentsScreen(
            userRole = userRole,
            modifier = modifier
        )

        AppPage.PERFIL -> ProfileScreen(
            username = username,
            role = userRole,
            onLogout = onLogout,
            onOpenSettings = { onNavigate(AppPage.AJUSTES) },
            modifier = modifier
        )

        AppPage.AJUSTES -> SettingsScreen(
            currentTheme = currentTheme,
            onThemeSelected = onThemeChanged,
            onBack = { onNavigate(AppPage.PERFIL) },
            modifier = modifier
        )

        else -> {
            ApplicantHomeScreen(username = username, onNewRequest = { onNavigate(AppPage.NUEVA_SOLICITUD) }, onOpenDocuments = { onNavigate(AppPage.DOCUMENTOS_PDF) }, modifier = modifier)
        }
    }
}