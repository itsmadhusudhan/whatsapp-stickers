package com.devwhispers.whatsapp_stickers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.devwhispers.whatsapp_stickers.ui.theme.WhatsappstickersTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsappstickersTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StickersList(
                        launcher = { intent: Intent ->
//                            startForResult.launch(intent)
                            startActivityForResult(intent, 200)
                        }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            println("Result Code: $resultCode")
            if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    val validationError = data.getStringExtra("validation_error")
                    if (validationError != null) {
                      println("Validation failed:$validationError")
                    }
                } else {
                    println("User canceled the operation")
                }
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data

            // Handle the Intent
            //do stuff here
        }
    }
}

const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

@Composable
fun StickersList(launcher: (Intent) -> Unit ) {
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
        identifier: String,
        stickerPackName: String,
        whatsappPackageName: String
    ) {
        println("intent was called!! with $identifier and $stickerPackName for $whatsappPackageName")

        try {
            val intent = createIntentToAddStickerPack(identifier, stickerPackName)
            intent.setPackage(whatsappPackageName)

            context.startActivity(intent)

//            launcher(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.add_pack_fail_prompt_update_whatsapp,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    fun addStickerPackToWhatsApp(identifier: String, stickerPackName: String) {
        try {
            //if neither WhatsApp Consumer or WhatsApp Business is installed, then tell user to install the apps.
            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(context.packageManager) && !WhitelistCheck.isWhatsAppSmbAppInstalled(
                    context.packageManager
                )
            ) {
                Toast.makeText(
                    context,
                    R.string.add_pack_fail_prompt_update_whatsapp,
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            println("Sticker Pack Identifier: $identifier")

            val stickerPackWhitelistedInWhatsAppConsumer =
                WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(context, identifier)
            val stickerPackWhitelistedInWhatsAppSmb =
                WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(context, identifier)


            println("stickerPackWhitelistedInWhatsAppConsumer: $stickerPackWhitelistedInWhatsAppConsumer")
            println("stickerPackWhitelistedInWhatsAppSmb: $stickerPackWhitelistedInWhatsAppSmb")

            if (!stickerPackWhitelistedInWhatsAppConsumer) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToSpecificPackage(
                    identifier,
                    stickerPackName,
                    WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                R.string.add_pack_fail_prompt_update_whatsapp,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    LaunchedEffect(Unit) {
        val _stickerPacks = StickerPackLoader.fetchStickerPacks(context)

        stickerPacks.addAll(_stickerPacks)
    }


    Column {
        Text(text = "Stickers List")

        Button(onClick = {
            val pack = stickerPacks[0]
            addStickerPackToWhatsApp(pack.identifier!!, pack.name!!)
        }) {
            Text(text = "Add Stickers to WhatsApp")
        }
    }
}

