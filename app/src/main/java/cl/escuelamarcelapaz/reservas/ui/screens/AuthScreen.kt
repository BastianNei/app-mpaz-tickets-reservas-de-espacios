package cl.escuelamarcelapaz.reservas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.escuelamarcelapaz.reservas.ui.navigation.AppRole
import com.google.firebase.firestore.FirebaseFirestore

private fun validarRutChileno(rut: String): Boolean {
    val cleanRut = rut.replace(".", "").replace("-", "").uppercase()
    if (cleanRut.length < 8) return false
    val cuerpo = cleanRut.dropLast(1)
    val dvIngresado = cleanRut.last()
    var suma = 0
    var multiplicador = 2
    for (i in cuerpo.reversed()) {
        val digito = i.toString().toIntOrNull() ?: return false
        suma += digito * multiplicador
        multiplicador = if (multiplicador < 7) multiplicador + 1 else 2
    }
    val resto = 11 - (suma % 11)
    val dvEsperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.toString()[0]
    }
    return dvIngresado == dvEsperado
}

private fun validarPasswordRequisitos(pass: String): String? {
    if (pass.length < 8) return "La contraseña debe tener al menos 8 caracteres"
    if (!pass.any { it.isUpperCase() }) return "Debe contener al menos una letra mayúscula"
    if (pass.count { it.isDigit() } < 2) return "Debe contener al menos 2 números"
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe contener al menos 1 símbolo (ej: @, #, $, !)"
    return null
}

@Composable
fun AuthScreen(
    onLoginSuccess: (String, AppRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegister by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var forgotEmail by remember { mutableStateOf("") }
    var forgotMsg by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var rutError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var generalError by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun limpiarErrores() {
        firstNameError = ""
        lastNameError = ""
        rutError = ""
        emailError = ""
        passwordError = ""
        confirmPasswordError = ""
        generalError = ""
        successMsg = ""
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isRegister) "Registro de Solicitante" else "Acceso - Escuela Marcela Paz",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) firstName = it },
                    label = { Text("Nombre") },
                    isError = firstNameError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (firstNameError.isNotBlank()) Text(firstNameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) lastName = it },
                    label = { Text("Apellido") },
                    isError = lastNameError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (lastNameError.isNotBlank()) Text(lastNameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT (ej: 12345678-9)") },
                    isError = rutError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (rutError.isNotBlank()) Text(rutError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError.isNotBlank()) Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError.isNotBlank()) Text(passwordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPasswordError.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (confirmPasswordError.isNotBlank()) Text(confirmPasswordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (generalError.isNotBlank()) {
                Text(generalError, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (successMsg.isNotBlank()) {
                Text(successMsg, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    limpiarErrores()
                    var tieneError = false
                    if (isRegister) {
                        if (firstName.trim().length < 3) { firstNameError = "Mínimo 3 letras"; tieneError = true }
                        if (lastName.trim().length < 3) { lastNameError = "Mínimo 3 letras"; tieneError = true }
                        if (!validarRutChileno(rut)) { rutError = "RUT inválido"; tieneError = true }
                        if (!email.lowercase().endsWith("@gmail.com")) { emailError = "Debe ser @gmail.com"; tieneError = true }
                        val errPass = validarPasswordRequisitos(password)
                        if (errPass != null) { passwordError = errPass; tieneError = true }
                        else if (password != confirmPassword) { confirmPasswordError = "No coinciden"; tieneError = true }

                        if (tieneError) return@Button
                        isLoading = true
                        val db = FirebaseFirestore.getInstance()
                        db.collection("usuarios").document(email.lowercase()).get().addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                isLoading = false
                                emailError = "El correo ya está registrado"
                            } else {
                                val userMap = hashMapOf(
                                    "username" to email.lowercase(),
                                    "firstName" to firstName.trim(),
                                    "lastName" to lastName.trim(),
                                    "rut" to rut.trim(),
                                    "email" to email.lowercase(),
                                    "password" to password,
                                    "role" to AppRole.SOLICITANTE.name
                                )
                                db.collection("usuarios").document(email.lowercase()).set(userMap).addOnSuccessListener {
                                    isLoading = false
                                    successMsg = "Cuenta creada. Inicia sesión."
                                    isRegister = false
                                    password = ""
                                    confirmPassword = ""
                                }.addOnFailureListener { isLoading = false; generalError = "Error al registrar" }
                            }
                        }.addOnFailureListener { isLoading = false; generalError = "Error de conexión" }
                    } else {
                        if (email.isBlank()) { emailError = "Ingresa tu correo"; tieneError = true }
                        if (password.isBlank()) { passwordError = "Ingresa tu contraseña"; tieneError = true }
                        if (tieneError) return@Button

                        isLoading = true
                        val db = FirebaseFirestore.getInstance()
                        db.collection("usuarios").document(email.lowercase()).get().addOnSuccessListener { doc ->
                            isLoading = false
                            if (doc.exists() && doc.getString("password") == password) {
                                val userRoleStr = doc.getString("role") ?: AppRole.SOLICITANTE.name
                                val role = if (userRoleStr == AppRole.INSPECTORIA.name) AppRole.INSPECTORIA else AppRole.SOLICITANTE
                                onLoginSuccess(email.lowercase(), role)
                            } else {
                                generalError = "Correo o contraseña incorrectos"
                            }
                        }.addOnFailureListener { isLoading = false; generalError = "Error de red" }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Cargando..." else if (isRegister) "Registrarse" else "Iniciar Sesión")
            }

            if (!isRegister) {
                TextButton(onClick = { showForgotDialog = true }) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }

            TextButton(onClick = { isRegister = !isRegister; limpiarErrores() }) {
                Text(if (isRegister) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false; forgotMsg = "" },
            title = { Text("Recuperar Contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa tu correo registrado para verificar tu contraseña actual en el sistema:")
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (forgotMsg.isNotBlank()) {
                        Text(forgotMsg, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (forgotEmail.isBlank()) return@Button
                    FirebaseFirestore.getInstance().collection("usuarios").document(forgotEmail.trim().lowercase())
                        .get().addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val pass = doc.getString("password") ?: "No encontrada"
                                forgotMsg = "Tu contraseña es: $pass"
                            } else {
                                forgotMsg = "El correo no está registrado"
                            }
                        }
                }) {
                    Text("Consultar clave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false; forgotMsg = "" }) {
                    Text("Cerrar")
                }
            }
        )
    }
}