package com.example.blogapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Adapter.BlogAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivitySavedBlogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedBlogActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySavedBlogBinding

    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedBlogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blogAdapter = BlogAdapter(savedBlogArticles.filter { it.isSaved } .toMutableList())
        binding.saveBlogRecycler.adapter = blogAdapter
        binding.saveBlogRecycler.layoutManager = LinearLayoutManager(this)

        val userId : String? = firebaseAuth.currentUser?.uid
        if(userId != null){

            val userReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saveBlogs")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(postSnapshot in snapshot.children){
                        val postId: String? = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if(postId != null && isSaved){

                            // Fetch the blog item using Coroutine    (Add data using thread)
                            CoroutineScope(Dispatchers.IO).launch {

                                val blogItem = fetchBlogItem(postId)
                                if(blogItem != null){
                                    savedBlogArticles.add(blogItem)
                                }

                                // New changes here
                                // To show the lastest saved blog on above
                                //savedBlogArticles.reverse()    // not working

                                launch (Dispatchers.Main){
                                    blogAdapter.updateData(savedBlogArticles)
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

        binding.backButton.setOnClickListener {
            finish()
        }

    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {

        val blogReference = FirebaseDatabase.getInstance().getReference("blogs")

        return try{
            val dataSnapshot: DataSnapshot? = blogReference.child(postId).get().await()
            val blogData : BlogItemModel? = dataSnapshot?.getValue(BlogItemModel::class.java)
            blogData
        }
        catch (e : Exception){
            null
        }

    }
}