package com.example.pdfsign.screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.example.pdfsign.utils.PdfRender

class SharedViewModel: ViewModel() {
    var imageState= mutableStateOf<ImageBitmap?>(null)
        private set

    var pdfRender = mutableStateOf<PdfRender?>(null)
        private set
    fun setImageState(imageBitmap: ImageBitmap){
        imageState.value = imageBitmap
    }
    fun setPdfRender(pdfRender: PdfRender){
        this.pdfRender.value = pdfRender
    }
    fun resetPdfRender(){
        pdfRender.value = null
    }
}