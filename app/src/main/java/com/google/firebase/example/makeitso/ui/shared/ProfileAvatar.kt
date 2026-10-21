package com.google.firebase.example.makeitso.ui.shared

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/** Round account photo loaded from [photoUrl]; a placeholder icon while loading or if none. */
@Composable
fun ProfileAvatar(photoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    val photo by produceState<ImageBitmap?>(initialValue = null, photoUrl) {
        value = if (photoUrl.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                try {
                    URL(photoUrl).openStream().use { BitmapFactory.decodeStream(it) }
                        ?.asImageBitmap()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    val bitmap = photo
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Profile photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            tint = LocalContentColor.current,
            contentDescription = "Profile picture placeholder",
            modifier = modifier.size(size)
        )
    }
}