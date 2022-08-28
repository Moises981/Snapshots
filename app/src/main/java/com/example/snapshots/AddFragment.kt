package com.example.snapshots

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.databinding.FragmentAddBinding
import com.example.snapshots.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var photoSelectedUri: Uri? = null

    companion object {
        const val PATH_SNAPSHOTS = "snapshots"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnPost.setOnClickListener {
                postSnapshot()
            }
            btnSelect.setOnClickListener {
                getImageUri.launch("image/*")
            }
        }
        storageReference = FirebaseStorage.getInstance().getReference(PATH_SNAPSHOTS)
        databaseReference = FirebaseDatabase.getInstance().getReference(PATH_SNAPSHOTS)
    }

    private val getImageUri =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            photoSelectedUri = uri
            binding.imgPhoto.setImageURI(uri)
            binding.tilTitle.visibility = View.VISIBLE;
            binding.tvMessage.text = getString(R.string.post_message_valid_title)
        }

    private fun postSnapshot() {
        binding.progressBar.visibility = View.VISIBLE
        val key = databaseReference.push().key!!

        if (photoSelectedUri == null) return;
        storageReference.child(FirebaseAuth.getInstance().currentUser!!.uid).child(key)
            .putFile(photoSelectedUri!!)
            .addOnProgressListener {
                val progress = (100 * it.bytesTransferred / it.totalByteCount).toFloat()
                binding.progressBar.progress = progress.toInt()
                binding.tvMessage.text = "$progress%"
            }
            .addOnCompleteListener {
                binding.progressBar.visibility = View.INVISIBLE

            }
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { url ->
                    saveSnapshot(key, url.toString(), binding.etTitle.text.toString().trim())
                    binding.tvMessage.text = getString(R.string.post_message_title)
                }
                binding.progressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.INVISIBLE
            }
    }

    private fun saveSnapshot(key: String, url: String, title: String) {
        val snapshot: Snapshot = Snapshot(title = title, photoUrl = url)
        databaseReference.child(key).setValue(snapshot)
    }

}