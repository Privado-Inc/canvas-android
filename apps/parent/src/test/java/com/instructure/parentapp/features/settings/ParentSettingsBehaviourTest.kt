/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.settings

import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.features.settings.SettingsItem
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.TestSelectStudentHolder
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class ParentSettingsBehaviourTest {

    private val selectedStudentFlow = MutableStateFlow<User?>(null)
    private val selectedStudentHolder = TestSelectStudentHolder(selectedStudentFlow)

    @Test
    fun `Settings behaviour has the correct items`() {
        val settingsBehaviour = ParentSettingsBehaviour(selectedStudentHolder)

        val expected = mapOf(
            R.string.preferences to listOf(SettingsItem.APP_THEME),
            R.string.inboxSettingsTitle to listOf(SettingsItem.INBOX_SIGNATURE),
            R.string.legal to listOf(
                SettingsItem.ABOUT,
                SettingsItem.LEGAL
            )
        )

        assertEquals(expected, settingsBehaviour.settingsItems)
    }
}