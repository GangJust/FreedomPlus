package io.github.fplus.core.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.github.fplus.core.R

class DialogChoiceAdapter<T : CharSequence>(
    private val context: Context,
    private val items: Array<T>,
    private val color: Int,
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): T = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)

        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            viewHolder = ViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.dialog_choice_item, parent, false)
            viewHolder.itemTextView = view.findViewById(R.id.choiceItemText)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.itemTextView?.text = item
        viewHolder.itemTextView?.setTextColor(color)
        return view
    }

    class ViewHolder {
        var itemTextView: TextView? = null
    }
}