package sp.apc.sample.module

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sp.apc.sample.util.androidx.compose.foundation.layout.RowButtons
import sp.apc.sample.util.androidx.compose.ui.window.DialogTextField

object Encrypted {
    @Composable
    private fun Item(value: String, onDelete: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            BasicText(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = value,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xffffffff)
                )
            )
            BasicText(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(color = Color(0xff000000))
                    .clickable {
                        onDelete()
                    },
                text = "x",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xffff0000)
                )
            )
        }
    }

    @Composable
    fun Screen(
        list: List<String>,
        onDelete: () -> Unit,
        onLock: () -> Unit,
        onAdd: (String) -> Unit,
        onDeleteItem: (Int) -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(256.dp)
                    .background(color = Color(0xff222222))
            ) {
                for (i in list.indices) {
                    Item(value = list[i], onDelete = { onDeleteItem(i) })
                }
                val isAdd = remember { mutableStateOf(false) }
                if (isAdd.value) {
                    DialogTextField(
                        label = "add",
                        onDismissRequest = {
                            isAdd.value = false
                        },
                        onConfirm = { item ->
                            onAdd(item)
                            isAdd.value = false
                        }
                    )
                }
                RowButtons(
                    buttons = mapOf(
                        "add" to {
                            isAdd.value = true
                        },
                        "delete" to {
                            onDelete()
                        },
                        "lock" to {
                            onLock()
                        }
                    )
                )
            }
        }
    }
}
