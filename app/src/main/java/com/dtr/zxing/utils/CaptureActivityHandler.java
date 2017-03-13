/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtr.zxing.utils;

import android.app.Activity;
import android.content.Intent;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.dtr.zxing.activity.CaptureActivity;
import com.dtr.zxing.camera.CameraManager;
import com.dtr.zxing.decode.DecodeThread;
import com.google.zxing.Result;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ToastUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class CaptureActivityHandler extends Handler {

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private final CameraManager cameraManager;
    private State state;

    private Map<String, String> postMap;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(CaptureActivity activity, CameraManager cameraManager, int decodeMode) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeMode);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }


    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case GlobalConstant.VIDEO_URL_RECEIVE:
                try {
                    if (postMap != null) {
                        if (message.obj instanceof String) {
                            String link = (String) message.obj;
                            LunaLog.d("link: " + link);
                            String linkencry = Base64.encodeToString(link.getBytes(), Base64.DEFAULT);
                            postMap.put("link", linkencry);
                            VideoCatchManager.playVideoInWeb(link, postMap);
                        } else if (message.obj instanceof JSONObject) {
                            //对爱奇艺做专门的处理
                            JSONObject dataJo = (JSONObject) message.obj;
                            JSONObject mp4Jo = new JSONObject();
                            String link = postMap.get("link");
                            if (!StringUtils.isBlank(link)) {
                                link = new String(Base64.decode(link, Base64.DEFAULT));
                                mp4Jo.putOpt("normal", URLEncoder.encode(link));
                                dataJo.putOpt("mp4", mp4Jo);
                            }
                            postMap.put("data", dataJo.toString());
                            VideoCatchManager.playVideoInWeb(null, postMap);
                        } else {
                            String link = postMap.get("link");
                            if (!StringUtils.isBlank(link)) {
                                VideoCatchManager.playVideoInWeb(link, postMap);
                            }
                        }

                        if (message != null && !StringUtils.isBlank(message.obj.toString())) {
                            int times = PreferencesUtils.getInt(activity, GlobalConstant.SP_CAPTURE_TIMES, 0);
                            times++;
                            LunaLog.d("times: " + times);
                            PreferencesUtils.putInt(activity, GlobalConstant.SP_CAPTURE_TIMES, times);
                        }
                    }
                    ToastUtils.show(activity, "扫码成功，正在同步中...");
                } catch (Exception e) {
                    LunaLog.e(e);
                }
                activity.finish();
                break;
            case R.id.restart_preview:
                restartPreviewAndDecode();
                break;
            case R.id.decode_succeeded:
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                activity.handleDecode((Result) message.obj, bundle);
                break;
            case R.id.decode_failed:
                // We're decoding as fast as possible, so when one decode fails,
                // start another.
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                break;
            case R.id.return_scan_result:
                activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                activity.finish();
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
    }

    public void setPostMap(Map<String, String> postMap) {
        this.postMap = postMap;
    }
}
