package cl.escuelamarcelapaz.reservas.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScrollablePage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { it / 20 },
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .windowInsetsPadding(WindowInsets.ime),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InformationCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ReservationCard(
    activity: String,
    space: String,
    group: String,
    schedule: String,
    status: String,
    timestamp: Long = 0L
) {
    val tiempoTranscurrido = if (timestamp > 0L) {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutos = diffMs / (1000 * 60)
        val horas = minutos / 60
        when {
            horas > 0 -> "Creada hace $horas hora${if (horas > 1) "s" else ""}"
            minutos > 0 -> "Creada hace $minutos minuto${if (minutos > 1) "s" else ""}"
            else -> "Creada hace un momento"
        }
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(activity, style = MaterialTheme.typography.titleMedium)
            Text(space, style = MaterialTheme.typography.bodyMedium)
            Text(group, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(schedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (tiempoTranscurrido.isNotBlank()) {
                Text(tiempoTranscurrido, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            StatusPill(status)
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (container, content) = when (status) {
        "Aprobada" -> Color(0xFFE1F1E9) to Color(0xFF245B3F)
        "Rechazada" -> Color(0xFFF8E5E3) to Color(0xFF8C352D)
        else -> Color(0xFFFFF0D6) to Color(0xFF795417)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}