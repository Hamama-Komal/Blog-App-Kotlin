package com.example.blogapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.ReadMoreActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: BlogItemBinding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)

    }

    fun updateData(savedBlogArticles: List<BlogItemModel>) {

        items.clear()
        items.addAll(savedBlogArticles)
        notifyDataSetChanged()

    }


    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItemModel: BlogItemModel) {

            val postId = blogItemModel.postId
            val context = binding.root.context

            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context).load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.blog.text = blogItemModel.blog
            binding.likeCount.text = blogItemModel.likeCount.toString()

            // Item click listener
            binding.readMoreBtn.setOnClickListener {

                val context: Context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)

            }

            // like button toggling
            var postLikeReference: DatabaseReference =
                databaseReference.child("blogs").child(postId.toString()).child("likes")
            val currentUserLiked = currentUser?.uid?.let { uid ->
                postLikeReference.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeBtn.setImageResource(R.drawable.red_like)
                            } else {
                                binding.likeBtn.setImageResource(R.drawable.black_like)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }

            // Like Button Function
            binding.likeBtn.setOnClickListener {

                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            // set initial state of save button
            val userReference : DatabaseReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val postSaveReference : DatabaseReference = userReference.child("saveBlogs").child(postId!!)

            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        // already save blog icon
                        binding.saveBtn.setImageResource(R.drawable.red_bookmark)
                    }
                    else{
                        // unsave blog icon
                        binding.saveBtn.setImageResource(R.drawable.bookmark)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            // Save Button Function
            binding.saveBtn.setOnClickListener {

                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

        }

        // like or unlike button function
        private fun handleLikeButtonClicked(postId: String?, blogItemModel: BlogItemModel, binding: BlogItemBinding) {

            val userReference: DatabaseReference =
                databaseReference.child("users").child(currentUser!!.uid)
            val postLikeReference: DatabaseReference =
                databaseReference.child("blogs").child(postId.toString()).child("likes")

            // Toggle On like button
            postLikeReference.child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Unlike Posts
                        if (snapshot.exists()) {

                            userReference.child("likes").child(postId.toString()).removeValue()
                                .addOnSuccessListener {

                                    postLikeReference.child(currentUser.uid).removeValue()
                                    blogItemModel.likedBy?.remove(currentUser.uid)
                                    updateLikeButtonImage(false, binding)

                                    // Decrement like count
                                    val newLikeCount = blogItemModel.likeCount - 1
                                    blogItemModel.likeCount = newLikeCount
                                    databaseReference.child("blogs").child(postId.toString())
                                        .child("likeCount").setValue(newLikeCount)
                                    notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "likedClicked",
                                        "onDataSetChanged: Failed to unlike the blog $e"
                                    )
                                }
                        }
                        else {
                            // Like Posts
                            userReference.child("likes").child(postId.toString()).setValue(true)
                                .addOnSuccessListener {
                                    postLikeReference.child(currentUser.uid).setValue(true)
                                    blogItemModel.likedBy?.add(currentUser.uid)
                                    updateLikeButtonImage(true, binding)

                                    // Increment like count
                                    val newLikeCount = blogItemModel.likeCount + 1
                                    blogItemModel.likeCount = newLikeCount
                                    databaseReference.child("blogs").child(postId.toString())
                                        .child("likeCount").setValue(newLikeCount)
                                    notifyDataSetChanged()
                                }.addOnFailureListener { e ->
                                    Log.e(
                                        "likedClicked",
                                        "onDataSetChanged: Failed to unlike the blog $e"
                                    )
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

        // update like or unlike icon
        private fun updateLikeButtonImage(liked: Boolean, binding: BlogItemBinding) {

            if (liked) {
                binding.likeBtn.setImageResource(R.drawable.black_like)
            } else {
                binding.likeBtn.setImageResource(R.drawable.red_like)
            }
        }

        // save or unsave button function
        private fun handleSaveButtonClicked(postId: String?, blogItemModel: BlogItemModel, binding: BlogItemBinding) {

            val userReference: DatabaseReference =
                databaseReference.child("users").child(currentUser!!.uid)
            // Toggle save button
            userReference.child("saveBlogs").child(postId.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            // Remove save Blog
                            userReference.child("saveBlogs").child(postId.toString()).removeValue()
                                .addOnSuccessListener {
                                    val clickedBlogItem: BlogItemModel? = items.find { it.postId == postId }
                                    clickedBlogItem?.isSaved = false
                                    notifyDataSetChanged()

                                    /*val context: Context = binding.root.context
                                    Toast.makeText(context, "Remove Bookmark", Toast.LENGTH_SHORT)
                                        .show()*/
                                }
                                .addOnFailureListener {
                                    val context: Context = binding.root.context
                                    Toast.makeText(context, "Action Failed", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            binding.saveBtn.setImageResource(R.drawable.bookmark)
                        }
                        else
                        {

                            // Save Blog
                            userReference.child("saveBlogs").child(postId.toString()).setValue(true)
                                .addOnSuccessListener {
                                    val clickedBlogItem: BlogItemModel?= items.find { it.postId == postId }
                                    clickedBlogItem?.isSaved = true
                                    notifyDataSetChanged()

                                    /*val context: Context = binding.root.context
                                    Toast.makeText(context, "Added to bookmark", Toast.LENGTH_SHORT)
                                        .show()*/
                                }
                                .addOnFailureListener {

                                    val context: Context = binding.root.context
                                    Toast.makeText(context, "Action Failed", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            // Update the bookmark icon
                            binding.saveBtn.setImageResource(R.drawable.red_bookmark)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val context: Context = binding.root.context
                        Toast.makeText(context, "Error! $error", Toast.LENGTH_SHORT)
                            .show()
                    }

                })


        }


    }


}

