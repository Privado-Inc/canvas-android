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

package com.instructure.parentapp.features.alerts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.analytics.SCREEN_VIEW_ALERTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.showSnackbar
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_ALERTS)
@AndroidEntryPoint
class AlertsFragment : BaseCanvasFragment() {

    @Inject
    lateinit var navigation: Navigation

    private val viewModel: AlertsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                AlertsScreen(uiState = uiState, actionHandler = viewModel::handleAction, lazyListState = viewModel.lazyListState)
            }
        }
    }

    private fun handleAction(action: AlertsViewModelAction) {
        when (action) {
            is AlertsViewModelAction.NavigateToRoute -> {
                action.route?.let {
                    navigation.navigate(activity, it)
                }
            }

            is AlertsViewModelAction.NavigateToGlobalAnnouncement -> {
                navigation.navigate(activity, navigation.globalAnnouncementRoute(action.alertId))
            }

            is AlertsViewModelAction.ShowSnackbar -> {
                view?.showSnackbar(
                    message = action.message,
                    actionTextRes = action.action,
                    actionCallback = action.actionCallback
                )
            }
        }
    }
}