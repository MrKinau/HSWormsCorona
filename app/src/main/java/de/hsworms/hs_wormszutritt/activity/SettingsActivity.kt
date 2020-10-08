package de.hsworms.hs_wormszutritt.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.hsworms.hs_wormszutritt.R
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.regex.Pattern


class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        // setup pref change listener
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        setSupportActionBar(appBarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val button: Preference? = findPreference("matrikel")
            button?.setOnPreferenceClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Matrikelnummer eingeben")
                val input = EditText(requireContext())
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)

                builder.setPositiveButton("OK") { dialog, which ->
                    if (isMatrikelNumberValid(input.text.toString()))
                        preferenceManager.sharedPreferences.edit().putString("matrikel", input.text.toString()).apply()
                    else {
                        Toast.makeText(requireContext(), "Matrikelnummer nicht gültig, glaub ich", Toast.LENGTH_LONG).show()
                        dialog.cancel()
                    }
                }
                builder.setNegativeButton("Abbrechen") {
                        dialog, which -> dialog.cancel()
                }

                builder.show()
                true
            }
        }

        private fun isMatrikelNumberValid(matrikel: String) : Boolean{
            val pattern = Pattern.compile("[0-9]{5,6}")
            return pattern.matcher(matrikel).matches()
        }

    }

}