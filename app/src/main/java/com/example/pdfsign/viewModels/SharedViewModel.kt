package com.example.pdfsign.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfsign.utils.PdfRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PathInfo(val path: Path, val width: Float, val height: Float)
class SharedViewModel: ViewModel() {
    var imageIndex= mutableStateOf<ImageBitmap?>(null)
        private set

    var pdfRender = mutableStateOf<PdfRender?>(null)
        private set

    var signatures = mutableStateListOf<PathInfo>()
        private set

    var isSignVisibleList = mutableStateListOf<Boolean>()
        private set

    fun setImageState(imageBitmap: ImageBitmap){
        imageIndex.value = imageBitmap
    }
    fun setPdfRender(pdfRender: PdfRender){
        this.pdfRender.value = pdfRender
    }
    fun addPath(pathInfo: PathInfo){
        signatures.add(pathInfo)
    }

    fun removeSignatureAtIndex(index: Int){
        viewModelScope.launch(Dispatchers.Default){
            signatures.removeAt(index)
        }
    }

    fun addIsVisible(boolean: Boolean){
        isSignVisibleList.add(boolean)
    }
    fun resetIsSignVisibleList(){
        viewModelScope.launch {
            for(i in 0 until isSignVisibleList.size){
                isSignVisibleList[i] = false
            }
        }
    }
    fun resetImage(index: Int){
        pdfRender.value?.pageLists?.get(index)?.recycle()
        println("Recycling image")
    }
    fun resetPdfRender(){
        viewModelScope.launch(Dispatchers.Default){
            pdfRender.value?.close()
            pdfRender.value = null
            println("Resource closed")
        }
    }
}