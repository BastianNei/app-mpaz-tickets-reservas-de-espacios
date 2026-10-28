package cl.escuelamarcelapaz.reservas.ui.navigation

enum class AppRole(val label: String) {
    SOLICITANTE("Personal solicitante"),
    INSPECTORIA("Inspectoría")
}

enum class AppPage(val title: String) {
    INICIO_SOLICITANTE("Panel Principal"),
    ESPACIOS("Catálogo de Espacios"),
    MIS_SOLICITUDES("Seguimiento"),
    HISTORIAL("Archivo de Reservas"),
    NUEVA_SOLICITUD("Formulario de Reserva"),
    DETALLE_ESPACIO("Características"),
    DETALLE_SOLICITUD_SOLICITANTE("Detalle de Petición"),
    INICIO_INSPECTORIA("Panel General"),
    SOLICITUDES_PENDIENTES("Gestión de Solicitudes"),
    DETALLE_SOLICITUD("Evaluación"),
    GESTION_ESPACIOS("Control de Espacios"),
    BLOQUEOS("Reportes y Archivo"),
    PERFIL("Mi Cuenta"),
    AJUSTES("Ajustes y Preferencias"),
    DOCUMENTOS_PDF("Informes y Documentos")
}

data class BottomDestination(
    val page: AppPage,
    val label: String,
    val symbol: String
)

fun homePage(role: AppRole): AppPage = when (role) {
    AppRole.SOLICITANTE -> AppPage.INICIO_SOLICITANTE
    AppRole.INSPECTORIA -> AppPage.INICIO_INSPECTORIA
}

fun bottomDestinations(role: AppRole): List<BottomDestination> = when (role) {
    AppRole.SOLICITANTE -> listOf(
        BottomDestination(AppPage.INICIO_SOLICITANTE, "Inicio", "⌂"),
        BottomDestination(AppPage.ESPACIOS, "Espacios", "▦"),
        BottomDestination(AppPage.MIS_SOLICITUDES, "Solicitudes", "☷"),
        BottomDestination(AppPage.PERFIL, "Perfil", "👤")
    )

    AppRole.INSPECTORIA -> listOf(
        BottomDestination(AppPage.INICIO_INSPECTORIA, "Inicio", "⌂"),
        BottomDestination(AppPage.SOLICITUDES_PENDIENTES, "Solicitudes", "✓"),
        BottomDestination(AppPage.GESTION_ESPACIOS, "Espacios", "▦"),
        BottomDestination(AppPage.PERFIL, "Perfil", "👤")
    )
}

fun AppPage.isSecondaryPage(): Boolean = when (this) {
    AppPage.NUEVA_SOLICITUD,
    AppPage.DETALLE_ESPACIO,
    AppPage.DETALLE_SOLICITUD,
    AppPage.DETALLE_SOLICITUD_SOLICITANTE,
    AppPage.BLOQUEOS,
    AppPage.DOCUMENTOS_PDF -> true

    else -> false
}