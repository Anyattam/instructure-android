/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.candroid.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.app.Fragment
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.instructure.annotations.PdfSubmissionView
import com.instructure.candroid.AnnotationComments.AnnotationCommentListFragment
import com.instructure.candroid.R
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import kotlinx.android.synthetic.main.view_student_submission.view.*
import kotlinx.coroutines.experimental.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@SuppressLint("ViewConstructor")
class StudentSubmissionView(
        context: Context,
        private val course: Course,
        private val studentSubmission: GradeableStudentSubmission,
        private val assignment: Assignment,
        private val attachmentPosition: Int = 0
) : PdfSubmissionView(context), AnnotationManager.OnAnnotationCreationModeChangeListener, AnnotationManager.OnAnnotationEditingModeChangeListener {

    private val assignee: Assignee get() = studentSubmission.assignee
    private val rootSubmission: Submission? get() = studentSubmission.submission
    private var initJob: Job? = null
    private var deleteJob: Job? = null

    override val annotationToolbarLayout: ToolbarCoordinatorLayout
        get() = findViewById(R.id.annotationToolbarLayout)
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
        get() = findViewById(R.id.inspectorCoordinatorLayout)
    override val commentsButton: ImageView
        get() = findViewById(R.id.commentsButton)
    override val loadingContainer: FrameLayout
        get() = findViewById(R.id.loadingContainer)
    override val progressBar: ProgressiveCanvasLoadingView
        get() = findViewById(R.id.progressBar)
    override val progressColor: Int
        get() = R.color.login_studentAppTheme

    private fun getAttachmentPosition(): Int {
        return rootSubmission?.attachments?.size?.let {
            if ((it - 1) >= attachmentPosition) attachmentPosition
            else 0
        } ?: 0
    }


    override fun disableViewPager() {}
    override fun enableViewPager() {}
    override fun setIsCurrentlyAnnotating(boolean: Boolean) {}

    override fun showAnnotationComments(commentList: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession) {
        val bundle = AnnotationCommentListFragment.makeBundle(commentList, headAnnotationId, docSession, assignee.id)
        val fragment = AnnotationCommentListFragment.newInstance(bundle)
        if (isAttachedToWindow) {
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.annotationCommentsContainer, fragment, fragment::class.java.name)
            ft.addToBackStack(fragment::class.java.name)
            ft.commit()
        }
    }

    override fun showFileError() {
        loadingView.setGone()
        retryLoadingContainer.setVisible()
        retryLoadingButton.onClick {
            setLoading(true)
            obtainSubmissionData()
        }
    }

    override fun showNoInternetDialog() {
        NoInternetConnectionDialog.show(supportFragmentManager)
    }

    init {
        View.inflate(context, R.layout.view_student_submission, this)

        setLoading(true)
    }

    private fun setLoading(isLoading: Boolean) {
        loadingView?.setVisible(isLoading)
        contentRoot?.setVisible(!isLoading)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupToolbar()
        obtainSubmissionData()
    }

    private fun setupToolbar() {
        studentSubmissionToolbar.setupAsBackButton {
            (context as? Activity)?.finish()
        }

        titleTextView.text = rootSubmission?.attachments?.get(getAttachmentPosition())?.displayName ?: ""
        if (rootSubmission?.submittedAt != null) subtitleTextView.text = DateHelper.getDateTimeString(context, rootSubmission?.submittedAt) ?: ""
        ViewStyler.colorToolbarIconsAndText(context as Activity, studentSubmissionToolbar, Color.BLACK)
        ViewStyler.setStatusBarLight(context as Activity)
        ViewStyler.setToolbarElevationSmall(context, studentSubmissionToolbar)
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun obtainSubmissionData() {
        initJob = tryWeave {
            if (!studentSubmission.isCached) {
                studentSubmission.submission = awaitApi { SubmissionManager.getSingleSubmission(course.id, assignment.id, studentSubmission.assigneeId, it, true) }
                studentSubmission.isCached = true
            }
            setup()
        } catch {
            loadingView.setGone()
            retryLoadingContainer.setVisible()
            retryLoadingButton.onClick {
                setLoading(true)
                obtainSubmissionData()
            }
        }
    }

    fun setup() {
        setupToolbar()
        setSubmission(rootSubmission)
        //we must set up the sliding panel prior to registering to the event
        EventBus.getDefault().register(this)
        setLoading(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        initJob?.cancel()
        EventBus.getDefault().unregister(this)
    }

    private fun setSubmission(submission: Submission?) {
        submission?.attachments?.get(getAttachmentPosition())?.let {
            if (it.contentType == "application/pdf" || it.previewUrl?.contains("canvadoc") == true) {
                if (it.previewUrl?.contains("canvadoc") == true) {
                    handlePdfContent(it.previewUrl ?: "")
                } else {
                    handlePdfContent(it.url ?: "")
                }
            }
        }
    }

    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (isAttachedToWindow) supportFragmentManager.beginTransaction().replace(content.id, fragment).commitNowAllowingStateLoss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentAdded(event: AnnotationCommentAdded) {
        if (event.assigneeId == assignee.id) {
            //add the comment to the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.add(event.annotation)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentEdited(event: AnnotationCommentEdited) {
        if (event.assigneeId == assignee.id) {
                //update the annotation in the hashmap
                commentRepliesHashMap[event.annotation.inReplyTo]?.
                        find { it.annotationId == event.annotation.annotationId }?.contents = event.annotation.contents
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleted(event: AnnotationCommentDeleted) {
        if (event.assigneeId == assignee.id) {
            if (event.isHeadAnnotation) {
                //we need to delete the entire list of comments from the hashmap
                commentRepliesHashMap.remove(event.annotation.inReplyTo)
            } else {
                //otherwise just remove the comment
                commentRepliesHashMap[event.annotation.inReplyTo]?.remove(event.annotation)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleteAcknowledged(event: AnnotationCommentDeleteAcknowledged) {
        if (event.assigneeId == assignee.id) {
            deleteJob = tryWeave {
                for(annotation in event.annotationList) {
                    awaitApi<ResponseBody> { CanvaDocsManager.deleteAnnotation(docSession.apiValues.sessionId, annotation.annotationId, docSession.apiValues.canvaDocsDomain, it) }
                    commentRepliesHashMap[annotation.inReplyTo]?.remove(annotation)
                }
            } catch {
                Logger.d("There was an error acknowledging the delete!")
            }
        }
    }

    class AnnotationCommentAdded(val annotation: CanvaDocAnnotation, val assigneeId: Long)
    class AnnotationCommentEdited(val annotation: CanvaDocAnnotation, val assigneeId: Long)
    class AnnotationCommentDeleted(val annotation: CanvaDocAnnotation, val isHeadAnnotation: Boolean, val assigneeId: Long)
    class AnnotationCommentDeleteAcknowledged(val annotationList: List<CanvaDocAnnotation>, val assigneeId: Long)
}