package com.example.blogapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.blogapp.RegisterAndLoginUser.StartActivity
import com.example.blogapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {

    private val binding: ActivityUserProfileBinding by lazy {
        ActivityUserProfileBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // show user profile
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {

            loadUserProfileData(userId)
        }

        // add new article activity
        binding.txtNewArticle.setOnClickListener {
            finish()
            startActivity(Intent(this@UserProfileActivity, AddArticleActivity::class.java))
        }

        // your Article activity
        binding.txtMyArticles.setOnClickListener {

            startActivity(Intent(this@UserProfileActivity, MYArticleActivity::class.java))
        }

        // logout
        binding.txtLogout.setOnClickListener {

            val builder = AlertDialog.Builder(this@UserProfileActivity)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure to logout?")
            builder.setPositiveButton("Yes") { dialog, _ ->

                firebaseAuth.signOut()
                startActivity(Intent(this@UserProfileActivity, StartActivity::class.java))
                finishAffinity()

            }
            builder.setNegativeButton("No") { dialog, _ ->
                // Do something when the OK button is clicked
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()


        }

    }

    private fun loadUserProfileData(userId: String) {

        val userReference = databaseReference.child(userId)

        // To load user Image
        userReference.child("profileImage").addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if(profileImageUrl != null){

                    Glide.with(this@UserProfileActivity).load(profileImageUrl).into(binding.userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@UserProfileActivity,"Error! Profile image not found", Toast.LENGTH_SHORT).show()
            }
        })

            // To load user name

        userReference.child("name").addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)
                if(userName != null){

                    binding.txtUserName.text = userName
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@UserProfileActivity,"Error! user name not found", Toast.LENGTH_SHORT).show()
            }
        })


    }
}