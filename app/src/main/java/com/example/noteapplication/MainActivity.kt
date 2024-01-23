package com.example.noteapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapplication.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NotesDatabaseHelper
    private lateinit var notesAdapter: NoteAdapter


    private val BASE_URL = "https://zenquotes.io/api/"
    private val TAG: String = "CHECK_RESPONSE"
    private val handler = Handler(Looper.getMainLooper()) // Handler for periodic tasks
    private val fetchInterval = 15 * 1000L // Fetch data every 15 seconds

    private val notesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //update the UI
            fetchMockNotes()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = NotesDatabaseHelper(this)
        notesAdapter = NoteAdapter(db.getAllNotes(), this)

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener{
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
        getRandomQuote()

        // Register the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notesReceiver,
            IntentFilter("NEW_NOTES_ADDED")
        )
        // Schedule periodic data fetch using Handler
        handler.postDelayed(fetchDataRunnable, fetchInterval)
    }

    override fun onResume(){
        super.onResume()
        val notes = db.getAllNotes()

        if (notes.isEmpty()) {
            binding.noNotesTextView.visibility = View.VISIBLE
        } else {
            binding.noNotesTextView.visibility = View.GONE
        }

        notesAdapter.refreshData(notes)
    }

    private fun fetchMockNotes() {
        // Retrieve the JSON string
        val jsonNotes = MockApi.getMockNotesJson()
        // Parse the JSON string into a list of Note objects
        val noteList = parseJsonToNoteList(jsonNotes)
        // Update the database with the parsed Note objects
        updateDatabaseWithMockNotes(noteList)
        // Refresh the adapter with the updated notes
        notesAdapter.refreshData(db.getAllNotes())
    }

    private fun parseJsonToNoteList(jsonNotes: String): List<Note> {
        val gson = Gson()

        // Use Gson to parse the JSON string into a list of Note objects
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(jsonNotes, type)
    }

    private fun updateDatabaseWithMockNotes(mockNotes: List<Note>) {
        val dbHelper = NotesDatabaseHelper(this)

        for (mockNote in mockNotes) {
            // Check if the note already exists in the database
            val existingNote = dbHelper.getNoteByID(mockNote.id)

            if (existingNote.id == mockNote.id) {
                // If the note exists, update it
                dbHelper.updateNote(mockNote)
            } else {
                // If the note doesn't exist, insert it
                dbHelper.insertNote(mockNote)
            }
        }

        // Close the database helper
        dbHelper.close()
    }

    private fun getRandomQuote(){
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyApi::class.java)

        api.getQuote().enqueue(object : Callback<List<Quote>>{
            override fun onResponse(call: Call<List<Quote>>, response: Response<List<Quote>>) {
                if (response.isSuccessful) {
                    val quotes = response.body()
                    if (quotes != null && quotes.isNotEmpty()) {
                        val randomQuote = quotes.first()
                        showRandomQuote(randomQuote.q)
                    } else {
                        Log.e(TAG, "Response body is null or empty.")
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Quote>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })


        }

    private fun showRandomQuote(quoteText: String) {
        val quoteTextView: TextView = findViewById(R.id.quoteTextView)
        quoteTextView.text = quoteText
    }

    // Runnable to fetch mock data periodically
    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchMockNotes()
            // Schedule the next fetch after the interval
            handler.postDelayed(this, fetchInterval)
            showToast("Data refreshed from Mock API")
        }
    }

    // Cleanup the handler when the activity is destroyed
    override fun onDestroy() {
        // Unregister the BroadcastReceiver to avoid memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notesReceiver)

        // Remove callbacks from the handler
        handler.removeCallbacks(fetchDataRunnable)

        super.onDestroy()
    }

    // Function to show Toast on the main thread
    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
    }