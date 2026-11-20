# Notas técnicas y cierre de hardening

Registro cronológico de decisiones técnicas, verificación y trazabilidad del repo `limpio_model`. PostgreSQL (Lemoria) es la fuente de verdad; este archivo es su reflejo legible en el repositorio.

---

## Flujo de trabajo SDD con Lemoria (manual de traspaso)

Esta sección explica, paso a paso y con comandos reales, cómo se trabaja con **SDD** (Specification/Software Design Driven Development, desarrollo guiado por especificación/diseño) usando la herramienta **Lemoria** (`lemoria`, CLI instalada en el entorno). Está pensada para quien no conoce el flujo.

### Identificadores de este trabajo de documentación

Inclúyelos en reportes y trazabilidad cuando corresponda:

| Identificador | Valor |
|---|---|
| `project-id` | `136316a2-36dd-43b5-aa80-05b9e97d3f22` |
| `prd-id` | `c46eb0c9-d681-40b3-99fb-c2336db498d6` |
| `conv-id` | `46c48af9-f33b-4546-a4b0-96393bcf6eeb` |
| `task-id` (auditoría) | `f4b1d51e-13e2-4878-9164-38b2bda8b9f6` |
| `task-id` (GUIA_ESTUDIO) | `4ad8e55f-e7bd-46bc-925e-f75f64593621` |
| `task-id` (NOTAS_TECNICAS) | `815d9c90-62b7-4737-82e7-c2e85026aafc` |
| `task-id` (README) | `c20ea3bb-cc8c-4ab2-8734-64bf7fefa54f` |
| `task-id` (mock API README) | `bf4cb5cc-f359-4aa5-bcda-86c2e5ea772d` |

> Nota: el cierre de hardening de 2026-11-19 se registró con otro conjunto de identificadores (`prd-id 045a2de8-e11f-4286-930f-ed582728e411`, `conv-id 9b337131-5f44-49d8-b4f1-8edad505507b`, `task-id f8dfcced-828f-4140-8a27-3e350e295a43`). Cada ciclo SDD puede tener sus propios IDs; todos forman parte de la trazabilidad del proyecto.

### Pasos del flujo SDD (de principio a fin)

| Paso | Qué significa | Responsable típico | Comandos reales (ejemplos) |
|---|---|---|---|
| 1. User request | El usuario pide algo (bug, mejora, documentación). | Usuario / Orquestador | (conversación; se registra en el sistema) |
| 2. Conversación | Se aclara el alcance y se crea/usa una conversación de trabajo. | Orquestador | `lemoria conv create <project-id> -t "Manual de traspaso"` · `lemoria conv add <conv-id> user "Actualizar docs..."` · `lemoria conv list <project-id>` |
| 3. PRD | Se define el **PRD** (Product Requirements Document: documento de requisitos/alcance). | Orquestador / flujo | `lemoria flow start <project-id> "Crear manual de traspaso para principiantes"` · `lemoria flow list <project-id>` · `lemoria flow status <flow-id>` |
| 4. Tareas (INVEST) | Se descompone en tareas pequeñas, independientes y verificables. | Orquestador | `lemoria task create <project-id> <prd-id> -t "Actualizar README con traspaso"` · `lemoria task list <project-id>` · `lemoria task status <task-id> in_progress` |
| 5. Implement | Se hacen los cambios (código o documentación) según la tarea. | Agente/autor | (editar archivos; en código se usarían herramientas de build; en docs, editar Markdown) |
| 6. Test | Se verifica: en código, compilar y correr pruebas; en docs, revisar enlaces, rutas y que no se invente nada. | Agente/autor | `./gradlew :app:testDebugUnitTest` · `./gradlew :app:assembleDebug` · (revisión manual de Markdown y enlaces relativos) |
| 7. Review | Revisión de calidad y alineación con el PRD/alcance. | Revisor | (revisión; registrar resultado en conversación/tarea) |
| 8. Commit | Se registra el commit en el control de versiones y en la trazabilidad. | Autor | `git add <archivos>` · `git commit -m "docs: manual de traspaso para principiantes"` · `lemoria commit add <sha> --task <task-id>` · `lemoria commit list --task <task-id>` |
| 9. Document | Se actualiza documentación (README, `docs/`, guías) en el mismo ciclo del cambio. | Agente doc | (este paso: actualizar los 5 archivos solicitados) |
| 10. `vault_sync` | Se sincroniza la bóveda/exportación de conocimiento del proyecto. | Sistema/orquestador | `lemoria vault sync <project-id>` (ej.: `lemoria vault sync 136316a2-36dd-43b5-aa80-05b9e97d3f22`) |
| 11. Consolidate | Se consolidan resultados, decisiones y estado final de tareas/flujo. | Orquestador | `lemoria flow step <flow-id> consolidate -s completed -o "Docs actualizadas"` · `lemoria task status <task-id> completed` · `lemoria decision log <project-id> -t "..." -d "..."` · `lemoria decision list <project-id>` |
| 12. Complete | Se cierra el flujo/ciclo. | Orquestador | `lemoria flow complete <flow-id>` · `lemoria flow status <flow-id>` |

> Los comandos anteriores son los reales de la CLI Lemoria disponible (`lemoria flow`, `task`, `conv`, `decision`, `commit`, `vault sync`, etc.). Los argumentos `<...>` son IDs reales del proyecto/flujo/tarea/conversación según corresponda. Algunos pasos los ejecuta el orquestador; este cambio de documentación **no** ejecuta commit ni `vault_sync` por sí mismo.

### Cómo se registró este proyecto (resumen de trazabilidad)

- **Proyecto Lemoria:** `136316a2-36dd-43b5-aa80-05b9e97d3f22` (este repositorio/app).
- **PRD de este manual:** `c46eb0c9-d681-40b3-99fb-c2336db498d6`, conversación `46c48af9-f33b-4546-a4b0-96393bcf6eeb`.
- **Tareas de esta entrega documental:** auditoría `f4b1d51e-13e2-4878-9164-38b2bda8b9f6`, guía de estudio `4ad8e55f-e7bd-46bc-925e-f75f64593621`, notas técnicas `815d9c90-62b7-4737-82e7-c2e85026aafc`, README `c20ea3bb-cc8c-4ab2-8734-64bf7fefa54f`, mock API README `bf4cb5cc-f359-4aa5-bcda-86c2e5ea772d`.
- **Cierre de hardening previo (registrado):** PRD `045a2de8-e11f-4286-930f-ed582728e411`, conversación `9b337131-5f44-49d8-b4f1-8edad505507b`, flujo SDD con pasos `prd`, `tasks`, `implement`, `test`, `review`, `commit` completados; commit `8832145` (hardening b/c/d) y `8595fb9` (docs del hardening) aún **sin push** por falta de credenciales HTTPS. Detalle en la sección fechada más abajo.


---

## [2026-11-19] Hardening: siembra transaccional, login robusto y tests de regresión

**Estado:** implementado, testeado, revisado y commiteado. Review: **APROBADO CON OBSERVACIONES** (observaciones resueltas). Commit `8832145` — **no pusheado aún** por falta de credenciales HTTPS.

### Identificadores

| Dato | Valor |
|---|---|
| project-id | `136316a2-36dd-43b5-aa80-05b9e97d3f22` |
| prd-id | `045a2de8-e11f-4286-930f-ed582728e411` |
| conv-id | `9b337131-5f44-49d8-b4f1-8edad505507b` |
| task-id (documentación) | `f8dfcced-828f-4140-8a27-3e350e295a43` |
| commit | `8832145` (pendiente de push) |
| commit previo (frentes a/a2) | `d648c7e` |

### Alcance (4 frentes)

- **(a) `BASE_URL` corregida** a `http://10.0.2.2:8000/` más test de regresión (en `d648c7e`).
- **(a2) Integración IT3** con Retrofit/Gson/StateFlow (en `d648c7e`): `GET /documentos` contra API simulada local.
- **(b) Siembra Room transaccional y arranque tolerante a fallos** (en `8832145`).
- **(c) Login robusto con auditoría best-effort fuera de la ruta crítica de errores** (en `8832145`).
- **(d) Tests unitarios e instrumentados** (en `8832145`).

### Cambios de comportamiento

#### `data/local/Sembrador.kt` — siembra transaccional, convergente y con ids reales

- Todo el cuerpo de `sembrarSiVacio(db)` corre dentro de `db.withTransaction { }`: si falla cualquier inserción, Room revierte **todos** los cambios y el siguiente arranque lo reintenta.
- `habiaDocumentos = db.documentoDao().contar() > 0` se calcula **antes** de insertar. La auditoría `accion = "SEED"` se escribe solo con `if (!habiaDocumentos) sembrarAuditoriaSeed(db)`, de modo que una siembra parcial heredada no genera un SEED duplicado.
- Convergencia por fila: dependencias y usuarios por `id`; documentos por **clave natural estable** (`titulo`, vía `porTitulo`); versiones por `(documentoId, numeroVersion)`; solicitud demo por `(titulo, solicitante)`. Si la fila ya existe, se reutiliza y no se duplica.
- `sembrarDocumentos` devuelve `Map<String, Long>` (`idPorTitulo`) con el **id real** del documento, existente o recién insertado. `sembrarVersiones` y `sembrarSolicitud` usan ese mapa, así que las versiones apuntan a documentos existentes aunque `sqlite_sequence` esté adelantado.
- Si la base ya está completa, la pasada no escribe nada y no invalida los `Flow`.
- Estado tras una siembra limpia: 4 dependencias, 7 usuarios, 6 documentos, 7 versiones, 1 solicitud y 1 auditoría SEED.

#### `data/repository/Repositorios.kt` / `viewmodel/SesionViewModel.kt` — login robusto

- `iniciarSesion` normaliza el correo (`trim().lowercase()`). `CancellationException` del `UsuarioDao` se **re-lanza**; cualquier otra excepción se convierte en `Result.failure(IllegalStateException("No se pudo acceder a los datos locales. Intenta nuevamente."))` y **no** audita.
- `registrarAuditoria` es **best-effort**: los errores ordinarios se descartan a propósito; `CancellationException` se re-lanza (no se disfraza de fallo de negocio).
- `_actual.value = usuario` se asigna **después** de `registrarAuditoria(... LOGIN ...)`; si la auditoría se cancela, la sesión no queda iniciada.
- `cerrarSesion()` audita `LOGOUT` y limpia `_actual` en un bloque `finally`, así que la sesión se cierra aunque falle o se cancele la auditoría.
- `SesionViewModel` expone `actual`, `error` y `cargando` como `StateFlow`, re-lanza `CancellationException`, y usa `"No se pudo iniciar sesión."` solo como mensaje genérico de último recurso.

#### `limpio_moviles/LemacApplication.kt` — arranque tolerante a fallos

- `CoroutineExceptionHandler` (`manejadorErrores`) registrando en `Log.e` y guardando el error en `@Volatile var ultimoErrorSiembra`.
- `CoroutineScope(SupervisorJob() + Dispatchers.IO + manejadorErrores)`.
- En `onCreate()`: `runCatching { Sembrador.sembrarSiVacio(gestionDatabase) }.onFailure { ... }` con re-lanzamiento explícito de `CancellationException`; cualquier otro error se guarda en `ultimoErrorSiembra` para diagnóstico. Un fallo de Room no cierra la app.

### Hallazgos de revisión corregidos

| Hallazgo | Corrección |
|---|---|
| **M1** — propagar `CancellationException` en login/auditoría | `catch (e: CancellationException) { throw e }` en la búsqueda de usuario, en `registrarAuditoria` y en `SesionViewModel`. |
| **M2** — siembra convergente por clave estable con ids reales | Documentos por `titulo`, versiones por `(documentoId, numeroVersion)`, `idPorTitulo` con ids reales. |
| **m10** — sesión consistente ante cancelación de auditoría | `_actual` se asigna tras auditar LOGIN; en `cerrarSesion()` se limpia en `finally`. |
| Ajuste post-review | Re-lanzar la cancelación también en el seed de `LemacApplication`; versiones de AndroidX test alineadas a `1.6.1`. |

### Configuración Gradle

- `app/build.gradle.kts`: `testOptions { unitTests.isReturnDefaultValues = true }`. Necesario para ejecutar tests JVM con `kotlinx-coroutines-test` cuando el classpath incluye clases Android (`android.os.Looper`) no disponibles en la JVM local; los métodos devuelven valores por defecto en vez de lanzar `Stub!`. Afecta solo a tests, no a producción.
- `gradle/libs.versions.toml`: `androidxTestCore = "1.6.1"` y `androidxTestRunner = "1.6.1"` (antes desalineados). `androidx.test.ext:junit` 1.1.5 y `espresso-core` 3.5.1 sin cambios.
- Test dependencies ya presentes: `kotlinx-coroutines-test` 1.9.0, `okhttp3:mockwebserver` 4.12.0.

### Verificación

| Ítem | Resultado |
|---|---|
| `./gradlew :app:testDebugUnitTest` | **105 pruebas JVM, 0 fallos** (33 bienestar + 72 gestión documental; +10 nuevas de `SesionRepositoryTest`). |
| `./gradlew :app:assembleDebugAndroidTest` | `BUILD SUCCESSFUL`: 9 instrumentadas compiladas (8 `SembradorInstrumentedTest` + 1 `PortalSmokeTest`). |
| `./gradlew :app:connectedDebugAndroidTest` | **No ejecutado**: el entorno no tiene `adb` ni dispositivo/emulador conectado. |

Otros datos del entorno de verificación: JVM 21, Android SDK en `/home/simon/Android/Sdk`, `google-chrome` en `/usr/bin/google-chrome`, manifest fusionado sin permiso `CAMERA`, desde IT3 con `INTERNET` y cleartext limitado a `10.0.2.2`, `localhost` y `127.0.0.1`.

### Inventario de tests

**JVM (105):**

| Módulo | Cantidad | Archivos nuevos / relevantes |
|---|---|---|
| Bienestar | 33 | `RegistroValidatorTest` 9, `ResumenCalculatorTest` 4, `RegistroCsvExporterTest` 8, `RegistroRepositoryTest` 5, `RegistroViewModelTest` 7. |
| Gestión documental | 72 | `ApiConfigTest` 5, `SesionRepositoryTest` 10, `ValidadoresTest` 4, `PermisosTest` 7, `PermisosCargaTest` 9, `FlujoModificacionTest` 6, `ReportesTest` 9, `ArchivosMimeTest` 8, `DocumentoDtoTest` 6, `DocumentosRemotosRepositoryTest` 4, `DocumentosRemotosViewModelTest` 4. |

**Instrumentados (9, compilados no ejecutados):**

| Archivo | Cantidad | Cobertura |
|---|---|---|
| `SembradorInstrumentedTest.kt` | 8 | Room real en memoria: conteos, idempotencia, reparación de siembras parciales, ids reales con `sqlite_sequence` adelantado, reversión transaccional por trigger, integridad referencial de versiones. |
| `PortalSmokeTest.kt` | 1 | `createAndroidComposeRule<MainActivity>()`: el portal muestra las dos tarjetas sin esperar a la siembra asíncrona. |

### Comandos

```bash
# Suite JVM
./gradlew :app:testDebugUnitTest --console=plain --no-daemon

# Compilar instrumentados (sin dispositivo)
./gradlew :app:assembleDebugAndroidTest --console=plain --no-daemon

# Solo con adb + emulador/dispositivo API 33+
# En este entorno adb no está en el PATH y no hay dispositivo/emulador conectado
adb devices
./gradlew :app:connectedDebugAndroidTest --console=plain --no-daemon

# Compilación general
./gradlew :app:assembleDebug --offline
```

### Trazabilidad Lemoria

- Proyecto: `136316a2-36dd-43b5-aa80-05b9e97d3f22`
- PRD: `045a2de8-e11f-4286-930f-ed582728e411`
- Conversación: `9b337131-5f44-49d8-b4f1-8edad505507b`
- Task de documentación: `f8dfcced-828f-4140-8a27-3e350e295a43`
- Flujo SDD: `prd`, `tasks`, `implement`, `test`, `review` y `commit` completados.
- ADRs:

| ID | Decisión |
|---|---|
| `37d7d92c-c68b-46cd-971e-5583af38e655` | Fix M1: propagar `CancellationException` en login y auditoría |
| `e32848b6-0f4c-42dd-8ac9-0eda3c3f558c` | Fix M2: siembra convergente por clave estable |
| `b0bf8edc-6f6a-4a78-91d7-241a1359b318` | Fix m10: sesión consistente ante cancelación de auditoría |
| `ab094bb8-1cae-453a-8cd3-260fe8cfe43e` | Tests JVM: `unitTests.isReturnDefaultValues=true` |
| `42d32a6e-76c0-43c6-9a38-0f3488ac2176` | Auditoría best-effort y login con captura de fallos de DAO |
| `4e2dd090-56e9-47fe-9f15-a40364cca5cb` | Mover asignación de sesión después de auditar LOGIN |
| `30e5e778-2039-4732-9b0f-f5c5511d6e53` | Siembra demo transaccional e idempotente |
| `abaccc4f-2dc3-446d-98dc-cbbbe002979b` | Arranque tolerante a fallos de siembra |

### Seguimiento

- Push pendiente de `8832145` (falta de credenciales HTTPS).
- Ejecutar las 9 instrumentadas en emulador/dispositivo con `adb` cuando esté disponible.
- Pendiente regenerar `docs/informe_arquitectura.html` y `docs/INFORME_ARQUITECTURA.pdf` para reflejar IT3 y este hardening.

---

## Historial previo

| Fecha | Entrada | Commit |
|---|---|---|
| 2026-11-15 | IT3: `GET /documentos` con Retrofit/Gson/OkHttp, pantalla "Documentos en línea", 14 pruebas nuevas; `BASE_URL` corregida a `http://10.0.2.2:8000/` y 5 pruebas de regresión. Suite real 76 → 95 (el informe de IT3 cito 90 por omitir `ApiConfigTest` del inventario). | `d648c7e` |
| 2026-11-12 | Informe de arquitectura y guías a 76/33/43 pruebas; guía maestra de estudio y replicación. | `8e87e82`, `a80f3f8` |
| 2026-11-04 | Flujo de cámara `TakePicture`/`FileProvider`/EXIF y semántica de edición de registros. | `452b274` |
