package sp.apc.sample.util.androidx.compose.foundation.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun Text(
    modifier: Modifier,
    alignment: Alignment,
    text: String,
    style: TextStyle
) {
    Box(modifier = modifier) {
        BasicText(
            modifier = Modifier.align(alignment),
            text = text,
            style = style
        )
    }
}
