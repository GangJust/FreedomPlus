package io.github.fplus.core.hook

import com.freegang.extension.asOrNull
import io.github.fplus.core.base.BaseHook
import io.github.xpler.core.entity.EmptyHook
import io.github.xpler.core.hookClass
import io.github.xpler.core.lparam

class HFeedPlayerView : BaseHook() {
    override fun setTargetClass(): Class<*> {
        return EmptyHook::class.java
    }

    override fun onInit() {
        lparam.hookClass("com.ss.android.ugc.aweme.feed.model.FeedPlayingParam")
            .constructorAll {
                onAfter {
                    HPlayerController.playingAid = args.firstOrNull()?.asOrNull()
                    HPlayerController.isPlaying = true
                }
            }

        lparam.hookClass("com.ss.android.ugc.aweme.feed.model.FeedPausePlayParam")
            .constructorAll {
                onAfter {
                    HPlayerController.playingAid = args.firstOrNull()?.asOrNull()
                    HPlayerController.isPlaying = false
                }
            }

        lparam.hookClass("com.ss.android.ugc.aweme.feed.model.FeedPlayProgressParam")
            .constructorAll {
                onAfter {
                    HPlayerController.isPlaying = true
                }
            }
    }
}