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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.pandautils.utils.dismissExisting
import kotlinx.android.synthetic.main.dialog_create_folder.view.*
import kotlin.properties.Delegates

class CreateFolderDialog : AppCompatDialogFragment() {
    init {
        retainInstance = true
    }

    private var mCreateFolderCallback: (String) -> Unit by Delegates.notNull()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(activity, R.layout.dialog_create_folder, null)
        val folderNameEditText = view.newFolderName
        ViewStyler.themeEditText(context, folderNameEditText, ThemePrefs.brandColor)

        val nameDialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(getString(R.string.createFolder))
                .setView(view)
                .setPositiveButton(activity.getString(android.R.string.ok), { _, _ ->
                    mCreateFolderCallback(folderNameEditText.text.toString())
                })
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .create()

        // Adjust the dialog to the top so keyboard does not cover it up, issue happens on tablets in landscape
        val params = nameDialog.window.attributes
        params.gravity = Gravity.CENTER or Gravity.TOP
        params.y = 120
        nameDialog.window.attributes = params
        nameDialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        nameDialog.setOnShowListener {
            nameDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            nameDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }
        return nameDialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun show(fm: FragmentManager, callback: (String) -> Unit) {
            fm.dismissExisting<CreateFolderDialog>()
            CreateFolderDialog().apply {
                mCreateFolderCallback = callback
            }.show(fm, CreateFolderDialog::class.java.simpleName)
        }
    }
}
