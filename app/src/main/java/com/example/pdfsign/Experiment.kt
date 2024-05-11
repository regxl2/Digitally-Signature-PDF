package com.example.pdfsign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
data class StringInfo(var name: MutableState<String> = mutableStateOf(""), var offset: MutableState<Offset> = mutableStateOf(Offset.Zero))
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Experiment() {
    val textFieldList = remember {
        mutableStateListOf<StringInfo>()
    }
    var enabled by remember {
        mutableStateOf(false)
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Experiment", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            actions = {
                IconButton(onClick = { textFieldList.add(StringInfo()) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "TextView add")
                }
            })
    }){paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .clickable(onClick = { enabled = !enabled }), contentAlignment = Alignment.Center){
            textFieldList.forEachIndexed{index, stringInfo ->
                TextField(value = stringInfo.name.value, onValueChange = {
                    textFieldList[index].name.value = it
                },
                    colors = TextFieldDefaults.textFieldColors(),
                    enabled = enabled,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                stringInfo.offset.value = Offset(x = dragAmount.x, y = dragAmount.y)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures {
                                enabled = !enabled
                            }
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExperiment() {
    Experiment()
}