package io.github.fplus.activity

import android.os.Bundle
import androidx.activity.ComponentActivity

class StubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw RuntimeException("starting this activity is not allowed.")
    }
}