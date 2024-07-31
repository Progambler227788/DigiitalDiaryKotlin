package com.diary.digitaldiary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diary.digitaldiary.R
import com.diary.digitaldiary.models.DiaryEntry
import java.util.Locale

class DiaryAdapter(private var diaryEntries: List<DiaryEntry>, private val onClick: (DiaryEntry) -> Unit) :
    RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_diary, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryEntry = diaryEntries[position]
        holder.textViewItemNumber.text = (position + 1).toString()
        holder.textViewDiaryTitle.text = diaryEntry.title
        holder.textViewLocation.text = diaryEntry.location
        holder.itemView.setOnClickListener { onClick(diaryEntry) }
    }

    override fun getItemCount(): Int {
        return diaryEntries.size
    }

    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewItemNumber: TextView = itemView.findViewById(R.id.textViewItemNumber)
        val textViewDiaryTitle: TextView = itemView.findViewById(R.id.textViewDiaryTitle)
        val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
    }
    fun filterDiaries( data : List<DiaryEntry>){
        diaryEntries = data
        notifyDataSetChanged()
    }


}
