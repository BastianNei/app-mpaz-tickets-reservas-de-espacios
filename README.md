# Proyecto MPAZ Ticket - Colegio Marcela Paz
Repositorio del proyecto semestral para el Colegio Marcela Paz. La idea es reemplazar los avisos de pasillo y reclamos de palabra por una app móvil donde se puedan reportar fallas y gestionar reservas de espacios de manera digital y ordenada.
---
# Caso de Estudio y Contexto
Este proyecto fue desarrollado en el contexto de una solución tecnológica orientada a optimizar la gestión de espacios educativos comunes y la administración de requerimientos internos en la institución educativa. Tradicionalmente, los procesos se realizaban de manera informal (hablando directamente o con avisos de pasillo), lo que provocaba desorden, superposición de horarios y falta de trazabilidad.

La aplicación está construida nativamente para Android utilizando Jetpack Compose y Firebase Firestore para la sincronización en tiempo real, estructurándose bajo un modelo de doble rol:

Personal Solicitante (Profesores y Docentes): Permite explorar el catálogo de espacios, verificar disponibilidad, enviar reservas detalladas (curso, cantidad de alumnos, actividad), hacer seguimiento de peticiones y consultar documentos normativos en formato PDF.

Inspectoría General / TI: Panel administrativo para aprobar, rechazar o editar solicitudes, bloquear recintos por mantenimiento preventivo y gestionar informes institucionales.
---
#El Problema
En el colegio todo se pide hablando directamente con el encargado de TI o con los auxiliares. Esto genera varios problemas:

No hay registro de cuándo se solicitó la mantención ni quién la pidió.

Muchas cosas se quedan sin resolver o se olvidan en el camino.

No hay forma de priorizar lo urgente de lo que puede esperar.

Los profesores y funcionarios no tienen cómo saber en qué estado va su solicitud.

Superposición de horarios y reservas de espacios sin coordinación previa con la administración.
---
# La Solución al Problema
Crear la app móvil MPAZ Ticket para que:

Los funcionarios puedan crear solicitudes e ingresar tickets reportando fallas o reservas de espacios (categoría, ubicación, descripción, curso y cantidad de asistentes) y ver en qué estado van (pendiente, aprobada, rechazada).

El encargado de TI e Inspectoría tengan un panel para revisar, gestionar el estado de las peticiones, dejar motivos de rechazo y administrar la disponibilidad de las salas.

El equipo administrativo cuente con una limpieza automática de solicitudes resueltas tras 48 horas (2 días), manteniendo intactas las pendientes para su debida atención.

Lo que llevamos hecho en la app (Kotlin / Jetpack Compose)
Actualmente tenemos armada y sincronizada la estructura completa de la interfaz, la persistencia en la nube y la navegación por roles en Android:
---
# Navegacion y Roles
Sistema de autenticación seguro con registro validado de RUT chileno, dominios institucionales (@gmail.com) y requisitos estrictos de contraseña.

Selector y persistencia local de sesión para cambiar entre el rol de Solicitante e Inspectoría.

Barras de navegación superiores e inferiores adaptadas dinámicamente según el perfil actual, incorporando contadores en tiempo real para novedades y peticiones pendientes.
---
# Pantallas y Funcionalidades Implementadas
Solicitante:

Panel principal (ApplicantHomeScreen) con indicador de nuevos documentos PDF.

Catálogo de espacios (SpacesScreen) con verificación en tiempo real de disponibilidad por mantenimiento.

Formulario de nueva reserva (NewRequestScreen) con restricciones de calendario (mínimo desde el día siguiente) y validación de capacidades máximas por recinto.

Seguimiento de peticiones (MyRequestsScreen) con tarjetas dinámicas de novedad cuando la administración modifica o evalúa una solicitud.

Historial de reservas (HistoryScreen) y visor de detalle con opción de edición y eliminación manual.

Inspectoría / TI:

Panel general (InspectorHomeScreen) con control institucional.

Bandeja de solicitudes (PendingRequestsScreen) para aprobar o rechazar con motivo obligatorio.

Evaluación y revisión de antecedentes (RequestReviewScreen) con soporte para compartir comprobantes institucionales mediante aplicaciones externas.

Control de espacios y mantenimiento (SpaceManagementScreen) con restricciones de calendario a partir del día siguiente (minDate) y reactivación automática de recintos pasada la fecha y hora programada.

Historial general y reportes institucionales (BlockScheduleScreen).

Módulo de informes y documentos PDF (PdfDocumentsScreen) con subida y lectura sincronizada en tiempo real.

Ajustes de la aplicación (SettingsScreen) para alternar entre el tema del sistema, modo claro y modo oscuro.
