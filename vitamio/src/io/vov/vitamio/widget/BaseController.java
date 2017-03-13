package io.vov.vitamio.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Locale;

import io.vov.vitamio.R;
import io.vov.vitamio.interfaces.MediaPlayerControl;
import io.vov.vitamio.interfaces.OnSeekBarChangeListener;

/**
 * Created by bintou on 16/3/15.
 */
public class BaseController extends FrameLayout {

    public interface HiddenChainListener {
        void hiddenChain();

        void showChain();
    }

    protected View mVideoJjRooView;
    protected Activity mContext;
    private LayoutParams lp;
    protected View mRootView;
    private HiddenChainListener mHiddenChainListener;
    protected OnSeekBarChangeListener mBarChangeListener;

    public BaseController(Context var1) {
        super(var1);
        this.initMediaController(var1);
    }

    public BaseController(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.initMediaController(var1);
    }

    public BaseController(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.initMediaController(var1);
    }


    private void initMediaController(Context var1) {
        this.mContext = (Activity) var1;
        this.mVideoJjRooView = this.inflateLayout();
        this.mRootView = this.mVideoJjRooView.findViewById(R.id.sdk_curview_root);
    }

    private View inflateLayout() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.venvy_video_mediacontroller_sdk, null);
    }

    public void setAnchorView(ViewGroup var1) {
        var1.removeView(this.mVideoJjRooView);
        if (!this.mContext.isFinishing()) {
            this.lp = new LayoutParams(-1, -1);
            this.mVideoJjRooView.setFocusable(true);
            this.mVideoJjRooView.setFocusableInTouchMode(true);
            this.mVideoJjRooView.setClickable(true);
            var1.addView(this.mVideoJjRooView, this.lp);
        }

    }

    @SuppressLint({"InlinedApi"})
    @TargetApi(11)
    public void showSystemUi(boolean var1) {
        if (Build.VERSION.SDK_INT >= 11) {
            int var2 = var1 ? 0 : 3;
            this.mRootView.setSystemUiVisibility(var2);
        }

    }

    public void setFileName(String var1) {
    }

    public void setMediaPlayer(MediaPlayerControl var1) {
    }

    public void show() {
        this.show(5000);
    }

    public void show(int var1) {
        if (this.mHiddenChainListener != null) {
            this.mHiddenChainListener.showChain();
        }

    }

    public void hide() {
        if (this.mHiddenChainListener != null) {
            this.mHiddenChainListener.hiddenChain();
        }

    }

    public void setHiddenChainListener(HiddenChainListener var1) {
        this.mHiddenChainListener = var1;
    }

    public void release() {
        this.initMediaController(this.mContext);
    }

    public void setVideoCofLand() {
    }

    public void setVideoCofPort() {
    }

    public void onVideoCompletion() {
    }

    public void setMediaControllerResetState() {
    }

    public void setBarChangeListener(OnSeekBarChangeListener var1) {
        this.mBarChangeListener = var1;
    }


}
