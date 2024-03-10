package com.example.blogapp.RegisterAndLoginUser

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Activity.MainActivity
import com.example.blogapp.Model.UserData
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class LoginActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Login and Registration activity

        val action : String? = intent.getStringExtra("action")


        if(action == "login"){

            // adjust visibility for login
            binding.edtEmailLogin.visibility = View.VISIBLE
            binding.edtPasswordLogin.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE

            // change button color in runtime
            val color = ContextCompat.getColor(this, R.color.red)
            binding.btnLogin.setBackgroundColor(color)
            binding.btnLogin.setTextColor(Color.WHITE)

            binding.edtEmailRegister.visibility = View.GONE
            binding.edtNameRegister.visibility = View.GONE
            binding.edtPasswordRegister.visibility = View.GONE
            binding.userImage.visibility = View.GONE

            binding.txtRegister.isEnabled = false
            binding.txtRegister.alpha = 0.6f
            binding.btnRegister.isEnabled = false
            binding.btnRegister.alpha = 0.6f

            // login user
            binding.btnLogin.setOnClickListener {

                loginUser();

            }

        }
        else if(action == "register"){

            binding.btnLogin.isEnabled = false
            binding.btnLogin.alpha = 0.6f

            // change button color in runtime
            val color = ContextCompat.getColor(this, R.color.blue)
            binding.btnRegister.setBackgroundColor(color)
            binding.btnRegister.setTextColor(Color.WHITE)

            // register user
            binding.btnRegister.setOnClickListener {

                registerNewUser();
            }

            binding.userImage.setOnClickListener {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser( intent,"Select image"), PICK_IMAGE_REQUEST )

            }


        }




    }

    private fun loginUser() {

        val loginEmail = binding.edtEmailLogin.text.toString()
        val loginPassword = binding.edtPasswordLogin.text.toString()

        if(loginEmail.isEmpty() || loginPassword.isEmpty()){
            Toast.makeText(this@LoginActivity, "Fill all fields", Toast.LENGTH_SHORT).show()
        }
        else{
            firebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener { task ->

                if(task.isSuccessful){

                    Toast.makeText(this@LoginActivity, "Login Successful 😀", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else{
                    Toast.makeText(this@LoginActivity, "Login Failed 😕", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }

    private fun registerNewUser() {

        val registerName = binding.edtNameRegister.text.toString()
        val registerEmail = binding.edtEmailRegister.text.toString()
        val registerPassword = binding.edtPasswordRegister.text.toString()

        if(registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()){

            Toast.makeText(this@LoginActivity, "Fill all fields", Toast.LENGTH_SHORT).show()
        }
        else{

            firebaseAuth.createUserWithEmailAndPassword(registerEmail, registerPassword).addOnCompleteListener {
                task ->
                if (task.isSuccessful){

                    val user = firebaseAuth.currentUser
                    firebaseAuth.signOut()

                    user?.let {

                        // Saver user data into firebase realtime database
                        val userReference: DatabaseReference = database.getReference("users")
                        val userID: String = user.uid
                        val userData = UserData(registerName, registerEmail)

                        userReference.child(userID).setValue(userData)

                        // upload image to firebase storage
                        val  storageReference: StorageReference = storage.reference.child("profile_image/$userID.jpg")
                        storageReference.putFile(imageUri!!).addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                storageReference.downloadUrl.addOnCompleteListener { uri ->
                                    val imageUrl = uri.result.toString()

                                    // Add Image in RealtimeDatabase
                                    userReference.child(userID).child("profileImage")
                                        .setValue(imageUrl)

                                }
                            }
                        }

                        Toast.makeText(this@LoginActivity, "Registration Successful 😀", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, StartActivity::class.java))
                        finishAffinity()
                    }
                }
                else{
                    Toast.makeText(this@LoginActivity, "Registration Failed 😕", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, StartActivity::class.java))
                    finishAffinity()
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
        {
            imageUri = data.data

            Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(binding.userImage)
        }

    }
}