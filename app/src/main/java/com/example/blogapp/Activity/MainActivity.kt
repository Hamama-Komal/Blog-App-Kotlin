package com.example.blogapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Adapter.BlogAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth
    private val blogItems = mutableListOf<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // To change the icon color of floating button
        // binding.floatingActionButton.imageTintList = ColorStateList.valueOf(Color.WHITE)

        databaseReference = FirebaseDatabase.getInstance().reference.child("blogs")
        firebaseAuth = FirebaseAuth.getInstance()
        val userId  = firebaseAuth.currentUser?.uid

        // Set user profile Image
        if(userId != null){
            loadProfileImage(userId)
        }


        // Add new Blog
        binding.addArticleBtn.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // Move to Bookmark article activity
        binding.saveArticleBtn.setOnClickListener {
            startActivity(Intent(this, SavedBlogActivity::class.java))
        }

        // Move to user profile activity
        binding.profile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }


        // Set Blog Recycler View
        val  recyclerView: RecyclerView = binding.blogRecycler
        val blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchBlogItemDataFromFB(blogAdapter)





    }

    private fun fetchBlogItemDataFromFB(blogAdapter: BlogAdapter) {

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

               blogItems.clear()
                for (snapshot : DataSnapshot in snapshot.children){
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if(blogItem != null){
                        blogItems.add(blogItem)
                    }
                }
                // to reverse the list
                blogItems.reverse()

                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Error! No data found", Toast.LENGTH_SHORT).show()

            }
        })

    }

    private fun loadProfileImage(userId: String) {

        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userReference.child("profileImage").addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if(profileImageUrl != null){

                    Glide.with(this@MainActivity).load(profileImageUrl).into(binding.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@MainActivity,"Error! Profile image not found", Toast.LENGTH_SHORT).show()
            }
        })
    }
}