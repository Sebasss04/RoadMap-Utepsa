package com.example.roadmaputepsa.interfaz.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun ReproductorAudio(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val player = remember(context) {
        ExoPlayer.Builder(context.applicationContext)
            .build()
            .apply {
                val audioUri =
                    "android.resource://${context.packageName}/raw/musica"

                setMediaItem(MediaItem.fromUri(audioUri))
                prepare()
            }
    }

    var reproduciendo by remember {
        mutableStateOf(player.isPlaying)
    }

    var estado by remember {
        mutableStateOf("Listo para reproducir")
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
                reproduciendo = isPlaying

                estado = if (isPlaying) {
                    "Reproduciendo"
                } else {
                    "En pausa"
                }
            }

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {
                estado = when (playbackState) {
                    Player.STATE_IDLE -> "Detenido"
                    Player.STATE_BUFFERING -> "Cargando audio..."
                    Player.STATE_READY ->
                        if (player.isPlaying) {
                            "Reproduciendo"
                        } else {
                            "Listo para reproducir"
                        }
                    Player.STATE_ENDED -> "Audio finalizado"
                    else -> estado
                }
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.headlineMedium
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Mi reproductor",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = estado,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            if (
                                player.playbackState ==
                                Player.STATE_ENDED
                            ) {
                                player.seekTo(0)
                            }
                            player.play()
                        }
                    }
                ) {
                    Text(
                        text = if (reproduciendo) {
                            "Pausar"
                        } else {
                            "Reproducir"
                        }
                    )
                }
            }
        }
    }
}