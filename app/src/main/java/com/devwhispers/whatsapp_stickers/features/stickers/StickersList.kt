package com.devwhispers.whatsapp_stickers.features.stickers

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.devwhispers.whatsapp_stickers.BuildConfig
import com.devwhispers.whatsapp_stickers.R
import com.devwhispers.whatsapp_stickers.features.stickers.data.StickerPackLoader
import com.devwhispers.whatsapp_stickers.features.stickers.data.WhitelistCheck
import com.devwhispers.whatsapp_stickers.features.stickers.domain.StickerPack


const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StickersList(launcher: (Intent) -> Unit) {
    val context = LocalContext.current

    val stickerPacks = remember { mutableStateListOf<StickerPack>() }

    fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent {
        val intent = Intent()
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK")
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }

    fun launchIntentToAddPackToSpecificPackage(
        identifier: String, stickerPackName: String, whatsappPackageName: String
    ) {
        try {
            val intent = createIntentToAddStickerPack(identifier, stickerPackName)
            intent.setPackage(whatsappPackageName)

            context.startActivity(intent)

//            launcher(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG
            ).show()
        }
    }

    fun addStickerPackToWhatsApp(identifier: String, stickerPackName: String) {
        try {
            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(context.packageManager)) {
                Toast.makeText(
                    context, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG
                ).show()
                return
            }

            val stickerPackWhitelistedInWhatsAppConsumer =
                WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(context, identifier)

            if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(
                    identifier, stickerPackName, WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                context, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG
            ).show()
        }

    }

    LaunchedEffect(Unit) {
        val _stickerPacks = StickerPackLoader.fetchStickerPacks(context)

        stickerPacks.addAll(_stickerPacks)
    }


    Scaffold {
        LazyColumn {
            items(stickerPacks.size) { index ->
                val pack = stickerPacks[index]

                ListItem(
                    modifier = Modifier.clickable {
                        addStickerPackToWhatsApp(pack.identifier!!, pack.name!!)
                    },
                    leadingContent = {
                        StickerImage(
                            imagePath = StickerPackLoader.getStickerAssetUri(
                                pack.identifier,
                                pack.trayImageFile
                            )
                        )
                    },
                    headlineContent = {
                        Text(
                            text = pack.name!!
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${pack.stickers.size} Stickers"
                        )
                    },
                )
            }
        }
    }
}

