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
package com.instructure.canvasapi2.models.post_models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.APIHelper
import java.util.*

class DiscussionTopicPostBody {
    var title: String? = null

    var message: String? = null

    var published: Boolean? = null

    var subscribed: Boolean? = null

    @SerializedName("require_initial_post")
    var requireInitialPost: Boolean? = null

    @SerializedName("discussion_type")
    var discussionType: String? = null

    @SerializedName("delayed_post_at")
    var delayedPostAt: String? = null

    @SerializedName("lock_at")
    var lockAt: Date? = null

    var assignment: AssignmentPostBody? = null

    @SerializedName("remove_attachment")
    var removeAttachment: String? = null

    companion object {
        @JvmStatic
        fun fromAnnouncement(announcement: DiscussionTopicHeader, shouldRemoveAttachment: Boolean) = DiscussionTopicPostBody().apply {
            title = announcement.title
            message = announcement.message
            requireInitialPost = announcement.isRequireInitialPost
            delayedPostAt = announcement.delayedPostAt?.let { APIHelper.dateToString(it) } ?: ""
            if (shouldRemoveAttachment) removeAttachment = ""
        }
    }
}
