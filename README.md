# Aplicación B — Profesional (Caso 2: Reservas con Cloud Firestore)

Proyecto Android de la **Aplicación B (Profesional)** de la actividad integradora *Comunicación entre dos aplicaciones Android* — Caso 2 **Reservas de horas** con **Cloud Firestore**.

Compañero del proyecto **Cliente** (App A): ambas apps comparten la colección `reservas` del mismo proyecto Firebase.

## Funcionalidades implementadas

| RF | Descripción |
|----|-------------|
| RF-11 | Listado de reservas con **filtro por fecha** (DatePicker, por defecto hoy) |
| RF-12 | Filtro por **estado**: SOLICITADA, ACEPTADA, RECHAZADA, REPROGRAMADA (Spinner) |
| RF-13 | Detalle con todos los campos de la reserva |
| RF-14 | **Aceptar** reserva → estado `ACEPTADA` |
| RF-15 | **Rechazar** con observación obligatoria → estado `RECHAZADA` |
| RF-16 | **Reprogramar** con nueva fecha/hora → estado `REPROGRAMADA` (guarda `fechaNueva`, `horaNueva`) |
| RF-18 | Retroalimentación visual con Snackbar tras cada acción |
| RNF-02 | Capa `repository` para todo el acceso a Firestore |
| RNF-03 | Listeners removidos en `onDestroy` |
| RNF-06 | Mensaje "Sin conexión" al guardar sin red |

Errores cubiertos: ERR-01 (sin conexión), ERR-04 (reserva inexistente), ERR-05 (rechazo sin observación), ERR-06 (permiso denegado).

## Pantallas

```
P-PR01 Principal → P-PR02 Listado (fecha + estado + RecyclerView) → P-PR03 Detalle + acciones
```

Los botones **Aceptar / Rechazar / Reprogramar** solo se muestran cuando la reserva está `SOLICITADA` o `REPROGRAMADA`.

## Conectar a Firebase (con google-services.json)

La app usa el plugin `com.google.gms.google-services`, igual que la App Cliente. Firebase se inicializa solo a partir del archivo.

1. Pide al dueño del proyecto `reservas-h1-2026` que te agregue en *Project settings → Users and permissions → Add member* (rol **Editor**).
2. En la [consola de Firebase](https://console.firebase.google.com/) abre `reservas-h1-2026` → **Add app → Android**, con package `com.example.profesional_app_reservas` (sin Analytics).
3. Descarga `google-services.json` y cópialo en `app/google-services.json`.
4. Sincroniza Gradle y ejecuta la app.

Reglas de desarrollo en [firestore.rules](firestore.rules) (lectura y escritura abiertas, solo para desarrollo). Las que valen son las del proyecto en la consola: *Firestore → Reglas*.

Si Firestore pide un **índice compuesto** al filtrar por fecha y estado, créalo desde el enlace que aparece en el error o en *Firestore → Índices*.

## Contrato de datos (colección `reservas`)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| idCliente | string | UUID del cliente (App A) |
| nombreCliente | string | Nombre de quien reserva |
| servicioId / servicio | string | Ej. S01 / "Consulta general" |
| profesionalId / profesional | string | Ej. P01 / "Dr. Pérez" |
| fecha | string | YYYY-MM-DD |
| hora | string | HH:mm |
| estado | string | SOLICITADA, ACEPTADA, RECHAZADA, REPROGRAMADA |
| observacion | string | Notas o motivo del rechazo |
| fechaNueva / horaNueva | string | Solo si está REPROGRAMADA |
| creadoEn / actualizadoEn | timestamp | `serverTimestamp()` |

## Requisitos del entorno

Kotlin + XML + Material, AGP `9.4.0-alpha07`, Gradle `9.6.0`, `compileSdk 37`, `minSdk 29`. Firebase se configura con `google-services.json` (plugin `com.google.gms.google-services`), archivo que **debe estar en `app/`** para que el proyecto compile.

## Probar

1. Abre el proyecto en Android Studio y pulsa **Run**.
2. Con la App Cliente crea una reserva → aparece aquí al filtrar por la fecha.
3. Acepta / rechaza / reprograma y verifica que la App Cliente refleja el cambio vía listener.
