package com.devwhispers.whatsapp_stickers.features.stickers

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun StickerImage(imagePath: Uri) {
    val painter = rememberImagePainter(data = imagePath)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(60.dp)
    )
}