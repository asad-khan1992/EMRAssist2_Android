package com.emrassist.audio.ui.activity.dictation_list.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.emrassist.audio.R
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.utils.RecyclerItemTouchHelper
import com.emrassist.audio.utils.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import kotlinx.android.synthetic.main.item_dictation_list_database_adaper.view.*

class DictationListDatabaseAdapter(
    private var context: Context,
    private var listOfAudios: ArrayList<RecordedItem>,
    private var isPending: Boolean,
    private var recyclerView: RecyclerView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RecyclerItemTouchHelperListener {
    private var onDeleteListener: OnItemDeleteClickListener? = null
    private var itemTouchHelperCallback: RecyclerItemTouchHelper? = null

    private var onRetryListener: OnItemRetryClickListener? = null

    private var onClickListener: OnItemClickListener? = null
    var filteredList: ArrayList<RecordedItem> = listOfAudios

    init {
        if (!isPending) {
            itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
            ItemTouchHelper(itemTouchHelperCallback!!).attachToRecyclerView(recyclerView)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_dictation_list_database_adaper, null)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return listOfAudios.size
    }

    fun setList(listOfAudios: ArrayList<RecordedItem>) {
        this.listOfAudios = listOfAudios
        filteredList.clear();
        filteredList.addAll(listOfAudios)
        notifyDataSetChanged()
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var viewForeground: RelativeLayout = itemView.relative_view_foreground
        var imagePlay: ImageView = itemView.image_play
        var imageRetry: ImageView = itemView.image_upload
        var textName: TextView = itemView.text_name
        var textRecordedDate: TextView = itemView.text_recorded_date

        fun bind() {
            textName.setText(filteredList[adapterPosition].fileName)
            textRecordedDate.setText(filteredList[adapterPosition].localPath)
            imageRetry.visibility = if (isPending) View.VISIBLE else View.GONE
        }

        init {
            imagePlay.setOnClickListener {
                onClickListener?.onClick(adapterPosition, filteredList[adapterPosition])
            }
            imageRetry.setOnClickListener {
                onRetryListener?.onClick(adapterPosition, filteredList[adapterPosition])
            }
        }
    }

    fun filterList(charString: String) {
        if (charString.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(listOfAudios)
        } else {
            val tempList: ArrayList<RecordedItem> = ArrayList()
            for (row in listOfAudios) {
                // here we are looking for name or recordedDate number match
                if (row.fileName.contains(charString, true) ||
                    row.recordedDate.contains(charString, true)
                ) {
                    tempList.add(row)
                }
            }
            filteredList = tempList
        }
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, item: RecordedItem) {
        if (position < listOfAudios.size) {
            listOfAudios[position] = item
            notifyItemChanged(position)
        }
    }

    fun itemUploaded(item: RecordedItem) {
        if (listOfAudios.contains(item)) {
            listOfAudios.remove(item)
        }
        notifyDataSetChanged()
    }

    public fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onClickListener = onItemClickListener
    }

    public fun setOnItemRetryClickListener(onRetryClick: OnItemRetryClickListener) {
        onRetryListener = onRetryClick
    }

    public fun setOnItemDeleteClickListener(onDeletelick: OnItemDeleteClickListener) {
        onDeleteListener = onDeletelick
    }


    interface OnItemClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    interface OnItemRetryClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    interface OnItemDeleteClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        if (viewHolder != null && viewHolder is ItemViewHolder) {
            onDeleteListener?.onClick(position, listOfAudios[position])
            listOfAudios.remove(listOfAudios[position])
            notifyDataSetChanged()

        }

    }

    fun restoreItem(position: Int, item: RecordedItem) {
        listOfAudios.add(position, item)
        notifyDataSetChanged()
    }


}