package cl.escuelamarcelapaz.reservas.ui.navigation

enum class AppRole(val label: String) {
    SOLICITANTE("Personal solicitante"),
    INSPECTORIA("Inspectoría")
}

enum class AppPage(val title: String) {
    INICIO_SOLICITANTE("Inicio"),
    ESPACIOS("Espacios disponibles"),
    MIS_SOLICITUDES("Mis solicitudes"),
    HISTORIAL("Historial"),
    NUEVA_SOLICITUD("Nueva solicitud"),
    DETALLE_ESPACIO("Detalle del espacio"),
    INICIO_INSPECTORIA("Panel de Inspectoría"),
    SOLICITUDES_PENDIENTES("Solicitudes pendientes"),
    DETALLE_SOLICITUD("Revisar solicitud"),
    GESTION_ESPACIOS("Gestión de espacios"),
    BLOQUEOS("Bloqueos de horario")
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
        BottomDestination(AppPage.HISTORIAL, "Historial", "◷")
    )

    AppRole.INSPECTORIA -> listOf(
        BottomDestination(AppPage.INICIO_INSPECTORIA, "Inicio", "⌂"),
        BottomDestination(AppPage.SOLICITUDES_PENDIENTES, "Pendientes", "✓"),
        BottomDestination(AppPage.GESTION_ESPACIOS, "Espacios", "▦"),
        BottomDestination(AppPage.BLOQUEOS, "Bloqueos", "◷")
    )
}

fun AppPage.isSecondaryPage(): Boolean = when (this) {
    AppPage.NUEVA_SOLICITUD,
    AppPage.DETALLE_ESPACIO,
    AppPage.DETALLE_SOLICITUD -> true

    else -> false
}
