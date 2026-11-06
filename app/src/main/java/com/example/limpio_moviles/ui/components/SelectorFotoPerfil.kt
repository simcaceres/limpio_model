package com.example.limpio_moviles.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.limpio_moviles.util.FotoStorage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SelectorFotoPerfil(
    bitmap: Bitmap?,
    textoBoton: String,
    onFotoCapturada: (Bitmap) -> Unit,
    onCamaraNoDisponible: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var capturaUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturaArchivo by rememberSaveable { mutableStateOf<String?>(null) }
    var capturando by rememberSaveable { mutableStateOf(false) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = TomarFotoConUri()
    ) { exito ->
        val uriTexto = capturaUri
        val archivoTexto = capturaArchivo
        capturaUri = null
        capturaArchivo = null
        capturando = false

        if (uriTexto == null || archivoTexto == null) {
            onCamaraNoDisponible()
            return@rememberLauncherForActivityResult
        }

        val archivo = File(archivoTexto)
        if (!exito) {
            archivo.delete()
            onCamaraNoDisponible()
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val foto = withContext(Dispatchers.IO) {
                try {
                    runCatching {
                        FotoStorage.cargarDesdeUri(context, Uri.parse(uriTexto))
                    }.getOrNull()
                } finally {
                    archivo.delete()
                }
            }
            if (foto != null) {
                onFotoCapturada(foto)
            } else {
                onCamaraNoDisponible()
            }
        }
    }

    val abrirCamara: () -> Unit = {
        if (!capturando) {
            runCatching {
                val archivo = FotoStorage.crearArchivoTemporal(context)
                capturaArchivo = archivo.absolutePath
                val uri = FotoStorage.uriParaArchivo(context, archivo)
                capturaUri = uri.toString()
                capturando = true
                camaraLauncher.launch(uri)
            }.onFailure {
                capturando = false
                capturaArchivo?.let { ruta -> File(ruta).delete() }
                capturaUri = null
                capturaArchivo = null
                onCamaraNoDisponible()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        OutlinedButton(onClick = abrirCamara, enabled = !capturando) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(textoBoton)
        }
    }
}

private class TomarFotoConUri : ActivityResultContract<Uri, Boolean>() {

    override fun createIntent(context: Context, input: Uri): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            clipData = ClipData.newRawUri("foto_perfil", input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}
