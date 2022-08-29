package com.example.snapshots

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomeFragment : Fragment(), HomeAux {
    private lateinit var homeBinding: FragmentHomeBinding
    private lateinit var firebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var adapterLayoutManager: RecyclerView.LayoutManager
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storageReference = FirebaseStorage.getInstance().getReference(Constants.PATH_SNAPSHOTS)
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.PATH_SNAPSHOTS)

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(databaseReference) {
            val snapshot = it.getValue(Snapshot::class.java)
            snapshot!!.id = it.key!!
            snapshot
        }.build()

        firebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options) {

            private lateinit var context: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                context = parent.context
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)
                with(holder) {
                    with(binding) {
                        setListener(snapshot)
                        tvTitle.text = snapshot.title
                        cbLike.text = snapshot.likeList.size.toString()
                        Helper.getUser()?.let {
                            cbLike.isChecked =
                                snapshot.likeList.containsKey(it.uid)
                        }
                        Helper.loadImage(context, snapshot.photoUrl, imgPhoto)
                    }
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                homeBinding.progressBar.visibility = View.GONE
                firebaseAdapter.notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        }

        adapterLayoutManager = LinearLayoutManager(context)
        homeBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = adapterLayoutManager
            adapter = firebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseAdapter.stopListening()
    }

    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSnapshotBinding.bind(view)
        fun setListener(snapshot: Snapshot) {
            with(binding) {
                btnDelete.setOnClickListener() {
                    deleteSnapshot(snapshot)
                }
                cbLike.setOnCheckedChangeListener { _, checked ->
                    setLike(snapshot, checked)
                }
            }
        }
    }

    private fun deleteSnapshot(snapshot: Snapshot) {
        context?.let {
            MaterialAlertDialogBuilder(it).setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.delete_confirm) { _, _ ->
                    storageReference.child(Helper.getUser()!!.uid).child(snapshot.id).delete()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                databaseReference.child(snapshot.id).removeValue()
                            } else {
                                Snackbar.make(
                                    homeBinding.root,
                                    getString(R.string.home_delete_photo_error),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                }
                .setNegativeButton(R.string.dialog_delete_cancel, null)
                .show()
        }
    }

    private fun setLike(snapshot: Snapshot, checked: Boolean) {
        if (checked) {
            databaseReference.child(snapshot.id).child("likeList")
                .child(Helper.getUser()!!.uid).setValue(true)
        } else {
            databaseReference.child(snapshot.id).child("likeList")
                .child(Helper.getUser()!!.uid).setValue(null)
        }
    }

    override fun gotoTop(): Unit = homeBinding.recyclerView.smoothScrollToPosition(0)
}