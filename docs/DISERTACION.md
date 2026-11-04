# Guion de disertacion: Lemac DataLab

Este documento propone una exposicion de 10 a 15 minutos sobre el proyecto Android `limpio_moviles`. Incluye bloques con tiempos, un guion sugerido, el orden de la demo en vivo, puntos tecnicos para mencionar, preguntas posibles del profesor y una lista de verificacion previa.

## Datos de la presentacion

- **Titulo:** Lemac DataLab: autorregistro privado en Android con Kotlin y Jetpack Compose.
- **Duracion objetivo:** 13 minutos, con margen para 15.
- **Idea central:** una app local, simple y honesta sobre sus limites, que demuestra un flujo completo de datos: formulario, validacion, Room, historial, resumen y recordatorio.
- **Mensaje final:** el valor del proyecto no esta solo en la pantalla, sino en como se separan responsabilidades y se protege la privacidad desde las decisiones tecnicas.

## Preparacion en cinco ideas

Si se olvida todo lo demas, recordar estas cinco ideas:

1. Es un MVP academico con datos ficticios o sinteticos; no es diagnostico ni tratamiento.
2. La arquitectura es UI Compose, un ViewModel compartido, repositorios y fuentes de datos locales.
3. Room guarda los registros y emite `Flow`; el ViewModel lo convierte en `StateFlow` y la UI lo recolecta con `collectAsStateWithLifecycle`.
4. La sincronizacion es simulada con `FakeLemacApi`; no hay backend ni permiso `INTERNET`.
5. Hay 13 pruebas unitarias del validador y del calculador de resumen; no hay pruebas de UI ni instrumentadas.

## Guion por bloques

### Bloque 0. Apertura (0:00 a 1:00)

Objetivo: presentar el problema y el alcance en una frase.

Guion sugerido:

> "Lemac DataLab es una aplicacion Android de autorregistro para cuatro ambitos de bienestar: neurodesarrollo, habilidades DBT, estado de animo y adicciones. La persona elige un ambito, registra que paso, con que intensidad y que estrategia uso, y despues puede revisar su historial y un resumen. Es un MVP academico: usa datos ficticios o sinteticos, no es diagnostico ni tratamiento, y no tiene backend real."

Mencionar que la app se llama Lemac DataLab aunque el proyecto tecnico se llame `limpio_moviles`.

### Bloque 1. Problema, publico y privacidad (1:00 a 3:00)

Objetivo: justificar por que existe la app y que limites tiene.

Puntos:

- El autorregistro breve ayuda a observar patrones, pero muchas apps piden cuentas y suben datos a la nube.
- Aqui se elige lo contrario: alias, almacenamiento local y una advertencia clara.
- El aviso de privacidad aparece en Onboarding y en Ajustes.
- No hay registro en linea, no hay nombre real y las notificaciones son genericas.
- Decir con claridad: "No debe usarse con datos reales de salud; el proyecto no tiene cifrado ni una politica de backup restrictiva".

Frase de transicion:

> "Para sostener esa promesa de simplicidad, la arquitectura separa interfaz, estado, repositorios y datos."

### Bloque 2. Arquitectura y decisiones (3:00 a 6:00)

Objetivo: explicar el recorrido de una accion sin entrar en cada linea.

Puntos:

- Una sola `Activity` (`MainActivity`) monta `LemacTheme` y `LemacApp()`.
- `LemacApp()` crea un unico `RegistroViewModel` con una `Factory` y arma el `NavHost`.
- Capas: pantallas Compose, ViewModel, repositorios, Room / `SharedPreferences` / `FakeLemacApi`.
- `LemacApplication` actua como contenedor manual de dependencias.
- Room guarda en la tabla `registros`; el DAO devuelve `Flow`; el ViewModel usa `stateIn` con `WhileSubscribed(5_000)`.
- La UI recolecta con `collectAsStateWithLifecycle`.
- El formulario usa `mutableStateOf` porque es estado de presentacion que cambia con cada tecla.

Si hay pizarra o diapositiva, mostrar el diagrama de capas de `docs/GUIA_ESTUDIO.md`.

Frase de transicion:

> "Veamos ese recorrido en la aplicacion funcionando."

### Bloque 3. Demo de onboarding y registro (6:00 a 9:00)

Objetivo: mostrar el flujo principal completo.

Orden:

1. Abrir la app con datos borrados para que aparezca Onboarding.
2. Mostrar el aviso de privacidad, escribir un alias y marcar el checkbox.
3. Explicar que la foto es opcional y que si se deniega el permiso la app sigue.
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
4. Explicar que la edicion actualiza la fecha y vuelve a marcar el registro como no sincronizado.
5. Eliminar un registro y mostrar el dialogo de confirmacion.
6. Abrir Resumen.
7. Mostrar el total, las barras por ambito y el promedio.
8. Pulsar "Sincronizar" y explicar que es una simulacion de 700 ms.

Que decir:

- "El filtro es estado local de la pantalla, no se guarda en la base."
- "El resumen recorre los cuatro ambitos y calcula cantidad, promedio simple y ultima fecha."
- "La barra compara la cantidad del ambito contra el ambito con mas registros, no contra la intensidad."
- "Sincronizar no envia datos: `FakeLemacApi` solo simula la operacion."

### Bloque 5. Recordatorio, foto y privacidad (11:00 a 13:00)

Objetivo: cubrir las funciones secundarias y volver al mensaje de privacidad.

Orden:

1. Abrir Ajustes.
2. Activar el recordatorio diario y mostrar el dialogo de permiso de notificaciones.
3. Explicar que WorkManager programa un trabajo unico cada 24 horas con retraso hasta las 20:00.
4. Aclarar que no es una alarma exacta y que puede retrasarse por el sistema.
5. Mostrar el cambio de foto y explicar que se guarda en `filesDir/perfil.png`.
6. Leer el bloque de privacidad.
7. No borrar todos los registros en vivo salvo que sea el final de la demo.

Que decir:

- "El canal de notificaciones se crea en `LemacApplication.onCreate`."
- "El worker solo llama a `NotificacionHelper.mostrar`; el texto no tiene datos del registro."
- "La foto no necesita permiso de almacenamiento porque queda en el directorio interno de la app."
- "El permiso de camara se pide en el momento de usarlo, no al instalar."

### Bloque 6. Limites, pruebas y cierre (13:00 a 15:00)

Objetivo: demostrar criterio y cerrar con una idea clara.

Puntos:

- La app no tiene backend real, autenticacion, cifrado ni migraciones de Room.
- La sincronizacion solo marca una bandera local.
- Hay 13 pruebas unitarias: 9 del validador y 4 del resumen.
- No hay pruebas instrumentadas ni de interfaz.
- El ViewModel es unico y compartido; para una app mas grande convendria separar por pantalla.
- `fallbackToDestructiveMigration` es aceptable en el MVP, no en produccion.
- `allowBackup="true"` y las reglas de backup de ejemplo son un punto a revisar.

Cierre sugerido:

> "El proyecto demuestra un flujo completo y verificable: el usuario registra, la validacion protege la calidad del dato, Room lo persiste, el Flow actualiza la interfaz y el resumen agrega la informacion. Las limitaciones estan documentadas a proposito: reconocerlas es parte del diseno. Lo que falta para produccion es backend real, cifrado, migraciones y pruebas de integracion."

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

En la version corta, no demostrar la edicion ni el borrado; alcanza con mencionarlos y mostrar el historial.

## Demo en vivo paso a paso

### Antes de empezar

Tener el emulador abierto, la app instalada y los datos borrados si se quiere mostrar Onboarding. Comando opcional:

```bash
adb shell pm clear com.example.limpio_moviles
```

Si se prefiere no mostrar el borrado de datos, se puede entrar directamente a Home con un alias ya guardado.

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
- "Editar reutiliza el mismo formulario con `editandoId`."
- "El borrado pide confirmacion porque no se puede deshacer."
- "La lista se actualiza sola por el Flow de Room."

### Paso 5. Resumen y sincronizacion

Que hacer:

- Abrir Resumen.
- Mostrar el total y las barras.
- Pulsar "Sincronizar".
- Senalar el cambio de texto mientras espera y el mensaje final.

Que decir:

- "El promedio es simple: suma de intensidades dividida entre la cantidad."
- "La barra usa la cantidad de registros, no la intensidad."
- "La sincronizacion espera 700 ms y marca los pendientes; no hay red."

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
- "La foto se guarda en el almacenamiento interno y la ruta en preferencias."
- "El aviso de privacidad se repite en Ajustes para que no dependa del onboarding."

### Paso 7. Cierre de la demo

Que hacer:

- Volver a Home.
- Mostrar como el ultimo registro aparece en la tarjeta del ambito.

Que decir:

- "Este es el recorrido completo: formulario, validacion, persistencia, actualizacion reactiva y agregacion."

Si se quiere mostrar el borrado total, hacerlo al final y decir que solo borra registros, no el alias ni la foto.

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
12. **Permisos en contexto.** Camara y notificaciones se piden cuando se necesitan, no al iniciar.
13. **WorkManager para trabajo diferible.** Sobrevive a reinicios y no requiere la app abierta.
14. **Privacidad por diseno y por omision.** Sin `INTERNET`, datos locales, notificaciones genericas y advertencias visibles.
15. **Honestidad sobre limites.** No hay cifrado, migraciones ni pruebas de UI; decirlo suma criterio.
16. **Catalogo de versiones.** `gradle/libs.versions.toml` centraliza versiones y evita numeros dispersos.
17. **KSP.** Room genera codigo en compilacion; KSP es el procesador recomendado para Kotlin.
18. **Pruebas unitarias.** 13 pruebas rapidas que no necesitan emulador.

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

Con alias en lugar de nombre real, datos locales, ausencia de permiso `INTERNET`, foto en almacenamiento interno, notificaciones genericas y advertencias visibles. Con honestidad: no hay cifrado ni backups restringidos; eso falta para produccion.

### 12. Que pasa si el usuario deniega la camara

La app sigue funcionando. El selector informa el rechazo, no se guarda foto y el resto de las funciones no se ve afectado. La foto es opcional por diseno.

### 13. Que pasa si deniega las notificaciones

El interruptor no se activa, no se programa el trabajo y se muestra un mensaje. La app sigue funcionando sin recordatorios.

### 14. Por que WorkManager y no AlarmManager

Porque el recordatorio es trabajo diferible, no una alarma critica. WorkManager sobrevive a reinicios, aplica politicas del sistema y no requiere que la app este abierta. AlarmManager exacto requeriria permisos adicionales y justificacion.

### 15. El recordatorio es exacto a las 20:00

No. Se programa con retraso inicial hasta las 20:00, pero WorkManager puede retrasarlo por Doze, bateria o carga del sistema. Hay que decir "aproximado".

### 16. Que se prueba y que no

Se prueban con JUnit el validador y el calculador de resumen: 13 casos. No hay pruebas de Room, repositorios, ViewModel, UI ni WorkManager. Es una deuda tecnica reconocida.

### 17. Por que no se uso Hilt

Porque el grafo de dependencias es pequeno: una base de datos, dos repositorios y un ViewModel. `LemacApplication` y una `Factory` resuelven el caso sin agregar un framework y sin costo de aprendizaje. En una app mas grande, Hilt seria razonable.

### 18. Por que KSP y no KAPT

Porque KSP es el procesador de simbolos nativo de Kotlin, mas rapido y alineado con el compilador. Room lo soporta. KAPT es mas lento y esta en modo de mantenimiento.

### 19. Los datos estan cifrados

No. Room y `SharedPreferences` guardan en el sandbox de la app, pero sin cifrado adicional. Para datos de salud reales habria que usar cifrado, gestion de claves y una revision de cumplimiento normativo.

### 20. Por que no hay permiso de Internet

Porque no hay llamadas de red reales. `FakeLemacApi` solo espera 700 ms. Declarar `INTERNET` sin necesidad seria una mala practica de minimo privilegio.

### 21. Por que `fallbackToDestructiveMigration`

Porque es un MVP con version 1 y se prioriza no bloquear el arranque ante un cambio de esquema. En produccion se deben escribir migraciones para no perder datos.

### 22. Que mejoraria primero

Antes de agregar funciones: migraciones de Room, pruebas de repositorio y ViewModel, cifrado o almacenamiento seguro, y una politica de backup. Despues: backend real, exportacion de datos y configuracion de la hora del recordatorio.

### 23. Por que el ViewModel es compartido

Porque simplifica el MVP: el formulario, el perfil y los mensajes estan disponibles en todas las pantallas. El costo es un estado global; en una app grande convendria un ViewModel por pantalla o por flujo.

### 24. Como se manejan los errores

La validacion devuelve errores por campo y la UI los muestra. Las operaciones de repositorio se envuelven en `runCatching`; si fallan, se publica un mensaje y no se actualiza la bandera de exito. No hay reintentos automaticos ni cola de sincronizacion.

## Checklist previo a la presentacion

### La noche anterior

- [ ] Ejecutar `./gradlew :app:assembleDebug` y confirmar que compila.
- [ ] Ejecutar `./gradlew :app:testDebugUnitTest` y confirmar las 13 pruebas.
- [ ] Abrir el reporte de pruebas y verificar que no hay fallos.
- [ ] Revisar que el emulador tenga Android 13 (API 33) o superior.
- [ ] Probar la app de punta a punta una vez.
- [ ] Confirmar que `docs/GUIA_ESTUDIO.md` y `docs/DISERTACION.md` estan actualizados.
- [ ] Verificar que el repositorio local este limpio y hacer el push cuando el repositorio remoto ya exista.

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
- [ ] Confirmar que el emulador tiene camara configurada si se va a mostrar la foto; si no, decir que es opcional.
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
- "Las decisiones tecnicas estan al servicio de la privacidad y de la claridad, no de la cantidad de funciones."
- "Las limitaciones estan documentadas: no hay backend real, cifrado ni pruebas de UI; el siguiente paso seria cerrar esas brechas."
- "Gracias. Quedo atento a preguntas."
