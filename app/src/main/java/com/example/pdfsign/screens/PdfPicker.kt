package com.example.pdfsign.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID


@Composable
fun PdfPicker() {
    var file: File? by remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pdfPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            scope.launch {
                uri?.let {
                    copyExternalFile(context, uri) {
                        file = it
                    }
                }
            }
        }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        file?.let {
            PDFReader(file = it)
        } ?: Button(onClick = {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        }) {
            Text("Select the PDF")
        }
    }
}

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


@Preview(showSystemUi = true)
@Composable
private fun PreviewPdfPicker() {
    PdfPicker()
}