package com.freegang.base

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.NinePatchDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.freegang.ktutils.app.IProgressNotification
import com.freegang.ktutils.app.KNotifiUtils
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.log.KLogCat
import com.freegang.view.KDialog
import com.freegang.view.adapter.DialogChoiceAdapter
import com.freegang.xpler.R
import com.freegang.xpler.core.KtOnHook
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.inflateModuleView
import com.freegang.xpler.databinding.DialogChoiceLayoutBinding
import com.freegang.xpler.databinding.DialogInputChoiceLayoutBinding
import com.freegang.xpler.databinding.DialogMessageLayoutBinding
import com.freegang.xpler.databinding.DialogProgressLayoutBinding
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseHook<T>(lpparam: XC_LoadPackage.LoadPackageParam) : KtOnHook<T>(lpparam) {
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

    fun refresh(block: () -> Unit) {
        handler.post(block)
    }

    fun showToast(context: Context, message: String) {
        refresh {
            toast = if (toast == null) {
                Toast.makeText(context.applicationContext, null, Toast.LENGTH_LONG)
            } else {
                toast?.cancel()
                Toast.makeText(context.applicationContext, null, Toast.LENGTH_LONG)
            }

            runCatching {
                //val view = toast?.findFieldAndGet("mNextView")

                toast?.view?.isClickable = false
                toast?.view?.isLongClickable = false

                val modeNight = context.isDarkMode

                //背景色
                val drawable = toast?.view?.background as NinePatchDrawable?
                drawable?.colorFilter = if (modeNight) {
                    PorterDuffColorFilter(Color.parseColor("#FF161823"), PorterDuff.Mode.SRC_IN)
                } else {
                    PorterDuffColorFilter(Color.parseColor("#FFFFFFFF"), PorterDuff.Mode.SRC_IN)
                }

                //文字颜色
                val textView: TextView? = toast?.view?.findViewById(android.R.id.message)
                textView?.setTextColor(
                    if (modeNight) {
                        Color.parseColor("#FFFFFFFF")
                    } else {
                        Color.parseColor("#FF161823")
                    }
                )

                //文本对齐
                textView?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
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

    @Synchronized
    fun showDialog(
        view: View,
        needMultiple: Boolean = false,
    ) {
        kDialog = if (kDialog == null) KDialog() else kDialog
        val kDialog = if (needMultiple) KDialog() else kDialog
        kDialog!!.setView(view)
        kDialog.show()
    }

    fun showMessageDialog(
        context: Context,
        title: CharSequence,
        content: CharSequence,
        cancel: CharSequence = "取消",
        confirm: CharSequence = "确定",
        singleButton: Boolean = false, //只会响应 onConfirm 方法
        needMultiple: Boolean = false,
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {},
    ) {
        val dialogView = context.inflateModuleView<FrameLayout>(R.layout.dialog_message_layout)
        val binding = DialogMessageLayoutBinding.bind(dialogView)

        binding.messageDialogContainer.background = KtXposedHelpers.getDrawable(R.drawable.dialog_background)
        if (singleButton) {
            binding.messageDialogCancel.visibility = View.GONE
            binding.messageDialogConfirm.background = KtXposedHelpers.getDrawable(R.drawable.dialog_single_button_background)
        } else {
            binding.messageDialogCancel.background = KtXposedHelpers.getDrawable(R.drawable.dialog_cancel_button_background)
            binding.messageDialogConfirm.background = KtXposedHelpers.getDrawable(R.drawable.dialog_confirm_button_background)
        }
        binding.messageDialogTitle.text = title
        binding.messageDialogContent.text = content
        binding.messageDialogCancel.text = cancel
        binding.messageDialogConfirm.text = confirm
        binding.messageDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }
        binding.messageDialogConfirm.setOnClickListener {
            kDialog!!.dismiss()
            onConfirm.invoke()
        }

        showDialog(binding.root, needMultiple)
    }

    fun showProgressDialog(
        context: Context,
        title: CharSequence,
        needMultiple: Boolean = false,
        progress: Int = 0,
        listener: (dialog: KDialog, progress: ProgressDialogNotification) -> Unit,
    ) {
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_progress_layout)
        val binding = DialogProgressLayoutBinding.bind(dialogView)

        binding.progressDialogContainer.background = KtXposedHelpers.getDrawable(R.drawable.dialog_background)
        binding.progressDialogBar.progress = progress
        binding.progressDialogTitle.text = title
        listener.invoke(kDialog!!, ProgressDialogNotification(binding.progressDialogBar, binding.progressDialogText))

        showDialog(binding.root, needMultiple)
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
        needMultiple: Boolean = false,
        onChoice: (view: View, input1: String, input2: String, item: CharSequence, position: Int) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_input_choice_layout)
        val binding = DialogInputChoiceLayoutBinding.bind(dialogView)

        binding.choiceDialogContainer.background = KtXposedHelpers.getDrawable(R.drawable.dialog_background)
        binding.choiceDialogCancel.background = KtXposedHelpers.getDrawable(R.drawable.dialog_single_button_background)

        binding.choiceDialogTitle.text = title

        binding.choiceDialogInput1.background = KtXposedHelpers.getDrawable(R.drawable.dialog_input_background)
        binding.choiceDialogInput1.hint = input1Hint
        binding.choiceDialogInput1.setText(input1DefaultValue)
        if (!showInput1) {
            binding.choiceDialogInput1.setText("")
            binding.choiceDialogInput1.isVisible = false
        }

        binding.choiceDialogInput2.background = KtXposedHelpers.getDrawable(R.drawable.dialog_input_background)
        binding.choiceDialogInput2.hint = input2Hint
        binding.choiceDialogInput2.setText(input2DefaultValue)

        binding.choiceDialogCancel.text = cancel
        binding.choiceDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }

        binding.choiceDialogList.adapter = DialogChoiceAdapter(context, items)
        binding.choiceDialogList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.choiceDialogList.selector = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
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

        showDialog(binding.root, needMultiple)
    }

    fun showChoiceDialog(
        context: Context,
        title: CharSequence,
        items: Array<String>,
        cancel: CharSequence = "取消",
        needMultiple: Boolean = false,
        onChoice: (view: View, item: CharSequence, position: Int) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        val dialogView = KtXposedHelpers.inflateView<FrameLayout>(context, R.layout.dialog_choice_layout)
        val binding = DialogChoiceLayoutBinding.bind(dialogView)

        binding.choiceDialogContainer.background = KtXposedHelpers.getDrawable(R.drawable.dialog_background)
        binding.choiceDialogCancel.background = KtXposedHelpers.getDrawable(R.drawable.dialog_single_button_background)

        binding.choiceDialogTitle.text = title

        binding.choiceDialogCancel.text = cancel
        binding.choiceDialogCancel.setOnClickListener {
            kDialog!!.dismiss()
            onCancel.invoke()
        }

        binding.choiceDialogList.adapter = DialogChoiceAdapter(context, items)
        binding.choiceDialogList.divider = ColorDrawable(Color.TRANSPARENT)
        binding.choiceDialogList.selector = KtXposedHelpers.getDrawable(R.drawable.item_selector_background)
        binding.choiceDialogList.setOnItemClickListener { _, view, position, _ ->
            kDialog!!.dismiss()
            onChoice.invoke(
                view,
                items[position],
                position
            )
        }

        showDialog(binding.root, needMultiple)
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