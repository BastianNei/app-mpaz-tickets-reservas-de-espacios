package cl.escuelamarcelapaz.reservas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.escuelamarcelapaz.reservas.ui.components.SchoolBottomBar
import cl.escuelamarcelapaz.reservas.ui.components.SchoolTopBar
import cl.escuelamarcelapaz.reservas.ui.navigation.AppPage
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import cl.escuelamarcelapaz.reservas.ui.navigation.bottomDestinations
import cl.escuelamarcelapaz.reservas.ui.navigation.homePage
import cl.escuelamarcelapaz.reservas.ui.navigation.isSecondaryPage
import cl.escuelamarcelapaz.reservas.ui.screens.ApplicantHomeScreen
import cl.escuelamarcelapaz.reservas.ui.screens.BlockScheduleScreen
import cl.escuelamarcelapaz.reservas.ui.screens.HistoryScreen
import cl.escuelamarcelapaz.reservas.ui.screens.InspectorHomeScreen
import cl.escuelamarcelapaz.reservas.ui.screens.MyRequestsScreen
import cl.escuelamarcelapaz.reservas.ui.screens.NewRequestScreen
import cl.escuelamarcelapaz.reservas.ui.screens.PendingRequestsScreen
import cl.escuelamarcelapaz.reservas.ui.screens.RequestReviewScreen
import cl.escuelamarcelapaz.reservas.ui.screens.SpaceDetailScreen
import cl.escuelamarcelapaz.reservas.ui.screens.SpaceManagementScreen
import cl.escuelamarcelapaz.reservas.ui.screens.SpacesScreen
import cl.escuelamarcelapaz.reservas.ui.theme.ReservaEspaciosColegioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservaEspaciosColegioTheme {
                ReservationsApp()
            }
        }
    }
}

@Composable
private fun ReservationsApp() {
    var role by rememberSaveable { mutableStateOf(AppRole.INSPECTORIA) }
    var currentPage by rememberSaveable { mutableStateOf(AppPage.INICIO_INSPECTORIA) }
    var selectedTab by rememberSaveable { mutableStateOf(AppPage.INICIO_SOLICITANTE) }
    var returnPage by rememberSaveable { mutableStateOf(AppPage.INICIO_SOLICITANTE) }

    val destinations = bottomDestinations(role)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SchoolTopBar(
                title = currentPage.title,
                role = role,
                canGoBack = currentPage.isSecondaryPage(),
                onBack = {
                    currentPage = returnPage
                    selectedTab = returnPage
                },
                onRoleSelected = { newRole ->
                    role = newRole
                    val startPage = homePage(newRole)
                    currentPage = startPage
                    selectedTab = startPage
                    returnPage = startPage
                }
            )
        },
        bottomBar = {
            SchoolBottomBar(
                destinations = destinations,
                selectedPage = selectedTab,
                onDestinationSelected = { page ->
                    currentPage = page
                    selectedTab = page
                    returnPage = page
                }
            )
        }
    ) { innerPadding ->
        AppPageContent(
            page = currentPage,
            onNavigate = { page ->
                returnPage = currentPage
                currentPage = page
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AppPageContent(
    page: AppPage,
    onNavigate: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    when (page) {
        AppPage.INICIO_SOLICITANTE -> ApplicantHomeScreen(
            onBrowseSpaces = { onNavigate(AppPage.ESPACIOS) },
            onNewRequest = { onNavigate(AppPage.NUEVA_SOLICITUD) },
            modifier = modifier
        )

        AppPage.ESPACIOS -> SpacesScreen(
            onSpaceSelected = { onNavigate(AppPage.DETALLE_ESPACIO) },
            modifier = modifier
        )

        AppPage.DETALLE_ESPACIO -> SpaceDetailScreen(
            onNewRequest = { onNavigate(AppPage.NUEVA_SOLICITUD) },
            modifier = modifier
        )

        AppPage.NUEVA_SOLICITUD -> NewRequestScreen(modifier)
        AppPage.MIS_SOLICITUDES -> MyRequestsScreen(modifier)
        AppPage.HISTORIAL -> HistoryScreen(modifier)

        AppPage.INICIO_INSPECTORIA -> InspectorHomeScreen(
            onOpenRequests = { onNavigate(AppPage.SOLICITUDES_PENDIENTES) },
            onManageSpaces = { onNavigate(AppPage.GESTION_ESPACIOS) },
            onManageBlocks = { onNavigate(AppPage.BLOQUEOS) },
            modifier = modifier
        )

        AppPage.SOLICITUDES_PENDIENTES -> PendingRequestsScreen(
            onRequestSelected = { onNavigate(AppPage.DETALLE_SOLICITUD) },
            modifier = modifier
        )

        AppPage.DETALLE_SOLICITUD -> RequestReviewScreen(modifier)
        AppPage.GESTION_ESPACIOS -> SpaceManagementScreen(
            onManageBlocks = { onNavigate(AppPage.BLOQUEOS) },
            modifier = modifier
        )

        AppPage.BLOQUEOS -> BlockScheduleScreen(modifier)
    }
}
