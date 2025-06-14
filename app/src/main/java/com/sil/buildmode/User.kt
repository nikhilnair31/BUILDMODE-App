package com.sil.buildmode

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.text.Editable
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ThemedSpinnerAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.sil.others.Helpers
import com.sil.others.Helpers.Companion.showToast
import com.sil.services.ScreenshotService

class User : AppCompatActivity() {
    // region Vars
    private val TAG = "Settings"
    private val PREFS_GENERAL = "com.sil.buildmode.generalSharedPrefs"
    private val KEY_SCREENSHOT_ENABLED = "isScreenshotMonitoringEnabled"

    private lateinit var generalSharedPreferences: SharedPreferences

    private lateinit var usernameText: EditText
    private lateinit var editUsernameButton: Button
    private lateinit var emailText: EditText
    private lateinit var editEmailButton: Button
    private lateinit var userLogoutButton: Button
    // endregion

    // region Common
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        generalSharedPreferences = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE)

        initRelated()
    }
    private fun initRelated() {
        usernameText = findViewById(R.id.usernameEditText)
        editUsernameButton = findViewById(R.id.editUsername)
        emailText = findViewById(R.id.emailEditText)
        editEmailButton = findViewById(R.id.editEmail)
        userLogoutButton = findViewById(R.id.userLogout)

        val username = generalSharedPreferences.getString("username", "")
        usernameText.setText(username)
        editUsernameButton.isEnabled = false  // Disable by default
        usernameText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editUsernameButton.isEnabled = s.toString() != username
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val email = generalSharedPreferences.getString("email", "")
        emailText.setText(email)
        editEmailButton.isEnabled = false  // Disable by default
        emailText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editEmailButton.isEnabled = s.toString() != email
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editUsernameButton.setOnClickListener {
            editUsernameRelated()
        }
        editEmailButton.setOnClickListener {
            editEmailRelated()
        }
        userLogoutButton.setOnClickListener {
            userLogoutRelated()
        }
    }
    // endregion

    // region Auth Related
    private fun editUsernameRelated() {
        Log.i(TAG, "editUsernameRelated")

        val newUsername = usernameText.text.toString()
        Helpers.authEditUsernameToServer(this, newUsername) { success ->
            runOnUiThread {
                if (success) {
                    Log.i(TAG, "Edit username success")
                    showToast(this, "Edit username successful!")
                    editUsernameButton.isEnabled = false
                    generalSharedPreferences.edit {
                        putString("username", newUsername)
                    }
                } else {
                    Log.i(TAG, "Edit username failed!")
                    showToast(this, "Edit username failed!")
                }
            }
        }
    }
    private fun editEmailRelated() {
        Log.i(TAG, "editEmailRelated")

        val newEmail = emailText.text.toString()
        Helpers.authEditEmailToServer(this, newEmail) { success ->
            runOnUiThread {
                if (success) {
                    Log.i(TAG, "Edit email success")
                    showToast(this, "Edit email successful!")
                    editEmailButton.isEnabled = false
                    generalSharedPreferences.edit {
                        putString("email", newEmail)
                    }
                } else {
                    Log.i(TAG, "Edit email failed!")
                    showToast(this, "Edit email failed!")
                }
            }
        }
    }
    private fun userLogoutRelated() {
        Log.i(TAG, "userLogoutRelated")

        generalSharedPreferences.edit(commit = true) {
            remove("username")
            .remove("access_token")
            .remove("refresh_token")
            .remove("last_query")
            .remove("last_results_json")
            .remove("cached_saves_left")
            .putBoolean(KEY_SCREENSHOT_ENABLED, false)
        }  // ← block until written

        val serviceIntent = Intent(this@User, ScreenshotService::class.java)
        stopService(serviceIntent)

        val intent = Intent(this, Welcome::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    // endregion
}