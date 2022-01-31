package de.danoeh.apexpod.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import de.danoeh.apexpod.R

class ChecklistDialog(
    val items : List<String>,
    val onItemCheckedListener : OnItemCheckListener? = null,
    val onPositiveButtonClickListener : DialogInterface.OnClickListener? = null,
    val onNegativeButtonClickListener : DialogInterface.OnClickListener? = null
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_checklist, container)
        val recycler : RecyclerView = root.findViewById(R.id.checklist_recycler)
        recycler.adapter =
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Filter by Podcast")
        val view = layoutInflater.inflate(R.layout.dialog_checklist, null)
        builder.setView(view)
        builder.setPositiveButton("Ok", onPositiveButtonClickListener)
        builder.setNegativeButton("Cancel", onNegativeButtonClickListener)
        return builder.create()
    }

    interface OnItemCheckListener {
        fun onItemChecked(index : Int)
    }
}