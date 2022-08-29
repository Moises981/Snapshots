package com.example.snapshots

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object Helper {
    fun getUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun loadImage(context: Context, url: String, container: ImageView) {
        Glide.with(context).load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
            .into(container)
    }
}