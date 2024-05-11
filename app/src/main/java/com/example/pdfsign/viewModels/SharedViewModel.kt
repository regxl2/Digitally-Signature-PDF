package com.example.pdfsign.viewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfsign.utils.PdfRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PathInfo(val path: Path, val width: Float, val height: Float, var offset: Offset = Offset.Zero, var scale: Float = 1f)
data class ImageSignaturesInfo(
    val signatures: SnapshotStateList<PathInfo> = mutableStateListOf<PathInfo>(),
    val isSignVisible: SnapshotStateList<Boolean> = mutableStateListOf<Boolean>()
)

data class ImageInfo(val imageBitmap: ImageBitmap, val index: Int)

class SharedViewModel : ViewModel() {
    var imageState = mutableStateOf<ImageInfo?>(null)
        private set

    var pdfRender = mutableStateOf<PdfRender?>(null)
        private set

    var hashMap = hashMapOf<Int, ImageSignaturesInfo>()
        private set

    var pdfRenderAlt = hashMapOf<Int, MutableState<ImageBitmap>>()
        private set

    fun setImageState(imageInfo: ImageInfo) {
        imageState.value = imageInfo
        if(hashMap[imageInfo.index]==null){
            hashMap[imageInfo.index] = ImageSignaturesInfo()
        }
    }

    fun setPdfRender(pdfRender: PdfRender) {
        this.pdfRender.value = pdfRender
    }

    fun addPath(pageIndex: Int, pathInfo: PathInfo) {
        hashMap[pageIndex]?.signatures?.add(pathInfo)
    }

    fun addPdfRenderImage(index: Int, bitmap: ImageBitmap){
        hashMap[index]?.let { imageSignaturesInfo ->
            if(imageSignaturesInfo.signatures.size > 0){
                pdfRenderAlt[index] = mutableStateOf(bitmap)
                println("image saved")
            }
        }
    }

    fun removeSignatureAtIndex(pageIndex: Int, signIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            hashMap[pageIndex]?.signatures?.removeAt(signIndex)
            if(hashMap[pageIndex]?.signatures?.size==0){
                pdfRenderAlt.remove(pageIndex)
                println("removed image")
            }
        }
    }

    fun addIsVisible(pageIndex: Int, boolean: Boolean) {
        hashMap[pageIndex]?.isSignVisible?.add(boolean)
    }

    fun resetIsSignVisibleList(pageIndex: Int) {
        viewModelScope.launch(Dispatchers.Default){
            hashMap[pageIndex]?.isSignVisible?.let {list->
                for(i in 0 until list.size){
                    list[i] = false
                }
            }
        }
    }

    fun resetHashMaps(){
        viewModelScope.launch(Dispatchers.Default){
            hashMap.clear()
            pdfRenderAlt.clear()
        }
    }
    fun resetPdfRender() {
        viewModelScope.launch {
            pdfRender.value?.close()
            pdfRender.value = null
            println("clearing")
        }
    }
}