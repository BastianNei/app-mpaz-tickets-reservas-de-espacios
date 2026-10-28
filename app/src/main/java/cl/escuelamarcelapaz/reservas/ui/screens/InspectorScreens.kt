package cl.escuelamarcelapaz.reservas.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.components.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private fun limpiarSolicitudesAntiguas() {
    val db = FirebaseFirestore.getInstance()
    val hace48Horas = System.currentTimeMillis() - (48 * 60 * 60 * 1000)

    db.collection("solicitudes")
        .whereLessThan("timestamp", hace48Horas)
        .get()
        .addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                val estado = doc.getString("estado") ?: ""
                if (estado == "Aprobada" || estado == "Rechazada") {
                    db.collection("solicitudes").document(doc.id).delete()
                }
            }
        }
}

@Composable
fun InspectorHomeScreen(
    username: String = "",
    onOpenDocuments: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombreCompleto by remember { mutableStateOf("Cargando...") }
    var fechaHoraActual by remember { mutableStateOf("") }

    LaunchedEffect(username) {
        limpiarSolicitudesAntiguas()
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(username.lowercase())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val f = doc.getString("firstName") ?: ""
                    val l = doc.getString("lastName") ?: ""
                    nombreCompleto = if (f.isNotBlank()) "$f $l" else (doc.getString("username") ?: username)
                } else {
                    nombreCompleto = username
                }
            }

        while (true) {
            val sdf = SimpleDateFormat("EEEE dd 'de' MMMM, yyyy - HH:mm:ss", Locale("es", "ES"))
            fechaHoraActual = sdf.format(Date()).replaceFirstChar { it.uppercase() }
            delay(1000L)
        }
    }

    ScrollablePage(modifier) {
        SectionHeading("Administración General", "Control institucional")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Bienvendio/a al panel de Inspectoria,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(nombreCompleto, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📅 $fechaHoraActual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Sobre Documentos e Informes", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Sección destinada a la publicación, consulta y lectura de reglamentos internos, manuales y reportes normativos en formato PDF para el uso seguro de los espacios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onOpenDocuments,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Informes y Documentos Institucionales")
                }
            }
        }

        InformationCard("Gestión institucional", "Utiliza la barra inferior para revisar la bandeja de solicitudes o controlar la disponibilidad de los espacios.")
        InformationCard("Limpieza automática", "Las solicitudes aprobadas o rechazadas con más de 2 días de antigüedad se eliminan del sistema automáticamente.")
    }
}

@Composable
fun PendingRequestsScreen(
    onSelectRequest: (String) -> Unit,
    onOpenAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var solicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var filtroEstado by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) {
        limpiarSolicitudesAntiguas()
        val db = FirebaseFirestore.getInstance()
        db.collection("solicitudes")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    solicitudes = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                }
            }
    }

    val solicitudesFiltradas = if (filtroEstado == "Todas") {
        solicitudes
    } else {
        solicitudes.filter { it.estado.equals(filtroEstado, ignoreCase = true) }
    }

    ScrollablePage(modifier) {
        SectionHeading("Panel de Solicitudes", "Bandeja de peticiones y control de reservas")

        Button(
            onClick = onOpenAllHistory,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        ) {
            Text("Ver Historial General y Reportes PDF")
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todas", "Pendientes", "Aprobadas", "Rechazadas").forEach { estado ->
                FilterChip(
                    selected = filtroEstado == estado,
                    onClick = { filtroEstado = estado },
                    label = { Text(estado) }
                )
            }
        }

        if (solicitudesFiltradas.isEmpty()) {
            Text("No hay solicitudes registradas con este filtro.")
        } else {
            solicitudesFiltradas.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ReservationCard(
                            activity = item.actividad,
                            space = item.espacio,
                            group = "${item.curso} (${item.cantidadAsistentes} asist.) · Prof. ${item.solicitanteNombre} ${item.solicitanteApellido}",
                            schedule = "${item.fecha} (${item.horaInicio} - ${item.horaFin})",
                            status = item.estado,
                            timestamp = item.timestamp
                        )
                        if (item.editada) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⚠️ Esta solicitud fue editada previamente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onSelectRequest(item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (item.estado == "Pendiente") "Revisar / Gestionar solicitud" else "Gestionar estado / Editar solicitud")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestReviewScreen(
    requestId: String = "",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var solicitud by remember { mutableStateOf<Solicitud?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    var editando by remember { mutableStateOf(false) }
    var nuevoEstado by remember { mutableStateOf("Aprobada") }

    LaunchedEffect(requestId) {
        if (requestId.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                .get()
                .addOnSuccessListener { doc ->
                    val sol = doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                    solicitud = sol
                    if (sol != null) {
                        nuevoEstado = sol.estado
                        motivoRechazo = sol.motivoRechazo
                    }
                }
        }
    }

    fun compartirResumenSolicitud(sol: Solicitud) {
        val textoComprobante = """
              *COMPROBANTE DE RESERVA - ESCUELA MARCELA PAZ*
            --------------------------------------------------
            • *Estado:* ${sol.estado}
            • *Espacio:* ${sol.espacio}
            • *Solicitante:* ${sol.solicitanteNombre} ${sol.solicitanteApellido} (RUT: ${sol.solicitanteRut})
            • *Curso/Grupo:* ${sol.curso} (${sol.cantidadAsistentes} participantes)
            • *Fecha:* ${sol.fecha}
            • *Horario:* ${sol.horaInicio} - ${sol.horaFin}
            • *Actividad:* ${sol.actividad}
            ${if (sol.estado == "Rechazada" && sol.motivoRechazo.isNotBlank()) "• *Motivo Rechazo:* ${sol.motivoRechazo}" else ""}
            --------------------------------------------------
            -Emitido por Inspectoría General-
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoComprobante)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Comprobante de Reserva"))
    }

    ScrollablePage(modifier) {
        SectionHeading("Evaluación", "Revisión de antecedentes y disponibilidad")

        solicitud?.let { item ->
            if (item.editada) {
                Text("⚠️ Esta solicitud fue editada previamente", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            InformationCard(
                title = "Datos del Solicitante",
                description = "• Nombre: ${item.solicitanteNombre}\n• Apellido: ${item.solicitanteApellido}\n• RUT: ${item.solicitanteRut}\n• Correo: ${item.solicitanteEmail}"
            )
            InformationCard(
                title = "Detalle de la Actividad",
                description = "• Espacio: ${item.espacio}\n• Curso/Grupo: ${item.curso}\n• N° Asistentes: ${item.cantidadAsistentes} personas\n• Actividad: ${item.actividad}"
            )
            InformationCard(
                title = "Fecha y Horario",
                description = "• Fecha: ${item.fecha}\n• Horario: ${item.horaInicio} - ${item.horaFin}"
            )
            if (item.estado == "Rechazada" && !item.motivoRechazo.isNullOrBlank()) {
                InformationCard(
                    title = "Motivo de Rechazo Actual",
                    description = item.motivoRechazo
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!editando) {
                if (item.estado != "Pendiente") {
                    Button(
                        onClick = { compartirResumenSolicitud(item) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Compartir Comprobante")
                    }

                    OutlinedButton(
                        onClick = { editando = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar Estado / Gestionar Solicitud")
                    }
                }
            } else {
                Text("Cambiar Estado:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { nuevoEstado = "Aprobada" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (nuevoEstado == "Aprobada") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aprobada")
                    }
                    Button(
                        onClick = { nuevoEstado = "Rechazada" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (nuevoEstado == "Rechazada") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazada")
                    }
                }

                if (nuevoEstado == "Rechazada") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = {
                            motivoRechazo = it
                            if (errorMotivo) errorMotivo = false
                        },
                        label = { Text("Motivo de rechazo (Mín. 5 caracteres)") },
                        isError = errorMotivo,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMotivo) {
                        Text(
                            text = "El motivo debe tener al menos 5 caracteres",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (nuevoEstado == "Rechazada" && motivoRechazo.trim().length < 5) {
                            errorMotivo = true
                            return@Button
                        }
                        if (requestId.isNotBlank()) {
                            cargando = true
                            val actualizaciones = mutableMapOf<String, Any>(
                                "estado" to nuevoEstado,
                                "editada" to true,
                                "vistoPorSolicitante" to false
                            )
                            if (nuevoEstado == "Rechazada") {
                                actualizaciones["motivoRechazo"] = motivoRechazo.trim()
                            } else {
                                actualizaciones["motivoRechazo"] = ""
                            }

                            FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                                .update(actualizaciones)
                                .addOnSuccessListener {
                                    cargando = false
                                    editando = false
                                    FirebaseFirestore.getInstance().collection("solicitudes").document(requestId).get()
                                        .addOnSuccessListener { doc ->
                                            solicitud = doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                                        }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
                TextButton(onClick = { editando = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar edición")
                }
            }

        } ?: Text("Cargando información de la solicitud...")

        Spacer(modifier = Modifier.height(12.dp))

        if (!editando) {
            if (solicitud?.estado == "Pendiente") {
                Button(
                    onClick = {
                        if (requestId.isNotBlank()) {
                            cargando = true
                            FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                                .update(
                                    mapOf(
                                        "estado" to "Aprobada",
                                        "vistoPorSolicitante" to false
                                    )
                                )
                                .addOnSuccessListener {
                                    cargando = false
                                    onBack()
                                }
                        } else {
                            onBack()
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargando) "Guardando..." else "Aprobar")
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = motivoRechazo,
                    onValueChange = {
                        motivoRechazo = it
                        if (errorMotivo) errorMotivo = false
                    },
                    label = { Text("Motivo de rechazo") },
                    isError = errorMotivo,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMotivo) {
                    Text(
                        text = "El motivo debe tener al menos 5 caracteres",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        if (motivoRechazo.trim().length < 5) {
                            errorMotivo = true
                            return@OutlinedButton
                        }
                        if (requestId.isNotBlank()) {
                            cargando = true
                            FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                                .update(
                                    mapOf(
                                        "estado" to "Rechazada",
                                        "motivoRechazo" to motivoRechazo.trim(),
                                        "vistoPorSolicitante" to false
                                    )
                                )
                                .addOnSuccessListener {
                                    cargando = false
                                    onBack()
                                }
                        } else {
                            onBack()
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rechazar")
                }
            }

            OutlinedButton(
                onClick = {
                    if (requestId.isNotBlank()) {
                        FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                            .delete()
                            .addOnSuccessListener { onBack() }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar Solicitud")
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun SpaceManagementScreen(
    onManageBlocks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var espaciosInfo by remember {
        mutableStateOf(mapaDetalleEspacios.keys.associateWith { Triple(true, "", "") })
    }

    LaunchedEffect(Unit) {
        val dbRef = FirebaseFirestore.getInstance().collection("configuracion_espacios").document("estado")
        dbRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val data = doc.data as? Map<String, List<Any>>
                    if (data != null) {
                        val mapaCrudo = data.mapValues { (_, value) ->
                            Triple(
                                value.getOrNull(0) as? Boolean ?: true,
                                value.getOrNull(1) as? String ?: "",
                                value.getOrNull(2) as? String ?: ""
                            )
                        }

                        val ahoraMillis = System.currentTimeMillis()
                        val sdfVerificacion = SimpleDateFormat("yyyy-MM-dd 'a las' HH:mm", Locale("es", "ES"))
                        var huboCambios = false

                        val mapaActualizado = mapaCrudo.mapValues { (_, info) ->
                            val disponible = info.first
                            val fechaReactivacionStr = info.third
                            if (!disponible && fechaReactivacionStr.isNotBlank()) {
                                try {
                                    val fechaReactivacionDate = sdfVerificacion.parse(fechaReactivacionStr)
                                    if (fechaReactivacionDate != null && ahoraMillis >= fechaReactivacionDate.time) {
                                        huboCambios = true
                                        Triple(true, "", "")
                                    } else {
                                        info
                                    }
                                } catch (e: Exception) {
                                    info
                                }
                            } else {
                                info
                            }
                        }

                        val mapaCompleto = mapaDetalleEspacios.keys.associateWith { esp ->
                            mapaActualizado[esp] ?: Triple(true, "", "")
                        }

                        if (huboCambios || mapaCompleto.size != mapaActualizado.size) {
                            val mapaPlano = mapaCompleto.mapValues { (_, triple) ->
                                listOf(triple.first, triple.second, triple.third)
                            }
                            dbRef.set(mapaPlano)
                        }

                        espaciosInfo = mapaCompleto
                    }
                }
            }
    }

    fun guardarEstadoEnNube(nuevoMapa: Map<String, Triple<Boolean, String, String>>) {
        val mapaPlano = nuevoMapa.mapValues { (_, triple) ->
            listOf(triple.first, triple.second, triple.third)
        }
        FirebaseFirestore.getInstance().collection("configuracion_espacios").document("estado")
            .set(mapaPlano)
    }

    var espacioSeleccionadoParaBloquear by remember { mutableStateOf<String?>(null) }
    var motivoBloqueo by remember { mutableStateOf("") }
    var fechaDisponible by remember { mutableStateOf("") }
    var horaDisponible by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val mananaCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mes = String.format("%02d", month + 1)
            val dia = String.format("%02d", dayOfMonth)
            fechaDisponible = "$year-$mes-$dia"
        },
        mananaCalendar.get(Calendar.YEAR),
        mananaCalendar.get(Calendar.MONTH),
        mananaCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = mananaCalendar.timeInMillis
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            horaDisponible = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    ScrollablePage(modifier) {
        SectionHeading("Gestion de Espacios", "Administración de disponibilidad y mantenimiento")

        espaciosInfo.forEach { (nombreEspacio, info) ->
            val detalle = mapaDetalleEspacios[nombreEspacio]
            val disponible = info.first
            val motivo = info.second
            val fechaReactivacion = info.third

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nombreEspacio, style = MaterialTheme.typography.titleMedium)
                            detalle?.let {
                                Text("- ${it.ubicacion} · Capacidad: ${it.capacidad} pers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (disponible) "Habilitado para reservas" else "Bloqueado / En mantenimiento",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            if (!disponible) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Motivo: $motivo", style = MaterialTheme.typography.bodySmall)
                                Text("• Disponible desde: $fechaReactivacion", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Switch(
                            checked = disponible,
                            onCheckedChange = { newState ->
                                if (!newState) {
                                    espacioSeleccionadoParaBloquear = nombreEspacio
                                    motivoBloqueo = ""
                                    fechaDisponible = ""
                                    horaDisponible = ""
                                } else {
                                    val nuevoMapa = espaciosInfo.toMutableMap().apply {
                                        this[nombreEspacio] = Triple(true, "", "")
                                    }
                                    espaciosInfo = nuevoMapa
                                    guardarEstadoEnNube(nuevoMapa)
                                }
                            }
                        )
                    }
                }
            }
        }

        espacioSeleccionadoParaBloquear?.let { espacio ->
            AlertDialog(
                onDismissRequest = { espacioSeleccionadoParaBloquear = null },
                title = { Text("Bloquear $espacio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = motivoBloqueo,
                            onValueChange = { motivoBloqueo = it },
                            label = { Text("Motivo del mantenimiento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (fechaDisponible.isBlank()) "Seleccionar fecha de habilitación" else "Fecha: $fechaDisponible")
                        }
                        OutlinedButton(
                            onClick = { timePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (horaDisponible.isBlank()) "Seleccionar hora de habilitación" else "Hora: $horaDisponible")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (motivoBloqueo.isNotBlank() && fechaDisponible.isNotBlank() && horaDisponible.isNotBlank()) {
                                val fechaHoraCompleta = "$fechaDisponible a las $horaDisponible"
                                val nuevoMapa = espaciosInfo.toMutableMap().apply {
                                    this[espacio] = Triple(false, motivoBloqueo, fechaHoraCompleta)
                                }
                                espaciosInfo = nuevoMapa
                                guardarEstadoEnNube(nuevoMapa)
                                espacioSeleccionadoParaBloquear = null
                            }
                        },
                        enabled = motivoBloqueo.isNotBlank() && fechaDisponible.isNotBlank() && horaDisponible.isNotBlank()
                    ) {
                        Text("Confirmar Bloqueo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { espacioSeleccionadoParaBloquear = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun BlockScheduleScreen(
    onSelectRequest: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var todasLasSolicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var filtroEstado by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) {
        limpiarSolicitudesAntiguas()
        val db = FirebaseFirestore.getInstance()
        db.collection("solicitudes").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                todasLasSolicitudes = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.timestamp }
            }
        }
    }

    val solicitudesFiltradas = if (filtroEstado == "Todas") {
        todasLasSolicitudes
    } else {
        todasLasSolicitudes.filter { it.estado.equals(filtroEstado, ignoreCase = true) }
    }

    fun compartirReporteGeneral() {
        val total = todasLasSolicitudes.size
        val aprobadas = todasLasSolicitudes.count { it.estado == "Aprobada" }
        val pendientes = todasLasSolicitudes.count { it.estado == "Pendiente" }
        val rechazadas = todasLasSolicitudes.count { it.estado == "Rechazada" }

        val cuerpoReporte = StringBuilder()
        cuerpoReporte.append(" *REPORTE GENERAL DE RESERVAS - ESCUELA MARCELA PAZ*\n")
        cuerpoReporte.append("--------------------------------------------------\n")
        cuerpoReporte.append("• Total Solicitudes: $total\n")
        cuerpoReporte.append("• Aprobadas: $aprobadas | Pendientes: $pendientes | Rechazadas: $rechazadas\n")
        cuerpoReporte.append("--------------------------------------------------\n\n")

        solicitudesFiltradas.take(10).forEachIndexed { idx, item ->
            cuerpoReporte.append("${idx + 1}. [${item.estado.uppercase()}] ${item.espacio}\n")
            cuerpoReporte.append("   - Solicitante: ${item.solicitanteNombre} ${item.solicitanteApellido} (${item.curso})\n")
            cuerpoReporte.append("   - Horario: ${item.fecha} (${item.horaInicio} - ${item.horaFin})\n\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, cuerpoReporte.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Exportar Reporte General"))
    }

    ScrollablePage(modifier) {
        SectionHeading("Historial General", "Registro completo de todas las solicitudes institucionales")

        Button(
            onClick = { compartirReporteGeneral() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Compartir Reporte Resumen")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todas", "Pendiente", "Aprobada", "Rechazada").forEach { estado ->
                FilterChip(
                    selected = filtroEstado == estado,
                    onClick = { filtroEstado = estado },
                    label = { Text(estado) }
                )
            }
        }

        if (solicitudesFiltradas.isEmpty()) {
            Text("No hay registros que coincidan con el filtro seleccionado.")
        } else {
            solicitudesFiltradas.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ReservationCard(
                            activity = item.actividad,
                            space = item.espacio,
                            group = "${item.curso} (${item.cantidadAsistentes} asist.) · Solicitante: ${item.solicitanteNombre} ${item.solicitanteApellido}",
                            schedule = "${item.fecha} (${item.horaInicio} - ${item.horaFin})",
                            status = item.estado,
                            timestamp = item.timestamp
                        )
                        if (item.editada) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⚠️ Esta solicitud fue editada previamente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onSelectRequest(item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (item.estado == "Pendiente") "Ver detalle / Gestionar" else "Gestionar estado / Editar solicitud")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a Solicitudes")
        }
    }
}