package cl.escuelamarcelapaz.reservas.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.navigation.AppPage
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import cl.escuelamarcelapaz.reservas.ui.navigation.BottomDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTopBar(
    title: String,
    role: AppRole,
    username: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (role == AppRole.INSPECTORIA) "Sistema de Reservas - Inspectoría" else "Sistema de Reservas - Solicitante",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = if (role == AppRole.INSPECTORIA) "Inspectoría" else "Solicitante",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun SchoolBottomBar(
    destinations: List<*>,
    selectedPage: AppPage,
    onDestinationSelected: (AppPage) -> Unit,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val page = when (destination) {
                is BottomDestination -> destination.page
                is AppPage -> destination
                else -> {
                    try {
                        destination?.javaClass?.getMethod("getPage")?.invoke(destination) as? AppPage
                            ?: AppPage.INICIO_SOLICITANTE
                    } catch (e: Exception) {
                        AppPage.INICIO_SOLICITANTE
                    }
                }
            }

            val label = when (destination) {
                is BottomDestination -> destination.label
                else -> page.title
            }

            val icon: ImageVector = when (page) {
                AppPage.INICIO_SOLICITANTE, AppPage.INICIO_INSPECTORIA -> Icons.Default.Home
                AppPage.ESPACIOS, AppPage.GESTION_ESPACIOS -> Icons.Default.List
                AppPage.NUEVA_SOLICITUD -> Icons.Default.DateRange
                AppPage.MIS_SOLICITUDES, AppPage.SOLICITUDES_PENDIENTES -> Icons.Default.Menu
                AppPage.HISTORIAL, AppPage.BLOQUEOS -> Icons.Default.DateRange
                AppPage.PERFIL -> Icons.Default.Person
                else -> Icons.Default.Home
            }

            NavigationBarItem(
                selected = selectedPage == page,
                onClick = { onDestinationSelected(page) },
                icon = {
                    if ((page == AppPage.SOLICITUDES_PENDIENTES || page == AppPage.MIS_SOLICITUDES) && pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge { Text("$pendingCount") }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = { Text(label) }
            )
        }
    }
}