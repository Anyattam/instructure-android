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
package com.instructure.teacher.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.teacher.R
import com.nineoldandroids.animation.AnimatorInflater
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.adapter_assignment_group_header.view.*

class AssignmentGroupHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var mIsExpanded = false

    fun bind(assignmentGroup: AssignmentGroup,
             isExpanded: Boolean,
             callback: (AssignmentGroup) -> Unit) = with(itemView) {

        mIsExpanded = isExpanded

        groupName.text = assignmentGroup.name

        assignmentGroupContainer.setOnClickListener {
            val animationType = if (mIsExpanded) R.animator.rotation_from_0_to_neg90 else R.animator.rotation_from_neg90_to_0
            mIsExpanded = !mIsExpanded
            val flipAnimator = AnimatorInflater.loadAnimator(context, animationType) as ObjectAnimator
            flipAnimator.target = collapseIcon
            flipAnimator.duration = 200
            flipAnimator.start()
            callback(assignmentGroup)
        }
    }

    companion object {
        val holderResId = R.layout.adapter_assignment_group_header
    }
}

