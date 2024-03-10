package com.example.blogapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddArticleBinding

    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("blogs")
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addBlogBtn.setOnClickListener {

           // Toast.makeText(this, "Button Clicked!", Toast.LENGTH_SHORT).show()

            saveBlogInFirebase()
        }

        binding.imageButton.setOnClickListener {
            finish()
        }


    }

    private fun saveBlogInFirebase() {

        val title: String = binding.blogTitle.text.toString().trim()
        val description: String = binding.blogDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()

        }
        else{

        // Get current user
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {

            val userId: String = user.uid
            val userName: String = user.displayName ?: "Anonymous"
            val userImage = user.photoUrl ?: ""

            // Fetch user data from Firebase

            userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userData = snapshot.getValue(UserData::class.java)
                    if (userData != null) {

                        val userNameFromDB: String = userData.name
                        val userImageFromDB: String = userData.profileImage

                        // Get current date
                        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                        // Create BlogItemModel Item
                        val blogItem = BlogItemModel(
                            title,
                            userNameFromDB,
                            currentDate,
                            userId,
                            description,
                            0,
                            userImageFromDB
                        )

                        // Generate unique key for each blog
                        val key: String? = databaseReference.push().key
                        if (key != null) {

                            blogItem.postId = key
                            val blogReference: DatabaseReference = databaseReference.child(key)
                            blogReference.setValue(blogItem).addOnCompleteListener {

                                if (it.isSuccessful) {

                                    finish()

                                } else {

                                    Toast.makeText(
                                        this@AddArticleActivity,
                                        "Action Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        }


    }
}