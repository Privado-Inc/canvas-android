/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.factory

import com.instructure.teacher.presenters.ViewPdfFragmentPresenter
import com.instructure.teacher.viewinterface.ViewPdfFragmentView
import com.instructure.pandautils.blueprint.PresenterFactory

class ViewPdfFragmentPresenterFactory(val pdfUrl : String) : PresenterFactory<ViewPdfFragmentView, ViewPdfFragmentPresenter> {
    override fun create() = ViewPdfFragmentPresenter(pdfUrl)
}
