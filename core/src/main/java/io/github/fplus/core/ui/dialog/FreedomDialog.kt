package io.github.fplus.core.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fplus.core.ui.ModuleTheme
import io.github.fplus.core.ui.component.CardButton

@Composable
fun FreedomDialog(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    showIcon: Boolean = true,
    onIconClick: () -> Unit,
    items: List<CharSequence>,
    onChoice: (item: CharSequence) -> Unit
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(cornerRadius),
    ) {
        ModuleTheme(
            isDark = true,
        ) {
            Column {
                TopBar(
                    showIcon = showIcon,
                    onIconClick = onIconClick
                )
                LazyColumn(
                    horizontalAlignment = Alignment.Start
                ) {
                    items(items) {
                        ItemButton(
                            text = "$it",
                            onClick = onChoice,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    showIcon: Boolean = true,
    onIconClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
    ) {
        Text(
            text = "Freedom+",
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp)
        )
        if (showIcon) {
            IconButton(
                onClick = {
                    onIconClick.invoke()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Freedom+ Setting",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.typography.body1.color,
                )
            }
        }
    }
}

@Composable
private fun ItemButton(
    modifier: Modifier = Modifier,
    text: String,
    shape: Shape = RoundedCornerShape(0.dp),
    onClick: (String) -> Unit,
) {
    CardButton(
        shape = shape,
        modifier = modifier,
        onClick = {
            onClick.invoke(text)
        },
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            text = text,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.body1.copy(
                fontSize = 16.sp
            )
        )
    }
}