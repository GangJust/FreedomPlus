package io.github.fplus.plugin.base

import android.os.Bundle
import androidx.activity.ComponentActivity


open class BaseComponentActivity : ComponentActivity(), IXplerActivity {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}