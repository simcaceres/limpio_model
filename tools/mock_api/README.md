# API simulada de documentos (`tools/mock_api`)

Esta carpeta contiene un **servidor local muy simple** (un programa pequeño que "sirve" datos por la red dentro de tu equipo) para probar la pantalla **"Documentos en línea"** del módulo de Gestión Documental RR.HH. sin usar Internet ni un servidor real.

- **Qué hace:** responde a `GET /documentos` con una lista de documentos ficticios (archivo `tools/mock_api/documentos.json`) y a `GET /health` con `{"status":"ok"}`.
- **Tecnología:** solo Python 3 y su biblioteca estándar (no hay que instalar dependencias extra).
- **Datos:** totalmente ficticios. No uses datos reales.

## Requisitos

- **Python 3** instalado.
  - Linux: prueba `python3 --version`
  - Windows: prueba `python --version` o `py -3 --version`

## Cómo arrancar el servidor

### Linux / macOS (bash)

Desde la **raíz del repositorio** (`limpio_model`):

```bash
python3 tools/mock_api/server.py
```

Si quieres usar otro puerto (por ejemplo 9000):

```bash
PORT=9000 python3 tools/mock_api/server.py
```

Deberías ver un mensaje como: `API simulada de documentos escuchando en el puerto 8000`.

Para detenerlo: presiona `Ctrl+C` en esa terminal.

### Windows (CMD)

Desde la raíz del repositorio:

```bat
python tools\mock_api\server.py
```

Con otro puerto:

```bat
set PORT=9000
python tools\mock_api\server.py
```

(En otra ventana abre un nuevo CMD si quieres quitar la variable; o cierra la ventana.)

### Windows (PowerShell)

Desde la raíz del repositorio:

```powershell
python tools\mock_api\server.py
```

Con otro puerto:

```powershell
$env:PORT = "9000"
python tools\mock_api\server.py
```

> Nota: si tu instalación usa el lanzador de Python, puedes reemplazar `python` por `py -3`.

## URLs correctas según el contexto

La misma API se ve con **direcciones distintas** dependiendo de desde dónde la consultes:

| Contexto (desde dónde abres la URL) | URL a usar | Por qué |
|---|---|---|
| Navegador en el **mismo PC** donde corre el servidor | `http://localhost:8000/documentos` | `localhost` significa "este mismo equipo". |
| **Emulador de Android** (Android Studio) | `http://10.0.2.2:8000/documentos` | Dentro del emulador, `10.0.2.2` es una dirección especial que apunta al PC anfitrión. La app ya tiene esta URL en `app/src/main/java/com/example/gestiondocumental/data/remote/ApiConfig.kt` (`BASE_URL = "http://10.0.2.2:8000/"`). |
| **Dispositivo físico por Wi‑Fi** (teléfono y PC en la misma red) | `http://IP_LAN_DEL_PC:8000/documentos` (ej. `http://192.168.1.50:8000/documentos`) | El teléfono debe alcanzar al PC por la red local. Además hay que (1) permitir esa IP en `app/src/main/res/xml/network_security_config.xml` (cleartext) y (2) cambiar `BASE_URL` en `ApiConfig.kt` y recompilar. |
| **Dispositivo físico por USB** (con `adb`) | `http://127.0.0.1:8000/documentos` en el dispositivo **tras** redirigir puerto | Ejecuta `adb reverse tcp:8000 tcp:8000` en el PC, cambia `BASE_URL` a `http://127.0.0.1:8000/` en `ApiConfig.kt`, recompila e instala. `127.0.0.1` es "este mismo dispositivo" (el puerto se reenvía al PC). |

**Importante (problema real):** abrir `http://10.0.2.2:8000/documentos` en el **navegador del PC** da **timeout** o no conecta. Eso es normal: `10.0.2.2` **solo** funciona desde el emulador de Android. En el navegador del PC usa siempre `http://localhost:8000/documentos`.

## Comprobación rápida

Con el servidor encendido, abre en el navegador del PC:

- `http://localhost:8000/health` → debe responder `{"status":"ok"}`
- `http://localhost:8000/documentos` → debe mostrar el arreglo JSON de documentos

Desde el emulador (con la app instalada y la URL por defecto), entra al módulo **Gestión Documental RR.HH.** → opción **"Documentos en línea"** y debería cargar la lista (estados: Cargando → Datos). Si falla, ver "Solución de problemas".

## Firewall de Windows y red

Si usas un **dispositivo físico por Wi‑Fi**, el firewall de Windows puede bloquear las conexiones entrantes al puerto 8000:

1. Al iniciar el servidor, Windows puede mostrar un aviso de firewall: permite el acceso en redes **privadas** (la de tu casa/lab), si corresponde.
2. Si no aparece aviso o sigue sin conectar: abre **"Firewall de Windows Defender con seguridad avanzada"** → **Reglas de entrada** → **Nueva regla** → Tipo **Puerto** → TCP → puerto específico `8000` → Permitir la conexión (solo en perfil Privado, si es posible) → asigna un nombre (ej. "Mock API Lemac 8000").
3. Asegúrate de que el teléfono y el PC estén en la **misma red Wi‑Fi** y que uses la **IP LAN** del PC (en PowerShell: `ipconfig`, busca "Dirección IPv4").

> En Linux, si tienes firewall activo (ufw/firewalld), permite el puerto 8000 en tu red local de forma equivalente.

## Solución de problemas ("no genera datos" / no carga)

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| La app muestra error o "Reintentar" y no lista documentos | **Servidor apagado** (no está corriendo `server.py`) | Inicia el servidor en una terminal y deja la ventana abierta. Luego pulsa "Reintentar" en la app. |
| Timeout al abrir `http://10.0.2.2:8000/...` en el navegador del PC | Estás usando la URL del **emulador** desde el PC | Usa `http://localhost:8000/documentos` en el PC. `10.0.2.2` solo vale desde el emulador. |
| `Address already in use` / puerto ocupado al iniciar | Otro programa usa el puerto 8000 | Cierra el proceso que lo usa o inicia con otro puerto: `PORT=9000 python3 tools/mock_api/server.py` (y ajusta `ApiConfig.kt` + recompila si la app debe apuntar a ese puerto). |
| El emulador no conecta pero `localhost` en PC sí funciona | La app apunta a otra URL o no recompilaste tras cambiarla | Verifica `ApiConfig.BASE_URL` en `app/src/main/java/com/example/gestiondocumental/data/remote/ApiConfig.kt` (debe ser `http://10.0.2.2:8000/` para emulador) y que `app/src/main/res/xml/network_security_config.xml` permita cleartext a `10.0.2.2`. Recompila e instala. |
| Dispositivo físico por Wi‑Fi no conecta | IP incorrecta, firewall o falta permiso de cleartext para esa IP | Usa la IP LAN correcta del PC, abre el puerto en el firewall (ver arriba), agrega la IP al `network_security_config.xml`, cambia `BASE_URL` y recompila. |
| Responde pero la lista sale vacía o con error de datos | JSON inválido o contrato no esperado | Revisa `tools/mock_api/documentos.json` y que el servidor imprima peticiones sin errores en la terminal. |

## Archivos relevantes

- `tools/mock_api/server.py`: servidor HTTP (endpoints `/documentos` y `/health`).
- `tools/mock_api/documentos.json`: datos ficticios que devuelve el servidor.
- `app/src/main/java/com/example/gestiondocumental/data/remote/ApiConfig.kt`: URL base usada por la app.
- `app/src/main/res/xml/network_security_config.xml`: dominios con tráfico HTTP (cleartext) permitido.

