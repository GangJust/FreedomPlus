package io.github.fplus.core.hook

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.freegang.extension.asOrNull
import com.freegang.extension.dip2px
import com.ss.android.ugc.aweme.feed.model.Aweme
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.hook.logic.SaveCommentNewLogic
import io.github.fplus.drawable.selectorDrawable
import io.github.fplus.drawable.shapeDrawable
import io.github.xpler.core.entity.EmptyHook
import io.github.xpler.core.hookClass
import io.github.xpler.core.lparam

class HCommentFeedFragment : BaseHook() {
    private var urlList: List<String> = emptyList()

    override fun setTargetClass(): Class<*> {
        return EmptyHook::class.java
    }

    override fun onInit() {
        lparam.hookClass("com.ss.android.ugc.aweme.commentfeed.CommentFeedFragmentObserver")
            .methodAllByParamTypes(Aweme::class.java) {
                onAfter {
                    val aweme = args[0]?.asOrNull<Aweme>()
                    val commentFeedOuterComment = aweme?.commentFeedOuterComment
                    val imageList = commentFeedOuterComment?.imageList
                    val firstImage = imageList?.firstOrNull()
                    urlList = firstImage?.originUrl?.urlList ?: emptyList()
                }
            }

        lparam.hookClass("com.ss.android.ugc.aweme.commentfeed.uimodule.fragment.CommentFeedTopUIModule")
            .constructorAll {
                onAfter {
                    val topView = args[0]?.asOrNull<ViewGroup>() ?: return@onAfter
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        gravity = Gravity.CLIP_VERTICAL or Gravity.END
                        updateMargins(
                            right = 8.dip2px(),
                            top = 8.dip2px(),
                        )
                    }
                    topView.addView(
                        TextView(topView.context).apply {
                            text = "保存"
                            textSize = 18f
                            setTextColor(Color.WHITE)
                            updatePadding(
                                left = 24.dip2px(),
                                right = 24.dip2px(),
                                top = 8.dip2px(),
                                bottom = 8.dip2px(),
                            )

                            background = selectorDrawable {
                                normal = shapeDrawable {
                                    corner(8f)
                                    solid("#FFEDA664")
                                }

                                pressed = shapeDrawable {
                                    corner(8f)
                                    solid("#FFBD8B59")
                                }
                            }

                            setOnClickListener {
                                SaveCommentNewLogic.onSaveCommentImage(this@HCommentFeedFragment, context, urlList)
                            }

                        },
                        params
                    )
                }
            }
    }
}