package sp.apc.sample.util.androidx.compose.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import sp.apc.sample.util.androidx.compose.foundation.text.Text

@Composable
fun DialogTextField(
    onDismissRequest: () -> Unit,
    lKey: String,
    lValue: String,
    button: String,
    onConfirm: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .width(128.dp)
                .background(color = Color(0xff222222))
        ) {
            BasicText(
                text = lKey,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xff000000)
                )
            )
            val key = remember { mutableStateOf("") }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1,
                value = key.value,
                onValueChange = {
                    key.value = it
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xffffffff)
                )
            )
            BasicText(
                text = lValue,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xff000000)
                )
            )
            val value = remember { mutableStateOf("") }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1,
                value = value.value,
                onValueChange = {
                    value.value = it
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xffffffff)
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable {
                            onConfirm(key.value, value.value)
                        }
                        .padding(start = 8.dp, end = 8.dp),
                    alignment = Alignment.Center,
                    text = button,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xff00ff00)
                    )
                )
            }
        }
    }
}

@Composable
fun DialogTextField(
    onDismissRequest: () -> Unit,
    label: String,
    onConfirm: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .width(128.dp)
                .background(color = Color(0xff222222))
        ) {
            BasicText(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xff000000)
                )
            )
            val text = remember { mutableStateOf("") }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1,
                value = text.value,
                onValueChange = {
                    text.value = it
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xffffffff)
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable {
                            onConfirm(text.value)
                        }
                        .padding(start = 8.dp, end = 8.dp),
                    alignment = Alignment.Center,
                    text = "ok",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xff00ff00)
                    )
                )
            }
        }
    }
}
