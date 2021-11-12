package de.hsworms.hs_wormszutritt.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import de.hsworms.hs_wormszutritt.MainActivity
import de.hsworms.hs_wormszutritt.R
import de.hsworms.hs_wormszutritt.volley.RoomRegistration


class RoomAdapter(private val activity: MainActivity) : RecyclerView.Adapter<RoomAdapter.RoomsViewHolder>() {

    class RoomsViewHolder(itemView: View, activity: MainActivity) : RecyclerView.ViewHolder(itemView) {
        val room: TextView = itemView.findViewById(R.id.room)

        init {
            room.setOnClickListener {
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                if (!prefs.contains("matrikel")) {
                    Toast.makeText(activity, "Bitte setze zuerst deine Matrikelnummer in den Einstellungen", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                RoomRegistration.checkIn(room.text.toString(), prefs.getString("matrikel", "")!!, activity)
            }

            room.setOnLongClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val builder: AlertDialog.Builder = AlertDialog.Builder(it.context)
                builder.setTitle("Raum ${room.text} wirklich löschen?")

                builder.setPositiveButton("Löschen") { dialog, which ->
                    val prefs = PreferenceManager.getDefaultSharedPreferences(it.context)
                    val rooms = prefs.getStringSet("rooms", mutableSetOf())
                    rooms?.remove(room.text.toString())
                    prefs.edit().remove("rooms").apply()
                    prefs.edit().putStringSet("rooms", rooms).apply()
                    activity.viewModel.rooms.value!!.remove(room.text.toString())
                    activity.viewModel.rooms.postValue(activity.viewModel.rooms.value)
                    Toast.makeText(it.context, "Raum gelöscht", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("Abbrechen") {
                        dialog, which -> dialog.cancel()
                }
                builder.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_room_item, parent, false)
        return RoomsViewHolder(v, activity)
    }

    override fun getItemCount(): Int {
        return activity.viewModel.rooms.value!!.size
    }

    override fun onBindViewHolder(holder: RoomsViewHolder, position: Int) {
        holder.room.text = activity.viewModel.rooms.value!![position]
    }

}