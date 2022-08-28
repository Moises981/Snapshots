package com.example.snapshots

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
            tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
            btnLogout.setOnClickListener { logout() }
        }
    }

    private fun logout() {
        context?.let {
            AuthUI.getInstance().signOut(it).addOnCompleteListener {
                Toast.makeText(context, "See you later...", Toast.LENGTH_SHORT).show()
            }
        }
    }

}