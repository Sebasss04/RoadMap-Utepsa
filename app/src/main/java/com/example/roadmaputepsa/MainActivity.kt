package com.example.roadmaputepsa

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.roadmaputepsa.navigation.AppNavigation
import com.example.roadmaputepsa.ui.theme.RoadMapUtepsaTheme
import com.google.firebase.FirebaseApp

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            RoadMapUtepsaTheme { AppNavigation() }
        }
    }
}
