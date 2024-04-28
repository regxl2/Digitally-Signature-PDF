package com.example.pdfsign

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdfsign.screens.PDFPageEdit
import com.example.pdfsign.screens.PdfPicker
import com.example.pdfsign.viewModels.SharedViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

enum class Screen {
    PdfPicker,
    PdfPageEdit,
    DrawSign
}

@Parcelize
data class Image(val bitmap: @RawValue ImageBitmap) : Parcelable

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootScreen() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()
    NavHost(navController = navController, startDestination = Screen.PdfPicker.name) {
        composable(route = Screen.PdfPicker.name, enterTransition = { slideInAnimation() }) {
            PdfPicker(viewModel = sharedViewModel) { image ->
                sharedViewModel.setImageState(image)
                navController.navigate(route = Screen.PdfPageEdit.name)
            }
        }
        composable(route = Screen.PdfPageEdit.name, enterTransition = { slideInAnimation() }) {
            PDFPageEdit(viewModel = sharedViewModel){
                navController.popBackStack()
            }
        }
    }
}

fun slideInAnimation(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(durationMillis = 250, easing = EaseOut),
        initialOffsetX = { it + it / 2 }
    )
}