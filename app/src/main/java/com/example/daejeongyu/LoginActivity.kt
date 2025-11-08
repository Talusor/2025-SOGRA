package com.example.daejeongyu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            val id = findViewById<EditText>(R.id.et_id).text.toString()
            val pw = findViewById<EditText>(R.id.et_password).text.toString()
            Log.d("[APP]", String.format("ID : %s", id))
            Log.d("[APP]", String.format("PW : %s", pw))

            HttpClient.getInstance().login(id, pw, object : BoolCallBack {
                override fun onCallback(boolean: Boolean) {
                    if (boolean) {
                        Log.d("[APP]", "Login success")
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.d("[APP]", "Login failed")
                        Toast.makeText(baseContext, "로그인 실패.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}