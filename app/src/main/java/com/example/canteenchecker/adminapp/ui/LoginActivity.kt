package com.example.canteenchecker.adminapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import kotlinx.coroutines.launch


// Login Activity - copied from the labour project
class LoginActivity : AppCompatActivity() {
    companion object {
        fun intent(context: Context) =
            Intent(context, LoginActivity::class.java)
    }

    private lateinit var edtUserName : EditText
    private lateinit var edtPassword : EditText
    private lateinit var btnLogin : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtUserName = findViewById(R.id.edtUserName)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogIn)

        btnLogin.setOnClickListener{authenticate()}
    }

    private fun authenticate() = lifecycleScope.launch {
        setUIEnabled(false)

        AdminApiFactory.createAdminApiInstance().authenticate(
            edtUserName.text.toString(),
            edtPassword.text.toString())
            .onSuccess { authToken ->
                (application as CanteenCheckerAdminApplication).authenticationToken = authToken
                setResult(Activity.RESULT_OK)
                finish()
            }
            .onFailure {
                edtPassword.text = null
                Toast.makeText(this@LoginActivity, R.string.message_login_failed, Toast.LENGTH_LONG).show()
            }

        setUIEnabled(true)
    }

    private fun setUIEnabled(enabled: Boolean){
        btnLogin.isEnabled = enabled
        edtUserName.isEnabled = enabled
        edtPassword.isEnabled = enabled
    }
}