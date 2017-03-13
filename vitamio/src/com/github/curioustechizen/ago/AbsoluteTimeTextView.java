package com.github.curioustechizen.ago;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Locale;

public class AbsoluteTimeTextView extends TextView {

    private Handler mHandler = new Handler();
    private UpdateTimeRunnable mUpdateTimeTask;

    public AbsoluteTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        startShow();
    }

    public AbsoluteTimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        startShow();
    }

    public void startShow() {
        try {
            mUpdateTimeTask = new UpdateTimeRunnable();
            mUpdateTimeTask.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UpdateTimeRunnable implements Runnable {

        @Override
        public void run() {
            try {
                long interval = DateUtils.MINUTE_IN_MILLIS;
                showTime();
                mHandler.postDelayed(this, interval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Time showTime() {
        Time time = new Time();
        time.setToNow();
        int hour = time.hour;
        int min = time.minute;
        String strTime = String.format(Locale.CHINA, "%02d:%02d", hour,
                min);
        setText(strTime);
        return time;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (mHandler != null && mUpdateTimeTask != null)
                mHandler.removeCallbacksAndMessages(mUpdateTimeTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
