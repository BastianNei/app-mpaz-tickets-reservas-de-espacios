package cl.escuelamarcelapaz.reservas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.components.InformationCard
import cl.escuelamarcelapaz.reservas.ui.components.ScrollablePage
import cl.escuelamarcelapaz.reservas.ui.components.SectionHeading
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    username: String,
    role: AppRole,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("Cargando...") }
    var lastName by remember { mutableStateOf("Cargando...") }
    var rut by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf(username) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(username.lowercase())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fName = doc.getString("firstName")
                    val lName = doc.getString("lastName")
                    val uName = doc.getString("username")

                    if (!fName.isNullOrBlank()) {
                        firstName = fName
                        lastName = lName ?: ""
                    } else if (!uName.isNullOrBlank() && uName.contains(" ")) {
                        val partes = uName.split(" ", limit = 2)
                        firstName = partes[0]
                        lastName = partes.getOrNull(1) ?: ""
                    } else {
                        firstName = uName ?: username
                        lastName = ""
                    }

                    rut = doc.getString("rut") ?: "No informado"
                    email = doc.getString("email") ?: username
                } else {
                    firstName = username
                    lastName = ""
                    rut = "No informado"
                    email = username
                }
            }
            .addOnFailureListener {
                firstName = username
                lastName = ""
                rut = "No informado"
                email = username
            }
    }

    ScrollablePage(modifier) {
        SectionHeading("Perfil", "Información detallada del usuario actual")

        // Botón de Ajustes con la tuerca
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSettings() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            "Configuración de la App",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        InformationCard("Rol asignado", role.label)
        InformationCard("Nombre", firstName)
        InformationCard("Apellido", lastName)
        InformationCard("RUT", rut)
        InformationCard("Correo electrónico", email)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}