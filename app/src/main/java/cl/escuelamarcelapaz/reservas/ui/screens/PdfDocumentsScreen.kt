package cl.escuelamarcelapaz.reservas.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.components.InformationCard
import cl.escuelamarcelapaz.reservas.ui.components.ScrollablePage
import cl.escuelamarcelapaz.reservas.ui.components.SectionHeading
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import com.google.firebase.firestore.FirebaseFirestore

data class DocumentoPdf(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val url: String = "",
    val fechaSubida: String = ""
)

@Composable
fun PdfDocumentsScreen(
    userRole: AppRole,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var listaDocumentos by remember { mutableStateOf<List<DocumentoPdf>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDescripcion by remember { mutableStateOf("") }
    var nuevaUrlPdf by remember { mutableStateOf("") }

    val prefs = remember { context.getSharedPreferences("app_session", Context.MODE_PRIVATE) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            nuevaUrlPdf = it.toString()
        }
    }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("documentos_pdf")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listaDocumentos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DocumentoPdf::class.java)?.copy(id = doc.id)
                    }

                    if (userRole == AppRole.SOLICITANTE && listaDocumentos.isNotEmpty()) {
                        prefs.edit().putInt("last_read_docs_count", listaDocumentos.size).apply()
                    }
                }
            }
    }

    fun abrirPdf(url: String) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Documento PDF con:"))
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(webIntent)
        }
    }

    fun eliminarPdf(id: String) {
        FirebaseFirestore.getInstance().collection("documentos_pdf").document(id).delete()
    }

    ScrollablePage(modifier) {
        SectionHeading(
            "Control de Informes y Documentos",
            if (userRole == AppRole.INSPECTORIA) "Gestión de normativas, reportes e informes institucionales"
            else "Consulta y lectura de reglamentos e informes de uso"
        )

        if (userRole == AppRole.INSPECTORIA) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicar Nuevo Documento PDF")
            }
        }

        if (listaDocumentos.isEmpty()) {
            InformationCard(
                title = "Sin documentos",
                description = "Actualmente no hay reglamentos o informes PDF publicados."
            )
        } else {
            listaDocumentos.forEach { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.titulo, style = MaterialTheme.typography.titleMedium)
                                if (doc.descripcion.isNotBlank()) {
                                    Text(doc.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { abrirPdf(doc.url) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Leer PDF")
                            }

                            if (userRole == AppRole.INSPECTORIA) {
                                OutlinedButton(
                                    onClick = { eliminarPdf(doc.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Publicar Documento PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nuevoTitulo,
                        onValueChange = { nuevoTitulo = it },
                        label = { Text("Título del Documento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevaDescripcion,
                        onValueChange = { nuevaDescripcion = it },
                        label = { Text("Descripción / Categoría") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (nuevaUrlPdf.isBlank()) "Seleccionar Archivo PDF del dispositivo" else "PDF Seleccionado")
                    }
                    if (nuevaUrlPdf.isBlank()) {
                        OutlinedTextField(
                            value = nuevaUrlPdf,
                            onValueChange = { nuevaUrlPdf = it },
                            label = { Text("O bien, ingresa un enlace/URL web a un PDF") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoTitulo.isNotBlank() && nuevaUrlPdf.isNotBlank()) {
                            val nuevoDoc = hashMapOf(
                                "titulo" to nuevoTitulo.trim(),
                                "descripcion" to nuevaDescripcion.trim(),
                                "url" to nuevaUrlPdf.trim(),
                                "fechaSubida" to System.currentTimeMillis().toString()
                            )
                            FirebaseFirestore.getInstance().collection("documentos_pdf")
                                .add(nuevoDoc)
                                .addOnSuccessListener {
                                    showAddDialog = false
                                    nuevoTitulo = ""
                                    nuevaDescripcion = ""
                                    nuevaUrlPdf = ""
                                }
                        }
                    },
                    enabled = nuevoTitulo.isNotBlank() && nuevaUrlPdf.isNotBlank()
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}