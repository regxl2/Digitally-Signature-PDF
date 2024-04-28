package com.example.pdfsign.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfsign.utils.PdfRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Signature

data class PathWithSize(val path: Path, val width: Float, val height: Float)
class SharedViewModel: ViewModel() {
    var imageState= mutableStateOf<ImageBitmap?>(null)
        private set

    var pdfRender = mutableStateOf<PdfRender?>(null)
        private set

    var signatures = mutableStateListOf<PathWithSize>()
        private set
    fun setImageState(imageBitmap: ImageBitmap){
        imageState.value = imageBitmap
    }
    fun setPdfRender(pdfRender: PdfRender){
        this.pdfRender.value = pdfRender
    }
    fun addPath(pathWithSize: PathWithSize){
        signatures.add(pathWithSize)
    }

    fun removeSignatureAtIndex(index: Int){
        viewModelScope.launch(Dispatchers.Default){
            signatures.removeAt(index)
        }
    }

    fun resetPdfRender(){
        pdfRender.value = null
    }
}