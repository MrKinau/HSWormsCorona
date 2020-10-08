package de.hsworms.hs_wormszutritt.volley

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import de.hsworms.hs_wormszutritt.MainActivity
import de.hsworms.hs_wormszutritt.service.UpdateNotificationService

object RoomRegistration {

    fun checkIn(room: String, matrikel: String, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.contains("current_room")) {
            val currentRoom = prefs.getString("current_room", "")
            Toast.makeText(context, "Du bist aktuell noch in Raum $currentRoom angemeldet", Toast.LENGTH_LONG).show()
            return
        }
        sendRequest(room, matrikel, true, context)
    }

    fun checkOut(room: String, matrikel: String, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.contains("current_room")) {
            Toast.makeText(context, "Du bist aktuell in keinem Raum angemeldet", Toast.LENGTH_LONG).show()
            return
        }
        sendRequest(room, matrikel, false, context)
    }

    private fun sendRequest(room: String, matrikel: String, checkIn: Boolean, context: Context) {
        val queue = Volley.newRequestQueue(context)

        val request = FormRequest(
            Request.Method.POST, "https://campus.hs-worms.de/hs-zutritt/modules/index.php",
            Response.Listener {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)

                when {
                    it.contains("Vielen Dank für Ihre Registrierung in Raum") -> {
                        if (prefs.contains("current_room")) {
                            val currentRoom = prefs.getString("current_room", "")
                            Toast.makeText(context, "Du bist aktuell noch in Raum $currentRoom angemeldet", Toast.LENGTH_SHORT).show()
                            return@Listener
                        }
                        val startIndex = it.indexOf("Vielen Dank für Ihre Registrierung in Raum");
                        val msg = it.subSequence(startIndex, startIndex + 48)
                        prefs.edit().putString("current_room", room).apply()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (context is MainActivity)
                            context.setupCheckout()
                        val intent = Intent(context, UpdateNotificationService::class.java).apply {
                            putExtra("room", room)
                        }
                        context.startService(intent)
                        return@Listener
                    }
                    it.contains("Danke fürs Abmelden aus Raum") -> {
                        val startIndex = it.indexOf("Danke fürs Abmelden aus Raum");
                        val msg = it.subSequence(startIndex, startIndex + 33)
                        prefs.edit().remove("current_room").apply()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (context is MainActivity)
                            context.setupCheckout()
                        context.stopService(Intent(context, UpdateNotificationService::class.java))
                        return@Listener
                    }
                    else -> Toast.makeText(context, "Registrierung fehlgeschlagen", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                it.printStackTrace()
            }, room, matrikel, checkIn
        )
        queue.add(request)
    }
}