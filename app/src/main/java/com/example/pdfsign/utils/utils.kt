package com.example.pdfsign.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

suspend fun copyExternalFile(context: Context, uri: Uri, finish: suspend (File) -> Unit) {
    val name = "pdfSign.pdf"
    val file = File(context.cacheDir, name)
    val inputStream = context.contentResolver.openInputStream(uri)
    runCatching {
        val out: OutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        inputStream?.let {
            var read = it.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = it.read(buffer)
            }
        }
    }.onSuccess {
        inputStream?.close()
        finish.invoke(file)
    }
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
    } catch (err: FileNotFoundException) {
        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
    }
    return bitmap
}


fun Offset.calculateNewOffset(
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    gestureZoom: Float,
    size: IntSize
): Offset {
    val newScale = maxOf(1f, zoom * gestureZoom)
    val newOffset = (this + centroid / zoom) -
            (centroid / newScale + pan / zoom)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}

fun calculateDoubleTapOffset(
    zoom: Float,
    size: IntSize,
    tapOffset: Offset
): Offset {
    val newOffset = Offset(tapOffset.x, tapOffset.y)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}