package com.example.pdfsign

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdfsign.screens.PDFPageEdit
import com.example.pdfsign.screens.PdfPicker
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

enum class Screen{
    PdfPicker,
    PdfPageEdit
}

@Parcelize
data class Image(val bitmap: @RawValue ImageBitmap) : Parcelable

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.PdfPicker.name) {
        composable(route = Screen.PdfPicker.name){
            PdfPicker{ image ->
                navController.currentBackStackEntry?.savedStateHandle?.set(key= "image", value = image)
                navController.navigate(route = Screen.PdfPageEdit.name)
            }
        }
        composable(route = Screen.PdfPageEdit.name){
            val image = navController.previousBackStackEntry?.savedStateHandle?.get<ImageBitmap>("image")
            PDFPageEdit(image = image!!){
                navController.navigate(Screen.PdfPicker.name){
                    popUpTo(Screen.PdfPageEdit.name){
                        inclusive = true
                    }
                }
            }
        }
    }
}