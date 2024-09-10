package io.github.fplus.drawable

import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable


class SelectorBuilder : DrawableBuilder {

    var normal: Drawable? = null
    var pressed: Drawable? = null
    var unable: Drawable? = null
    var checked: Drawable? = null
    var selected: Drawable? = null

    override fun build(): Drawable = StateListDrawable().apply {
        pressed?.let {
            val pressedState = android.R.attr.state_pressed
            addState(intArrayOf(pressedState), it)
        }
        unable?.let {
            val enableState = android.R.attr.state_enabled
            addState(intArrayOf(-enableState), it)
        }
        checked?.let {
            val checkedState = android.R.attr.state_checked
            addState(intArrayOf(checkedState), it)
        }
        selected?.let {
            val selectedState = android.R.attr.state_selected
            addState(intArrayOf(selectedState), it)
        }
        addState(IntArray(0), normal)
    }

}