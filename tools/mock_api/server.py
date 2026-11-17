#!/usr/bin/env python3
"""API REST simulada para la funcionalidad de documentos (GET /documentos).

Solo usa la biblioteca estandar de Python 3. Sirve el contenido de
`documentos.json` y expone un endpoint de salud para pruebas manuales.
"""

import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

PUERTO = int(os.environ.get("PORT", "8000"))
DIRECTORIO = Path(__file__).resolve().parent
ARCHIVO_DOCUMENTOS = DIRECTORIO / "documentos.json"

CABECERAS_CORS = (
    ("Access-Control-Allow-Origin", "*"),
    ("Access-Control-Allow-Methods", "GET, OPTIONS"),
)


def cargar_documentos() -> bytes:
    """Devuelve el JSON de documentos tal como esta en disco."""
    return ARCHIVO_DOCUMENTOS.read_bytes()


class Manejador(BaseHTTPRequestHandler):
    server_version = "MockDocumentos/1.0"

    def _responder(self, codigo: int, cuerpo: bytes) -> None:
        self.send_response(codigo)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(cuerpo)))
        for nombre, valor in CABECERAS_CORS:
            self.send_header(nombre, valor)
        self.end_headers()
        self.wfile.write(cuerpo)

    def do_OPTIONS(self) -> None:  # noqa: N802 (nombre exigido por la libreria)
        self.send_response(204)
        self.send_header("Content-Length", "0")
        for nombre, valor in CABECERAS_CORS:
            self.send_header(nombre, valor)
        self.end_headers()

    def do_GET(self) -> None:  # noqa: N802 (nombre exigido por la libreria)
        ruta = self.path.split("?", 1)[0].rstrip("/") or "/"
        if ruta == "/documentos":
            self._responder(200, cargar_documentos())
        elif ruta == "/health":
            self._responder(200, json.dumps({"status": "ok"}).encode("utf-8"))
        else:
            cuerpo = json.dumps(
                {"error": "Ruta no encontrada", "ruta": self.path}
            ).encode("utf-8")
            self._responder(404, cuerpo)

    def log_message(self, formato: str, *argumentos: object) -> None:
        print(f"[mock] {self.address_string()} {formato % argumentos}", flush=True)


def main() -> None:
    servidor = ThreadingHTTPServer(("0.0.0.0", PUERTO), Manejador)
    print(f"API simulada de documentos escuchando en el puerto {PUERTO}")
    print(f"  Local:    http://localhost:{PUERTO}/documentos")
    print(f"  Emulador: http://10.0.2.2:{PUERTO}/documentos")
    print("Presiona Ctrl+C para detenerla.")
    try:
        servidor.serve_forever()
    except KeyboardInterrupt:
        print("\nDeteniendo la API simulada...")
    finally:
        servidor.server_close()


if __name__ == "__main__":
    main()
