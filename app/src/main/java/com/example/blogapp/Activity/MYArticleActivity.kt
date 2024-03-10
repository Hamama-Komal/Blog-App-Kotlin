package com.example.blogapp.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Adapter.MyArticleAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityMyarticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MYArticleActivity : AppCompatActivity() {
    private val binding: ActivityMyarticleBinding by lazy {
        ActivityMyarticleBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var myArticleAdapter: MyArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val currentUserId = firebaseAuth.currentUser?.uid

        binding.backButton.setOnClickListener {
            finish()
        }


        if (currentUserId != null) {

            myArticleAdapter =
                MyArticleAdapter(this, emptyList(), object : MyArticleAdapter.OnItemClickListener {
                    override fun OnReadClick(blogItem: BlogItemModel) {

                        val intent = Intent(this@MYArticleActivity, ReadMoreActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivity(intent)
                    }

                    override fun OnEditClick(blogItem: BlogItemModel) {

                        val intent = Intent(this@MYArticleActivity, EditMyBlogActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivityForResult(intent, EDIT_BLOG_REQUEST_CODE)
                    }

                    override fun OnDeleteClick(blogItem: BlogItemModel) {

                        //  deleteBlogItem(blogItem)
                        showAlert(
                            this@MYArticleActivity,
                            "Confirmation",
                            "Do you want to delete this Blog?",
                            blogItem
                        )

                    }
                })
        }

        binding.myBlogRecycler.layoutManager = LinearLayoutManager(this)
        binding.myBlogRecycler.adapter = myArticleAdapter

        // get blog data from
        databaseReference = FirebaseDatabase.getInstance().getReference("blogs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val myBlogList = ArrayList<BlogItemModel>()
                for (blogSnapshot in snapshot.children) {
                    val myBlog = blogSnapshot.getValue(BlogItemModel::class.java)
                    if (myBlog != null && currentUserId == myBlog.userId) {
                        myBlogList.add(myBlog)
                    }

                }
                myArticleAdapter.setData(myBlogList)
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@MYArticleActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun deleteBlogItem(blogItem: BlogItemModel) {

        val postId = blogItem.postId
        val blogReference = databaseReference.child(postId!!)

        blogReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this@MYArticleActivity, "Blog Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@MYArticleActivity, "Action Failed", Toast.LENGTH_SHORT).show()
            }

    }

    private fun showAlert(
        context: Context,
        title: String,
        message: String,
        blogItem: BlogItemModel
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Yes") { dialog, _ ->

            deleteBlogItem(blogItem)
        }
        builder.setNegativeButton("No") { dialog, _ ->
            // Do something when the OK button is clicked
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == EDIT_BLOG_REQUEST_CODE && resultCode == Activity.RESULT_OK){

        }
    }
}