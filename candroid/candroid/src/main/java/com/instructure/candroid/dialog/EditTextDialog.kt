package com.instructure.candroid.dialog

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
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.View
import android.view.WindowManager
import com.instructure.candroid.R
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.dismissExisting
import kotlinx.android.synthetic.main.dialog_edit_text.view.*
import kotlin.properties.Delegates

class EditTextDialog : AppCompatDialogFragment() {

    private var mEditTextCallback: (String) -> Unit by Delegates.notNull()
    private var mDefaultText by StringArg()
    private var mTitle by StringArg()

    init {
        retainInstance = true
    }

    companion object {
        @JvmStatic
        fun getInstance(manager: FragmentManager, title: String, defaultText: String, callback: (String) -> Unit) : EditTextDialog {
            manager.dismissExisting<EditTextDialog>()
            return EditTextDialog().apply {
                mEditTextCallback = callback
                mDefaultText = defaultText
                mTitle = title
            }
        }

        @JvmStatic
        fun show(manager: FragmentManager, title: String, defaultText: String, callback: (String) -> Unit) {
            getInstance(manager, title, defaultText, callback).show(manager, EditTextDialog::class.java.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(activity, R.layout.dialog_edit_text, null).apply {
            ViewStyler.themeEditText(context, textInput, ThemePrefs.brandColor)
            textInput.setText(mDefaultText)
            textInput.selectAll()
        }

        val dialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(mTitle)
                .setView(view)
                .setPositiveButton(activity.getString(android.R.string.ok).toUpperCase(), { _, _ ->
                    mEditTextCallback(view.textInput.text.toString())
                })
                .setNegativeButton(activity.getString(android.R.string.cancel).toUpperCase(), null)
                .create()

        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
