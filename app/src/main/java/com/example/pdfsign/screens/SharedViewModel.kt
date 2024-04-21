package com.example.pdfsign.screens

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SharedViewModel: ViewModel() {
    val _image  = MutableStateFlow<ImageBitmap>(ImageBitmap())
}