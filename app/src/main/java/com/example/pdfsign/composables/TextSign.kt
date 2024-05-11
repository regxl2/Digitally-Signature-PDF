package com.example.pdfsign.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun TextSign(
    modifier: Modifier = Modifier,
    onClickDone: (String) -> Unit,
    onClickCancel: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }
    Column(modifier = modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            SignButton(onClick = onClickCancel, text = "Cancel")
            Text(
                modifier = Modifier.weight(1f),
                text = "Enter Initial",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary
            )
            SignButton(onClick = {
                if(name.isNotBlank()){
                    onClickDone(name)
                }
            }, text = "Done")
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            placeholder = {
                Text(text = "Enter your initial here")
            },
            shape = RectangleShape
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        ) {
            SignButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    name = ""
                },
                text = "Clear"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDrawSign() {
    TextSign(onClickDone = {}, onClickCancel = {})
}