# API simulada de documentos

Servidor local que publica `GET /documentos` con datos ficticios para la
funcionalidad de Documentos (Retrofit + JSON).

```bash
python3 tools/mock_api/server.py
```

Endpoints: `http://localhost:8000/documentos` y `http://localhost:8000/health`.
Desde el emulador Android usar `http://10.0.2.2:8000/documentos`.
El puerto se puede cambiar con la variable de entorno `PORT`.
