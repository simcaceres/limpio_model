# Integración con API REST (IT3): Documentos en línea

## 1. Título y resumen

Esta entrega documenta la actividad **IT3** ("integración de sus aplicaciones móviles con servicios externos, utilizando API REST, HTTP GET, JSON y Retrofit") sobre el proyecto `limpio_model`, rama `entrega_hito2`.

**Qué pide IT3.** Cada equipo debía elegir una funcionalidad real que hoy usara datos locales, simulados o escritos en el código, definir un endpoint de ejemplo (`GET /...`), preparar un JSON de ejemplo, crear la data class en Kotlin, crear un `ApiService` con Retrofit y conectar la solicitud con la arquitectura del proyecto (**Compose → ViewModel → Repository → ApiService → Retrofit → API**), mostrando en la interfaz al menos tres estados (**Cargando → Datos → Error**). No se exigía todavía un CRUD remoto completo.

**Qué se entregó.** Se tomó la funcionalidad **Documentos** del módulo *Gestión Documental RR.HH.* (`com.example.gestiondocumental`), que antes solo existía contra Room con el sembrador local. Se agregó una **variante de solo lectura** contra una API REST `GET /documentos`, con una API simulada local (`tools/mock_api/server.py`) y una pantalla nueva, "Documentos en línea", accesible desde el inicio del módulo.

El flujo completo queda: `DocumentosRemotosScreen` (Compose) → `DocumentosRemotosViewModel` (`StateFlow`) → `DocumentosRemotosRepository` → `DocumentosApiService` (`@GET`) → Retrofit/Gson → OkHttp → API simulada. La pantalla refleja los tres estados pedidos y aplica la matriz de permisos (RBAC) del dominio sobre los datos recibidos.

> Nota de alcance: esta integración es **de solo lectura** (un `GET`). La funcionalidad original de documentos sobre Room sigue existiendo y no se modificó su comportamiento.

## 2. Requisito vs evidencia

| # | Lo que pedía IT3 | Evidencia en el repo |
|---|---|---|
| 1 | Elegir 1 funcionalidad real de la app | Documentos del módulo Gestión Documental RR.HH. (antes solo Room + sembrador). Variante nueva de solo lectura. |
| 2 | Definir 1 endpoint GET | `GET /documentos` en `data/remote/DocumentosApiService.kt` y `data/remote/ApiConfig.kt` (`ENDPOINT_DOCUMENTOS = "documentos"`). |
| 3 | Preparar un JSON de ejemplo | `tools/mock_api/documentos.json` (6 documentos ficticios) servido por `tools/mock_api/server.py`. |
| 4 | Crear la data class | `data/remote/DocumentoDto.kt` con `@SerializedName` y la conversión `aModelo()`. |
| 5 | Crear un ApiService con Retrofit | `data/remote/DocumentosApiService.kt` (`@GET("documentos") suspend fun obtenerDocumentos(): List<DocumentoDto>`). |
| 6 | Conectar con la arquitectura del proyecto | `DocumentosRemotosScreen` → `DocumentosRemotosViewModel` → `DocumentosRemotosRepository` → `DocumentosApiService` → `RedDocumentos` (Retrofit/Gson/OkHttp). |
| 7 | Mostrar 3 estados: Cargando/Datos/Error | `model/DocumentosRemotosUiState.kt` (sellado) y los tres bloques en `ui/screens/DocumentosRemotosScreen.kt`. |
| 8 | Poder explicar el flujo | Este documento (secciones 4, 9 y 13) y las pruebas de los tres archivos nuevos. |

## 3. Contrato del endpoint

- **Método y ruta:** `GET /documentos`.
- **URL en el emulador:** `http://10.0.2.2:8000/documentos` (configurada en `ApiConfig.BASE_URL = "http://10.0.2.2:8000/"`).
- **Dispositivo físico:** `10.0.2.2` solo existe en el emulador. Por USB usar `adb reverse tcp:8000 tcp:8000` y cambiar `BASE_URL` a `http://127.0.0.1:8000/`; por Wi-Fi usar la IP LAN del PC (ver sección 8).
- **Respuesta correcta:** `200 OK` con un **arreglo JSON** de documentos (ver ejemplo abajo).
- **Ruta inexistente:** `404` con `{"error": "Ruta no encontrada", "ruta": "..."}` (el servidor simulado la devuelve así).
- **Error del servidor:** `500` (Retrofit lo entrega como `HttpException` y el ViewModel muestra el código).
- **Autenticación:** el mock **no exige** credenciales. El filtrado por rol es de cliente (ver sección 10).

Ejemplo real (fragmento de `tools/mock_api/documentos.json`):

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "propietario": "Ana Pérez",
    "categoria": "CONTRATO",
    "titulo": "Contrato de trabajo 2026",
    "estado": "VIGENTE",
    "versionActual": 1,
    "fechaVencimiento": "2026-12-31",
    "dependencia": "Atención Primaria",
    "archivoNombre": "contrato_ana_perez_2026.pdf"
  },
  {
    "id": 2,
    "usuarioId": 1,
    "propietario": "Ana Pérez",
    "categoria": "LICENCIA_MEDICA",
    "titulo": "Licencia médica marzo",
    "estado": "VIGENTE",
    "versionActual": 1,
    "fechaVencimiento": "2027-03-20",
    "dependencia": "Atención Primaria",
    "archivoNombre": null
  }
]
```

Tabla de campos del JSON (contrato `DocumentoDto`):

| Campo JSON | Tipo Kotlin | ¿Obligatorio? | Descripción | Ejemplo |
|---|---|---|---|---|
| `id` | `Long` | Sí | Identificador del documento. Debe ser `> 0`. | `1` |
| `usuarioId` | `Long` | Sí | Id del usuario propietario (para decidir `propio` en el RBAC). | `1` |
| `propietario` | `String` | Sí | Nombre del dueño. No puede estar vacío. | `"Ana Pérez"` |
| `categoria` | `String` (enum) | Sí | Categoría del dominio. Debe coincidir con `CategoriaDocumento`. | `"CONTRATO"` |
| `titulo` | `String` | Sí | Título visible. No puede estar vacío. | `"Contrato de trabajo 2026"` |
| `estado` | `String` (enum) | Sí | Estado del dominio. Debe coincidir con `EstadoDocumento`. | `"VIGENTE"` |
| `versionActual` | `Int` | Sí | Número de versión vigente. | `1` |
| `fechaVencimiento` | `String?` | No | Fecha ISO (texto) o `null`. | `"2026-12-31"` |
| `dependencia` | `String` | Sí | Unidad organizacional (para decidir `mismaDependencia`). | `"Atención Primaria"` |
| `archivoNombre` | `String?` | No | Nombre del archivo adjunto o `null`. | `"contrato_ana_perez_2026.pdf"` |

Valores válidos de `categoria`: `CONTRATO`, `CERTIFICADO`, `PERMISO`, `LICENCIA_MEDICA`, `ANTECEDENTE_SENSIBLE`, `REPORTE`.
Valores válidos de `estado`: `VIGENTE`, `PENDIENTE_REVISION`, `APROBADO`, `RECHAZADO`, `ARCHIVADO`.

La conversión `DocumentoDto.aModelo()` (`data/remote/DocumentoDto.kt:33`) valida que `id > 0`, que `titulo` y `propietario` no estén en blanco y que `categoria`/`estado` pertenezcan al dominio; si algo falla, lanza `IllegalArgumentException`, que el repositorio convierte en `Result.failure`.

## 4. Cadena de arquitectura paso a paso

Recorrido de una consulta, de la interfaz a la API:

1. **Compose — `ui/screens/DocumentosRemotosScreen.kt:58`.** La pantalla recibe el `ViewModel` y el usuario de la sesión. Observa el estado con `collectAsStateWithLifecycle()` (`DocumentosRemotosScreen.kt:64`) y, sobre los datos recibidos, filtra con `Permisos.puedeVerDocumento` (`DocumentosRemotosScreen.kt:70-77`) para calcular los "visibles para tu rol".
2. **ViewModel — `viewmodel/DocumentosRemotosViewModel.kt`.** Expone `estado: StateFlow<DocumentosRemotosUiState>` (`DocumentosRemotosViewModel.kt:26-29`). En `init` llama a `cargar()`, que cancela la carga anterior y lanza una corrutina en `viewModelScope` (`DocumentosRemotosViewModel.kt:38-51`), publicando `Cargando`, luego `Datos` o `Error`.
3. **Repository — `data/repository/DocumentosRemotosRepository.kt:24`.** `obtenerDocumentos()` ejecuta en `Dispatchers.IO`, mapea cada DTO con `aModelo()`, ordena por `id` y devuelve `Result<List<Documento>>`. Los errores (salvo `CancellationException`) se convierten en `Result.failure`.
4. **ApiService — `data/remote/DocumentosApiService.kt:9`.** La anotación `@GET("documentos")` describe la llamada: `suspend fun obtenerDocumentos(): List<DocumentoDto>`.
5. **Retrofit/Gson — `data/remote/RedDocumentos.kt:24`.** Construye el `Retrofit` con `ApiConfig.BASE_URL`, un `OkHttpClient` con timeouts de 10 s y `GsonConverterFactory`; expone `RedDocumentos.api`.
6. **OkHttp.** Ejecuta el HTTP `GET` real y devuelve la respuesta.
7. **API.** La API simulada (`tools/mock_api/server.py`) responde el arreglo JSON de `documentos.json`.

Diagrama ASCII:

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Compose   DocumentosRemotosScreen.kt:58                               │
│   collectAsStateWithLifecycle()  +  RBAC Permisos.puedeVerDocumento   │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  vm.estado : StateFlow<DocumentosRemotosUiState>
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ DocumentosRemotosViewModel.kt                                         │
│   MutableStateFlow(Cargando)  ->  cargar()  ->  Datos / Error         │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  suspend obtenerDocumentos() : Result<List<Documento>>
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ DocumentosRemotosRepository.kt                                        │
│   withContext(Dispatchers.IO) + map(aModelo) + sortedBy(id)           │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  api.obtenerDocumentos()
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ DocumentosApiService.kt   @GET("documentos")                          │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  Retrofit.create(...)
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ RedDocumentos.kt   Retrofit + GsonConverterFactory + OkHttpClient     │
└───────────────────────────────┬──────────────────────────────────────┘
                                │  HTTP GET  http://10.0.2.2:8000/documentos
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ API REST simulada   tools/mock_api/server.py  ->  documentos.json     │
└──────────────────────────────────────────────────────────────────────┘
```

## 5. Estados de la UI

`DocumentosRemotosUiState` (`model/DocumentosRemotosUiState.kt`) es una interfaz sellada con tres casos:

| Estado | Qué se ve | Qué lo dispara |
|---|---|---|
| `Cargando` | Indicador circular + "Cargando documentos..." centrado. | Estado inicial del `ViewModel` y cada vez que se llama a `cargar()`. |
| `Datos(documentos)` | Cabecera con "Documentos recibidos: N" y "Visibles para tu rol: M"; lista de tarjetas (`LazyColumn`). Si el usuario no existe o no ve ninguno, muestra `EstadoVacio`. | La consulta terminó con `Result.success`. |
| `Error(mensaje)` | Ícono `CloudOff`, el mensaje legible, un recordatorio de arrancar el mock y un botón **Reintentar**. | `IOException` (sin conexión), `HttpException` (código del servidor) u otro fallo. |

- **Botón Reintentar:** visible solo en `Error`; llama a `vm.cargar()` (`DocumentosRemotosScreen.kt:181`).
- **Refresh de la barra:** la `BarraSuperior` tiene una acción con ícono `Refresh` que también llama a `vm.cargar()` (`DocumentosRemotosScreen.kt:83`), disponible en cualquier estado.
- El `ViewModel` se crea con scope de la ruta `documentos_api` (ver `navigation/AppNavigation.kt:122-134`), así que **carga al entrar** a la pantalla.

## 6. Archivos nuevos y modificados

| Ruta | Tipo | Propósito |
|---|---|---|
| `app/src/main/java/com/example/gestiondocumental/data/remote/ApiConfig.kt` | Nuevo | URL base del emulador y nombre del endpoint. |
| `app/src/main/java/com/example/gestiondocumental/data/remote/DocumentoDto.kt` | Nuevo | Contrato JSON + conversión DTO → `Documento`. |
| `app/src/main/java/com/example/gestiondocumental/data/remote/DocumentosApiService.kt` | Nuevo | Interfaz Retrofit con `@GET("documentos")`. |
| `app/src/main/java/com/example/gestiondocumental/data/remote/RedDocumentos.kt` | Nuevo | Retrofit + Gson + OkHttp configurados. |
| `app/src/main/java/com/example/gestiondocumental/data/repository/DocumentosRemotosRepository.kt` | Nuevo | Traduce DTOs a dominio y devuelve `Result`. |
| `app/src/main/java/com/example/gestiondocumental/model/DocumentosRemotosUiState.kt` | Nuevo | Estados sellados `Cargando`/`Datos`/`Error`. |
| `app/src/main/java/com/example/gestiondocumental/viewmodel/DocumentosRemotosViewModel.kt` | Nuevo | `StateFlow` de la pantalla y mensajes legibles. |
| `app/src/main/java/com/example/gestiondocumental/ui/screens/DocumentosRemotosScreen.kt` | Nuevo | Pantalla Compose con los tres estados y RBAC. |
| `app/src/main/res/xml/network_security_config.xml` | Nuevo | Permite cleartext solo a `10.0.2.2`, `localhost`, `127.0.0.1`. |
| `tools/mock_api/server.py` | Nuevo | API simulada (`GET /documentos`, `/health`). |
| `tools/mock_api/documentos.json` | Nuevo | Datos ficticios servidos por el mock. |
| `tools/mock_api/README.md` | Nuevo | Cómo arrancar el mock. |
| `app/src/test/java/com/example/gestiondocumental/data/remote/DocumentoDtoTest.kt` | Nuevo | 6 pruebas del contrato JSON. |
| `app/src/test/java/com/example/gestiondocumental/data/repository/DocumentosRemotosRepositoryTest.kt` | Nuevo | 4 pruebas de integración con `MockWebServer`. |
| `app/src/test/java/com/example/gestiondocumental/viewmodel/DocumentosRemotosViewModelTest.kt` | Nuevo | 4 pruebas del `ViewModel`. |
| `gradle/libs.versions.toml` | Modificado | Versiones y librerías Retrofit/OkHttp/MockWebServer. |
| `app/build.gradle.kts` | Modificado | Dependencias de red y de test. |
| `app/src/main/AndroidManifest.xml` | Modificado | Permiso `INTERNET` y `networkSecurityConfig`. |
| `app/src/main/java/com/example/limpio_moviles/LemacApplication.kt` | Modificado | Expone `documentosRemotosRepository`. |
| `app/src/main/java/com/example/gestiondocumental/navigation/AppNavigation.kt` | Modificado | Ruta `documentos_api` y su `ViewModel`. |
| `app/src/main/java/com/example/gestiondocumental/ui/screens/HomeScreen.kt` | Modificado | Tarjeta "Documentos en línea". |
| `app/src/main/java/com/example/gestiondocumental/ui/components/Componentes.kt` | Modificado | `BarraSuperior` acepta acciones (`RowScope`). |

## 7. Cómo ejecutar la demo

1. Arrancar la API simulada desde la raíz del proyecto:
   ```bash
   python3 tools/mock_api/server.py
   ```
   Queda escuchando en el puerto `8000` (se puede cambiar con `PORT`).
2. Verificar que responde (en otra terminal):
   ```bash
   curl -s http://127.0.0.1:8000/documentos
   curl -s http://127.0.0.1:8000/health
   ```
3. Abrir el emulador (la URL `10.0.2.2` apunta al PC anfitrión desde el emulador) e instalar/ejecutar la app.
4. En el portal, entrar a **Gestión Documental RR.HH.**
5. Iniciar sesión con `ana.perez@salud.cl` / `1234` (usuaria funcionaria).
6. En el inicio, abrir **"Documentos en línea"**. Debe verse el estado `Cargando` y luego la lista, con "Documentos recibidos: 6" y "Visibles para tu rol: N".
7. **Forzar el estado `Error`:** detener el servidor con `Ctrl+C` y pulsar **Reintentar** (o el ícono `Refresh` de la barra). Aparece el mensaje "No se pudo conectar con la API (...)".
8. **Volver a `Datos`:** reiniciar `python3 tools/mock_api/server.py` y pulsar **Reintentar**.

## 8. Cambiar la URL base

Se cambia en `data/remote/ApiConfig.kt` (`BASE_URL`).

| Escenario | `BASE_URL` | Notas |
|---|---|---|
| Emulador Android | `http://10.0.2.2:8000/` | `10.0.2.2` es el PC anfitrión visto desde el emulador. Es el valor por defecto. |
| Dispositivo físico (USB) | `http://127.0.0.1:8000/` | Requiere `adb reverse tcp:8000 tcp:8000` antes de usar la app. |
| Dispositivo físico (Wi-Fi) | `http://IP_DEL_PC:8000/` | Agregar esa IP a `res/xml/network_security_config.xml` (cleartext). |
| Backend real (HTTPS) | `https://servidor/` | No requiere cleartext; conviene quitar las excepciones locales del XML. |

Ejemplo de la variable usada para el dispositivo físico por USB:

```bash
adb reverse tcp:8000 tcp:8000
```

## 9. Pruebas

Las pruebas nuevas son **JVM** (no instrumentadas) y las de red usan **`MockWebServer`** para no depender de la API real. Son **19 pruebas JVM** respecto al estado pre-IT3: 14 de la integración IT3 (6 de DTO, 4 de repositorio con `MockWebServer` y 4 de ViewModel) y 5 de regresión de `BASE_URL` (`ApiConfigTest`), todas incluidas en el commit `d648c7e`:

| Archivo | Cantidad | Qué cubre |
|---|---|---|
| `ApiConfigTest.kt` | 5 | Regresión de la `BASE_URL`: valor exacto `http://10.0.2.2:8000/`, terminación en `/`, endpoint `documentos`, composición `http://10.0.2.2:8000/documentos` y rechazo de `127.0.0.1`/`localhost`. Añadidas en `d648c7e` junto con la corrección de la URL. |
| `DocumentoDtoTest.kt` | 6 | Deserialización y mapeo de todos los campos; `archivoNombre` ausente → `null`; categoría desconocida, estado desconocido, `id` cero y título vacío lanzan `IllegalArgumentException`. |
| `DocumentosRemotosRepositoryTest.kt` | 4 | Éxito ordenado por `id` y ruta `/documentos` correcta; `500` → `HttpException`; JSON malformado → `failure`; categoría inválida → `failure` con mensaje "Categoria desconocida". |
| `DocumentosRemotosViewModelTest.kt` | 4 | Estado inicial `Cargando` y luego `Datos`; `IOException` → `Error` con mensaje de conexión; reintentar tras error vuelve a llamar la API y pasa a `Datos`; `HttpException` 503 → `Error` con el código. |

Comando:

```bash
./gradlew :app:testDebugUnitTest --console=plain --no-daemon
```

**Recuento corregido:** la suite al cierre de IT3 (`d648c7e`) tenia **95 pruebas** (antes 76; +14 de IT3 y +5 de `ApiConfigTest`). Bienestar 33 y Gestión Documental 62 (43 previas + 14 de IT3 + 5 de regresión de `BASE_URL`). El informe original de IT3 cito 90/90 porque su inventario omitia `ApiConfigTest`, que se havia anadido en el mismo commit `d648c7e` que corrigio la URL (`git grep '@Test' d648c7e -- 'app/src/test/**/*.kt'` da 95). La suite JVM actual es de **105 pruebas, 0 fallos** (33 de bienestar + 72 de gestión documental) tras añadir las 10 de `SesionRepositoryTest` en el cierre de hardening (`8832145`, 19 de noviembre de 2026). Además hay 9 pruebas instrumentadas compiladas (8 de Room + 1 smoke Compose), no ejecutadas por falta de `adb`/dispositivo.

> Configuración relacionada: `app/build.gradle.kts` declara `testOptions { unitTests.isReturnDefaultValues = true }` para que los tests JVM con `kotlinx-coroutines-test` corran sin `Looper` de Android; `androidx.test:core` y `androidx.test:runner` están alineados a `1.6.1` en `gradle/libs.versions.toml` (ADR `ab094bb8`).

## 10. Limitaciones y siguientes pasos

1. **El mock no exige autenticación y el filtrado RBAC es de cliente.** En un backend real la autorización debe aplicarse en el servidor; el filtro de la pantalla solo decide qué mostrar, no qué se entrega.
2. **Gson no respeta la nulabilidad estricta de Kotlin.** Un `null` en un campo marcado como no nulo (`String`, `Long`) puede colarse sin error de compilación; conviene `@Nullable`/validaciones o un `TypeAdapter`.
3. **Aún no hay POST ni CRUD remoto.** La integración es de solo lectura (`GET`). Crear, editar o eliminar sigue siendo local (Room).
4. **Pendiente regenerar HTML/PDF.** Este cambio documental no regeneró `docs/informe_arquitectura.html` ni `docs/INFORME_ARQUITECTURA.pdf` (ver nota al final de `docs/INTEGRACION.md`).
5. **Pruebas instrumentadas sin ejecutar.** El cierre de hardening añadió 8 pruebas de siembra Room y 1 smoke Compose que compilan (`assembleDebugAndroidTest`), pero el entorno no tiene `adb`/dispositivo y no se ejecutaron. Tampoco hay tests instrumentados que exercise la pantalla "Documentos en línea" contra el mock.

## 11. Trazabilidad Lemoria

Registros de Lemoria asociados a IT3 (PostgreSQL es la fuente de verdad; esto es su reflejo en el repo):

- **Proyecto:** `136316a2-36dd-43b5-aa80-05b9e97d3f22`.
- **PRD:** `1c8bfe20-cc11-4429-8532-280c5d1720b2`.
- **Conversación:** `5bd0ebb1-d1bd-42ea-9a61-77fc0fe46b0a`.
- **Flujo SDD:** prd, tasks, implement, test, review, document, vault_sync y consolidate completados.

Tareas (9):

| # | ID |
|---|---|
| 1 | `cab8df7b-e659-4dba-9cd4-66c0140c7be5` |
| 2 | `5b51a46b-89d6-4bfa-88b9-059372813576` |
| 3 | `ed01d2ea-41ca-4c99-a823-4aa4c8b29774` |
| 4 | `08b23554-f28a-4f8e-9a5d-74add8a6960a` |
| 5 | `6600d1be-7466-4b8b-a527-63d3ea56e661` |
| 6 | `20cf01c8-1dbe-4773-955e-d325967bbabc` |
| 7 | `983284b8-9c3e-4ff8-a1c0-d8fd47bf4468` |
| 8 | `52f333f2-958d-4c5e-8689-3bef9ae3721a` |
| 9 | `96f813b5-c430-4872-a2e9-fa8fe6869783` |

### 11.1 Cierre de hardening (2026-11-19)

El frente **a** (corrección de `BASE_URL` a `http://10.0.2.2:8000/` y su test de regresión `ApiConfigTest`) y el frente **a2** (integración IT3 con Retrofit/Gson/StateFlow) se cerraron en `d648c7e`. Los frentes **b**, **c** y **d** se cerraron en el commit `8832145`, con revisión **APROBADO CON OBSERVACIONES** (observaciones resueltas: M1, M2, m10 y re-lanzamiento de cancelación en el seed; versiones AndroidX test alineadas a 1.6.1).

| Dato | Valor |
|---|---|
| Proyecto | `136316a2-36dd-43b5-aa80-05b9e97d3f22` |
| PRD de hardening | `045a2de8-e11f-4286-930f-ed582728e411` |
| Conversación | `9b337131-5f44-49d8-b4f1-8edad505507b` |
| Task de documentación | `f8dfcced-828f-4140-8a27-3e350e295a43` |
| Commit | `8832145` (pendiente de push: faltan credenciales HTTPS) |
| ADR Fix M1 | `37d7d92c-c68b-46cd-971e-5583af38e655` |
| ADR Fix M2 | `e32848b6-0f4c-42dd-8ac9-0eda3c3f558c` |
| ADR Fix m10 | `b0bf8edc-6f6a-4a78-91d7-241a1359b318` |
| ADR Tests JVM `isReturnDefaultValues` | `ab094bb8-1cae-453a-8cd3-260fe8cfe43e` |
| ADRs de soporte | `42d32a6e-76c0-43c6-9a38-0f3488ac2176`, `4e2dd090-56e9-47fe-9f15-a40364cca5cb`, `30e5e778-2039-4732-9b0f-f5c5511d6e53`, `abaccc4f-2dc3-446d-98dc-cbbbe002979b` |

El detalle del cierre está en [NOTAS_TECNICAS.md](NOTAS_TECNICAS.md) y en la sección [Hardening de gestion documental y arranque](../README.md#hardening-de-gestion-documental-y-arranque) del README.

## 12. Registro de comandos

Todos los comandos usados para investigar, implementar y verificar IT3, agrupados por etapa. La columna "Para qué sirvió" explica cada uno.

### 12.1 Descubrimiento del caso y del repo

| Comando | Para qué sirvió |
|---|---|
| `find /home/simon -maxdepth 5 -iname "*_IT3*"` | Localizar el enunciado del caso en el equipo. |
| `unzip -o -q "/home/simon/Descargas/_IT3.docx" -d /tmp/opencode/it3` | Descomprimir el `.docx` (es un ZIP) para leer su contenido. |
| `python3 -c "... word/document.xml ..."` | Extraer el texto del XML interno del `.docx` (leer, quitar etiquetas y desescapar). |
| `git status --short` | Ver qué archivos estaban modificados o sin rastrear antes de documentar. |
| `git log --oneline -15` | Revisar el historial reciente de la rama. |
| `git remote -v` | Confirmar el remoto (`origin`). |
| `grep -rn "INTERNET\|Retrofit\|76 pruebas" README.md docs/*.md` | Encontrar las afirmaciones que había que corregir en la documentación. |

### 12.2 Trazabilidad Lemoria

| Comando | Para qué sirvió |
|---|---|
| `lemoria project list` | Obtener el id del proyecto `limpio_moviles`. |
| `lemoria conv create 136316a2-36dd-43b5-aa80-05b9e97d3f22 -t "IT3: GET /documentos"` | Crear la conversación de IT3. |
| `lemoria conv add 5bd0ebb1-d1bd-42ea-9a61-77fc0fe46b0a user "..."` | Registrar el mensaje/idea del usuario en la conversación. |
| `lemoria flow start 136316a2-36dd-43b5-aa80-05b9e97d3f22 "Integrar GET /documentos con Retrofit"` | Abrir el flujo SDD (el id devuelto es el PRD). |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 prd --status completed --output "..."` | Registrar el paso `prd` del flujo. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 tasks --status completed --output "..."` | Registrar el paso `tasks`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 implement --status completed --output "..."` | Registrar el paso `implement`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 test --status completed --output "..."` | Registrar el paso `test`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 review --status completed --output "..."` | Registrar el paso `review`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 document --status completed --output "..."` | Registrar el paso `document`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 vault_sync --status completed --output "..."` | Registrar el paso `vault_sync`. |
| `lemoria flow step 1c8bfe20-cc11-4429-8532-280c5d1720b2 consolidate --status completed --output "..."` | Registrar el paso `consolidate`. |
| `lemoria decision log 136316a2-36dd-43b5-aa80-05b9e97d3f22 -t "..." -d "..." -r "..."` | Registrar una decisión técnica (p. ej. usar API simulada local con `10.0.2.2`). |
| `lemoria task create 136316a2-36dd-43b5-aa80-05b9e97d3f22 1c8bfe20-cc11-4429-8532-280c5d1720b2 --title "..." --description "..."` | Crear cada una de las 9 tareas del flujo. |
| `lemoria vault sync 136316a2-36dd-43b5-aa80-05b9e97d3f22` | Exportar el proyecto al vault en markdown. |
| `lemoria flow status 1c8bfe20-cc11-4429-8532-280c5d1720b2` | Verificar el estado de todos los pasos del flujo. |

### 12.3 Build y pruebas

| Comando | Para qué sirvió |
|---|---|
| `./gradlew :app:compileDebugKotlin --console=plain --no-daemon` | Compilar Kotlin sin arrancar el daemon; detecta errores de compilación. |
| `./gradlew :app:testDebugUnitTest --console=plain --no-daemon` | Ejecutar la suite unitaria de debug. |
| `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --console=plain --no-daemon` | Compilar y probar en un solo paso; terminó con `BUILD SUCCESSFUL`. |
| `ls app/build/test-results/testDebugUnitTest/*.xml` | Listar los reportes XML por clase de test. |
| `grep -c "<testcase" app/build/test-results/testDebugUnitTest/*.xml` | Contar pruebas por archivo para verificar el total (95 al cierre de IT3; 105 tras el hardening). |

### 12.4 API simulada

| Comando | Para qué sirvió |
|---|---|
| `python3 tools/mock_api/server.py` | Arrancar la API simulada en el puerto 8000. |
| `curl -s http://127.0.0.1:8000/documentos` | Ver el JSON de documentos que consume la app. |
| `curl -s http://127.0.0.1:8000/health` | Comprobar que el servidor está vivo. |
| `curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8000/nope` | Confirmar que una ruta inexistente devuelve `404`. |

### 12.5 Git

| Comando | Para qué sirvió |
|---|---|
| `git status --short` | Ver el estado antes de preparar el commit. |
| `git diff` | Revisar el detalle de los archivos modificados. |
| `git diff --stat` | Ver el resumen de líneas cambiadas por archivo. |
| `git add ...` | Preparar los archivos que entrarían al commit. |
| `git commit -m "..."` | IT3 quedó registrado en `d648c7e` (`feat(IT3, d25a83c7): integrar GET /documentos con Retrofit y corregir BASE_URL`). El cierre de hardening se registró en `8832145`; **pendiente de push** por falta de credenciales HTTPS. |

### 12.6 Adb (dispositivo físico)

| Comando | Para qué sirvió |
|---|---|
| `adb reverse tcp:8000 tcp:8000` | Redirigir el puerto 8000 del dispositivo al PC, para que `127.0.0.1:8000` en el teléfono llegue al mock. |

## 13. Cómo explicarlo al docente

Respuestas breves a las preguntas del caso:

- **¿Qué información está solicitando?** La lista de documentos del módulo de Gestión Documental RR.HH.: id, propietario, categoría, título, estado, versión, vencimiento, dependencia y archivo. Antes vivían solo en Room con el sembrador local; ahora también se consultan por red.
- **¿Qué endpoint utiliza?** `GET /documentos`, sobre `http://10.0.2.2:8000/` en el emulador. Definido en `ApiConfig` y `DocumentosApiService`.
- **¿Qué JSON recibe?** Un arreglo de objetos con los 10 campos del contrato (ver sección 3). Los enums viajan como texto (`"CONTRATO"`, `"VIGENTE"`) y los opcionales pueden ser `null`.
- **¿Cómo se transforma en una data class?** `DocumentoDto` (con `@SerializedName`) recibe el JSON vía Gson; el método `aModelo()` valida y convierte a `Documento` de dominio.
- **¿Cómo Retrofit realiza la solicitud?** `RedDocumentos` construye el `Retrofit` con la URL base, un `OkHttpClient` y `GsonConverterFactory`; la interfaz `DocumentosApiService` con `@GET("documentos")` genera la llamada `suspend` que el repositorio invoca en `Dispatchers.IO`.
- **¿Cómo ViewModel/StateFlow entrega el resultado a Compose?** `DocumentosRemotosViewModel` publica `DocumentosRemotosUiState` en un `StateFlow`; la pantalla lo observa con `collectAsStateWithLifecycle()` y recompone según el estado.
- **¿Cómo responde la interfaz ante carga, éxito o error?** `Cargando` muestra el indicador; `Datos` muestra la lista filtrada por rol (con conteos de recibidos y visibles); `Error` muestra el mensaje, la ayuda del mock y el botón **Reintentar**.
