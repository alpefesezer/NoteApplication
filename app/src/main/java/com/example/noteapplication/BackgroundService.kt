package com.example.noteapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BackgroundService : Service() {

    private val TAG: String = "BackgroundService"
    private val NOTIFICATION_ID = 1
    lateinit var sharedPreferences: SharedPreferences

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        if (!sharedPreferences.getBoolean("serviceStarted", false)) {
            // Start the service for the first time
            startBackgroundTask()

            // Set a flag to indicate that the service has been started
            sharedPreferences.edit().putBoolean("serviceStarted", true).apply()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun startBackgroundTask() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, BackgroundReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Set the alarm to start the service every 15 seconds
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR / 120,
            AlarmManager.INTERVAL_HALF_HOUR / 120,
            alarmIntent
        )

        // Simulate fetching data in the background
        val jsonNotes = MockApi.getMockNotesJson()
        val noteList = parseJsonToNoteList(jsonNotes)
        // Update the local database with mockNotes
        updateLocalDatabase(noteList)
    }

    fun parseJsonToNoteList(jsonNotes: String): List<Note> {
        val gson = Gson()

        // Use Gson to parse the JSON string into a list of Note objects
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(jsonNotes, type)
    }

    private fun showNotification() {
        // Check for the BIND_NOTIFICATION_LISTENER_SERVICE permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Create a notification to notify the user about new notes
            val builder = NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("New Notes Available")
                .setContentText("Check your notes for updates")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } else {
            // Handle the case where the permission is not granted
            Log.e(TAG, "Notification listener service permission not granted")
        }
    }

    fun updateLocalDatabase(mockNotes: List<Note>) {
        val dbHelper = NotesDatabaseHelper(this)

        // Iterate through mockNotes and insert/update each note in the database
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

        // Show a notification after updating the local database
        showNotification()
        // Send a broadcast to notify MainActivity about new notes
        val intent = Intent("NEW_NOTES_ADDED")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}
