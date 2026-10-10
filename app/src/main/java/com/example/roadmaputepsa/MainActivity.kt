package com.example.roadmaputepsa

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.roadmaputepsa.interfaz.audio.ReproductorAudio
import com.example.roadmaputepsa.navigation.AppNavigation
import com.example.roadmaputepsa.ui.theme.RoadMapUtepsaTheme
import com.google.firebase.FirebaseApp

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding()
            ) {
                ReproductorAudio(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            RoadMapUtepsaTheme { AppNavigation() }
        }
    }
}
