package com.example.pdfsign.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import java.io.FileNotFoundException

@Composable
fun ImagePicker() {
    var image: ImageBitmap? by remember {
        mutableStateOf(null)
    }
    val context = LocalContext.current
    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia())
        { uri ->
            image = uri?.let { getBitmapFromUri(context, it)?.asImageBitmap() }
        }
    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = "image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center)
            )
        } else {
            Button(modifier = Modifier.align(Alignment.Center),
                onClick = {
                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                Text(text = "Click to select Image")
            }
        }
    }
}

private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
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

@Preview(showBackground = true)
@Composable
private fun PreviewImagePicker() {
    ImagePicker()
}