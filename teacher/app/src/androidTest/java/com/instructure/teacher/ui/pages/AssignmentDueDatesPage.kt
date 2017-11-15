/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.PageWithIdAssert

@Suppress("unused")
class AssignmentDueDatesPage : BasePage(), PageAssert by PageWithIdAssert(R.id.dueDatesPage) {

    private val backButton by OnViewWithContentDescription(android.support.v7.appcompat.R.string.abc_action_bar_up_description)
    private val titleView by OnViewWithText(R.string.page_title_due_dates)
    private val editButton by OnViewWithId(R.id.menu_edit)
    private val recyclerView by WaitForViewWithId(R.id.recyclerView)

    fun openEditPage() = editButton.click()

    fun assertDisplaysNoDueDate() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        assertNoAvailabilityDates()
        onViewWithId(R.id.dueForTextView).assertDisplayed().assertHasText(R.string.everyone)
        onViewWithId(R.id.dueDateTextView).assertDisplayed().assertHasText(R.string.no_due_date)
    }

    fun assertDisplaysSingleDueDate() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        assertNoAvailabilityDates()
        onViewWithId(R.id.dueForTextView).assertDisplayed().assertHasText(R.string.everyone)
        onViewWithId(R.id.dueDateTextView).assertDisplayed().assertNotHasText(R.string.no_due_date)
    }

    fun assertDisplaysAvailabilityDates() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        onViewWithId(R.id.availableFromTextView).assertDisplayed().assertNotHasText(R.string.no_date_filler)
        onViewWithId(R.id.availableToTextView).assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    private fun assertLabelsDisplayedOnce() {
        onViewWithText(R.string.details_due_for_label).assertDisplayed()
        onViewWithText(R.string.details_available_from_label).assertDisplayed()
        onViewWithText(R.string.details_available_to_label).assertDisplayed()
    }

    private fun assertNoAvailabilityDates() {
        onViewWithId(R.id.availableFromTextView).assertDisplayed().assertHasText(R.string.no_date_filler)
        onViewWithId(R.id.availableToTextView).assertDisplayed().assertHasText(R.string.no_date_filler)
    }

}