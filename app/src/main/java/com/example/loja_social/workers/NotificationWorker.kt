package com.example.loja_social.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
        var hasAlerts = false
        
        try {
            // Ensure Retrofit is initialized
            com.example.loja_social.api.RetrofitHelper.ensureInitialized(applicationContext)

            // 1. Check Stock Expiry
            val stockResponse = RetrofitInstance.api.getAlertasValidade()
            if (stockResponse.success) {
                val expiringItems = stockResponse.data
                val nearbyExpiry = expiringItems.filter { it.diasRestantes <= 7 }
                
                if (nearbyExpiry.isNotEmpty()) {
                    hasAlerts = true
                    showNotification(
                        1,
                        "Atenção: Validade de Stock",
                        "${nearbyExpiry.size} produtos expiram em breve! Verifique o stock."
                    )
                }
            } else {
                 Log.e("NotificationWorker", "Stock API Failed: ${stockResponse.message}")
            }

            // 2. Check Deliveries for Today
            val entregasResponse = RetrofitInstance.api.getEntregas()
            if (entregasResponse.success) {
                val today = LocalDate.now().toString()
                val todaysDeliveries = entregasResponse.data.filter { 
                    it.dataAgendamento.startsWith(today) && it.estado == "agendada"
                }

                if (todaysDeliveries.isNotEmpty()) {
                    hasAlerts = true
                    showNotification(
                        2,
                        "Entregas Agendadas Hoje",
                        "Existem ${todaysDeliveries.size} entregas agendadas para hoje."
                    )
                }
            }

             // DEBUG only: Se não encontrou nada real, manda notificação de teste para confirmar que o Worker correu
            if (!hasAlerts) {
                 showNotification(
                    3,
                    "Teste de Notificação",
                    "Sistema operacional. Nenhum alerta urgente encontrado."
                )
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in background work", e)
             showNotification(
                4,
                "Erro na Verificação",
                "Falha ao verificar alertas: ${e.message}"
            )
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
