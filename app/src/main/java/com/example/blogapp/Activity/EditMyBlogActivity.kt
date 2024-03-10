package com.example.blogapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivityEditMyBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditMyBlogActivity : AppCompatActivity() {

    private val binding: ActivityEditMyBlogBinding by lazy {
        ActivityEditMyBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.blogTitle.setText(blogItemModel?.heading)
        binding.blogDescription.setText(blogItemModel?.blog)


        binding.imageButton.setOnClickListener {
            finish()
        }

        binding.saveBlogBtn.setOnClickListener {

            val updatedTitle = binding.blogTitle.text.toString().trim()
            val updatedDescription = binding.blogDescription.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {

                Toast.makeText(
                    this@EditMyBlogActivity, "Please fill all fields", Toast.LENGTH_SHORT
                ).show()

            }
            else {

                blogItemModel?.heading = updatedTitle
                blogItemModel?.blog = updatedDescription

                if (blogItemModel != null) {

                    UpdateBlogFromFirebase(blogItemModel)
                }

            }
        }

    }

    private fun UpdateBlogFromFirebase(blogItemModel: BlogItemModel) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("blogs")
        val postId = blogItemModel.postId

        databaseReference.child(postId!!).setValue(blogItemModel).addOnSuccessListener {
            Toast.makeText(this@EditMyBlogActivity, "Blog Updated", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@EditMyBlogActivity, "Action failed", Toast.LENGTH_SHORT).show()
        }
    }

}