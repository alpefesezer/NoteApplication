package com.example.noteapplication

import com.google.gson.Gson


// MOCKAPI CLASS RETURNS JSON NOTES
class MockApi {
    companion object {
        fun getMockNotesJson(): String {
            // Create a list of notes (replace this with your actual data)
            val notesList = listOf(
                Note(1, "Try to delete me! :D", "This note comes from MockApi.kt and will be refreshing every 15 seconds"),
            )

            // Convert the list of notes to JSON using Gson
            val gson = Gson()
            return gson.toJson(notesList)
        }
    }

}