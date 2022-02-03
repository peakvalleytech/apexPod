package de.danoeh.apexpod.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.CheckedTextViewCompat
import androidx.recyclerview.widget.RecyclerView
import de.danoeh.apexpod.R
import de.danoeh.apexpod.databinding.AboutTeaserBinding.inflate
import de.danoeh.apexpod.dialog.ChecklistDialog


 class CheckListAdapter<T>(
    val items : List<T>,
    val getValue : (Int) -> String,
    val onItemCheckListener: ChecklistDialog.OnItemCheckListener
) : RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder>() {
    class CheckListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var checkbox : CheckBox
        init {
            checkbox = itemView.findViewById(R.id.checkbox)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_checklist_item, parent, false)
        return CheckListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CheckListViewHolder, position : Int) {
        holder.checkbox.setText(getValue(position))
        holder.checkbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                onItemCheckListener.onItemChecked(holder.absoluteAdapterPosition, isChecked)
            }
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }
}