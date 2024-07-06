package io.github.fplus.plugin.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.contextaware.ContextAware
import androidx.activity.contextaware.ContextAwareHelper
import androidx.activity.contextaware.OnContextAvailableListener
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

open class BaseActivity : Activity(),
    IPluginActivity,
    ContextAware,
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {
    private val mContextAwareHelper = ContextAwareHelper()

    // lifecycle
    private var _lifecycleRegistry: LifecycleRegistry? = null

    private val lifecycleRegistry: LifecycleRegistry
        get() = _lifecycleRegistry ?: LifecycleRegistry(this)
            .also { _lifecycleRegistry = it }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // view model
    private var _viewModelStore: ViewModelStore? = null

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore ?: ViewModelStore()
            .also { _viewModelStore = ViewModelStore() }

    // instance state
    private var _savedStateRegistryController: SavedStateRegistryController? = null

    private val savedStateRegistryController
        get() = _savedStateRegistryController ?: SavedStateRegistryController.create(this)
            .also { _savedStateRegistryController = it }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // back pressed
    override val onBackPressedDispatcher = OnBackPressedDispatcher {
        super.onBackPressed()
    }

    override fun addOnContextAvailableListener(listener: OnContextAvailableListener) {
        mContextAwareHelper.addOnContextAvailableListener(listener)
    }

    override fun peekAvailableContext(): Context? {
        return mContextAwareHelper.peekAvailableContext()
    }

    override fun removeOnContextAvailableListener(listener: OnContextAvailableListener) {
        mContextAwareHelper.removeOnContextAvailableListener(listener)
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
        // Restore the Saved State first so that it is available to
        // OnContextAvailableListener instances
        savedStateRegistryController.performRestore(savedInstanceState)
        mContextAwareHelper.dispatchOnContextAvailable(this)
        super.onCreate(savedInstanceState)
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
        mContextAwareHelper.clearAvailableContext()
        if (isChangingConfigurations) {
            _viewModelStore?.clear()
        }
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
        window!!.decorView.setViewTreeViewModelStoreOwner(this)
        window!!.decorView.setViewTreeLifecycleOwner(this)
        window!!.decorView.setViewTreeSavedStateRegistryOwner(this)
        window!!.decorView.setViewTreeOnBackPressedDispatcherOwner(this)
    }
}