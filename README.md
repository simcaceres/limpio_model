# Lemac DataLab

Aplicacion Android de autorregistro desarrollada en Kotlin y Jetpack Compose. Permite anotar, en pocos pasos, como se siente la persona y que estrategias utiliza en cuatro ambitos de bienestar: neurodesarrollo, habilidades DBT, estado de animo y adicciones.

El proyecto se presenta como un **MVP academico**. Su objetivo es demostrar un flujo completo de aplicacion movil: interfaz Compose, navegacion, estado, persistencia local, recordatorios, una capa de sincronizacion simulada y exportacion local de los datos a CSV.

## Publico objetivo

- Personas que quieran llevar un autorregistro breve y privado en su telefono.
- Estudiantes o docentes que necesiten un ejemplo funcional de arquitectura Android moderna.
- Publico de una disertacion que quiera entender el recorrido de los datos dentro de una app.

La aplicacion usa un **alias o seudonimo**, no el nombre real. No hay cuentas, registro en linea ni perfiles en la nube. La exportacion CSV tambien es local: el archivo se genera en el dispositivo y se guarda donde el usuario lo indique.

## Aviso de privacidad

> Este proyecto es un **MVP con datos ficticios o sinteticos**. No es un instrumento de diagnostico, no es tratamiento y no reemplaza la atencion de un profesional de la salud. **No se deben ingresar datos reales de salud.**

En el estado actual del codigo:

- Los registros se guardan en una base de datos Room local del dispositivo.
- El alias, la ruta de la foto y el estado del recordatorio se guardan en `SharedPreferences`.
- La foto de perfil se guarda como un archivo PNG en el almacenamiento interno de la app.
- La exportacion CSV es **local**: el archivo se genera en el dispositivo y se guarda donde el usuario elige con el selector de documentos del sistema (SAF). No hay subida a la nube ni permiso de almacenamiento.
- No existe un backend real ni se envian datos a Internet. La sincronizacion usa `FakeLemacApi`, que solo simula una espera de 700 ms.
- El manifiesto no declara el permiso `INTERNET`.
- Las notificaciones tienen un texto generico, sin contenido sensible.
- El CSV es **texto plano** e incluye el alias y las notas, asi que conviene tratarlo como informacion privada.

Limites de privacidad que conviene conocer: el manifiesto mantiene `android:allowBackup="true"`, pero las reglas de backup estan acotadas. Se incluyen explicitamente la base de datos Room (`database`) y las `SharedPreferences` (`sharedpref`, con el alias o seudonimo y la ruta de la foto), y se excluye explicitamente la foto de perfil (`file/perfil.png`). La cache no se incluye porque la plataforma la excluye siempre de forma automatica y, al usar `<include>`, la lista de respaldo es una allowlist limitada a Room y `SharedPreferences`; las reglas XML no declaran una exclusion de cache. Las mismas reglas se aplican en `backup_rules.xml` para API 30 o inferior y en `data_extraction_rules.xml` para `<cloud-backup>` y `<device-transfer>` en API 31 o superior. La base de datos sigue sin cifrar. Ademas, la app no declara el permiso `CAMERA`: la vista previa de camara usa `TakePicturePreview`, que no lo requiere, y `uses-feature android.hardware.camera required="false"` permite instalar la app en equipos sin camara. Para un producto real habria que revisar el cifrado y el borrado de datos.

## Tecnologias y versiones reales

Las versiones se toman de `gradle/libs.versions.toml`, `app/build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`.

| Tecnologia | Version / configuracion |
|---|---|
| Lenguaje | Kotlin 2.0.21 |
| Android Gradle Plugin (AGP) | 9.2.1 |
| Gradle (wrapper) | 9.5.0 |
| Compose BOM | 2026.02.01 |
| Jetpack Compose | Gestionado por el BOM |
| Material 3 | Gestionado por el BOM |
| Navigation Compose | 2.8.9 |
| Room | 2.8.5 |
| WorkManager | 2.10.0 |
| KSP | 2.0.21-1.0.28 |
| Lifecycle Runtime / ViewModel Compose | 2.8.5 |
| Activity Compose | 1.8.0 |
| AndroidX Core KTX | 1.10.1 |
| JUnit | 4.13.2 |
| compileSdk | 37 |
| targetSdk | 37 |
| minSdk | 33 (Android 13 o superior) |
| Java source/target | 11 |
| Nombre del paquete | `com.example.limpio_moviles` |
| Nombre del proyecto Gradle | `limpio_moviles` |

Nota de configuracion: el modulo `app` aplica los plugins `com.android.application`, `org.jetbrains.kotlin.plugin.compose` y `com.google.devtools.ksp`. No hay una linea explicita de `org.jetbrains.kotlin.android` en `app/build.gradle.kts`, porque AGP 9 incorpora el soporte de Kotlin en el propio plugin de Android.

## Requisitos

- **Android Studio** actualizado, con soporte para AGP 9.2.1.
- **JDK 17 o superior**. Android Studio incluye un JDK compatible; tambien sirve un JDK instalado en el sistema.
- **Android SDK 37** instalado desde el SDK Manager.
- Un **emulador o dispositivo fisico con Android 13 (API 33) o superior**.
- Conexion a Internet la primera vez, para que Gradle descargue el wrapper 9.5.0 y las dependencias.

## Como abrir el proyecto

1. Abrir Android Studio.
2. Elegir `Open` y seleccionar la carpeta raiz `limpio_moviles`.
3. Esperar la sincronizacion de Gradle.
4. Verificar que el SDK 37 este instalado si Android Studio lo solicita.
5. No hace falta crear `local.properties` a mano: Android Studio lo genera con la ruta del SDK.

## Como compilar, instalar y probar

Desde la raiz del proyecto, en una terminal:

```bash
# Compilar el APK de debug
./gradlew :app:assembleDebug

# Instalar en el emulador o dispositivo conectado
./gradlew :app:installDebug

# Ejecutar las pruebas unitarias de debug
./gradlew :app:testDebugUnitTest
```

El APK de debug queda en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

El reporte HTML de las pruebas unitarias queda en:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

### Estado real de las pruebas

El proyecto sí incluye pruebas unitarias; conviene decirlo con precisión:

- `app/src/test/java/com/example/limpio_moviles/model/RegistroValidatorTest.kt` contiene 9 pruebas de validacion.
- `app/src/test/java/com/example/limpio_moviles/model/ResumenCalculatorTest.kt` contiene 4 pruebas de calculo del resumen.
- `app/src/test/java/com/example/limpio_moviles/data/local/RegistroCsvExporterTest.kt` contiene 6 pruebas del generador CSV:
  1. La salida empieza con BOM UTF-8 y contiene la cabecera exacta.
  2. Un registro completo genera la fila con todos los campos.
  3. Un campo con coma, comillas y salto de linea se escapa con comillas dobladas.
  4. Una lista vacia produce solo BOM, cabecera y CRLF.
  5. `nombreArchivo(millis)` cumple el patron `lemac_datalab_yyyyMMdd_HHmmss.csv`.
  6. Las filas usan saltos de linea CRLF, sin `\n` sueltos.
- En total son 19 pruebas unitarias con JUnit 4 (9 + 4 + 6).
- **No hay pruebas instrumentadas ni pruebas de interfaz** (`app/src/androidTest` no existe).
- **No hay pruebas de Room, repositorios, ViewModel ni WorkManager.**

## Exportar los datos

La app permite exportar todos los registros guardados en Room a un archivo **CSV local**. La exportacion no usa backend ni sube nada a Internet.

### Pasos

1. Abrir **Ajustes** y, en la seccion **Datos**, pulsar **Exportar datos (CSV)**. Tambien se puede hacer desde **Resumen**, con el boton principal **Exportar datos (CSV)**.
2. Se abre el selector de documentos del sistema (`ActivityResultContracts.CreateDocument("text/csv")`, SAF).
3. Elegir la carpeta de destino (por ejemplo, `Descargas` / `Downloads`) y confirmar el nombre sugerido.
4. La app escribe el archivo y muestra un mensaje: `Datos exportados (N registros)`. Si no hay registros muestra `No hay registros para exportar`; si falla la escritura, `No se pudo exportar`; si se cancela el selector, `Exportación cancelada`.

El archivo se guarda **donde el usuario lo indique con el selector del sistema**. La app no pide permiso de almacenamiento porque usa el mecanismo SAF y escribe a traves de `contentResolver.openOutputStream`.

### Nombre y formato

- Nombre sugerido: `lemac_datalab_yyyyMMdd_HHmmss.csv` (por ejemplo, `lemac_datalab_20261104_233000.csv`).
- Codificacion: **UTF-8 con BOM**, para que Excel y otras hojas de calculo muestren bien los acentos.
- Saltos de linea: **CRLF** (`\r\n`), compatible con Excel.
- Una fila por registro, ordenadas por `fechaHora DESC` (la mas reciente primero).
- Escapado **RFC 4180**: si un campo contiene coma, comillas o salto de linea, se encierra entre comillas dobles y las comillas internas se duplican.

### Columnas

Cabecera exacta:

```text
id,alias,ambito,tipo,intensidad,contexto,estrategia,nota,objetivo,fecha_hora_iso,fecha_hora_millis,sincronizado
```

| Columna | Contenido |
|---|---|
| `id` | Identificador interno del registro |
| `alias` | Alias o seudonimo del perfil |
| `ambito` | Nombre del ambito (`NEURODESARROLLO`, `DBT`, `ANIMO`, `ADICCIONES`) |
| `tipo` | Tipo elegido del catalogo |
| `intensidad` | Valor entero dentro del rango del ambito |
| `contexto` | Situacion o desencadenante |
| `estrategia` | Habilidad o accion registrada |
| `nota` | Nota libre (maximo 200 caracteres) |
| `objetivo` | Estado del objetivo (solo adicciones) |
| `fecha_hora_iso` | Fecha y hora en formato ISO 8601 (`Instant.toString()`) |
| `fecha_hora_millis` | La misma fecha en milisegundos desde epoch |
| `sincronizado` | `true` o `false` segun la bandera local |

> El CSV es **texto plano**: incluye el alias y las notas. No esta cifrado. Guardarlo en un lugar privado y no compartirlo sin necesidad.

## Estructura de carpetas resumida

```text
limpio_moviles/
├── app/
│   ├── build.gradle.kts              Configuracion del modulo Android
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   Permisos, Application y Activity
│       │   ├── java/com/example/limpio_moviles/
│       │   │   ├── LemacApplication.kt   Contenedor manual de dependencias
│       │   │   ├── MainActivity.kt       Unica Activity; monta Compose
│   │   │   ├── data/
│   │   │   │   ├── local/            Room (entidad, DAO, base) y RegistroCsvExporter.kt
│   │   │   │   ├── remote/           LemacApi y FakeLemacApi
│   │   │   │   └── repository/       RegistroRepository y PerfilRepository
│   │   │   ├── model/                Ambito, catalogos, estados y calculos
│   │   │   ├── navigation/           Rutas y NavHost
│   │   │   ├── ui/
│   │   │   │   ├── components/       Componentes reutilizables (incluye BotonExportarCsv.kt)
│   │   │   │   ├── screens/          Seis pantallas
│   │   │   │   ├── theme/            Color, tipografia y tema
│   │   │   │   └── util/             Formato de fechas
│   │   │   ├── util/                 Foto, notificaciones y recordatorios
│   │   │   └── viewmodel/            RegistroViewModel y su Factory
│   │   └── res/                      Strings, colores, temas e iconos
│       └── test/java/com/example/limpio_moviles/
│           ├── data/local/RegistroCsvExporterTest.kt
│           └── model/
│               ├── RegistroValidatorTest.kt
│               └── ResumenCalculatorTest.kt
├── docs/
│   ├── GUIA_ESTUDIO.md                   Guia principal de estudio
│   └── DISERTACION.md                    Guion de exposicion y demo
├── gradle/
│   ├── libs.versions.toml                Catalogo de versiones
│   └── wrapper/                          Gradle Wrapper 9.5.0
├── build.gradle.kts                      Plugins del proyecto raiz
├── settings.gradle.kts                   Repositorios y modulo :app
├── gradle.properties                     Configuracion de Gradle
├── gradlew / gradlew.bat                 Scripts del wrapper
└── .gitignore
```

Documentacion detallada:

- [Guia de estudio](docs/GUIA_ESTUDIO.md): arquitectura, componentes, diagramas y flujos paso a paso.
- [Guion de disertacion](docs/DISERTACION.md): bloques de exposicion, demo en vivo, preguntas y checklist.

## Como subir este proyecto a Git

El repositorio local **ya esta creado**. Para ver el estado real y el ultimo commit, ejecutar `git status` y `git log --oneline -1`; no conviene asumir un estado fijo, porque el arbol cambia a medida que se trabaja. La rama principal es `main`.

Si aun no existe un remoto configurado, falta crearlo y hacer el primer push. Reemplazar `USUARIO` y `REPOSITORIO` por los valores reales.

### 1. Crear el repositorio remoto

Crear primero un repositorio vacio en GitHub, GitLab o el servicio elegido. Conviene **no** marcar la opcion de agregar README, `.gitignore` ni licencia, para evitar conflictos con el primer push.

### 2. Revisar el estado local

Desde la raiz del proyecto (o con `cd limpio_moviles` si se esta en la carpeta padre):

```bash
cd limpio_moviles

git status
git log --oneline -1
```

`git log --oneline -1` muestra el ultimo commit y `git status` indica si hay cambios pendientes. No deben aparecer `local.properties`, `build/`, `.gradle/`, `.idea/` ni archivos `.apk` como archivos versionados. El archivo `.gitignore` ya los excluye.

El comando `git init` ya no forma parte de esta guia porque el repositorio local existe. Solo seria necesario si en otra maquina se parte de una copia de archivos sin historial Git.

### 3. Conectar el remoto y hacer el primer push

```bash
git remote add origin https://github.com/USUARIO/REPOSITORIO.git
git push -u origin main
```

Si se usa SSH, el remoto puede ser:

```bash
git remote add origin git@github.com:USUARIO/REPOSITORIO.git
```

### 4. Verificaciones utiles

```bash
git status
git log --oneline
git remote -v
```

### Que esta ignorado y que si se versiona

El `.gitignore` excluye, entre otros:

- `.gradle/` y `build/` (artefactos de Gradle).
- `.kotlin/` (cache del compilador).
- `local.properties` (ruta local del SDK y datos de la maquina).
- `.idea/`, `*.iml` (configuracion local de Android Studio).
- `captures/`, `*.apk`, `*.aab`, `*.ap_`, `*.dex`.
- `*.jks`, `*.keystore`, `keystore.properties` (material de firma).
- `.DS_Store`, `Thumbs.db`, `*.log`.

El `.gitignore` tiene una excepcion: `!gradle/wrapper/gradle-wrapper.jar`. Eso significa que **el JAR del wrapper si debe subirse**, porque permite que otra persona compile con `./gradlew` sin tener Gradle instalado globalmente.

### Si ya se agrego algo por error

Si `local.properties` o una carpeta `build/` ya quedaron en el indice de Git, se pueden quitar del indice sin borrarlos del disco:

```bash
git rm --cached local.properties
git rm -r --cached build .gradle .idea
git commit -m "Quitar archivos locales del control de versiones"
```

## Nota sobre el nombre

El nombre visible de la aplicacion es **Lemac DataLab** (`app_name` en `strings.xml`). El nombre tecnico del proyecto y del paquete sigue siendo `limpio_moviles` / `com.example.limpio_moviles`; no es necesario renombrarlo para la disertacion.
