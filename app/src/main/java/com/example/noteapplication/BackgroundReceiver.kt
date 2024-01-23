package com.example.noteapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BackgroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Start the background service when the alarm is triggered
        context?.startService(Intent(context, BackgroundService::class.java))
    }
}