# Proyecto MPAZ Ticket - Colegio Marcela Paz

Repositorio del proyecto semestral para el Colegio Marcela Paz. La idea es reemplazar los avisos de pasillo y reclamos de palabra por una app movil donde se puedan reportar fallas y gestionar reservas de espacios.

## El Problema

En el colegio todo se pide hablando directamente con el encargado de TI o con los auxiliares. Esto genera varios problemas:
* No hay registro de cuando se solicito la mantencion ni quien la pidio.
* Muchas cosas se quedan sin resolver o se olvidan en el camino.
* No hay forma de priorizar lo urgente de lo que puede esperar.
* Los profesores y funcionarios no tienen como saber en que estado va su solicitud.

## La Solucion al problema

Crear la app movil **MPAZ Ticket** para que:
* **Los funcionarios** puedan crear un ticket reportando la falla (categoria, ubicacion, descripcion y foto opcional) y ver en que estado va (pendiente, en proceso, resuelto, etc.).
* **El encargado de TI e Inspectoria** tengan un panel para revisar, asignar encargados, dejar comentarios y gestionar el uso de salas.
* **El equipo de mantencion** vea directamente que tareas tiene asignadas.

## Lo que llevamos hecho en la app (Kotlin / Jetpack Compose)

Actualmente tenemos armada la estructura base de la interfaz y la navegacion por roles en Android:

### Navegacion y Roles
* Selector para cambiar entre el rol de **Solicitante** e **Inspectoria/TI**.
* Barras de navegacion superiores e inferiores que cambian segun el perfil actual.

### Pantallas creadas
* **Solicitante:** Inicio (`ApplicantHomeScreen`), catalogo de espacios (`SpacesScreen`), detalle de recinto (`SpaceDetailScreen`), formulario para nuevo ticket (`NewRequestScreen`), mis solicitudes (`MyRequestsScreen`) e historial (`HistoryScreen`).
* **Inspectoria / TI:** Panel principal (`InspectorHomeScreen`), bandeja de tickets pendientes (`PendingRequestsScreen`), detalle y asignacion de tickets (`RequestReviewScreen`), gestion de espacios (`SpaceManagementScreen`) y bloqueo de horarios (`BlockScheduleScreen`).
