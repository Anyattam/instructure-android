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

package com.instructure.teacher.activities

import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.instructure.canvasapi2.utils.Logger
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled
import com.instructure.teacher.R
import com.instructure.teacher.dialog.HelpDialogStyled

open class BaseAppCompatActivity : AppCompatActivity(), ZendeskDialogStyled.ZendeskDialogResultListener {


    override fun onTicketPost() {
        dismissHelpDialog()
        Toast.makeText(applicationContext, R.string.zendesk_feedbackThankyou, Toast.LENGTH_LONG).show()
    }

    override fun onTicketError() {
        dismissHelpDialog()
        Toast.makeText(applicationContext, R.string.errorOccurred, Toast.LENGTH_LONG).show()
    }

    private fun dismissHelpDialog() {
        val fragment = supportFragmentManager.findFragmentByTag(HelpDialogStyled.TAG)
        if (fragment is HelpDialogStyled) {
            try {
                fragment.dismiss()
            } catch (e: IllegalStateException) {
                Logger.e("Committing a transaction after activities saved state was called: " + e)
            }
        }
    }
}