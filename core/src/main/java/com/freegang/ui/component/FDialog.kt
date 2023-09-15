package com.freegang.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.freegang.ui.asDp

@Composable
fun FMessageDialog(
    title: String,
    cancel: String = "cancel",
    confirm: String = "confirm",
    onlyConfirm: Boolean = false,
    isWaiting: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FMessageDialog(
        title = {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.body1,
            )
        },
        cancel = cancel,
        confirm = confirm,
        onlyConfirm = onlyConfirm,
        isWaiting = isWaiting,
        onCancel = onCancel,
        onConfirm = onConfirm,
    ) {
        content()
    }
}

@Composable
fun FMessageDialog(
    title: @Composable () -> Unit,
    cancel: String = "cancel",
    confirm: String = "confirm",
    onlyConfirm: Boolean = false,
    isWaiting: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cornerRadius = 12.dp
    FMessageDialog(
        title = {
            title()
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    if (!onlyConfirm) {
                        CardButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(bottomStart = cornerRadius),
                            onClick = { onCancel?.invoke() },
                            content = {
                                Text(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    text = cancel,
                                    style = MaterialTheme.typography.body1,
                                )
                            },
                        )
                    }

                    if (isWaiting) {
                        BoxWithConstraints(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                            content = {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(MaterialTheme.typography.body1.fontSize.asDp),
                                )
                            }
                        )
                    } else {
                        CardButton(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(
                                bottomStart = if (onlyConfirm) cornerRadius else 0.dp,
                                bottomEnd = cornerRadius,
                            ),
                            onClick = { onConfirm?.invoke() },
                            content = {
                                Text(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    text = confirm,
                                    style = MaterialTheme.typography.body1.copy(
                                        color = MaterialTheme.colors.primary
                                    ),
                                )
                            }
                        )
                    }
                }
            )
        }
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                .heightIn(max = 320.dp),
        ) {
            content()
        }
    }
}

@Composable
fun FMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    FDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        cornerRadius = cornerRadius,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
            content = {
                title()
                content()
                actions()
            }
        )
    }
}


@Composable
fun FDialog(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    cornerRadius: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
        ) {
            Card(
                modifier = modifier,
                shape = RoundedCornerShape(cornerRadius),
                content = content,
            )
        }
    }
}