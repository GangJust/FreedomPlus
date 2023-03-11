package com.freegang.base

import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.freegang.view.KDialog
import com.freegang.view.adapter.DialogChoiceAdapter
import com.freegang.xpler.R
import com.freegang.xpler.databinding.DialogChoiceLayoutBinding
import com.freegang.xpler.databinding.DialogMessageLayoutBinding
import com.freegang.xpler.utils.app.KNotifiUtils
import com.freegang.xpler.utils.log.KLogCat
import com.freegang.xpler.utils.other.KResourceUtils
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.*

abstract class BaseHook(
    protected val lpparam: XC_LoadPackage.LoadPackageParam,
) {
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private var toast: Toast? = null

    init {
        this.onHook()
    }

    abstract fun onHook()

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            if (throwable is CancellationException) {
                throwable.printStackTrace()
                return@CoroutineExceptionHandler
            }

            KLogCat.d("发生异常: \n${throwable.stackTraceToString()}")
            if (coroutineContext.isActive) {
                coroutineContext.cancel()
                coroutineContext.cancelChildren()
            }
        }
        val job = mainScope.launch(exceptionHandler) {
            try {
                block.invoke(this)
            } catch (e: CancellationException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
                KLogCat.d("发生异常: \n${e.stackTraceToString()}")
            }
        }
        return job
    }

    fun showToast(context: Context, message: String) {
        mainScope.launch {
            toast = if (toast == null) {
                Toast.makeText(context.applicationContext, null, Toast.LENGTH_LONG)
            } else {
                toast?.cancel()
                Toast.makeText(context.applicationContext, null, Toast.LENGTH_LONG)
            }
            toast?.setText(message)
            toast?.show()
        }
    }

    fun vibrate(context: Context, milliseconds: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(milliseconds)
    }

    fun showMessageDialog(
        activity: Activity,
        title: CharSequence,
        content: CharSequence,
        cancel: CharSequence = "取消",
        confirm: CharSequence = "确定",
        singleButton: Boolean = false, //只会响应 onConfirm 方法
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {},
    ) {
        val kDialog = KDialog(activity)

        val dialogView = KResourceUtils.inflateView<FrameLayout>(activity, R.layout.dialog_message_layout)
        val binding = DialogMessageLayoutBinding.bind(dialogView)

        binding.messageDialogContainer.background = KResourceUtils.getDrawable(R.drawable.dialog_background)
        if (singleButton) {
            binding.messageDialogCancel.visibility = View.GONE
            binding.messageDialogConfirm.background = KResourceUtils.getDrawable(R.drawable.dialog_single_button_background)
        } else {
            binding.messageDialogCancel.background = KResourceUtils.getDrawable(R.drawable.dialog_cancel_button_background)
            binding.messageDialogConfirm.background = KResourceUtils.getDrawable(R.drawable.dialog_confirm_button_background)
        }
        binding.messageDialogTitle.text = title
        binding.messageDialogContent.text = content
        binding.messageDialogCancel.text = cancel
        binding.messageDialogConfirm.text = confirm
        binding.messageDialogCancel.setOnClickListener {
            onCancel.invoke()
            kDialog.dismiss()
        }
        binding.messageDialogConfirm.setOnClickListener {
            onConfirm.invoke()
            kDialog.dismiss()
        }

        kDialog.setView(binding.root)
        kDialog.show()
    }

    fun showChoiceDialog(
        activity: Activity,
        title: CharSequence,
        items: Array<String>,
        cancel: CharSequence = "取消",
        onChoice: (view: View, item: CharSequence, position: Int) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val kDialog = KDialog(activity)
        val dialogView = KResourceUtils.inflateView<FrameLayout>(activity, R.layout.dialog_choice_layout)
        val binding = DialogChoiceLayoutBinding.bind(dialogView)

        binding.choiceDialogContainer.background = KResourceUtils.getDrawable(R.drawable.dialog_background)
        binding.choiceDialogCancel.background = KResourceUtils.getDrawable(R.drawable.dialog_single_button_background)

        binding.choiceDialogTitle.text = title
        binding.choiceDialogCancel.text = cancel
        binding.choiceDialogCancel.setOnClickListener {
            onCancel.invoke()
            kDialog.dismiss()
        }

        binding.choiceDialogList.adapter = DialogChoiceAdapter(activity, items)
        binding.choiceDialogList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.choiceDialogList.selector = KResourceUtils.getDrawable(R.drawable.item_selector_background)
        binding.choiceDialogList.setOnItemClickListener { _, view, position, _ ->
            onChoice.invoke(view, items[position], position)
            kDialog.dismiss()
        }

        kDialog.setView(binding.root)
        kDialog.show()
    }

    fun showNotification(
        context: Context,
        notifyId: Int,
        title: String,
        text: String,
    ) {
        val channelId = "Freedom+"
        val channelName = "Freedom+ Message"
        KNotifiUtils.showNotification(
            context = context,
            notifyId = notifyId,
            channelId = channelId,
            channelName = channelName,
            title = title,
            text = text,
        )
    }

    fun showDownloadNotification(
        context: Context,
        notifyId: Int,
        title: String,
        inProgressText: String = "%d%%",
        finishedText: String = "下载完成!",
        listener: (notify: KNotifiUtils.ProgressNotification) -> Unit,
    ) {
        val channelId = "Freedom+"
        val channelName = "Freedom+ Message"
        KNotifiUtils.showProgressNotification(
            context = context,
            notifyId = notifyId,
            channelId = channelId,
            channelName = channelName,
            title = title,
            inProgressText = inProgressText,
            finishedText = finishedText,
            listener = listener,
        )
    }
}