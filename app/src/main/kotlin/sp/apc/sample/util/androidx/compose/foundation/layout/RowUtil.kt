package sp.apc.sample.util.androidx.compose.foundation.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sp.apc.sample.util.androidx.compose.foundation.text.Text

@Composable
fun RowButtons(buttons: Map<String, () -> Unit>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        buttons.forEach { (text, onClick) ->
            Text(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = Color(0xffffffff))
                    .clickable {
                        onClick()
                    },
                alignment = Alignment.Center,
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xff000000)
                )
            )
        }
    }
}
