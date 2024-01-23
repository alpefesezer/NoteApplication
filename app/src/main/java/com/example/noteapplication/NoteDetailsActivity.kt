package com.example.noteapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplication.databinding.ActivityNoteDetailsBinding

class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the intent extras
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        // Set the title and content in the views
        binding.titleTextView.text = title
        binding.contentTextView.text = content

        binding.backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}