/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:JvmName("MasqueradeUI")

package com.instructure.pandautils.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.R
import com.instructure.pandautils.base.AppConfigProvider
import com.instructure.pandautils.base.BaseCanvasBottomSheetDialogFragment
import com.instructure.pandautils.databinding.LayoutMasqueradeNotificationBinding

/**
 * Adds a masquerade UI to this Activity if the user is currently masquerading.
 */
@Suppress("unused") // Added at compile time via MasqueradeUIInjector
fun Activity.showMasqueradeNotification() {
    window.showMasqueradeNotification()
}

/**
 * Adds a masquerade UI if the user is currently masquerading AND this DialogFragment is being displayed as a dialog.
 */
@Suppress("unused") // Added at compile time via MasqueradeUIInjector
fun DialogFragment.showMasqueradeNotification() {
    if (this is BaseCanvasBottomSheetDialogFragment && !isFullScreen()) return

    (dialog?.window?.decorView?.layoutParams as? WindowManager.LayoutParams)?.let {
        if (it.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            dialog?.window?.showMasqueradeNotification()
        }
    }
}

private fun Window.showMasqueradeNotification() {
    if (!ApiPrefs.isMasquerading) return

    val startingActivityClass = AppConfigProvider.appConfig?.startingActivityClass

    decorView.rootView?.let { rootView ->
        if (findViewById<View>(R.id.masqueradeUINotificationContainer) != null) return
        // Add border
        val borderDrawable = ContextCompat.getDrawable(context, R.drawable.masquerade_outline)
        @Suppress("CascadeIf")
        rootView.foreground = borderDrawable

        // Add notification view
        rootView.findViewById<View>(android.R.id.content).lastAncestorOrNull<LinearLayout>()?.let {
            val binding = LayoutMasqueradeNotificationBinding.inflate(LayoutInflater.from(context), it, false)
            binding.masqueradeLabel.text = Pronouns.resource(
                context,
                R.string.masqueradingAs,
                ApiPrefs.user?.pronouns,
                Pronouns.span(ApiPrefs.user?.name, ApiPrefs.user?.pronouns)
            )
            binding.cancelMasqueradeButton.onClick {
                AlertDialog.Builder(context)
                    .setTitle(R.string.stopActingAsTitle)
                    .setMessage(context.getString(R.string.stopActingAsMessage, ApiPrefs.user?.name))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        MasqueradeHelper.stopMasquerading(startingActivityClass)
                    }
                    .show()
            }
            it.addView(binding.root, 0)
        }
    }
}


