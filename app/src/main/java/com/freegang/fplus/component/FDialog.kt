package com.freegang.fplus.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.freegang.fplus.Themes
import kit.CardButton

@Composable
fun FDialog(
    modifier: Modifier = Modifier,
    title: String,
    cancel: String = "cancel",
    confirm: String = "confirm",
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val radius = 12.dp
    Dialog(
        onDismissRequest = { },
        content = {
            Card(
                shape = RoundedCornerShape(12.dp),
                content = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceBetween,
                        content = {
                            Text(
                                text = title,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                style = Themes.nowTypography.body1,
                            )
                            BoxWithConstraints(
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp).heightIn(max = 320.dp),
                                contentAlignment = Alignment.Center,
                                content = {
                                    content()
                                }
                            )
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom,
                                content = {
                                    if (!onlyConfirm) {
                                        CardButton(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp),
                                            shape = RoundedCornerShape(bottomStart = radius),
                                            onClick = { onCancel?.invoke() },
                                            content = {
                                                Text(
                                                    text = cancel,
                                                    style = Themes.nowTypography.body1,
                                                )
                                            },
                                        )
                                    }
                                    CardButton(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(
                                            bottomStart = if (onlyConfirm) radius else 0.dp,
                                            bottomEnd = radius,
                                        ),
                                        onClick = { onConfirm?.invoke() },
                                        content = {
                                            Text(
                                                text = confirm,
                                                style = Themes.nowTypography.body1.copy(
                                                    Themes.nowColors.colors.secondary,
                                                ),
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                },
            )
        },
    )
}