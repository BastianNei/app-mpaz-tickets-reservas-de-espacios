package cl.escuelamarcelapaz.reservas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

private val schoolSpaces = listOf(
    "Biblioteca",
    "Laboratorio de computación",
    "Laboratorio de ciencias",
    "Sala sensorial",
    "Centro de Innovación CIEMP",
    "Sala de reuniones o capacitación",
    "Huerto escolar"
)

@Composable
fun ApplicantHomeScreen(
    onBrowseSpaces: () -> Unit,
    onNewRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollablePage(modifier) {
        SectionHeading(
            title = "Hola, personal de la escuela",
            subtitle = "Consulta espacios compartidos y prepara una solicitud."
        )

        InformationCard(
            title = "Reserva de espacios",
            description = "Revisa la disponibilidad de salas y laboratorios antes de planificar tu actividad."
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryTile("Espacios", "Catálogo", Modifier.weight(1f))
            SummaryTile("Solicitudes", "Consulta estados", Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBrowseSpaces, modifier = Modifier.weight(1f)) {
                Text("Ver espacios")
            }
            OutlinedButton(onClick = onNewRequest, modifier = Modifier.weight(1f)) {
                Text("Nueva solicitud")
            }
        }

        SectionHeading("Ejemplo de próxima actividad", "Información ficticia para la maqueta")
        ReservationCard(
            activity = "Actividad de ciencias",
            space = "Laboratorio de ciencias",
            group = "4° básico · 28 participantes aprox.",
            schedule = "Fecha de ejemplo · 10:00 a 11:00",
            status = "Aprobada"
        )
    }
}

@Composable
private fun SummaryTile(title: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SpacesScreen(onSpaceSelected: () -> Unit, modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Espacios compartidos", "Selecciona un espacio para ver su ficha de ejemplo.")
        schoolSpaces.forEach { space ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(space, style = MaterialTheme.typography.titleMedium)
                    Text("Disponibilidad y características por mostrar", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = onSpaceSelected) { Text("Ver espacio") }
                }
            }
        }
    }
}

@Composable
fun SpaceDetailScreen(onNewRequest: () -> Unit, modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Ficha del espacio", "Ejemplo de detalle y disponibilidad")
        InformationCard("Laboratorio de computación", "Ubicación y capacidad: información de ejemplo por definir.")
        InformationCard("Horario de funcionamiento", "La vista de calendario y los bloques horarios se agregarán como maqueta.")
        Button(onClick = onNewRequest, modifier = Modifier.fillMaxWidth()) {
            Text("Preparar solicitud")
        }
    }
}

@Composable
fun NewRequestScreen(modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Nueva solicitud", "Maqueta visual del formulario")
        PlaceholderField("Espacio", "Seleccionar espacio")
        PlaceholderField("Fecha", "Seleccionar fecha")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlaceholderField("Hora de inicio", "00:00", Modifier.weight(1f))
            PlaceholderField("Hora de término", "00:00", Modifier.weight(1f))
        }
        PlaceholderField("Actividad", "Describe la actividad")
        PlaceholderField("Curso o grupo", "Ej.: 4° básico")
        PlaceholderField("Participantes aproximados", "Cantidad")
        PlaceholderField("Observaciones", "Opcional")
        InformationCard(
            title = "Datos de demostración",
            description = "La maqueta no envía solicitudes. Para el MVP se deben usar datos ficticios o anonimizados."
        )
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Enviar solicitud (maqueta)")
        }
    }
}

@Composable
fun MyRequestsScreen(modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Mis solicitudes", "Estados de ejemplo para la interfaz")
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
            status = "Aprobada"
        )
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    ScrollablePage(modifier) {
        SectionHeading("Historial", "Reservas anteriores de ejemplo")
        ReservationCard(
            activity = "Trabajo en computadores",
            space = "Laboratorio de computación",
            group = "6° básico · 30 participantes aprox.",
            schedule = "Registro ficticio · 09:00 a 10:00",
            status = "Aprobada"
        )
        ReservationCard(
            activity = "Taller de huerto",
            space = "Huerto escolar",
            group = "5° básico · 25 participantes aprox.",
            schedule = "Registro ficticio · 12:00 a 13:00",
            status = "Rechazada"
        )
    }
}
