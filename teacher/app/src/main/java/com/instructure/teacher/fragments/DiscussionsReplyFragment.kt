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
package com.instructure.teacher.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.dialog.FileUploadDialog
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.DiscussionEntryEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.DiscussionsReplyFactory
import com.instructure.teacher.presenters.DiscussionsReplyPresenter
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_EMPTY
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_FAILED_TO_SEND
import com.instructure.teacher.presenters.DiscussionsReplyPresenter.Companion.REASON_MESSAGE_IN_PROGRESS
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.AttachmentView
import com.instructure.teacher.viewinterface.DiscussionsReplyView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.fragment_discussions_reply.*

class DiscussionsReplyFragment : BasePresenterFragment<DiscussionsReplyPresenter, DiscussionsReplyView>(), DiscussionsReplyView {

    private var mCanvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))
    private var mDiscussionTopicHeaderId: Long by LongArg(default = 0L) //The topic the discussion belongs too
    private var mDiscussionEntryId: Long by LongArg(default = 0L) //The future parent of the discussion entry we are creating

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}

    override fun layoutResId(): Int {
        return R.layout.fragment_discussions_reply
    }

    override fun getPresenterFactory(): PresenterFactory<DiscussionsReplyPresenter> =
            DiscussionsReplyFactory(mCanvasContext, mDiscussionTopicHeaderId, mDiscussionEntryId)

    override fun onPresenterPrepared(presenter: DiscussionsReplyPresenter?) {}

    override fun onReadySetGo(presenter: DiscussionsReplyPresenter?) {
        rceTextEditor.setHint(R.string.rce_empty_message)
    }

    override fun messageSuccess(entry: DiscussionEntry) {
        DiscussionEntryEvent(entry).post()
        activity.onBackPressed()
        toast(R.string.discussion_sent_success)
    }

    override fun messageFailure(reason: Int) {
        when (reason) {
            REASON_MESSAGE_IN_PROGRESS -> { Logger.e("User tried to send message multiple times in a row.") }
            REASON_MESSAGE_EMPTY -> {
                Logger.e("User tried to send message an empty message.")
                toast(R.string.discussion_sent_empty)
            }
            REASON_MESSAGE_FAILED_TO_SEND -> {
                Logger.e("Message failed to send for some reason.")
                toast(R.string.discussion_sent_failure)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.reply)
        toolbar.setupCloseButton(this)
        toolbar.setupMenu(R.menu.menu_discussion_reply, menuItemCallback)
        ViewStyler.themeToolbarBottomSheet(activity, isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(context, toolbar)
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_send -> {
                if(APIHelper.hasNetworkConnection()) {
                    presenter.sendMessage(rceTextEditor.html)
                } else {
                    NoInternetConnectionDialog.show(fragmentManager)
                }
            }
            R.id.menu_attachment -> {
                if(APIHelper.hasNetworkConnection()) {
                    val attachments = ArrayList<FileSubmitObject>()
                    if (presenter.getAttachment() != null) {
                        attachments.add(presenter.getAttachment()!!)
                    }
                    val bundle = FileUploadDialog.createDiscussionsBundle(ApiPrefs.user!!.shortName, attachments)
                    val fileUploadDialog = FileUploadDialog.newInstanceSingleSelect(activity.supportFragmentManager, bundle, mFileSelectedListener)
                    fileUploadDialog.show(activity.supportFragmentManager, FileUploadDialog::class.java.simpleName)
                } else {
                    NoInternetConnectionDialog.show(fragmentManager)
                }
            }
        }
    }

    private val mFileSelectedListener = FileUploadDialog.OnSingleFileSelectedListener { file ->
        if(file != null) {
            presenter?.setAttachment(file)
            attachments.setAttachment(file.toAttachment()) { action, attachment ->
                if (action == AttachmentView.AttachmentAction.REMOVE) {
                    presenter?.setAttachment(null)
                }
            }
        } else {
            presenter?.setAttachment(null)
            attachments.clearAttachmentViews()
        }
    }

    companion object {
        val DISCUSSION_TOPIC_HEADER_ID = "DISCUSSION_TOPIC_HEADER_ID"
        val DISCUSSION_ENTRY_ID = "DISCUSSION_ENTRY_ID"
        val IS_ANNOUNCEMENT = "IS_ANNOUNCEMENT"

        @JvmStatic
        fun makeBundle(
                discussionTopicHeaderId: Long,
                discussionEntryId: Long,
                isAnnouncement: Boolean): Bundle = Bundle().apply {

            putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
            putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
            putBoolean(IS_ANNOUNCEMENT, isAnnouncement)
        }

        @JvmStatic
        fun newInstance(canvasContext: CanvasContext, args: Bundle) = DiscussionsReplyFragment().apply {
            mDiscussionTopicHeaderId = args.getLong(DISCUSSION_TOPIC_HEADER_ID)
            mDiscussionEntryId = args.getLong(DISCUSSION_ENTRY_ID)
            mCanvasContext = canvasContext
        }
    }
}
