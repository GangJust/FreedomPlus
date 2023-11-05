package com.freegang.ui.component

import android.os.CountDownTimer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.freegang.ui.asDp

///
@Composable
fun FMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: String,
    cancel: String = "cancel",
    confirm: String = "confirm",
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FMessageDialog(
        cornerRadius = cornerRadius,
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
        onCancel = onCancel,
        onConfirm = onConfirm,
    ) {
        content()
    }
}

@Composable
fun FMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: @Composable () -> Unit,
    cancel: String = "cancel",
    confirm: String = "confirm",
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FContentDialog(
        cornerRadius = cornerRadius,
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


///
@Composable
fun FWaitingMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: String,
    cancel: String = "cancel",
    confirm: String = "confirm",
    isWaiting: Boolean = false,
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FWaitingMessageDialog(
        cornerRadius = cornerRadius,
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
fun FWaitingMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: @Composable () -> Unit,
    cancel: String = "cancel",
    confirm: String = "confirm",
    isWaiting: Boolean = false,
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FContentDialog(
        cornerRadius = cornerRadius,
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

///
@Composable
fun FCountDownMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: String,
    cancel: String = "cancel",
    confirm: String = "confirm",
    seconds: Int = 5,
    waitingText: String = "waiting (%d)",
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    FCountDownMessageDialog(
        cornerRadius = cornerRadius,
        title = {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.body1,
            )
        },
        seconds = seconds,
        cancel = cancel,
        confirm = confirm,
        waitingText = waitingText,
        onlyConfirm = onlyConfirm,
        onCancel = onCancel,
        onConfirm = onConfirm,
        content = content,
    )
}

@Composable
fun FCountDownMessageDialog(
    cornerRadius: Dp = 12.dp,
    title: @Composable () -> Unit,
    cancel: String = "cancel",
    confirm: String = "confirm",
    seconds: Int = 5,
    waitingText: String = "waiting (%d)",
    onlyConfirm: Boolean = false,
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val confirmText = remember { mutableStateOf("$seconds") }
    object : CountDownTimer((seconds * 1000).toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val countDown = millisUntilFinished / 1000 + 1
            confirmText.value = String.format(waitingText, countDown)
        }

        override fun onFinish() {
            confirmText.value = confirm
        }
    }.start()

    FContentDialog(
        cornerRadius = cornerRadius,
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

                    CardButton(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(
                            bottomStart = if (onlyConfirm) cornerRadius else 0.dp,
                            bottomEnd = cornerRadius,
                        ),
                        onClick = {
                            if (confirmText.value == confirm) {
                                onConfirm?.invoke()
                            }
                        },
                        content = {
                            Text(
                                modifier = Modifier.padding(vertical = 12.dp),
                                text = confirmText.value,
                                style = MaterialTheme.typography.body1.copy(
                                    color = MaterialTheme.colors.primary
                                ),
                            )
                        }
                    )
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
fun FContentDialog(
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
    cornerRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
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