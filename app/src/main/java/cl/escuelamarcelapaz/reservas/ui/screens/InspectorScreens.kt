package cl.escuelamarcelapaz.reservas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.components.InformationCard
import cl.escuelamarcelapaz.reservas.ui.components.PlaceholderField
import cl.escuelamarcelapaz.reservas.ui.components.ReservationCard
import cl.escuelamarcelapaz.reservas.ui.components.ScrollablePage
import cl.escuelamarcelapaz.reservas.ui.components.SectionHeading

@Composable
fun InspectorHomeScreen(
    onOpenRequests: () -> Unit,
    onManageSpaces: () -> Unit,
    onManageBlocks: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollablePage(modifier) {
        SectionHeading("Panel de Inspectoría", "Vista de administración con información ficticia")
        InformationCard("Solicitudes pendientes", "Revisa las solicitudes de ejemplo y su horario.")
        InformationCard("Espacios compartidos", "Administra las fichas y disponibilidad de los espacios.")
        Button(onClick = onOpenRequests, modifier = Modifier.fillMaxWidth()) {
            Text("Ver solicitudes pendientes")
        }
        OutlinedButton(onClick = onManageSpaces, modifier = Modifier.fillMaxWidth()) {
            Text("Gestionar espacios")
        }
        OutlinedButton(onClick = onManageBlocks, modifier = Modifier.fillMaxWidth()) {
            Text("Ver bloqueos de horario")
        }
    }
}

@Composable
fun PendingRequestsScreen(onRequestSelected: () -> Unit, modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Solicitudes pendientes", "Selecciona una ficha para revisar su detalle de ejemplo.")
        ReservationCard(
            activity = "Actividad de ciencias",
            space = "Laboratorio de ciencias",
            group = "4° básico · 28 participantes aprox.",
            schedule = "Fecha de ejemplo · 10:00 a 11:00",
            status = "Pendiente"
        )
        ReservationCard(
            activity = "Lectura compartida",
            space = "Biblioteca",
            group = "3° básico · 24 participantes aprox.",
            schedule = "Fecha de ejemplo · 11:00 a 12:00",
            status = "Pendiente"
        )
        OutlinedButton(onClick = onRequestSelected, modifier = Modifier.fillMaxWidth()) {
            Text("Abrir detalle de ejemplo")
        }
    }
}

@Composable
fun RequestReviewScreen(modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Revisar solicitud", "Ficha de demostración para Inspectoría")
        InformationCard("Actividad", "Actividad de ciencias")
        InformationCard("Espacio y horario", "Laboratorio de ciencias · fecha de ejemplo · 10:00 a 11:00")
        InformationCard("Curso y participantes", "4° básico · 28 participantes aproximados")
        PlaceholderField("Motivo del rechazo", "Escribir una justificación")
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Aprobar (maqueta)")
        }
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Rechazar (maqueta)")
        }
        Text("Las acciones todavía no están conectadas a un sistema de reservas.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SpaceManagementScreen(onManageBlocks: () -> Unit, modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Gestión de espacios", "Catálogo de ejemplo para Inspectoría")
        listOf(
            "Biblioteca",
            "Laboratorio de computación",
            "Laboratorio de ciencias",
            "Sala sensorial",
            "Centro de Innovación CIEMP",
            "Huerto escolar"
        ).forEach { name ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text("Descripción, ubicación y capacidad por definir", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar espacio (maqueta)")
        }
        OutlinedButton(onClick = onManageBlocks, modifier = Modifier.fillMaxWidth()) {
            Text("Administrar bloqueos")
        }
    }
}

@Composable
fun BlockScheduleScreen(modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Bloqueos de horario", "Maqueta de bloqueo por mantenimiento o actividad institucional")
        PlaceholderField("Espacio", "Seleccionar espacio")
        PlaceholderField("Fecha", "Seleccionar fecha")
        PlaceholderField("Horario", "Seleccionar bloque horario")
        PlaceholderField("Motivo", "Mantenimiento o actividad institucional")
        InformationCard("Vista de calendario", "Aquí se mostrarán los horarios bloqueados.")
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar bloqueo (maqueta)")
        }
    }
}
