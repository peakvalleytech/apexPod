package de.danoeh.apexpod.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.danoeh.apexpod.R
import de.danoeh.apexpod.databinding.AboutTeaserBinding.inflate

class CheckListAdapter(
    val items : List<String>
) : RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder>() {
    class CheckListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var itemTextView : TextView
        private lateinit var checkbox : CheckBox
        init {
            itemTextView = itemView.findViewById(R.id.)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_multiple_choice)
        return CheckListViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CheckListViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
    }
}