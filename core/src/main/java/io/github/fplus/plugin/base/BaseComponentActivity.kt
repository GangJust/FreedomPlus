package io.github.fplus.plugin.base

import android.os.Bundle
import androidx.activity.ComponentActivity


open class BaseComponentActivity : ComponentActivity(), IPluginActivity {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}