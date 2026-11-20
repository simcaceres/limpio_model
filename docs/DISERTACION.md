# Guion de disertacion: Lemac DataLab + Gestion Documental

Este documento propone una exposicion de 10 a 15 minutos sobre el proyecto Android `limpio_moviles`, la app unificada que entrega los dos trabajos academicos en un solo APK. Incluye bloques con tiempos, un guion sugerido, el orden de la demo en vivo, puntos tecnicos para mencionar, preguntas posibles del profesor y una lista de verificacion previa. El foco de la exposicion es el modulo de bienestar **Lemac DataLab**; el modulo de **Gestion Documental RR.HH.** se presenta desde el portal, con una demo corta de login y flujo documental.

## Datos de la presentacion

- **Material de traspaso para principiantes (complementario):** ver [README](../README.md#traspaso-y-replicación-para-principiantes), [GUIA_ESTUDIO](GUIA_ESTUDIO.md) (glosario, mapa de carpetas y recorrido por capas) y [NOTAS_TECNICAS](NOTAS_TECNICAS.md) (flujo SDD con Lemoria y trazabilidad).

- **Titulo:** Lemac DataLab + Gestion Documental: dos modulos academicos en una sola app Android con Kotlin y Jetpack Compose.
- **Duracion objetivo:** 13 minutos, con margen para 15.
- **Idea central:** una app local, simple y honesta sobre sus limites, que demuestra un flujo completo de datos (formulario, validacion, Room, historial, resumen, recordatorio y exportacion CSV local) y que, detras del mismo portal, integra un segundo dominio: el flujo documental de RR.HH. con roles, aprobaciones y auditoria, sin mezclar los datos de ambos.
- **Mensaje final:** el valor del proyecto no esta solo en la pantalla, sino en como se separan responsabilidades, se integran dos dominios sin mezclarlos y se protege la privacidad desde las decisiones tecnicas.

## Preparacion en cinco ideas

Si se olvida todo lo demas, recordar estas cinco ideas:

1. Es un MVP academico con datos ficticios o sinteticos; no es diagnostico ni tratamiento.
2. La app es un **portal unificado** con dos modulos (bienestar y gestion documental), cada uno con su base Room propia; la arquitectura es UI Compose, un ViewModel compartido, repositorios y fuentes de datos locales.
3. Room guarda los registros y emite `Flow`; el ViewModel lo convierte en `StateFlow` y la UI lo recolecta con `collectAsStateWithLifecycle`.
4. La sincronizacion de bienestar es simulada con `FakeLemacApi` y no hay backend real. Antes de IT3 tampoco habia permiso `INTERNET`; desde IT3 el modulo de gestion documental consume `GET /documentos` de una API REST simulada local (ver [INTEGRACION_API_IT3.md](INTEGRACION_API_IT3.md)). La salida real de datos de bienestar es la **exportacion CSV local**, con el selector de documentos del sistema (SAF).
5. Hay 105 pruebas unitarias JVM (33 de bienestar y 72 de gestion documental) y 9 instrumentadas (8 de siembra Room y 1 smoke Compose), compiladas pero no ejecutadas aqui porque `adb` no está en el PATH y no hay dispositivo/emulador conectado.

## Guion por bloques

### Bloque 0. Apertura (0:00 a 1:00)

Objetivo: presentar el problema y el alcance en una frase.

Guion sugerido:

> "Entregamos los dos trabajos como una sola app. Al abrirla aparece un portal con dos modulos: Lemac DataLab, una aplicacion Android de autorregistro para cuatro ambitos de bienestar —neurodesarrollo, habilidades DBT, estado de animo y adicciones—, y Gestion Documental RR.HH., el flujo documental de una institucion de salud con roles, aprobaciones y auditoria. Hoy nos concentramos en el modulo de bienestar: la persona elige un ambito, registra que paso, con que intensidad y que estrategia uso, y despues puede revisar su historial, un resumen y exportar sus datos a CSV. Es un MVP academico: usa datos ficticios o sinteticos, no es diagnostico ni tratamiento, y no tiene backend real."

Mencionar que el APK unico ya contiene ambos modulos (un solo modulo Gradle `:app`, una sola Activity y una sola clase Application) y que cada modulo conserva su base Room propia (`lemac_datalab.db` y `gestion_documental.db`), de modo que los datos de bienestar y los laborales simulados nunca se mezclan. La app visible se llama "Lemac DataLab + Gestion Documental" aunque el proyecto tecnico se llame `limpio_moviles`.

### Bloque 1. Problema, publico y privacidad (1:00 a 3:00)

Objetivo: justificar por que existe la app y que limites tiene.

Puntos:

- El autorregistro breve ayuda a observar patrones, pero muchas apps piden cuentas y suben datos a la nube.
- Aqui se elige lo contrario: alias, almacenamiento local y una advertencia clara.
- El aviso de privacidad aparece en Onboarding y en Ajustes.
- No hay registro en linea, no hay nombre real y las notificaciones son genericas.
- Decir con claridad: "No debe usarse con datos reales de salud; el proyecto no tiene cifrado, aunque el backup esta acotado a la base de datos y las preferencias, excluye explicitamente la foto y no incluye la cache (la plataforma la excluye automaticamente)".

Frase de transicion:

> "Para sostener esa promesa de simplicidad, la arquitectura separa interfaz, estado, repositorios y datos."

### Bloque 2. Arquitectura y decisiones (3:00 a 6:00)

Objetivo: explicar el recorrido de una accion sin entrar en cada linea.

Puntos:

- Una sola `Activity` (`MainActivity`) monta `LemacTheme` y `UnifiedApp()`, la navegacion raiz que arranca en el **portal** con las dos tarjetas de modulo.
- El portal enruta sin mezclar dominios: bienestar entra a `LemacApp()` y gestion documental a `GestionTheme { GestionApp() }`, cada uno con su grafo interno.
- `LemacApp()` crea un unico `RegistroViewModel` con una `Factory` y arma el `NavHost` de bienestar.
- Capas: pantallas Compose, ViewModel, repositorios, Room / `SharedPreferences` / `FakeLemacApi`.
- `LemacApplication` actua como contenedor **dual**: bases y repositorios de los dos modulos, dos canales de notificacion y siembra de los datos demo de gestion en `Dispatchers.IO`, con `CoroutineExceptionHandler` + `runCatching`: re-lanza cancelaciones, guarda `ultimoErrorSiembra` y un fallo de siembra no cierra la app.
- Room guarda en la tabla `registros`; el DAO devuelve `Flow`; el ViewModel usa `stateIn` con `WhileSubscribed(5_000)`.
- La UI recolecta con `collectAsStateWithLifecycle`.
- El formulario usa `mutableStateOf` porque es estado de presentacion que cambia con cada tecla.

Si hay pizarra o diapositiva, mostrar el diagrama de capas de `docs/GUIA_ESTUDIO.md`.

Frase de transicion:

> "Veamos ese recorrido en la aplicacion funcionando."

### Bloque 3. Demo de onboarding y registro (6:00 a 9:00)

Objetivo: mostrar el flujo principal completo.

Orden:

1. Abrir la app con datos borrados para que aparezca Onboarding; el portal se ve un segundo antes de entrar a Bienestar — Lemac DataLab.
2. Mostrar el aviso de privacidad, escribir un alias y marcar el checkbox.
3. Explicar que la foto es opcional y que si no hay camara o se cancela la captura, la app sigue.
4. Entrar a Home.
5. Abrir un ambito, por ejemplo Neurodesarrollo.
6. Intentar guardar sin tipo para mostrar la validacion.
7. Completar tipo, intensidad, contexto, estrategia y nota.
8. Guardar y mostrar la confirmacion y la vuelta automatica.

Que decir mientras se usa la app:

- "El boton esta deshabilitado hasta que el alias tiene tres caracteres y el aviso esta aceptado."
- "El tipo se elige de un catalogo que depende del ambito."
- "La intensidad va de 1 a 5, salvo adicciones, que va de 0 a 10."
- "La validacion devuelve un mapa de errores por campo."
- "Al guardar se inserta en Room y la lista se actualiza sola porque el DAO devuelve un Flow."

No entrar todavia en el codigo. La demo primero, la explicacion tecnica despues.

### Bloque 4. Historial y resumen (9:00 a 11:00)

Objetivo: mostrar que los datos no solo se guardan, sino que se consultan y agregan.

Orden:

1. Abrir Historial.
2. Cambiar el filtro entre "Todos" y un ambito.
3. Editar un registro y guardar.
4. Explicar que la edicion conserva la fecha original del registro y vuelve a marcarlo como no sincronizado, para que se reenvie en la sincronizacion simulada.
5. Eliminar un registro y mostrar el dialogo de confirmacion.
6. Abrir Resumen.
7. Mostrar el total, las barras por ambito y el promedio.
8. Ir a Ajustes y pulsar "Sincronizar", explicando que es una simulacion de 700 ms (la sincronizacion vive en Ajustes; en Resumen la accion principal es la exportacion CSV).
9. Pulsar "Exportar datos (CSV)": recorrer el selector de documentos del sistema, confirmar el nombre sugerido y mostrar el mensaje "Datos exportados (N registros)". El detalle paso a paso esta en la seccion "Demo de exportacion CSV".

Que decir:

- "El filtro es estado local de la pantalla, no se guarda en la base."
- "Editar reutiliza el mismo formulario con `editandoId`; conserva la fecha original del registro, no lo mueve al tope del historial, y lo vuelve a encolar para sincronizar."
- "El resumen recorre los cuatro ambitos y calcula cantidad, promedio simple y ultima fecha."
- "La barra compara la cantidad del ambito contra el ambito con mas registros, no contra la intensidad."
- "Sincronizar no envia datos: `FakeLemacApi` solo simula la operacion."
- "La exportacion si es real: escribe un CSV en el dispositivo, donde el usuario elija, y no pide permisos de almacenamiento porque usa SAF."

### Bloque 5. Recordatorio, foto y privacidad (11:00 a 13:00)

Objetivo: cubrir las funciones secundarias y volver al mensaje de privacidad.

Orden:

1. Abrir Ajustes.
2. Activar el recordatorio diario y mostrar el dialogo de permiso de notificaciones.
3. Explicar que WorkManager programa un trabajo unico cada 24 horas con retraso hasta las 20:00.
4. Aclarar que no es una alarma exacta y que puede retrasarse por el sistema.
5. Mostrar el cambio de foto y explicar que se guarda en `filesDir/perfil.png` ya orientada: la rotacion EXIF se corrige antes de mostrar y de guardar, por eso la foto ya no sale rotada.
6. Leer el bloque de privacidad.
7. No borrar todos los registros en vivo salvo que sea el final de la demo.

Que decir:

- "El canal de notificaciones se crea en `LemacApplication.onCreate`."
- "El worker solo llama a `NotificacionHelper.mostrar`; el texto no tiene datos del registro."
- "La foto no necesita permiso de almacenamiento porque queda en el directorio interno de la app."
- "La app no pide permiso de camara: lanza la camara del sistema con `ACTION_IMAGE_CAPTURE` y `EXTRA_OUTPUT` hacia un archivo temporal propio compartido con `FileProvider`, y esta app solo recibe el resultado. La caracteristica de hardware se declara como opcional, la orientacion EXIF se corrige antes de mostrar, la decodificacion usa `inSampleSize` (maximo 1024 px por lado) y el trabajo con archivos va a `Dispatchers.IO`."

### Bloque 6. Limites, pruebas y cierre (13:00 a 15:00)

Objetivo: demostrar criterio y cerrar con una idea clara.

Puntos:

- La app no tiene backend real, autenticacion en bienestar, cifrado ni migraciones de Room.
- La sincronizacion solo marca una bandera local.
- Hay 105 pruebas unitarias JVM: 33 de bienestar (validador, resumen, generador CSV, repositorio con DAO falsificado y ViewModel en JVM sin Robolectric) y 72 de gestion documental (validadores, permisos, flujo de modificacion, reportes, tipos MIME, las 14 de IT3, las 5 de regresion de `BASE_URL` y las 10 de `SesionRepositoryTest`).
- Hay 9 pruebas instrumentadas: 8 de siembra Room sobre base real (transaccionalidad, ids reales, reparacion de siembras parciales) y 1 smoke Compose del portal. Compilan, pero no se ejecutaron en este entorno porque no hay `adb`/dispositivo; casi no hay pruebas de interfaz fuera del portal.
- Hardening (19 de noviembre de 2026): siembra transaccional y convergente, auditoria best-effort con `CancellationException` propagada, sesion consistente ante cancelacion de auditoria y arranque tolerante a fallos. Review aprobado con observaciones resueltas; commit `8832145` aun no pusheado.
- El ViewModel de bienestar es unico y compartido; para una app mas grande convendria separar por pantalla.
- `fallbackToDestructiveMigration` es aceptable en el MVP, no en produccion.
- `allowBackup="true"` se mantiene, pero el backup esta acotado: incluye la base y las preferencias de bienestar, excluye explicitamente la base y los documentos de gestion y la foto de perfil, y no incluye la cache (la plataforma la excluye automaticamente y la lista de `<include>` es una allowlist). Sigue faltando cifrado.
- La exportacion CSV es texto plano e incluye el alias y las notas: hay que tratarla como informacion privada.

Cierre sugerido:

> "El proyecto demuestra un flujo completo y verificable: el usuario registra, la validacion protege la calidad del dato, Room lo persiste, el Flow actualiza la interfaz y el resumen agrega la informacion. Los datos solo salen del dispositivo cuando el usuario decide exportarlos: un CSV local, sin nube y sin permisos extra. Las limitaciones estan documentadas a proposito: reconocerlas es parte del diseno. Lo que falta para produccion es backend real, cifrado, migraciones y ejecutar las pruebas instrumentadas en un dispositivo."

## Version corta de 10 minutos

Si el tiempo se reduce, recortar asi:

| Bloque | Tiempo |
|---|---|
| Apertura | 30 s |
| Problema y privacidad | 1 min 30 s |
| Arquitectura | 2 min |
| Demo de registro | 2 min 30 s |
| Historial y resumen | 1 min 30 s |
| Recordatorio y foto | 1 min |
| Limites y cierre | 1 min |

En la version corta, no demostrar la edicion ni el borrado; alcanza con mencionarlos y mostrar el historial. La exportacion CSV se puede solo mencionar ("los datos salen del dispositivo unicamente cuando el usuario exporta su CSV"); si sobra un minuto, es la demo mas clara del compromiso de privacidad.

## Demo en vivo paso a paso

### Antes de empezar

Tener el emulador abierto, la app instalada y los datos borrados si se quiere mostrar Onboarding. Comando opcional:

```bash
adb shell pm clear com.example.limpio_moviles
```

Si se prefiere no mostrar el borrado de datos, se puede entrar directamente a Home con un alias ya guardado. Con los datos borrados, la app abre en el portal y Onboarding aparece despues de entrar a la tarjeta de Bienestar (ver Paso 0).

### Paso 0. Portal de la app unificada

Que hacer:

- Abrir la app: aparece el portal "Centro de aplicaciones" con las dos tarjetas y el aviso de datos ficticios.
- Entrar a la tarjeta "Bienestar — Lemac DataLab".

Que decir:

- "Los dos trabajos se entregan como una sola app: un APK unico, una sola Activity y una sola Application, con dos bases Room separadas para no mezclar los dominios."
- "El modulo de Gestion Documental RR.HH. se muestra aparte, con usuarios demo."

### Paso 1. Onboarding

Que hacer:

- Abrir la app.
- Leer en voz alta el aviso de privacidad.
- Escribir un alias, por ejemplo "Luna".
- Marcar el checkbox.
- No tomar foto en la primera pasada, o tomarla si hay buena luz en el emulador.

Que decir:

- "El alias es un seudonimo; no se pide nombre real."
- "El boton se habilita con tres caracteres y el consentimiento."
- "La foto es opcional y se puede cambiar despues."

### Paso 2. Home vacio y datos demo

Que hacer:

- Entrar a Home.
- Mostrar las cuatro tarjetas de ambito.
- Senalar la tarjeta de "Todavia no tienes registros".

Que decir:

- "Cada tarjeta muestra el ultimo registro del ambito o 'Sin registros'."
- "Si no hay datos, se ofrece cargar un conjunto de ejemplo."

Opcion A: pulsar "Cargar datos de ejemplo" para tener contenido rapido y luego registrar uno propio.
Opcion B: no cargar demo y crear el primer registro desde cero. Es mas claro para explicar el flujo nuevo.

Recomendacion: usar la opcion B para el registro principal y, al final, si queda tiempo, mostrar los datos demo en el resumen.

### Paso 3. Registro con validacion

Que hacer:

- Abrir "Neurodesarrollo".
- Pulsar "Guardar registro" sin elegir tipo.
- Mostrar el error "Selecciona un tipo".
- Elegir "Rutina".
- Mover el slider de intensidad.
- Escribir contexto: "Manana en casa".
- Escribir estrategia: "Lista de pasos".
- Escribir una nota breve.
- Guardar.

Que decir:

- "La validacion no esta en la pantalla: esta en `RegistroValidator`, que devuelve un mapa de errores."
- "El mismo validador se prueba con JUnit, sin emulador."
- "Al guardar, la entidad se inserta con id 0; Room genera el id real."

### Paso 4. Historial

Que hacer:

- Abrir Historial.
- Mostrar el registro recien creado.
- Cambiar el filtro a "Todos" y a otro ambito.
- Editar el registro: cambiar la nota y guardar.
- Volver a Historial.
- Eliminar un registro y mostrar el dialogo.

Que decir:

- "El filtro es local y no modifica la base."
- "Editar reutiliza el mismo formulario con `editandoId`: conserva la fecha original del registro y lo vuelve a encolar para la sincronizacion simulada."
- "El borrado pide confirmacion porque no se puede deshacer."
- "La lista se actualiza sola por el Flow de Room."

### Paso 5. Resumen, sincronizacion y exportacion CSV

Que hacer:

- Abrir Resumen.
- Mostrar el total y las barras.
- Pulsar "Exportar datos (CSV)" y recorrer el selector de documentos del sistema: elegir carpeta, confirmar el nombre sugerido y mostrar el mensaje "Datos exportados (N registros)". El detalle completo esta en la seccion "Demo de exportacion CSV".
- Ir a Ajustes y pulsar "Sincronizar".
- Senalar el cambio de texto mientras espera y el mensaje final.

Que decir:

- "El promedio es simple: suma de intensidades dividida entre la cantidad."
- "La barra usa la cantidad de registros, no la intensidad."
- "La sincronizacion espera 700 ms y marca los pendientes; no hay red."
- "La exportacion si es real: escribe un archivo CSV en el dispositivo, donde el usuario elija, y no pide permisos de almacenamiento porque usa SAF."

### Paso 6. Ajustes, recordatorio y foto

Que hacer:

- Abrir Ajustes.
- Cambiar el alias y guardar.
- Activar el recordatorio diario.
- Aceptar o rechazar el permiso de notificaciones y explicar ambos caminos.
- Cambiar la foto si el emulador lo permite.
- Leer el bloque de privacidad.

Que decir:

- "El permiso de notificaciones se pide solo al activar el recordatorio."
- "WorkManager programa un trabajo periodico unico; no es una alarma exacta."
- "La foto se guarda en el almacenamiento interno (PNG atomico, ya orientada segun EXIF) y la ruta en preferencias; el temporal de captura vive en la cache y se borra tras el uso."
- "El aviso de privacidad se repite en Ajustes para que no dependa del onboarding."

### Paso 7. Cierre de la demo

Que hacer:

- Volver a Home.
- Mostrar como el ultimo registro aparece en la tarjeta del ambito.

Que decir:

- "Este es el recorrido completo: formulario, validacion, persistencia, actualizacion reactiva y agregacion."

Si se quiere mostrar el borrado total, hacerlo al final y decir que solo borra registros, no el alias ni la foto.

## Demo de exportacion CSV

Esta seccion detalla como demostrar la exportacion de datos en la disertacion. Es la unica via real por la que los datos salen de la app, por eso conviene mostrarla completa y explicar sus limites. Toma menos de un minuto y no requiere preparacion de red ni permisos.

### Antes de la demo

- Tener al menos dos o tres registros guardados en el modulo de bienestar (sirven los "datos de ejemplo" de Home o los creados en los pasos anteriores).
- Tener claro donde se va a guardar el archivo (por ejemplo, la carpeta `Descargas` del emulador), para poder mostrarlo despues si se quiere.
- No hace falta nada mas: la app no pide permisos de almacenamiento ni conexion.

### Pasos

1. Entrar al modulo de bienestar desde el portal y abrir **Resumen** (el boton principal) o **Ajustes**, seccion **Datos**.
2. Pulsar **Exportar datos (CSV)**.
3. El sistema abre el **selector de documentos (SAF)**. No es un permiso: el usuario elige la carpeta y confirma el nombre; la app solo recibe un `Uri` para escribir.
4. Confirmar el nombre sugerido, con la forma `lemac_datalab_yyyyMMdd_HHmmss.csv` (por ejemplo, `lemac_datalab_20261111_203000.csv`), por ejemplo en `Descargas`.
5. La app escribe el archivo y muestra el mensaje `Datos exportados (N registros)`.
6. Opcional: abrir el CSV desde el administrador de archivos del emulador con un visor de texto, para mostrar la cabecera y que los acentos se ven bien gracias al BOM UTF-8.

### Que decir mientras se exporta

- "El boton lanza `ActivityResultContracts.CreateDocument("text/csv")`: el sistema crea el documento y entrega un `Uri`; por eso no hay permisos de almacenamiento."
- "El archivo empieza con BOM UTF-8 y usa CRLF, para que Excel reconozca la codificacion y muestre bien los acentos."
- "El texto CSV lo genera `RegistroCsvExporter`, un objeto puro sin dependencias de Android; por eso se prueba con JUnit sin emulador (8 pruebas)."
- "Los campos se escapan segun RFC 4180 y un campo que empieza como formula (`=`, `+`, `-`, `@`) se neutraliza con un apostrofo para que la hoja de calculo no lo ejecute."
- "La escritura va en `Dispatchers.IO` con `contentResolver.openOutputStream`; el hilo principal no se bloquea."
- "Una fila por registro, ordenadas de la mas reciente a la mas antigua, con 12 columnas: id, alias, ambito, tipo, intensidad, contexto, estrategia, nota, objetivo y dos fechas mas la bandera de sincronizado."

### Mensajes posibles y casos de borde

| Situacion | Mensaje |
|---|---|
| Exportacion exitosa | `Datos exportados (N registros)` |
| No hay registros (el boton ya llega deshabilitado; mensaje defensivo) | `No hay registros para exportar` |
| Escritura fallida | `No se pudo exportar` |
| Usuario cancela el selector | `Exportación cancelada` |
| Falla el lanzamiento del selector | `No se pudo abrir el selector de archivos` |

### Privacidad y limites que conviene decir

- El CSV es **texto plano** e incluye el alias y las notas: conviene tratarlo como informacion privada y guardar el archivo en un lugar seguro.
- La exportacion no borra nada ni reemplaza a la sincronizacion (que sigue simulada con `FakeLemacApi`).
- No hay cifrado ni exportacion parcial por ambito: siempre se exportan todos los registros guardados en el dispositivo.
- No se sube nada a la nube: el archivo queda donde el usuario lo decida. Antes de IT3 la app no declaraba `INTERNET`; desde IT3 sí lo declara, pero solo para la API REST simulada local de documentos.

## Puntos tecnicos que conviene mencionar

1. **Una sola Activity y navegacion declarativa.** `MainActivity` solo monta Compose; `NavHost` resuelve las rutas.
2. **Rutas con parametros.** `registro/{ambito}?id={id}` permite reutilizar la misma pantalla para crear y editar.
3. **Estado inmutable.** `RegistroUiState` es un `data class`; cada cambio usa `copy`.
4. **Separacion de estado.** `mutableStateOf` para formulario y perfil; `StateFlow` para datos de Room.
5. **Ciclo de vida.** `collectAsStateWithLifecycle` y `SharingStarted.WhileSubscribed(5_000)`.
6. **Un solo ViewModel compartido.** Es una decision de MVP; simplifica y tiene como costo un estado global.
7. **Repository como fachada.** `RegistroRepository` decide insertar o actualizar y coordina Room con la API.
8. **API simulada detras de una interfaz.** `LemacApi` permite cambiar `FakeLemacApi` por una implementacion real.
9. **Room con Flow.** Las consultas reactivas evitan refrescos manuales.
10. **Validacion fuera de la UI.** `RegistroValidator` es testeable y reutilizable.
11. **Calculo puro.** `ResumenCalculator` no depende de Android y se prueba con JUnit.
12. **Permisos en contexto.** Las notificaciones se piden cuando se necesitan, no al iniciar. La foto de perfil no requiere permiso `CAMERA` porque la captura delega en la app de camara del sistema (`ACTION_IMAGE_CAPTURE` + `EXTRA_OUTPUT` hacia un temporal propio compartido con `FileProvider`, mediante un contrato custom mas defensivo que el estandar); la orientacion EXIF se corrige antes de mostrar y guardar, y la decodificacion y el guardado van a `Dispatchers.IO`.
13. **WorkManager para trabajo diferible.** Sobrevive a reinicios y no requiere la app abierta.
14. **Privacidad por diseno y por omision.** Sin `INTERNET` antes de IT3 (desde IT3 solo para la API simulada local), datos locales, notificaciones genericas y advertencias visibles.
15. **Honestidad sobre limites.** No hay cifrado, migraciones ni pruebas de UI; decirlo suma criterio.
16. **Catalogo de versiones.** `gradle/libs.versions.toml` centraliza versiones y evita numeros dispersos.
17. **KSP.** Room genera codigo en compilacion; KSP es el procesador recomendado para Kotlin.
18. **Pruebas unitarias y de integracion local.** 105 pruebas rapidas que no necesitan emulador: 33 de bienestar (incluye 7 del ViewModel en JVM) y 72 de gestion documental (incluye las 14 de IT3, 5 de `BASE_URL` y 10 de sesion), mas 9 instrumentadas compiladas (8 de Room real y 1 smoke Compose) pendientes de ejecucion en dispositivo.
19. **Exportacion CSV con SAF.** `CreateDocument("text/csv")` evita permisos de almacenamiento; BOM UTF-8 y CRLF para Excel; escapado RFC 4180 y neutralizacion de formulas; el generador (`RegistroCsvExporter`) es puro y testeable con JUnit.
20. **Portal unificado sin mezclar dominios.** Un APK, dos paquetes (`com.example.limpio_moviles` y `com.example.gestiondocumental`), dos bases Room; `LemacApplication` como contenedor dual con dos canales de notificacion y siembra de datos demo de gestion en `Dispatchers.IO`. El hardening hizo esa siembra transaccional y convergente (SEED solo si no habia documentos, ids reales para las versiones) y tolerante a fallos en el arranque.
21. **Semantica de edicion explicita.** Al editar se conserva la `fechaHora` original y se fuerza `sincronizado = false`, de modo que el registro editado vuelve a la cola de sincronizacion simulada; el repositorio usa insert/update explicitos (sin upsert `REPLACE`, no resucita registros borrados) y `cargarDemo` es un `@Transaction` atomico que avisa cuando ya habia datos.

## Posibles preguntas del profesor y respuestas sugeridas

### 1. Por que Jetpack Compose y no XML

Porque permite escribir la interfaz en Kotlin, con estado y composicion declarativa. Reduce el salto entre logica y vista, facilita los componentes reutilizables y evita inflar layouts. Ademas, el proyecto es nuevo y no necesita compatibilidad con vistas legacy.

### 2. Por que una sola Activity

Porque Navigation Compose maneja los destinos dentro de un `NavHost`. Una sola Activity simplifica el ciclo de vida y evita duplicar configuracion. Es el enfoque recomendado para apps nuevas con Compose.

### 3. Cual es la diferencia entre `mutableStateOf` y `StateFlow`

`mutableStateOf` es estado observable de Compose: al cambiar, se recomponen las funciones que lo leen. `StateFlow` es un flujo de Kotlin con un valor actual, pensado para fuentes reactivas y para separar la capa de datos de la UI. En el proyecto se usan juntos: `mutableStateOf` para el formulario, `StateFlow` para Room.

### 4. Por que Room y no SQLite directo

Porque Room verifica las consultas en compilacion, genera el codigo de acceso, integra `Flow` y reduce errores. SQLite directo obligaria a escribir `Cursor`, SQL manual y conversion de tipos.

### 5. Por que un repositorio si no hay backend real

Porque igual hay dos fuentes: Room y una API simulada. El repositorio oculta esa decision y permite cambiar `FakeLemacApi` por una implementacion real sin tocar el ViewModel. Tambien ordena la logica de insertar o actualizar.

### 6. Por que la API es falsa

Porque el objetivo es demostrar el flujo de sincronizacion sin depender de un servidor. Una API real requeriria autenticacion, manejo de errores de red, contratos y credenciales. La interfaz deja preparado el reemplazo.

### 7. Como llega un dato desde Room hasta la pantalla

Room observa la tabla y emite una lista por el `Flow` del DAO. El repositorio expone ese `Flow`. El ViewModel lo convierte en `StateFlow` con `stateIn`. La pantalla lo recolecta con `collectAsStateWithLifecycle` y se recompone cuando cambia.

### 8. Que pasa si se rota la pantalla

La Activity se recrea, pero el ViewModel sobrevive porque esta asociado al propietario y no se destruye en un cambio de configuracion. El estado del formulario y el perfil se conservan. La recoleccion se reanuda con el ciclo de vida.

### 9. Que pasa si el sistema mata el proceso

El alias, la foto y el recordatorio se recuperan de `SharedPreferences` y del archivo. Los registros se recuperan de Room. Lo que se pierde es el formulario no guardado, porque `uiState` vive en el ViewModel y no se guarda con `SavedStateHandle`. Es una mejora posible.

### 10. Por que `minSdk` 33

Porque el proyecto usa APIs modernas y simplifica el manejo del permiso de notificaciones, que existe desde Android 13. La contrapartida es que deja fuera dispositivos con versiones anteriores. Para una app real habria que evaluar el publico y bajar el minimo con chequeos de version.

### 11. Como se protege la privacidad

Con alias en lugar de nombre real, datos locales, ausencia de permiso `INTERNET` antes de IT3 (desde IT3 solo para la API REST simulada local), foto en almacenamiento interno, notificaciones genericas y advertencias visibles. El backup esta acotado a Room y preferencias, excluye explicitamente la foto y no incluye la cache (la plataforma la excluye automaticamente). Con honestidad: no hay cifrado; eso falta para produccion.

### 12. Que pasa si no hay camara o se cancela la captura

La app sigue funcionando. El selector llama a `onCamaraNoDisponible`, la pantalla muestra "No se capturo la foto" y el perfil queda sin foto o conserva la anterior. No hay permiso de camara que denegar; la foto es opcional por diseno.

### 13. Que pasa si deniega las notificaciones

El interruptor no se activa, no se programa el trabajo y se muestra un mensaje. La app sigue funcionando sin recordatorios.

### 14. Por que WorkManager y no AlarmManager

Porque el recordatorio es trabajo diferible, no una alarma critica. WorkManager sobrevive a reinicios, aplica politicas del sistema y no requiere que la app este abierta. AlarmManager exacto requeriria permisos adicionales y justificacion.

### 15. El recordatorio es exacto a las 20:00

No. Se programa con retraso inicial hasta las 20:00, pero WorkManager puede retrasarlo por Doze, bateria o carga del sistema. Hay que decir "aproximado".

### 16. Que se prueba y que no

Se prueban con JUnit **105 casos JVM**: 33 del modulo de bienestar (el validador (9), el calculador de resumen (4), el generador CSV (8), el repositorio con un DAO falsificado (5) y el ViewModel en la JVM sin Robolectric (7)) y 72 de gestion documental —el modulo Lemac DataLab— (validadores, permisos, flujo de modificacion, reportes y tipos MIME, mas las 14 de IT3, las 5 de regresion de `BASE_URL` y las 10 de `SesionRepositoryTest`). El ViewModel ya no es deuda: se cubren creacion, edicion, edicion inexistente, validacion y carga demo con dobles de las interfaces y un reloj fijo. Siguen sin cubrirse Room real, UI instrumentada, WorkManager y la escritura real SAF/ContentResolver. Es una deuda tecnica reconocida y acotada.

### 17. Por que no se uso Hilt

Porque el grafo de dependencias es pequeno: una base de datos, dos repositorios y un ViewModel. `LemacApplication` y una `Factory` resuelven el caso sin agregar un framework y sin costo de aprendizaje. En una app mas grande, Hilt seria razonable.

### 18. Por que KSP y no KAPT

Porque KSP es el procesador de simbolos nativo de Kotlin, mas rapido y alineado con el compilador. Room lo soporta. KAPT es mas lento y esta en modo de mantenimiento.

### 19. Los datos estan cifrados

No. Room y `SharedPreferences` guardan en el sandbox de la app, pero sin cifrado adicional. Para datos de salud reales habria que usar cifrado, gestion de claves y una revision de cumplimiento normativo.

### 20. Por que (antes de IT3) no habia permiso de Internet

Porque en bienestar no hay llamadas de red reales: `FakeLemacApi` solo espera 700 ms. Declarar `INTERNET` sin necesidad seria una mala practica de minimo privilegio. Desde IT3 el modulo de gestion documental sí hace una llamada de red real contra `GET /documentos`, por lo que el permiso `INTERNET` se declara y el cleartext queda restringido a `10.0.2.2`, `localhost` y `127.0.0.1`.

### 21. Por que `fallbackToDestructiveMigration`

Porque es un MVP con version 1 y se prioriza no bloquear el arranque ante un cambio de esquema. En produccion se deben escribir migraciones para no perder datos.

### 22. Que mejoraria primero

Antes de agregar funciones: migraciones de Room, pruebas de Room real y de UI instrumentada, cifrado o almacenamiento seguro, y completar la politica de backup (hoy acotada a Room y preferencias, y sin cifrado). Despues: backend real, exportacion parcial por ambito (hoy se exportan todos los registros de una vez) y configuracion de la hora del recordatorio.

### 23. Por que el ViewModel es compartido

Porque simplifica el MVP: el formulario, el perfil y los mensajes estan disponibles en todas las pantallas. El costo es un estado global; en una app grande convendria un ViewModel por pantalla o por flujo.

### 24. Como se manejan los errores

La validacion devuelve errores por campo y la UI los muestra. Las operaciones de repositorio se envuelven en `runCatching`; si fallan, se publica un mensaje y no se actualiza la bandera de exito. No hay reintentos automaticos ni cola de sincronizacion.

### 25. Por que la exportacion usa SAF y no un permiso de almacenamiento

Porque `CreateDocument` le pide al sistema crear el documento y devolver un `Uri`: el usuario elige la carpeta y el nombre, y la app solo escribe en ese documento. No necesita `WRITE_EXTERNAL_STORAGE` (permiso obsoleto ademas) y respeta el principio de minimo privilegio. La escritura se hace con `contentResolver.openOutputStream` en `Dispatchers.IO`.

### 26. Que contiene el CSV y como se protege

Una fila por registro con 12 columnas: id, alias, ambito, tipo, intensidad, contexto, estrategia, nota, objetivo, fecha ISO 8601, fecha en milisegundos y bandera de sincronizado. Es texto plano con BOM UTF-8, saltos CRLF y escapado RFC 4180, y neutraliza campos que empiezan como formula. No esta cifrado e incluye el alias y las notas, asi que la app avisa que hay que tratarlo como informacion privada.

### 27. Por que dos bases Room y no una sola

Porque los dominios son distintos: bienestar es privado y funciona con alias, sin cuentas; gestion documental es un flujo laboral con roles y auditoria. Bases separadas evitan mezclar datos y permiten excluir del respaldo la base y los documentos de gestion. El costo es infraestructura de persistencia duplicada, documentado como deuda tecnica en INTEGRACION.md.

### 28. Como se integraron los dos trabajos

Con un portal: un unico modulo Gradle `:app`, una sola Activity y una sola Application que actua como contenedor dual. El paquete de gestion se copio dentro de `:app` conservando su nombre original (`com.example.gestiondocumental`), la navegacion raiz (`UnifiedApp`) arranca en el portal y cada modulo se monta con su tema y su grafo interno. El detalle completo esta en docs/INTEGRACION.md.

### 29. Por que la app no pide permiso de camara

Porque no usa la camara directamente: `SelectorFotoPerfil` lanza la app de camara del sistema (o de terceros) con un Intent `ACTION_IMAGE_CAPTURE` y `EXTRA_OUTPUT` apuntando a un archivo temporal de esta app, compartido mediante un `FileProvider` propio (`com.example.limpio_moviles.fileprovider`). Quien accede al hardware es la aplicacion de camara, con sus propios permisos; esta app solo recibe el resultado por la Activity Result API. El contrato es custom (`TomarFotoConUri`), equivalente al estandar `ActivityResultContracts.TakePicture` de androidx.activity 1.8.0 pero mas defensivo: agrega `ClipData` y flags de lectura y escritura para que las apps de camara OEM de terceros con permiso persistan el grant temporal sobre el archivo. El manifiesto, por su parte, solo declara `uses-feature android.hardware.camera required="false"`, para poder instalarse en equipos sin camara; el manifest fusionado no contiene ningun `uses-permission` de `CAMERA`.

### 30. Que pasa con la fecha cuando se edita un registro

La fecha original se conserva. Al editar, `guardar()` relee el registro existente y construye la entidad con su `fechaHora` original (`existente?.fechaHora ?: ahora`); la hora actual solo se usa al crear. Lo que si cambia siempre es la bandera: `sincronizado = false`, de modo que el registro editado vuelve a la cola de pendientes y se reenvia en la sincronizacion simulada. Como el historial ordena por `fechaHora DESC` y la ultima fecha del resumen es el maximo de `fechaHora`, editar no reordena la lista ni altera el resumen. Y si el registro ya no existe al momento de guardar (por ejemplo, se borro en otra pantalla), se avisa con "No se encontró el registro que intentas editar" en lugar de crear uno nuevo.

## Checklist previo a la presentacion

### La noche anterior

- [ ] Ejecutar `./gradlew :app:assembleDebug` y confirmar que compila.
- [ ] Ejecutar `./gradlew :app:testDebugUnitTest` y confirmar las 105 pruebas JVM.
- [ ] Si hay emulador/dispositivo con `adb`, ejecutar `./gradlew :app:connectedDebugAndroidTest` y confirmar las 9 instrumentadas (aqui solo se compilaron con `assembleDebugAndroidTest`).
- [ ] Abrir el reporte de pruebas y verificar que no hay fallos.
- [ ] Revisar que el emulador tenga Android 13 (API 33) o superior.
- [ ] Probar la app de punta a punta una vez, incluido el portal con las dos tarjetas.
- [ ] Probar una vez la exportacion CSV y verificar donde queda el archivo.
- [ ] Probar la foto de perfil en el emulador: capturar, confirmar que no sale rotada (la orientacion EXIF se corrige antes de mostrar) y que no aparece dialogo de permiso de camara.
- [ ] Editar un registro y confirmar que la fecha mostrada no cambia y que el registro vuelve a quedar pendiente de sincronizar.
- [ ] Confirmar que `docs/GUIA_ESTUDIO.md` y `docs/DISERTACION.md` estan actualizados.
- [ ] Verificar que el repositorio local este limpio y hacer el push cuando el repositorio remoto ya exista.

Referencia de la verificacion previa (12 de noviembre de 2026, antes de IT3), con clean build: `clean compileDebugKotlin testDebugUnitTest assembleDebug` con `BUILD SUCCESSFUL`, 76/76 pruebas (33 de bienestar + 43 de gestion documental), y APK de debug de ~62 MB (64,915,563 bytes). Verificacion de IT3 (15 de noviembre de 2026): `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --console=plain --no-daemon` con `BUILD SUCCESSFUL`; el arbol de `d648c7e` tenia 95 pruebas (33 de bienestar + 62 de gestion documental; el informe de IT3 documento 90 por omission de `ApiConfigTest`). **Verificacion del hardening (19 de noviembre de 2026, commit `8832145`): 105/105 pruebas JVM, 0 fallidas (33 de bienestar + 72 de gestion documental) y `assembleDebugAndroidTest` correcto con 9 instrumentadas compiladas, no ejecutadas porque `adb` no está en el PATH y no hay dispositivo/emulador conectado.** Entorno: JVM 21, Android SDK en `/home/simon/Android/Sdk` y `google-chrome` en `/usr/bin/google-chrome`. Se mantiene el manifest fusionado sin `uses-permission` de `CAMERA` y con el `FileProvider` y su `file_paths`. Repetir esta verificacion si se vuelve a tocar codigo.

### El dia de la presentacion

- [ ] Abrir Android Studio con el proyecto ya sincronizado.
- [ ] Verificar `adb devices` y que el emulador este conectado.
- [ ] Instalar la version actual: `./gradlew :app:installDebug`.
- [ ] Borrar datos de la app si se quiere mostrar Onboarding: `adb shell pm clear com.example.limpio_moviles`.
- [ ] Verificar que el teclado del emulador no tape los campos.
- [ ] Bajar el brillo de la pantalla si el proyector lo requiere.
- [ ] Silenciar notificaciones personales del equipo.
- [ ] Tener el guion abierto en otra ventana o en papel.
- [ ] Tener a mano los comandos de compilacion e instalacion.
- [ ] Confirmar que el emulador tiene camara configurada si se va a mostrar la foto; si no, decir que es opcional. Si se captura en vivo, verificar que la foto no salga rotada y que el sistema no pida permiso de camara a esta app.
- [ ] Confirmar el permiso de notificaciones: se puede mostrar el dialogo o preaprobarlo.
- [ ] Tener una version de respaldo del APK: `app/build/outputs/apk/debug/app-debug.apk`.

### Verificaciones tecnicas utiles

```bash
# Ver dispositivos conectados
adb devices

# Instalar
./gradlew :app:installDebug

# Abrir la app
adb shell am start -n com.example.limpio_moviles/.MainActivity

# Borrar datos para volver a Onboarding
adb shell pm clear com.example.limpio_moviles

# Ejecutar pruebas
./gradlew :app:testDebugUnitTest

# Ver el reporte
xdg-open app/build/reports/tests/testDebugUnitTest/index.html
```

### Sobre la notificacion en vivo

No conviene esperar hasta las 20:00 durante la exposicion. Lo practico es:

1. Activar el recordatorio.
2. Explicar que WorkManager lo ejecutara a las 20:00.
3. Aclarar que puede retrasarse por el sistema.
4. No prometer que la notificacion aparecera durante la exposicion; el valor de la demo esta en mostrar la activacion y explicar el flujo.

Si se quiere una demostracion real de la notificacion, habria que preparar un mecanismo de prueba o cambiar la hora del emulador; no se recomienda hacerlo en vivo porque agrega riesgo y no es el objetivo de la disertacion.

### Plan B si falla el emulador

- Tener el APK compilado y mencionar que se puede instalar en un dispositivo fisico.
- Tener abierta la guia de estudio con los diagramas y explicar el flujo con ellos.
- Mostrar el reporte de pruebas unitarias como evidencia de que la logica principal funciona.
- No improvisar funcionalidades que no existen: si no se puede mostrar la camara, decir que es opcional y que el resto del flujo sigue.

### Frases de cierre por si falta tiempo

- "La app demuestra el ciclo completo: captura, validacion, persistencia, observacion reactiva y agregacion."
- "Los datos solo salen del dispositivo cuando el usuario decide exportarlos: un CSV local, sin nube y sin permisos extra."
- "Las decisiones tecnicas estan al servicio de la privacidad y de la claridad, no de la cantidad de funciones."
- "Las limitaciones estan documentadas: no hay backend real, cifrado ni pruebas de UI; el siguiente paso seria cerrar esas brechas."
- "Gracias. Quedo atento a preguntas."
