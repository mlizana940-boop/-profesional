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

## Conectar a Firebase (paso pendiente por el estudiante)

La app configura Firebase **de forma programática** (no se usa `google-services.json`). Solo completa 2 valores:

1. Ve a la [consola de Firebase](https://console.firebase.google.com), crea un proyecto y agrega una **app Android** con `applicationId = com.example.profesional`.
2. En *Configuración del proyecto → Tus apps* copia el **ID del proyecto** y la **Clave de API (Web API Key)**.
3. Ábrelo en `app/src/main/java/com/example/profesional/FirebaseConfig.kt`:

```kotlin
const val PROJECT_ID = "mi-proyecto-12345"
const val API_KEY = "AIzaSy..."
```

4. Activa **Cloud Firestore** en la consola (creación de base de datos en modo de pruebas).
5. Aplica las reglas de desarrollo de [firestore.rules](firestore.rules):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /reservas/{reserva} {
      allow read, write: if true;
    }
  }
}
```

6. Crea los **índices compuestos** que solicite Firestore al filtrar (fecha + estado) o créalos manualmente en *Firestore → Índices*:
   - `fecha` ASC + `estado` ASC
   - `idCliente` ASC + `fecha` DESC (es el índice que usa la App Cliente)

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

El proyecto usa la misma configuración que tus lecciones anteriores: Kotlin + XML + Material, AGP `9.4.0-alpha07`, Gradle `9.6.0`, `compileSdk 37`, `minSdk 29`. La configuración de Firebase se hace sin `google-services.json` para que compile de inmediato.

## Probar

1. Abre el proyecto en Android Studio y pulsa **Run**.
2. Con la App Cliente crea una reserva → aparece aquí al filtrar por la fecha.
3. Acepta / rechaza / reprograma y verifica que la App Cliente refleja el cambio vía listener.