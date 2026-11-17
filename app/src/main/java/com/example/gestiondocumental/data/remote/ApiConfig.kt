package com.example.gestiondocumental.data.remote

/**
 * Configuracion de la API REST simulada del modulo de gestion documental.
 *
 * La URL debe apuntar al equipo de desarrollo segun donde se ejecute la app:
 * - Emulador Android: `10.0.2.2` es el PC anfitrion visto desde el emulador.
 * - Dispositivo fisico por Wi-Fi: usar la IP LAN del PC (por ejemplo
 *   `http://192.168.1.10:8000/`) y agregar esa IP a
 *   `res/xml/network_security_config.xml` para permitir cleartext.
 * - Dispositivo fisico por USB: ejecutar `adb reverse tcp:8000 tcp:8000`,
 *   cambiar esta constante a `http://127.0.0.1:8000/` y recompilar; el puerto
 *   del telefono se redirige al PC.
 */
object ApiConfig {
    /** Emulador Android -> PC local (`10.0.2.2`). */
    const val BASE_URL = "http://10.0.2.2:8000/"
    const val ENDPOINT_DOCUMENTOS = "documentos"
}
