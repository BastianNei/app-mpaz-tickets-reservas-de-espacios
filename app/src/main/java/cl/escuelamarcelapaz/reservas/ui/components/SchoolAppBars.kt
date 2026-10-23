package cl.escuelamarcelapaz.reservas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cl.escuelamarcelapaz.reservas.ui.navigation.AppPage
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import cl.escuelamarcelapaz.reservas.ui.navigation.BottomDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTopBar(
    title: String,
    role: AppRole,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onRoleSelected: (AppRole) -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (canGoBack) {
                TextButton(onClick = onBack) {
                    Text("Volver")
                }
            }
        },
        actions = {
            Box {
                TextButton(onClick = { roleMenuExpanded = true }) {
                    Text(role.label)
                }
                DropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false }
                ) {
                    AppRole.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleSelected(option)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SchoolBottomBar(
    destinations: List<BottomDestination>,
    selectedPage: AppPage,
    onDestinationSelected: (AppPage) -> Unit
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = selectedPage == destination.page,
                onClick = { onDestinationSelected(destination.page) },
                icon = { Text(destination.symbol, fontSize = 20.sp) },
                label = { Text(destination.label, maxLines = 1) }
            )
        }
    }
}
