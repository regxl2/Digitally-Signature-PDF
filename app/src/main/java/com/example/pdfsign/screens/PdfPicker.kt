package com.example.pdfsign.screens

import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pdfsign.R
import com.example.pdfsign.composables.PDFReader
import com.example.pdfsign.utils.PdfRender
import com.example.pdfsign.utils.Result
import com.example.pdfsign.utils.copyExternalFile
import com.example.pdfsign.utils.generatePdf
import com.example.pdfsign.viewModels.ImageInfo
import com.example.pdfsign.viewModels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPicker(viewModel: SharedViewModel, navigateToPdfPageEdit: (ImageInfo) -> Unit) {
    val pdfRender = viewModel.pdfRender.value
    var isProgressVisible by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pdfPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                isProgressVisible = true
                scope.launch(Dispatchers.Default) {
                    copyExternalFile(context, uri) { file ->
                        viewModel.setPdfRender(
                            PdfRender(
                                fileDescriptor = ParcelFileDescriptor.open(
                                    file,
                                    ParcelFileDescriptor.MODE_READ_ONLY
                                ),
                                context = context
                            )
                        )
                        viewModel.setExportPdfRender(
                            PdfRender(
                                fileDescriptor = ParcelFileDescriptor.open(
                                    file,
                                    ParcelFileDescriptor.MODE_READ_ONLY
                                ),
                                context = context
                            )
                        )
                    }
                    isProgressVisible = false
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "PDF Sign"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (pdfRender != null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(modifier = Modifier
                            .padding(8.dp), onClick = {
                            scope.launch {
                                viewModel.resetHashMaps()
                                viewModel.resetPdfRender()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "close pdf button"
                            )
                        }
                        IconButton(modifier = Modifier.padding(8.dp),
                            onClick = {
                                val deferredResult = scope.async(Dispatchers.IO){
                                    viewModel.exportPdfRender?.let { generatePdf(context = context, pdfRender = it, pdfAltHashMap = viewModel.pdfRenderAltHashMap) }
                                }
                                scope.launch {
                                    isProgressVisible = true
                                    val result = deferredResult.await()
                                    isProgressVisible = false
                                    if(result is Result.Success){
                                        Toast.makeText(context, result.msg, Toast.LENGTH_LONG).show()
                                    }
                                    else{
                                        Toast.makeText(context,"PDF file export failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.export),
                                contentDescription = "export pdf file"
                            )
                        }
                    }
                    if(isProgressVisible){
                        ProgressIndicator()
                    }
                }
                PDFReader(
                    pdfImageAlt = viewModel.pdfRenderAltHashMap,
                    pdfRender = pdfRender,
                    navigateToPdfPageEdit = navigateToPdfPageEdit
                )
            }
        } else {
            ButtonProgressScreen(
                modifier = Modifier.padding(paddingValues),
                isProgressVisible = isProgressVisible,
                pdfPickerLauncher = pdfPickerLauncher
            )
        }
    }
}

@Composable
fun ButtonProgressScreen(
    modifier: Modifier = Modifier,
    isProgressVisible: Boolean,
    pdfPickerLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>
) {
    Box(modifier = modifier.fillMaxSize()) {
        Button(
            modifier = Modifier.align(Alignment.Center), onClick = {
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
            }) {
            Text("Select the PDF")
        }
        if (isProgressVisible) {
            ProgressIndicator()
        }
    }
}

@Composable
fun ProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.5f))
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showSystemUi = true)
@Composable
private fun PreviewPdfPicker() {
    PdfPicker(viewModel = SharedViewModel(), navigateToPdfPageEdit = {})
}