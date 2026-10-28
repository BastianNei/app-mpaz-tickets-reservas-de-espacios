package cl.escuelamarcelapaz.reservas.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.components.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class DetalleEspacio(
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val capacidad: Int
)

val mapaDetalleEspacios = mapOf(
    "Biblioteca" to DetalleEspacio("Biblioteca", "Espacio silencioso enfocado en lectura, investigación y estudio grupal.", "Piso 1, Pabellón Principal", 35),
    "Laboratorio de computación" to DetalleEspacio("Laboratorio de computación", "Equipado con 30 ordenadores, proyector e internet de alta velocidad.", "Piso 2, Edificio B", 30),
    "Laboratorio de ciencias" to DetalleEspacio("Laboratorio de ciencias", "Cuenta con microscopios, mesones de trabajo y elementos de laboratorio.", "Piso 1, Edificio B", 25),
    "Sala sensorial" to DetalleEspacio("Sala sensorial", "Espacio acondicionado para estimulación cognitiva, relajación y PIE.", "Piso 1, Anexo A", 15),
    "Centro de Innovación CIEMP" to DetalleEspacio("Centro de Innovación CIEMP", "Espacio multifuncional para proyectos Maker, robótica e innovación pedagógica.", "Piso 2, Pabellón Central", 40),
    "Huerto escolar" to DetalleEspacio("Huerto escolar", "Área verde al aire libre para proyectos medioambientales y talleres ecológicos.", "Patio Trasero", 30),
    "Sala de capacitación" to DetalleEspacio("Sala de capacitación", "Espacio habilitado con proyector, pizarra interactiva y mobiliario modular para reuniones o talleres.", "Piso 2, Administración", 25)
)

val listaEspacios = mapaDetalleEspacios.keys.toList()

private val listaCursos = listOf(
    "1° Básico A", "1° Básico B",
    "2° Básico A", "2° Básico B",
    "3° Básico A", "3° Básico B",
    "4° Básico A", "4° Básico B",
    "5° Básico A", "5° Básico B",
    "6° Básico A", "6° Básico B",
    "7° Básico A", "7° Básico B",
    "8° Básico A", "8° Básico B"
)

data class Solicitud(
    val id: String = "",
    val espacio: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val actividad: String = "",
    val curso: String = "",
    val cantidadAsistentes: Int = 1,
    val estado: String = "Pendiente",
    val usuarioId: String = "",
    val solicitanteNombre: String = "",
    val solicitanteApellido: String = "",
    val solicitanteRut: String = "",
    val solicitanteEmail: String = "",
    val motivoRechazo: String = "",
    val editada: Boolean = false,
    val vistoPorSolicitante: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ApplicantHomeScreen(
    username: String = "",
    onNewRequest: () -> Unit,
    onOpenDocuments: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nombreCompleto by remember { mutableStateOf("Cargando...") }
    var fechaHoraActual by remember { mutableStateOf("") }
    var documentosNuevosCount by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_session", Context.MODE_PRIVATE) }

    LaunchedEffect(username) {
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

        db.collection("documentos_pdf").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val totalDocs = snapshot.size()
                val lastRead = prefs.getInt("last_read_docs_count", totalDocs)
                documentosNuevosCount = maxOf(0, totalDocs - lastRead)
            }
        }

        while (true) {
            val sdf = SimpleDateFormat("EEEE dd 'de' MMMM, yyyy - HH:mm:ss", Locale("es", "ES"))
            fechaHoraActual = sdf.format(Date()).replaceFirstChar { it.uppercase() }
            delay(1000L)
        }
    }

    ScrollablePage(modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("¡Bienvenido/a,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Informes y Documentos", style = MaterialTheme.typography.titleMedium)
                    if (documentosNuevosCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text("Nuevo ($documentosNuevosCount)")
                        }
                    }
                }
                Text(
                    "Consulta y lectura de reglamentos internos, manuales y reportes normativos en formato PDF publicados por la institución.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onOpenDocuments,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Informes y Documentos PDF")
                }
            }
        }

        InformationCard(
            title = "Aviso importante",
            description = "Recuerde solicitar los espacios con al menos 1 día hábil de anticipación para coordinar oportunamente con Inspectoría."
        )
    }
}

@Composable
fun SpacesScreen(
    onReserveSpace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var espaciosInfo by remember { mutableStateOf<Map<String, Triple<Boolean, String, String>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val dbRef = FirebaseFirestore.getInstance().collection("configuracion_espacios").document("estado")
        dbRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
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

                    if (huboCambios) {
                        val mapaPlano = mapaActualizado.mapValues { (_, triple) ->
                            listOf(triple.first, triple.second, triple.third)
                        }
                        dbRef.set(mapaPlano)
                    }

                    espaciosInfo = mapaActualizado
                }
            }
        }
    }

    ScrollablePage(modifier) {
        SectionHeading("Lista de Espacios", "Consulta características, ubicación y disponibilidad")

        listaEspacios.forEach { espacio ->
            val detalle = mapaDetalleEspacios[espacio]
            val info = espaciosInfo[espacio] ?: Triple(true, "", "")
            val disponible = info.first
            val motivo = info.second
            val fechaReactivacion = info.third

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(espacio, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    detalle?.let {
                        Text("- Ubicación: ${it.ubicacion}", style = MaterialTheme.typography.bodySmall)
                        Text("- Capacidad máxima: ${it.capacidad} personas", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        if (disponible) "Habilitado para reservas" else "En mantenimiento / Bloqueado",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    if (!disponible) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Motivo: $motivo", style = MaterialTheme.typography.bodySmall)
                        Text("• Disponible desde: $fechaReactivacion", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onReserveSpace(espacio) },
                        enabled = disponible,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (disponible) "Reservar este espacio" else "Espacio en mantenimiento")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    username: String = "",
    preselectedSpace: String = "",
    onSaveSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var espaciosInfo by remember { mutableStateOf<Map<String, Triple<Boolean, String, String>>>(emptyMap()) }

    var espacioSeleccionado by remember { mutableStateOf(preselectedSpace) }
    var cursoSeleccionado by remember { mutableStateOf(listaCursos[0]) }
    var cantidadAsistentesText by remember { mutableStateOf("25") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var actividad by remember { mutableStateOf("") }

    var errorActividad by remember { mutableStateOf(false) }
    var errorFecha by remember { mutableStateOf(false) }
    var errorHora by remember { mutableStateOf(false) }
    var errorEspacio by remember { mutableStateOf(false) }
    var errorAsistentes by remember { mutableStateOf(false) }
    var errorCapacidadMensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var menuCursoExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val dbRef = FirebaseFirestore.getInstance().collection("configuracion_espacios").document("estado")
        dbRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
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

                    if (huboCambios) {
                        val mapaPlano = mapaActualizado.mapValues { (_, triple) ->
                            listOf(triple.first, triple.second, triple.third)
                        }
                        dbRef.set(mapaPlano)
                    }

                    espaciosInfo = mapaActualizado
                }
            }
        }
    }

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
            fecha = "$year-$mes-$dia"
            errorFecha = false
        },
        mananaCalendar.get(Calendar.YEAR),
        mananaCalendar.get(Calendar.MONTH),
        mananaCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = mananaCalendar.timeInMillis
    }

    val timePickerDialogInicio = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            horaInicio = String.format("%02d:%02d", hourOfDay, minute)
            errorHora = false
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val timePickerDialogFin = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            horaFin = String.format("%02d:%02d", hourOfDay, minute)
            errorHora = false
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    ScrollablePage(modifier) {
        SectionHeading("Reservar un Espacio", "Seleccione el espacio, horario y datos del grupo")

        Text("Seleccionar Espacio:", style = MaterialTheme.typography.titleSmall)
        listaEspacios.forEach { esp ->
            val detalle = mapaDetalleEspacios[esp]
            val info = espaciosInfo[esp] ?: Triple(true, "", "")
            val disponible = info.first
            val motivo = info.second
            val fechaReactivacion = info.third

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (disponible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = espacioSeleccionado == esp,
                        onClick = {
                            if (disponible) {
                                espacioSeleccionado = esp
                                errorEspacio = false
                            }
                        },
                        enabled = disponible
                    )
                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                        Text(esp, style = MaterialTheme.typography.bodyLarge)
                        detalle?.let {
                            Text("- ${it.ubicacion} (Capacidad: ${it.capacidad} pers.)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!disponible) {
                            Text(
                                "En mantenimiento: $motivo (Disponible: $fechaReactivacion)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        if (errorEspacio) {
            Text(
                text = "Debe seleccionar un espacio válido que no esté en mantenimiento",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Curso / Grupo y Alumnos:", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(
                expanded = menuCursoExpanded,
                onExpandedChange = { menuCursoExpanded = !menuCursoExpanded },
                modifier = Modifier.weight(1.5f)
            ) {
                OutlinedTextField(
                    value = cursoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Curso") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCursoExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuCursoExpanded,
                    onDismissRequest = { menuCursoExpanded = false }
                ) {
                    listaCursos.forEach { itemCurso ->
                        DropdownMenuItem(
                            text = { Text(itemCurso) },
                            onClick = {
                                cursoSeleccionado = itemCurso
                                menuCursoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cantidadAsistentesText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        cantidadAsistentesText = it
                        errorAsistentes = false
                    }
                },
                label = { Text("N° Asistentes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorAsistentes,
                modifier = Modifier.weight(1f)
            )
        }
        if (errorAsistentes && errorCapacidadMensaje.isNotBlank()) {
            Text(
                text = errorCapacidadMensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (fecha.isBlank()) "Seleccionar Fecha" else "Fecha: $fecha")
        }
        if (errorFecha) {
            Text(
                text = "Debe seleccionar una fecha válida a partir de mañana (mínimo 1 día hábil de anticipación)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { timePickerDialogInicio.show() },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (horaInicio.isBlank()) "Hora Inicio" else "Inicio: $horaInicio")
            }
            OutlinedButton(
                onClick = { timePickerDialogFin.show() },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (horaFin.isBlank()) "Hora Fin" else "Fin: $horaFin")
            }
        }
        if (errorHora) {
            Text(
                text = "Debes seleccionar hora de inicio y fin",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = actividad,
            onValueChange = {
                actividad = it
                if (errorActividad) errorActividad = false
            },
            label = { Text("¿Qué actividad se realizara?") },
            isError = errorActividad,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorActividad) {
            Text(
                text = "La actividad debe tener al menos 6 caracteres",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                var hayError = false
                val cantAsistentes = cantidadAsistentesText.toIntOrNull() ?: 0

                if (espacioSeleccionado.isBlank()) {
                    errorEspacio = true
                    hayError = true
                } else {
                    val infoEspacio = espaciosInfo[espacioSeleccionado]
                    if (infoEspacio != null && !infoEspacio.first) {
                        errorEspacio = true
                        hayError = true
                    } else {
                        val detalleEspacio = mapaDetalleEspacios[espacioSeleccionado]
                        if (detalleEspacio != null && cantAsistentes > detalleEspacio.capacidad) {
                            errorAsistentes = true
                            errorCapacidadMensaje = "Supera la capacidad máxima de ${detalleEspacio.capacidad} personas"
                            hayError = true
                        }
                    }
                }

                if (cantAsistentes <= 0) {
                    errorAsistentes = true
                    errorCapacidadMensaje = "Ingresa una cantidad válida de alumnos"
                    hayError = true
                }

                if (fecha.isBlank()) {
                    errorFecha = true
                    hayError = true
                }
                if (horaInicio.isBlank() || horaFin.isBlank()) {
                    errorHora = true
                    hayError = true
                }
                if (actividad.trim().length < 6) {
                    errorActividad = true
                    hayError = true
                }
                if (hayError) return@Button

                cargando = true
                val db = FirebaseFirestore.getInstance()

                db.collection("usuarios").document(username.lowercase()).get()
                    .addOnSuccessListener { userDoc ->
                        val nombre = userDoc.getString("firstName") ?: userDoc.getString("username") ?: "No informado"
                        val apellido = userDoc.getString("lastName") ?: ""
                        val rutReal = userDoc.getString("rut") ?: "No informado"
                        val emailReal = userDoc.getString("email") ?: username

                        val datos = hashMapOf(
                            "espacio" to espacioSeleccionado,
                            "fecha" to fecha,
                            "horaInicio" to horaInicio,
                            "horaFin" to horaFin,
                            "actividad" to actividad.trim(),
                            "curso" to cursoSeleccionado,
                            "cantidadAsistentes" to cantAsistentes,
                            "estado" to "Pendiente",
                            "usuarioId" to username.lowercase(),
                            "solicitanteNombre" to nombre,
                            "solicitanteApellido" to apellido,
                            "solicitanteRut" to rutReal,
                            "solicitanteEmail" to emailReal,
                            "editada" to false,
                            "vistoPorSolicitante" to true,
                            "timestamp" to System.currentTimeMillis()
                        )

                        val idPersonalizado = "${espacioSeleccionado.replace(" ", "_")}_${fecha}_${horaInicio.replace(":", "")}"

                        db.collection("solicitudes").document(idPersonalizado).set(datos)
                            .addOnSuccessListener {
                                cargando = false
                                onSaveSuccess()
                            }
                            .addOnFailureListener {
                                cargando = false
                            }
                    }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Enviando..." else "Enviar Solicitud")
        }
    }
}

@Composable
fun MyRequestsScreen(
    username: String = "",
    onSelectRequest: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var listaSolicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var filtroEstado by remember { mutableStateOf("Todas") }

    LaunchedEffect(username) {
        val db = FirebaseFirestore.getInstance()
        db.collection("solicitudes")
            .whereEqualTo("usuarioId", username.lowercase())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listaSolicitudes = snapshot.documents.mapNotNull { doc ->
                        val solObj = doc.toObject(Solicitud::class.java)
                        val visto = doc.getBoolean("vistoPorSolicitante") ?: true
                        solObj?.copy(id = doc.id, vistoPorSolicitante = visto)
                    }.sortedByDescending { it.timestamp }
                }
            }
    }

    val solicitudesFiltradas = if (filtroEstado == "Todas") {
        listaSolicitudes
    } else {
        listaSolicitudes.filter { it.estado.equals(filtroEstado, ignoreCase = true) }
    }

    ScrollablePage(modifier) {
        SectionHeading("Historial de Solicitudes", "Estado actual de tus peticiones enviadas")

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

        Spacer(modifier = Modifier.height(4.dp))

        if (solicitudesFiltradas.isEmpty()) {
            Text("No hay solicitudes registradas con este filtro.")
        } else {
            solicitudesFiltradas.forEach { sol ->
                val esNovedadNoVista = !sol.vistoPorSolicitante

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esNovedadNoVista) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (esNovedadNoVista) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "¡NUEVA ACTUALIZACIÓN DE ESTADO!",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        ReservationCard(
                            activity = sol.actividad,
                            space = sol.espacio,
                            group = "${sol.curso} (${sol.cantidadAsistentes} alumnos)",
                            schedule = "${sol.fecha} (${sol.horaInicio} - ${sol.horaFin})",
                            status = sol.estado,
                            timestamp = sol.timestamp
                        )
                        if (sol.editada) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⚠️ Esta solicitud fue modificada por la administración", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                FirebaseFirestore.getInstance().collection("solicitudes").document(sol.id)
                                    .update("vistoPorSolicitante", true)
                                onSelectRequest(sol.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (sol.estado == "Pendiente") "Ver detalle / Editar" else "Ver detalle")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    username: String = "",
    onSelectRequest: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var solicitudesFinalizadas by remember { mutableStateOf<List<Solicitud>>(emptyList()) }

    LaunchedEffect(username) {
        val db = FirebaseFirestore.getInstance()
        db.collection("solicitudes")
            .whereEqualTo("usuarioId", username.lowercase())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val hace24Horas = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                    solicitudesFinalizadas = snapshot.documents.mapNotNull { doc ->
                        val solObj = doc.toObject(Solicitud::class.java)
                        val visto = doc.getBoolean("vistoPorSolicitante") ?: true
                        solObj?.copy(id = doc.id, vistoPorSolicitante = visto)
                    }.filter { it.estado != "Pendiente" && it.timestamp >= hace24Horas }
                        .sortedByDescending { it.timestamp }
                }
            }
    }

    ScrollablePage(modifier) {
        SectionHeading("Archivo", "Registro histórico de reservas resueltas (Últimas 24h)")

        if (solicitudesFinalizadas.isEmpty()) {
            Text("No tienes historial de solicitudes resueltas recientes.")
        } else {
            solicitudesFinalizadas.forEach { sol ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ReservationCard(
                            activity = sol.actividad,
                            space = sol.espacio,
                            group = "${sol.curso} (${sol.cantidadAsistentes} alumnos)",
                            schedule = "${sol.fecha} (${sol.horaInicio} - ${sol.horaFin})",
                            status = sol.estado,
                            timestamp = sol.timestamp
                        )
                        if (sol.editada) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⚠️ Esta solicitud fue modificada por la administración", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                FirebaseFirestore.getInstance().collection("solicitudes").document(sol.id)
                                    .update("vistoPorSolicitante", true)
                                onSelectRequest(sol.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver detalle")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequesterRequestDetailScreen(
    requestId: String = "",
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var solicitud by remember { mutableStateOf<Solicitud?>(null) }
    var editando by remember { mutableStateOf(false) }

    var nuevoEspacio by remember { mutableStateOf("") }
    var nuevoCurso by remember { mutableStateOf("") }
    var nuevosAsistentesText by remember { mutableStateOf("1") }
    var nuevaActividad by remember { mutableStateOf("") }
    var nuevaFecha by remember { mutableStateOf("") }
    var nuevaHoraInicio by remember { mutableStateOf("") }
    var nuevaHoraFin by remember { mutableStateOf("") }
    var menuCursoExpanded by remember { mutableStateOf(false) }
    var menuEspacioExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mes = String.format("%02d", month + 1)
            val dia = String.format("%02d", dayOfMonth)
            nuevaFecha = "$year-$mes-$dia"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialogInicio = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            nuevaHoraInicio = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val timePickerDialogFin = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            nuevaHoraFin = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    LaunchedEffect(requestId) {
        if (requestId.isNotBlank()) {
            val docRef = FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
            docRef.get().addOnSuccessListener { doc ->
                val solObj = doc.toObject(Solicitud::class.java)
                val visto = doc.getBoolean("vistoPorSolicitante") ?: true
                val sol = solObj?.copy(id = doc.id, vistoPorSolicitante = visto)
                solicitud = sol
                if (sol != null) {
                    nuevoEspacio = sol.espacio
                    nuevoCurso = sol.curso
                    nuevosAsistentesText = sol.cantidadAsistentes.toString()
                    nuevaActividad = sol.actividad
                    nuevaFecha = sol.fecha
                    nuevaHoraInicio = sol.horaInicio
                    nuevaHoraFin = sol.horaFin

                    docRef.update("vistoPorSolicitante", true)
                }
            }
        }
    }

    ScrollablePage(modifier) {
        SectionHeading("Detalle de la Solicitud", "Información completa de tu reserva")

        solicitud?.let { item ->
            if (item.editada) {
                InformationCard(
                    title = "Aviso",
                    description = "⚠️ Esta solicitud fue modificada por la administración."
                )
            }

            if (!editando) {
                InformationCard(
                    title = "Estado Actual",
                    description = "• Estado: ${item.estado}"
                )
                if (!item.motivoRechazo.isNullOrBlank() && item.estado == "Rechazada") {
                    InformationCard(
                        title = "Motivo de Rechazo",
                        description = item.motivoRechazo
                    )
                }
                InformationCard(
                    title = "Detalle de la Actividad",
                    description = "• Espacio: ${item.espacio}\n• Curso/Grupo: ${item.curso}\n• N° Asistentes: ${item.cantidadAsistentes} personas\n• Actividad: ${item.actividad}"
                )
                InformationCard(
                    title = "Fecha y Horario",
                    description = "• Fecha: ${item.fecha}\n• Horario: ${item.horaInicio} - ${item.horaFin}"
                )

                if (item.estado == "Pendiente") {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { editando = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar Solicitud")
                    }
                }
            } else {
                Text("Editar Espacio:", style = MaterialTheme.typography.titleSmall)
                ExposedDropdownMenuBox(
                    expanded = menuEspacioExpanded,
                    onExpandedChange = { menuEspacioExpanded = !menuEspacioExpanded }
                ) {
                    OutlinedTextField(
                        value = nuevoEspacio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Espacio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEspacioExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuEspacioExpanded,
                        onDismissRequest = { menuEspacioExpanded = false }
                    ) {
                        listaEspacios.forEach { esp ->
                            DropdownMenuItem(
                                text = { Text(esp) },
                                onClick = {
                                    nuevoEspacio = esp
                                    menuEspacioExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Editar Curso y Asistentes:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = menuCursoExpanded,
                        onExpandedChange = { menuCursoExpanded = !menuCursoExpanded },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        OutlinedTextField(
                            value = nuevoCurso,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Curso") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCursoExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuCursoExpanded,
                            onDismissRequest = { menuCursoExpanded = false }
                        ) {
                            listaCursos.forEach { curso ->
                                DropdownMenuItem(
                                    text = { Text(curso) },
                                    onClick = {
                                        nuevoCurso = curso
                                        menuCursoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nuevosAsistentesText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) nuevosAsistentesText = it
                        },
                        label = { Text("Asistentes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nuevaActividad,
                    onValueChange = { nuevaActividad = it },
                    label = { Text("Actividad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha: $nuevaFecha")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { timePickerDialogInicio.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Inicio: $nuevaHoraInicio")
                    }
                    OutlinedButton(
                        onClick = { timePickerDialogFin.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fin: $nuevaHoraFin")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (requestId.isNotBlank()) {
                            val actualizados = mapOf(
                                "espacio" to nuevoEspacio,
                                "curso" to nuevoCurso,
                                "cantidadAsistentes" to (nuevosAsistentesText.toIntOrNull() ?: 1),
                                "actividad" to nuevaActividad.trim(),
                                "fecha" to nuevaFecha,
                                "horaInicio" to nuevaHoraInicio,
                                "horaFin" to nuevaHoraFin,
                                "editada" to true
                            )
                            FirebaseFirestore.getInstance().collection("solicitudes").document(requestId)
                                .update(actualizados)
                                .addOnSuccessListener {
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

            Spacer(modifier = Modifier.height(8.dp))

            if (!editando) {
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
        } ?: Text("Cargando detalles...")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}