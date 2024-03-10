package com.example.blogapp.RegisterAndLoginUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.blogapp.Activity.MainActivity
import com.example.blogapp.databinding.ActivityStartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {

            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
            finish()

        }

        binding.btnRegister.setOnClickListener {

            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
            finish()


        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser : FirebaseUser? = firebaseAuth.currentUser

        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}