package com.emrassist.audio.ui.activity.dictation_list.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.emrassist.audio.R
import com.emrassist.audio.model.RecordedItem
import kotlinx.android.synthetic.main.item_dictation_list_adaper.view.*
import java.util.*

class DictationListAdapter(
    private var context: Context,
    private var listOfAudios: ArrayList<RecordedItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() , Filterable {
    val TYPE_ITEM: Int = 0
    val TYPE_LOADING: Int = 1

    private var onRetryListener: OnItemRetryClickListener? = null
    private var onDeleteListener: OnItemDeleteClickListener? = null
    private var onClickListener: OnItemClickListener? = null
    var filteredList: ArrayList<RecordedItem> = listOfAudios
    var allAudioList: ArrayList<RecordedItem> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM)
            ItemViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_dictation_list_adaper, null)
            )
        else
            LoadingViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.item_loader_dictation_list_adapter, null)
            )
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfAudios[position].uniqueId.equals("-1"))
            TYPE_LOADING
        else
            TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind()
        } else if (holder is LoadingViewHolder) {
            holder.bind()
        }
    }

    fun addLoadingView() {
        //add loading item
        Handler(Looper.getMainLooper()).post {
            var item = RecordedItem();
            item.uniqueId = "-1"
            listOfAudios.add(item)
            notifyItemInserted(listOfAudios.size - 1)
        }
    }

    fun removeLoadingView() {
        //Remove loading item
        if (listOfAudios.size != 0) {
            listOfAudios.removeAt(listOfAudios.size - 1)
            notifyItemRemoved(listOfAudios.size)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun setList(listOfAudios: ArrayList<RecordedItem>) {
        this.listOfAudios = listOfAudios
        filteredList.clear();
        filteredList.addAll(listOfAudios)
        notifyDataSetChanged()
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        var viewForeground: RelativeLayout = itemView.relative_view_background
        var viewBackground: RelativeLayout = itemView.relative_view_foreground

        //        var imageDelete = itemView.image_delete
        var imageStatus = itemView.image_status

        //        var imageRetry = itemView.image_retry
        var textName = itemView.text_name
        var textRecordedDate = itemView.text_recorded_date

        fun bind() {
            textName.setText(filteredList[adapterPosition].fileName)
            textRecordedDate.text = filteredList[adapterPosition]
                .recordedDate.replace("+", " ")
                .replace("%3A", ":")
            if (filteredList[adapterPosition].status.equals("done", true)) {
                imageStatus.setBackgroundResource(R.drawable.status_green_round)
            } else {
                imageStatus.setBackgroundResource(R.drawable.status_yellow_round)
            }
        }

        init {
//            imageRetry.setOnClickListener {
//                onRetryListener?.onClick(adapterPosition, filteredList[adapterPosition])
//            }
            viewBackground.setOnClickListener {
                onClickListener?.onClick(adapterPosition, filteredList[adapterPosition])
            }
//            imageDelete.setOnClickListener {
//                onDeleteListener?.onClick(adapterPosition, filteredList[adapterPosition])
//            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
        }

        init {
        }
    }

    fun filterList(charString: String) {
        if (charString.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(allAudioList)
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

    public fun setOnItemRetryClickListener(onRetryClick: OnItemRetryClickListener) {
        onRetryListener = onRetryClick
    }

    public fun setOnItemRetryClickListener(onDeleteClick: OnItemDeleteClickListener) {
        onDeleteListener = onDeleteClick
    }

    public fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onClickListener = onItemClickListener
    }

    fun addAll(list: ArrayList<RecordedItem>) {
        list.addAll(list)
        notifyDataSetChanged()
    }

    interface OnItemRetryClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    interface OnItemDeleteClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    interface OnItemClickListener {
        fun onClick(position: Int, item: RecordedItem)
    }

    init {
        allAudioList.addAll(listOfAudios)
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filteredList = if (charString.isEmpty()) {
                    listOfAudios
                } else {
                    filteredList.clear()
                    val tempList: ArrayList<RecordedItem> = ArrayList()
                    for (row in listOfAudios) {
                        // here we are looking for name or recordedDate number match
                        if (row.fileName.contains(charString, true) ||
                                row.recordedDate.contains(charString, true)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<RecordedItem>
                notifyDataSetChanged()
            }
        }
    }

}