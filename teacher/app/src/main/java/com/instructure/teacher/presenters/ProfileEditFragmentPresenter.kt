/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.presenters

import android.net.Uri
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.viewinterface.ProfileEditFragmentView
import instructure.androidblueprint.FragmentPresenter
import kotlinx.coroutines.experimental.Job
import retrofit2.Call
import retrofit2.Response

class ProfileEditFragmentPresenter : FragmentPresenter<ProfileEditFragmentView>() {

    var capturedImageUri: Uri? = null
    private var mApiCalls: Job? = null

    override fun refresh(forceNetwork: Boolean) {
        loadData(forceNetwork)
    }

    override fun loadData(forceNetwork: Boolean) {
        UserManager.getSelfWithPermissions(forceNetwork, object: StatusCallback<User>() {
            override fun onResponse(response: Response<User>?, linkHeaders: LinkHeaders?, type: ApiType?) {
                if(response != null && response.body() != null) {
                    var permissions = ApiPrefs.user?.permissions
                    if(permissions == null) {
                        permissions = CanvasContextPermission()
                    }

                    permissions.canUpdateAvatar = response.body().canUpdateAvatar()
                    permissions.canUpdateName = response.body().canUpdateName()

                    ApiPrefs.user?.permissions = permissions
                }
                viewCallback?.readyToLoadUI(ApiPrefs.user)
            }

            override fun onFail(response: Call<User>?, error: Throwable?) {
                super.onFail(response, error)
                viewCallback?.readyToLoadUI(null)
            }
        })
    }

    //Do input validation prior to calling saveEdit
    fun  saveChanges(name: String, bio: String) {
        if (name.isNullOrBlank()) {
            viewCallback?.errorSavingProfile()
            return
        }
        publishUserData(name, bio)
    }

    private fun publishUserData(name: String, bio: String) {
        //Note: if the API ever allows us to update the bio we'll need to update the api call below to include it
        val user = ApiPrefs.user
        if(user != null) {
            if (user.canUpdateName()) {
                UserManager.updateUserShortName(name, object : StatusCallback<User>() {
                    override fun onResponse(response: Response<User>?, linkHeaders: LinkHeaders?, type: ApiType?) {
                        if (response != null && response.body() != null) {
                            val updatedUser = ApiPrefs.user
                            updatedUser?.shortName = response.body().shortName
                            //For some crazy reason the api returns the changed email as email, which actually makes sense....
                            //But for some other crazy reason when a user is fetched from canvas the email is now primary email.
                            updatedUser?.primaryEmail = response.body().email
                            ApiPrefs.user = updatedUser
                            viewCallback?.successSavingProfile()
                        } else {
                            viewCallback?.errorSavingProfile()
                        }
                    }

                    override fun onFail(response: Call<User>?, error: Throwable?) {
                        viewCallback?.errorSavingProfile()
                    }
                })
            }
        }
    }
}