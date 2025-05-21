package com.example.taskmanager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DeadlineCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L

        try {
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
            if (currentUserEmail == null) return Result.failure()

            val firestore = FirebaseFirestore.getInstance()
            val tasksSnapshot = firestore.collection("tasks").whereEqualTo("assignedTo", currentUserEmail).get().await()

            for (doc in tasksSnapshot.documents) {
                val taskName = doc.getString("name") ?: continue
                val deadline = doc.getLong("deadline") ?: continue

                if (deadline - now in 0..threeDaysInMillis) {
                    showNotification(taskName)
                }
            }


            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun showNotification(taskName: String) {
        val channelId = "task_reminders"

        // Notification channel (Android 8+)
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val name = "Task Deadline Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance)

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Task Reminder")
            .setContentText("Task '$taskName' deadline is in 3 days")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(taskName.hashCode(), builder.build())
            }
        }
    }

}