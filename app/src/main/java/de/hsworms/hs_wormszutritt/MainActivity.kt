package de.hsworms.hs_wormszutritt

import android.R.string
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hsworms.hs_wormszutritt.activity.SettingsActivity
import de.hsworms.hs_wormszutritt.adapter.RoomAdapter
import de.hsworms.hs_wormszutritt.service.UpdateNotificationService
import de.hsworms.hs_wormszutritt.viewmodel.RoomsViewModel
import de.hsworms.hs_wormszutritt.volley.RoomRegistration
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var viewModel: RoomsViewModel
    private val possibleRooms by lazy { generateRooms() }

    companion object {
        const val CHANNEL_ID = "de.hsworms.zutritt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel = ViewModelProvider(this).get(RoomsViewModel::class.java)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val rooms = prefs.getStringSet("rooms", mutableSetOf())
        viewModel.rooms.value = rooms?.toMutableList()

        setupCheckout()
        setupAdapter()
        setupRoomInput()
        createNotificationChannel()

        viewModel.rooms.observe(this, Observer {
            (roomsList.adapter as RoomAdapter).notifyDataSetChanged()
        })

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Raum hinzufügen")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("Hinzufügen") { dialog, which ->
                if (!possibleRooms.contains(input.text.toString().trim().toUpperCase())) {
                    dialog.cancel()
                    Toast.makeText(this, "Raum existiert nicht, glaub ich", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val rooms = prefs.getStringSet("rooms", mutableSetOf())
                rooms?.add(input.text.toString().trim().toUpperCase(Locale.ROOT))
                prefs.edit().remove("rooms").apply() // Love you Android :*
                prefs.edit().putStringSet("rooms", rooms).apply()
                viewModel.rooms.value!!.add(input.text.toString().trim().toUpperCase(Locale.ROOT))
                viewModel.rooms.postValue(viewModel.rooms.value)
            }
            builder.setNegativeButton("Abbrechen") {
                    dialog, which -> dialog.cancel()
            }

            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val matrikel = prefs.getString("matrikel", "")
        if (matrikel != null) {
            if (matrikel.isNotBlank())
                title = getString(R.string.app_name) + ": " + matrikel
        }
        setupCheckout()
    }

    private fun setupAdapter() {
        val adapter = RoomAdapter(this)
        roomsList.adapter = adapter
    }

    private fun generateRooms() : MutableList<String> {
        val list = mutableListOf<String>()
        listOf('A', 'B', 'C', 'D', 'F', 'G', 'H', 'K', 'L', 'M', 'N', 'O', 'P').forEach { letter ->
            (0..499)
                .map { it.toString().padStart(3, '0') }
                .forEach { list.add(letter + it) }
        }
        return list
    }

    private fun setupRoomInput() {
        roomInput.addTextChangedListener { input ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val rooms = prefs.getStringSet("rooms", mutableSetOf())
            var sortedRooms = rooms?.sorted()
            Log.v("HSWORMS", sortedRooms.toString())
            if (sortedRooms != null) {
                if (input.toString().trim().isNotBlank()) {
                    sortedRooms = sortedRooms.filter {
                        it.startsWith(input.toString().trim().toUpperCase(Locale.ROOT))
                    }.toList()
                }
                viewModel.rooms.value?.clear()
                viewModel.rooms.value!!.addAll(sortedRooms)
                viewModel.rooms.postValue(viewModel.rooms.value)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setupCheckout() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.contains("current_room")) {
            val currentRoom = prefs.getString("current_room", "")
            val startTime = prefs.getLong("registration_time", 0L)

            checkoutButton.text = "Checkout: Raum " + currentRoom
            checkoutButton.visibility = View.VISIBLE
            checkoutButton.setOnClickListener {
                RoomRegistration.checkOut(
                    prefs.getString("current_room", "")!!, prefs.getString(
                        "matrikel",
                        ""
                    )!!, this
                )
                checkoutButton.visibility = View.GONE
            }

            if (!isNotificationServiceRunning()) {
                val intent = Intent(this, UpdateNotificationService::class.java).apply {
                    putExtra("room", currentRoom)
                    putExtra("startTime", startTime)
                }
                this.startService(intent)
            }

        } else
            checkoutButton.visibility = View.GONE
    }

    private fun isNotificationServiceRunning(): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (UpdateNotificationService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

}