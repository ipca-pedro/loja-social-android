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

            // 1. Check Stock Expiry (Only if user has permission/is admin? API might restrict, but let's try)
            // Ideally we should check role, but SessionManager is needed.
            // For now, we wrap in try-catch in case of 403
            try {
                val stockResponse = RetrofitInstance.api.getAlertasValidade()
                if (stockResponse.success) {
                    val expiringItems = stockResponse.data
                    val nearbyExpiry = expiringItems.filter { it.diasRestantes <= 7 }
                    
                    if (nearbyExpiry.isNotEmpty()) {
                        showNotification(
                            1,
                            "Atenção: Validade de Stock",
                            "${nearbyExpiry.size} produtos expiram em breve! Verifique o stock."
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore stock errors (likely not admin)
            }

            // 2. Check NEW Deliveries (For Beneficiaries)
            try {
                val entregasResponse = RetrofitInstance.api.getMinhasEntregas() // Use specialized endpoint or generic
                // Note: getEntregas() filters by role server-side.
                
                if (entregasResponse.success) {
                    val allDeliveries = entregasResponse.data
                    val prefs: SharedPreferences = context.getSharedPreferences("loja_social_prefs", Context.MODE_PRIVATE)
                    val knownIds = prefs.getStringSet("known_delivery_ids", emptySet()) ?: emptySet()
                    
                    val currentIds = allDeliveries.map { it.id }.toSet()
                    val newIds = currentIds - knownIds
                    
                    // Filter specifically for future/agendada deliveries among the new ones
                    val newFutureDeliveries = allDeliveries.filter { 
                        it.id in newIds && 
                        it.estado == "agendada" &&
                        LocalDate.parse(it.dataAgendamento.substringBefore("T")) >= LocalDate.now()
                    }

                    if (newFutureDeliveries.isNotEmpty()) {
                        val firstNew = newFutureDeliveries.first()
                        val date = firstNew.dataAgendamento.substringBefore("T")
                        
                        showNotification(
                            2,
                            "Nova Entrega Agendada",
                            "Tens uma nova entrega agendada para o dia $date." + 
                            if (newFutureDeliveries.size > 1) " (+${newFutureDeliveries.size - 1} outras)" else ""
                        )
                    }

                    // Update known IDs (save ALL current IDs to handle deletions/updates correctly? 
                    // No, just save current state so next time we diff against it)
                    prefs.edit().putStringSet("known_delivery_ids", currentIds).apply()
                }
            } catch (e: Exception) {
               Log.e("NotificationWorker", "Deliveries Check Failed: ${e.message}")
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
