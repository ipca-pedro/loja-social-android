package com.example.loja_social.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitInstance
import java.time.LocalDate

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Fetching alerts in background...")
        val context = applicationContext
        
        try {
            // Ensure Retrofit is initialized
            com.example.loja_social.api.RetrofitHelper.ensureInitialized(context)
            
            val prefs: SharedPreferences = context.getSharedPreferences("loja_social_prefs", Context.MODE_PRIVATE)
            val lastKnownId = prefs.getString("last_notification_id", "") ?: ""

            // 1. Fetch Notifications from API
            val response = RetrofitInstance.api.getNotificacoes()
            
            if (response.success && !response.data.isNullOrEmpty()) {
                val notifications = response.data
                
                // Better strategy: Keep Set of IDs we have already alerted about.
                val alertedIds = prefs.getStringSet("alerted_notification_ids", emptySet()) ?: emptySet()
                
                val toAlert = notifications.filter { !it.lida && it.id !in alertedIds }

                if (toAlert.isNotEmpty()) {
                    val count = toAlert.size
                    val first = toAlert.first()
                    
                    showNotification(
                        100, // Fixed ID for summary, or use unique IDs
                        if (count == 1) first.titulo else "Novas Notificações ($count)",
                        if (count == 1) first.mensagem else "Tens $count novas notificações por ler."
                    )
                    
                    // Update alerted list
                    val newAlertedSet = alertedIds.toMutableSet()
                    newAlertedSet.addAll(toAlert.map { it.id })
                    prefs.edit().putStringSet("alerted_notification_ids", newAlertedSet).apply()
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in background work", e)
            return Result.retry()
        }
    }

    private fun showNotification(id: Int, title: String, content: String) {
        val channelId = "loja_social_channel"
        val context = applicationContext

        // Create Channel (Safe to call repeatedly)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificações Loja Social"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationWorker", "No permission to post notifications")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon if available
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
