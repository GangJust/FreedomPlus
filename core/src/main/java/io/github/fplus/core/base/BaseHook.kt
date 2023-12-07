package io.github.fplus.core.base

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.core.view.isVisible
import com.freegang.ktutils.app.IProgressNotification
import com.freegang.ktutils.app.KNotifiUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.isDarkMode
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.R
import io.github.fplus.core.databinding.DialogChoiceLayoutBinding
import io.github.fplus.core.databinding.DialogInputChoiceLayoutBinding
import io.github.fplus.core.databinding.DialogMessageLayoutBinding
import io.github.fplus.core.databinding.DialogProgressLayoutBinding
import io.github.fplus.core.ui.dialog.XplerDialogWrapper
import io.github.fplus.core.view.KDialog
import io.github.fplus.core.view.adapter.DialogChoiceAdapter
import io.github.xpler.core.KtOnHook
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.inflateModuleView
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.xposedLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseHook<T>(lpparam: XC_LoadPackage.LoadPackageParam) :
    KtOnHook<T>(lpparam) {
    protected val handler: Handler = Handler(Looper.getMainLooper())
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private var toast: Toast? = null
    private var kDialog: KDialog? = null

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            if (throwable is CancellationException) {
                throwable.printStackTrace()
                return@CoroutineExceptionHandler
            }

            XplerLog.xposedLog("错误堆栈: \n${throwable.stackTraceToString()}")
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
                XplerLog.xposedLog("错误堆栈: \n${e.stackTraceToString()}")
            }
        }
        return job
    }

    fun refresh(block: () -> Unit) {
        handler.post(block)
    }

    fun showToast(context: Context, message: String) {
        refresh {
            KToastUtils.show(context, message)
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

    @Synchronized
    fun showDialog(view: View) {
        kDialog = if (kDialog == null) KDialog() else kDialog
        kDialog!!.setView(view)
        kDialog!!.show()
    }

    @Synchronized
    fun showComposeDialog(
        context: Context,
        content: @Composable (closeHandler: () -> Unit) -> Unit
    ) {
        XplerDialogWrapper(context).apply {
            setWrapperContent {
                content.invoke {
                    this.dismiss()
                }
            }
        }.show()
    }

    fun showMessageDialog(
        context: Context,
        title: CharSequence,
        content: CharSequence,
        cancel: CharSequence = "取消",
        confirm: CharSequence = "确定",
        singleButton: Boolean = false, // 只会响应 onConfirm 方法
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {},
    ) {
        val isDarkMode = context.isDarkMode
        val dialogView = context.inflateModuleView<FrameLayout>(R.layout.dialog_message_layout)
        val binding = DialogMessageLayoutBinding.bind(dialogView)

        binding.messageDialogContainer.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_background_night,
            R.drawable.dialog_background,
        )
        if (singleButton) {
            binding.messageDialogCancel.visibility = View.GONE
            binding.messageDialogConfirm.background = getDrawable(
                isDarkMode,
                R.drawable.dialog_single_button_background_night,
                R.drawable.dialog_single_button_background,
            )
        } else {
            binding.messageDialogCancel.background = getDrawable(
                isDarkMode,
                R.drawable.dialog_cancel_button_background_night,
                R.drawable.dialog_cancel_button_background,
            )
            binding.messageDialogConfirm.background = getDrawable(
                isDarkMode,
                R.drawable.dialog_confirm_button_background_night,
                R.drawable.dialog_confirm_button_background,
            )
        }
        binding.messageDialogTitle.text = title
        binding.messageDialogTitle.setTextColor(getTextColor(isDarkMode))
        binding.messageDialogContent.text = content
        binding.messageDialogContent.setTextColor(getTextColor(isDarkMode))
        binding.messageDialogCancel.text = cancel
        binding.messageDialogCancel.setTextColor(getTextColor(isDarkMode))
        binding.messageDialogConfirm.text = confirm
        binding.messageDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }
        binding.messageDialogConfirm.setOnClickListener {
            kDialog!!.dismiss()
            onConfirm.invoke()
        }

        showDialog(binding.root)
    }

    fun showProgressDialog(
        context: Context,
        title: CharSequence,
        progress: Int = 0,
        listener: (dialog: KDialog, progress: io.github.fplus.core.base.BaseHook.ProgressDialogNotification) -> Unit,
    ) {
        val isDarkMode = context.isDarkMode
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_progress_layout)
        val binding = DialogProgressLayoutBinding.bind(dialogView)

        binding.progressDialogContainer.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_background_night,
            R.drawable.dialog_background,
        )
        binding.progressDialogBar.progress = progress
        binding.progressDialogTitle.text = title
        binding.progressDialogTitle.setTextColor(getTextColor(isDarkMode))
        binding.progressDialogText.setTextColor(getTextColor(isDarkMode))
        listener.invoke(
            kDialog!!,
            ProgressDialogNotification(
                binding.progressDialogBar,
                binding.progressDialogText
            )
        )

        showDialog(binding.root)
    }

    fun showInputChoiceDialog(
        context: Context,
        title: CharSequence,
        showInput1: Boolean,
        input1Hint: CharSequence,
        input1DefaultValue: CharSequence,
        input2Hint: CharSequence,
        input2DefaultValue: CharSequence,
        items: Array<String>,
        cancel: CharSequence = "取消",
        onChoice: (view: View, input1: String, input2: String, item: CharSequence, position: Int) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val isDarkMode = context.isDarkMode
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_input_choice_layout)
        val binding = DialogInputChoiceLayoutBinding.bind(dialogView)

        binding.choiceDialogContainer.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_background_night,
            R.drawable.dialog_background,
        )

        binding.choiceDialogCancel.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_single_button_background_night,
            R.drawable.dialog_single_button_background,
        )

        binding.choiceDialogTitle.text = title
        binding.choiceDialogTitle.setTextColor(getTextColor(isDarkMode))

        binding.choiceDialogInput1.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_input_background_night,
            R.drawable.dialog_input_background,
        )
        binding.choiceDialogInput1.hint = input1Hint
        binding.choiceDialogInput1.setHintTextColor(getHintTextColor(isDarkMode))
        binding.choiceDialogInput1.setText(input1DefaultValue)
        binding.choiceDialogInput1.setTextColor(getTextColor(isDarkMode))
        if (!showInput1) {
            binding.choiceDialogInput1.setText("")
            binding.choiceDialogInput1.isVisible = false
        }

        binding.choiceDialogInput2.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_input_background_night,
            R.drawable.dialog_input_background,
        )
        binding.choiceDialogInput2.hint = input2Hint
        binding.choiceDialogInput2.setHintTextColor(getHintTextColor(isDarkMode))
        binding.choiceDialogInput2.setText(input2DefaultValue)
        binding.choiceDialogInput2.setTextColor(getTextColor(isDarkMode))

        binding.choiceDialogCancel.text = cancel
        binding.choiceDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }

        binding.choiceDialogList.adapter = DialogChoiceAdapter(context, items, getTextColor(isDarkMode))
        binding.choiceDialogList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.choiceDialogList.selector = getDrawable(
            isDarkMode,
            R.drawable.item_selector_background_night,
            R.drawable.item_selector_background,
        )
        binding.choiceDialogList.setOnItemClickListener { _, view, position, _ ->
            kDialog!!.dismiss()
            onChoice.invoke(
                view,
                "${binding.choiceDialogInput1.text}",
                "${binding.choiceDialogInput2.text}",
                items[position],
                position
            )
        }

        showDialog(binding.root)
    }

    fun showChoiceDialog(
        context: Context,
        title: CharSequence,
        items: Array<String>,
        cancel: CharSequence = "取消",
        onChoice: (view: View, item: CharSequence, position: Int) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_choice_layout)
        val binding = DialogChoiceLayoutBinding.bind(dialogView)

        val isDarkMode = context.isDarkMode

        binding.choiceDialogContainer.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_background_night,
            R.drawable.dialog_background,
        )

        binding.choiceDialogCancel.background = getDrawable(
            isDarkMode,
            R.drawable.dialog_single_button_background_night,
            R.drawable.dialog_single_button_background,
        )

        binding.choiceDialogTitle.text = title
        binding.choiceDialogTitle.setTextColor(getTextColor(isDarkMode))

        binding.choiceDialogCancel.text = cancel
        binding.choiceDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }

        binding.choiceDialogList.adapter = DialogChoiceAdapter(context, items, getTextColor(isDarkMode))
        binding.choiceDialogList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.choiceDialogList.selector = getDrawable(
            isDarkMode,
            R.drawable.item_selector_background_night,
            R.drawable.item_selector_background,
        )
        binding.choiceDialogList.setOnItemClickListener { _, view, position, _ ->
            kDialog!!.dismiss()
            onChoice.invoke(
                view,
                items[position],
                position
            )
        }

        showDialog(binding.root)
    }


    private fun getHintTextColor(isDarkMode: Boolean): Int {
        return if (isDarkMode) {
            Color.parseColor("#FFAAAAAA")
        } else {
            Color.parseColor("#FF666666")
        }
    }

    private fun getTextColor(isDarkMode: Boolean): Int {
        return if (isDarkMode) {
            Color.parseColor("#FFFFFFFF")
        } else {
            Color.parseColor("#FF2C2F39")
        }
    }

    private fun getDrawable(isDarkMode: Boolean, @DrawableRes darkId: Int, @DrawableRes lightId: Int): Drawable? {
        return if (isDarkMode) {
            KtXposedHelpers.getDrawable(darkId)
        } else {
            KtXposedHelpers.getDrawable(lightId)
        }
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

    class ProgressDialogNotification(
        private val progressBar: ProgressBar,
        private val progressText: TextView,
    ) : IProgressNotification {
        override fun setFinishedText(finishedText: String) {
            progressBar.progress = 100
            progressText.text = finishedText
        }

        override fun notifyProgress(step: Int, inProgressText: String) {
            progressBar.progress = step
            progressText.text = String.format(inProgressText, step)
        }
    }
}