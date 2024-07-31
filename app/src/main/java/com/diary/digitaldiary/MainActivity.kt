@file:Suppress("DEPRECATION")

package com.diary.digitaldiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diary.digitaldiary.adapter.DiaryAdapter
import com.diary.digitaldiary.database.DiaryDatabaseHelper
import com.diary.digitaldiary.databinding.ActivityMainBinding
import com.diary.digitaldiary.models.DiaryEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var diaryEntries: MutableList<DiaryEntry>
    private lateinit var diaryDbHelper: DiaryDatabaseHelper
    private lateinit var binding: ActivityMainBinding
    val ADD_DIARY_REQUEST_CODE = 1  // Define a request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView( binding.root )

        diaryDbHelper  = DiaryDatabaseHelper(this)



        diaryEntries = mutableListOf()


        // Load diary entries from database
         diaryEntries = diaryDbHelper.loadEntriesFromDatabase()

        diaryEntries.add( DiaryEntry(199,"title","testing","testing","testing","testing"))
        diaryEntries.add( DiaryEntry(196,"title","testing","testing","testing","testing"))
        diaryEntries.add( DiaryEntry(194,"title","testing","testing","testing","testing"))
        diaryEntries.add( DiaryEntry(196,"title","testing","testing","testing","testing"))

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        diaryAdapter = DiaryAdapter(diaryEntries) { diaryEntry ->
            val intent = Intent(this, AddDiary::class.java)
            intent.putExtra("DIARY_ENTRY_ID", diaryEntry.id)
            intent.putExtra("Update",true)
            startActivityForResult(intent, ADD_DIARY_REQUEST_CODE)
        }

        binding.recyclerView.adapter = diaryAdapter


        binding.fab.setOnClickListener {
            val intent = Intent(this, AddDiary::class.java)
            startActivityForResult(intent, ADD_DIARY_REQUEST_CODE)
        }
        setupSearchView()

    }
    private fun setupSearchView() {
        binding.searchTitle.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText!!)
                return false
            }
        })
    }
    fun filterList(title : String){
        val filtersDiaries = mutableListOf<DiaryEntry>()
        for ( diary in diaryEntries) {
            if (diary.title.lowercase().contains(title.lowercase())) {
                filtersDiaries.add(diary)
            }
        }
        if(filtersDiaries.isNullOrEmpty()){
        }
        else {
            diaryAdapter.filterDiaries(filtersDiaries)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DIARY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Reload diary entries from database
            diaryEntries.clear()
            diaryEntries.addAll(diaryDbHelper.loadEntriesFromDatabase())

            Log.d("Updated","IDS")

            diaryAdapter.filterDiaries(diaryEntries)

            diaryAdapter.notifyDataSetChanged() // Notify adapter that data has changed
        }
    }

}
