package sp.apc.sample.module

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sp.apc.sample.util.androidx.compose.foundation.text.Text
import sp.apc.sample.util.androidx.compose.ui.window.DialogTextField

object Init {
    @Composable
    fun Screen(onEncrypt: (String) -> Unit) {
        val isRequested = remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize()) {
            if (isRequested.value) {
                DialogTextField(
                    onDismissRequest = {
                        isRequested.value = false
                    },
                    label = "password",
                    onConfirm = { password ->
                        isRequested.value = false
                        onEncrypt(password)
                    }
                )
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(color = Color(0xffffffff))
                        .clickable {
                            isRequested.value = true
                        },
                    alignment = Alignment.Center,
                    text = "init",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xff000000)
                    )
                )
            }
        }
    }
}
