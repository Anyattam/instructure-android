/*
 *  Copyright 2013 The Chromium Authors. All rights reserved.
 *      Use of this source code is governed by a BSD-style license that can be
 *      found in the LICENSE file.
 *
 */

package com.instructure.pandautils.video;

import android.view.View;

/**
 *  Main callback class used by ContentVideoView.
 *
 *  This contains the superset of callbacks that must be implemented by the embedder.
 *
 *  onShowCustomView and onDestoryContentVideoView must be implemented,
 *  getVideoLoadingProgressView() is optional, and may return null if not required.
 *
 *  The implementer is responsible for displaying the Android view when
 *  {@link #onShowCustomView(View)} is called.
 */
public interface ContentVideoViewClient {
    /**
     * Called when the video view is ready to be shown. Must be implemented.
     * @param view The view to show.
     */
    void onShowCustomView(View view);

    /**
     * Called when it's time to destroy the video view. Must be implemented.
     */
    void onDestroyContentVideoView();

    boolean isFullscreen();

    /**
     * Allows the embedder to replace the view indicating that the video is loading.
     * If null is returned, the default video loading view is used.
     */
    View getVideoLoadingProgressView();

    /**
     * Allows the embedder to replace the default playback controls by returning a custom
     * implementation. If null is returned, the default controls are used.
     */
    //public ContentVideoViewControls createControls();
}
