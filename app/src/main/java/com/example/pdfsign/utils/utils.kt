package com.example.pdfsign.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
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

sealed interface Result {
    data class Success(val msg: String) : Result
    data object Failure : Result
}

const val PDF_WIDTH = 794
const val PDF_HEIGHT = 1123

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun generatePdf(
    context: Context,
    pdfRender: PdfRender,
    pdfAltHashMap: HashMap<Int, MutableState<ImageBitmap>>
): Result {
    val pdfDocument = PdfDocument()
    pdfRender.pageLists[0].loadForExport()
    val width = pdfRender.pageLists[0].pageContent.value?.width ?: PDF_WIDTH
    val height = pdfRender.pageLists[0].pageContent.value?.height ?: PDF_HEIGHT
    pdfRender.pageLists[0].recycle()
    for (index in 0 until pdfRender.pageCount) {
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
        val newPage = pdfDocument.startPage(pageInfo)
        if (pdfAltHashMap[index]?.value != null) {
            val bitmap = pdfAltHashMap[index]?.value?.asAndroidBitmap()?.copy(Bitmap.Config.ARGB_8888, false)
            bitmap?.let {
                newPage.canvas.drawBitmap(getResizedBitmap(it, width, height), 0f, 0f, null)
            }
        } else {
            pdfRender.pageLists[index].loadForExport()
            val bitmap =
                pdfRender.pageLists[index].pageContent.value?.let { Bitmap.createBitmap(it) }
            bitmap?.let { newPage.canvas.drawBitmap(it, 0f, 0f, null) }
            pdfRender.pageLists[index].recycle()
        }
        pdfDocument.finishPage(newPage)
    }
    val values = ContentValues()
    val fileName = "pdfSign"
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)

    val uri = context.contentResolver.insert(
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        values
    )
    lateinit var result: Result
    uri?.let {
        try {
            val outputStream = context.contentResolver.openOutputStream(it)
            pdfDocument.writeTo(outputStream)
            outputStream?.close()
            result = Result.Success("File is exported to location: Documents")
        } catch (e: IOException) {
            e.printStackTrace()
            result = Result.Failure
        }
    }
    pdfRender.close()
    pdfDocument.close()
    return result
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

fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
    val width = bm.width
    val height = bm.height

    // Calculate the scale factor for width and height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height

    // Create a new matrix for the manipulation
    val matrix = Matrix()

    // Resize the bitmap
    matrix.postScale(scaleWidth, scaleHeight)

    // Recreate the new bitmap using sampling to maintain quality
    val resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(resizedBitmap)
    val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(bm, matrix, paint)

    return resizedBitmap
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