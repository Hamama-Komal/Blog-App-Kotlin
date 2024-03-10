package com.example.blogapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivityReadMoreBinding
import com.example.blogapp.databinding.ActivitySplashBinding

class ReadMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if(blogs != null){

            binding.apply {

                blogHeading.text = blogs.heading
                userName.text = blogs.userName
                date.text = blogs.date
                blogDescription.text = blogs.blog
                Glide.with(this@ReadMoreActivity).load(blogs.profileImage).into(profile)
            }

        }
        else{

            Toast.makeText(this@ReadMoreActivity,"Fail to load data", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {

            finish()
        }
    }
}