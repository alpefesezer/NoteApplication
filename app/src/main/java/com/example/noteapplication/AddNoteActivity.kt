package com.example.noteapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.noteapplication.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NotesDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_add_note)
        val view = binding.root  // Get the root view from the binding
        setContentView(view)     // Set the content view with the root view

        db = NotesDatabaseHelper(this)

        binding.saveButton.setOnClickListener{
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()
            val note = Note(0, title,content)
            db.insertNote(note)
            finish()
            Toast.makeText(this,"Note added", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}