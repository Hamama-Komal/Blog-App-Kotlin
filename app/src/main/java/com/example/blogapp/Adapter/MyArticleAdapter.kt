package com.example.blogapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.MyBlogItemBinding

class MyArticleAdapter(
    private val context: Context,
    private var blogList: List<BlogItemModel>,
    private val  itemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<MyArticleAdapter.BlogViewHolder>(){


        interface OnItemClickListener{

            fun OnReadClick(blogItem: BlogItemModel)
            fun OnEditClick(blogItem: BlogItemModel)
            fun OnDeleteClick(blogItem: BlogItemModel)

        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MyBlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return  blogList.size
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {

        val blogItem = blogList[position]
        holder.bind(blogItem)
    }

    fun setData(myBlogList: ArrayList<BlogItemModel>) {
        this.blogList = myBlogList
        notifyDataSetChanged()

    }

    inner class BlogViewHolder(private val binding: MyBlogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItem: BlogItemModel) {

            binding.heading.text = blogItem.heading
            Glide.with(binding.profile.context).load(blogItem.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItem.userName
            binding.date.text = blogItem.date
            binding.blog.text = blogItem.blog

            // read article button
            binding.readBtn.setOnClickListener {
                itemClickListener.OnReadClick(blogItem)
            }

            // edit article button
            binding.editBtn.setOnClickListener {
                itemClickListener.OnEditClick(blogItem)
            }

            // delete article button
            binding.deleteBtn.setOnClickListener {
                itemClickListener.OnDeleteClick(blogItem)
            }



        }

    }
}