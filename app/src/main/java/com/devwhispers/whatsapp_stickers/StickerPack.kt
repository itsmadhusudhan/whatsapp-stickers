package com.devwhispers.whatsapp_stickers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

@Parcelize
internal class StickerPack(
    val identifier: String?,
    val name: String?,
    val publisher: String?,
    val trayImageFile: String?,
    val publisherEmail: String?,
    val publisherWebsite: String?,
    val privacyPolicyWebsite: String?,
    val licenseAgreementWebsite: String?,
    val imageDataVersion: String?,
    val avoidCache: Boolean,
    val animatedStickerPack: Boolean,
    var androidPlayStoreLink: String? = null,
    var iosAppStoreLink: String? = null,
    var stickers: List<Sticker> = listOf()
) : Parcelable
