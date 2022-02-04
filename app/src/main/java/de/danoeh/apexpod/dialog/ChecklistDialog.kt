package de.danoeh.apexpod.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.icu.lang.UCharacter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.danoeh.apexpod.R
import de.danoeh.apexpod.adapter.CheckListAdapter

class ChecklistDialog<T>(
    val title : Int,
    val items : List<T>,
    val getValue : (Int) -> String,
    val isChecked : (Int) -> Boolean,
    val onItemCheckedListener : OnItemCheckListener? = null,
    val onPositiveButtonClickListener : DialogInterface.OnClickListener? = null,
    val onNegativeButtonClickListener : DialogInterface.OnClickListener? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(title))
        val view = layoutInflater.inflate(R.layout.dialog_checklist,null , false)
        val recycler : RecyclerView = view.findViewById(R.id.checklist_recycler)
        val linearLayoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = linearLayoutManager
        recycler.adapter = onItemCheckedListener?.let { CheckListAdapter(items, getValue, isChecked, it) }
        builder.setView(view)
        builder.setPositiveButton(getString(R.string.confirm_label), onPositiveButtonClickListener)
        builder.setNegativeButton(getString(R.string.cancel_label), onNegativeButtonClickListener)
        return builder.create()
    }

    interface OnItemCheckListener {
        fun onItemChecked(index : Int, isChecked : Boolean)
    }
}