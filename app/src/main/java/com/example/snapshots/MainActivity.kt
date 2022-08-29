package com.example.snapshots

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentFragment: Fragment
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNav()
        setupAuth()
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            if (res.resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    getString(
                        R.string.main_greetings,
                        Helper.getUser()?.displayName
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (res.idpResponse == null) {
                finish()
            }
        }

    private fun setupAuth() {
        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user == null) {
                signInLauncher.launch(authIntent())
            }
        }
    }

    private fun authIntent(): Intent {
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(
                arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                )
            )
            .build()
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }


    private fun setupBottomNav() {
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        currentFragment = homeFragment

        initFragmentsHidden(profileFragment, addFragment)
        loadFragment(homeFragment).commit()

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> updateFragmentView(homeFragment)
                R.id.action_add -> updateFragmentView(addFragment)
                R.id.action_profile -> updateFragmentView(profileFragment)
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.action_home -> (homeFragment as HomeAux).gotoTop()
            }
        }
    }

    private fun initFragmentsHidden(vararg fragments: Fragment) {
        for (fragment in fragments) {
            loadFragment(fragment).hide(fragment).commit()
        }
    }

    private fun loadFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction()
            .add(R.id.hostFragment, fragment, fragment::class.java.name)

    private fun updateFragmentView(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction().hide(currentFragment).show(fragment)
            .commit()
        currentFragment = fragment
        return true
    }

}