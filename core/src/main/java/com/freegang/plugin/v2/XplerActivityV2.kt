package com.freegang.plugin.v2

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.freegang.plugin.base.BaseXplerActivityV2

open class XplerActivityV2 : BaseXplerActivityV2(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {

    private var _lifecycleRegistry: LifecycleRegistry? = null

    private val lifecycleRegistry: LifecycleRegistry
        get() = _lifecycleRegistry ?: LifecycleRegistry(this)
            .also { _lifecycleRegistry = it }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private var _viewModelStore: ViewModelStore? = null

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore ?: ViewModelStore()
            .also { _viewModelStore = ViewModelStore() }


    private var _savedStateRegistryController: SavedStateRegistryController? = null

    private val savedStateRegistryController
        get() = _savedStateRegistryController ?: SavedStateRegistryController.create(this)
            .also { _savedStateRegistryController = it }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry


    override val onBackPressedDispatcher = OnBackPressedDispatcher {
        super.onBackPressed()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    @CallSuper
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistryController.performSave(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore the Saved State first so that it is available to
        // OnContextAvailableListener instances
        savedStateRegistryController.performRestore(savedInstanceState)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        super.onPause()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        super.onStop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _lifecycleRegistry = null
        _viewModelStore = null
        _savedStateRegistryController = null
    }

    override fun setContentView(layoutResID: Int) {
        initViewTreeOwners()
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        initViewTreeOwners()
        super.setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        initViewTreeOwners()
        super.setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams?) {
        initViewTreeOwners()
        super.addContentView(view, params)
    }

    private fun initViewTreeOwners() {
        window!!.decorView.setViewTreeLifecycleOwner(this)
        window!!.decorView.setViewTreeOnBackPressedDispatcherOwner(this)
        window!!.decorView.setViewTreeSavedStateRegistryOwner(this)
    }
}