package com.example.nittalk.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.nittalk.R
import com.example.nittalk.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onStart() {
        super.onStart()
        mainActivityViewModel.makeCurrentUserOnline()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serverUrl = URL("https://meet.jit.si")
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            .setWelcomePageEnabled(false)
            .setAudioOnly(true)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.groupChatFragment, R.id.inboxFragment, R.id.searchFragment, R.id.profileFragment))

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
//                R.id.groupChatFragment -> hideBottomNav()
//                R.id.friendChatFragment -> hideBottomNav()
//                R.id.inviteMemberFragment -> hideBottomNav()
                R.id.createServerFragment -> hideBottomNav()
                R.id.editProfileFragment -> hideBottomNav()
                else -> showBottomNav()
            }
        }

        binding.bottomNavigationView.setupWithNavController(navController)

    }

    private fun showBottomNav() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        mainActivityViewModel.makeCurrentUserOffline()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityViewModel.makeCurrentUserOffline()
    }

}