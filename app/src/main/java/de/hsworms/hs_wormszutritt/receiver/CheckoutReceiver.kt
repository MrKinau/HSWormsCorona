package de.hsworms.hs_wormszutritt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import de.hsworms.hs_wormszutritt.service.UpdateNotificationService
import de.hsworms.hs_wormszutritt.volley.RoomRegistration

class CheckoutReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.stopService(Intent(context, UpdateNotificationService::class.java))
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.contains("current_room"))
            return
        if (!prefs.contains("matrikel"))
            return
        val room = prefs.getString("current_room","")
        val matrikel = prefs.getString("matrikel","")
        if (context != null)
            RoomRegistration.checkOut(room!!, matrikel!!, context)
    }

}