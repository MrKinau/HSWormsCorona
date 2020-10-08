package de.hsworms.hs_wormszutritt.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.hsworms.hs_wormszutritt.MainActivity
import de.hsworms.hs_wormszutritt.R
import de.hsworms.hs_wormszutritt.receiver.CheckoutReceiver
import java.util.concurrent.TimeUnit


class UpdateNotificationService : Service() {

    private var room: String = ""
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var countDownTimer: CountDownTimer? = null

    companion object {
        private const val ONE_DAY = 86400000L
        private const val NOTIFICATION_ID = 72624
        private const val NOTIFICATION_TEXT = "Du bist seit %s in Raum %s registriert."
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.hasExtra("room"))
            room = intent.getStringExtra("room")!!
        val checkoutIntent = PendingIntent.getBroadcast(this, 0, Intent(this, CheckoutReceiver::class.java), 0)
        notificationBuilder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
        notificationBuilder.setContentTitle("Kontaktregistrierung")
        notificationBuilder.setContentText(String.format(NOTIFICATION_TEXT, format(0), room))
        notificationBuilder.priority = NotificationCompat.PRIORITY_LOW
        notificationBuilder.addAction(R.drawable.checkout, "Checkout", checkoutIntent)
        notificationBuilder.setSmallIcon(R.drawable.notification_icon)
        notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setOngoing(true)

        countDownTimer = object : CountDownTimer(ONE_DAY, 1000) {
            override fun onTick(tick: Long) {
                notificationBuilder.setContentText(String.format(NOTIFICATION_TEXT, format(ONE_DAY - tick), room))
                with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
                    notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            }
            override fun onFinish() {}
        }
        countDownTimer?.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
            cancel(NOTIFICATION_ID)
        }
        countDownTimer?.cancel()
        stopSelf()
    }

    private fun format(ms: Long) : String {
        var millis = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return String.format("%s:%s:%s",
            hours.toString().padStart(2, '0'),
            minutes.toString().padStart(2, '0'),
            seconds.toString().padStart(2, '0')
        )
    }

}