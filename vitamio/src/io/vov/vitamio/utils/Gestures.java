package io.vov.vitamio.utils;

import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import io.vov.vitamio.interfaces.MediaContollerTouchListener;

/**
 * Created by bintou on 16/3/15.
 */
public class Gestures {
    public static final int SCALE_STATE_BEGIN = 0;
    public static final int SCALE_STATE_SCALEING = 1;
    public static final int SCALE_STATE_END = 2;
    private boolean mGestureEnabled;
    private GestureDetectorCompat mDoubleTapGestureDetector;
    private GestureDetectorCompat mTapGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private Activity mContext;
    private boolean firstScroll = false;
    private int GESTURE_FLAG = 0;
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final float STEP_PROGRESS = 2.0F;
    private MediaContollerTouchListener mListener;

    public Gestures(Activity var1) {
        this.mContext = var1;
        this.mDoubleTapGestureDetector = new GestureDetectorCompat(this.mContext, new DoubleTapGestureDetectorListener());
        this.mTapGestureDetector = new GestureDetectorCompat(this.mContext, new TapGestureDetectorListener());
        this.mScaleDetector = new ScaleGestureDetector(this.mContext, new ScaleDetectorListener());
    }

    public boolean onTouchEvent(MotionEvent var1) {

        if (this.mListener == null) {
            return false;
        } else if (this.mTapGestureDetector.onTouchEvent(var1)) {
            return true;
        } else {
            if (var1.getPointerCount() > 1) {
                try {
                    if (this.mScaleDetector != null && this.mScaleDetector.onTouchEvent(var1)) {
                        return true;
                    }
                } catch (Exception var2) {
                    var2.printStackTrace();
                }
            }

            if (this.mDoubleTapGestureDetector.onTouchEvent(var1)) {
                return true;
            } else {
                switch (var1.getAction() & 255) {
                    case 1:
                        this.mListener.onGestureEnd();
                    default:
                        return false;
                }
            }
        }
    }

    public void setTouchListener(MediaContollerTouchListener var1, boolean var2) {
        this.mListener = var1;
        this.mGestureEnabled = var2;
    }

    class DoubleTapGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        private boolean bY = false;


        public final boolean onDown(MotionEvent var1) {
            firstScroll = true;
            this.bY = true;
            return super.onDown(var1);
        }


        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mListener != null && mGestureEnabled && e1 != null && e2 != null) {
                if (this.bY) {
                    mListener.onGestureBegin();
                    this.bY = false;
                }

                float var5 = e1.getX();
                float var8 = e1.getY();
                int width = VideoScreenUtil.getDisplayWidth(mContext);
                int heigth = VideoScreenUtil.getDisplayHeight(mContext);
                if (firstScroll) {
                    if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                        GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
                    } else {
                        GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                    }
                }

                if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX >= (float) VideoScreenUtil.getDensity(mContext, 2.0F)) {
                            mListener.onLeftSpeedSlide(111.0F);
                        } else if (distanceX <= (float) (-VideoScreenUtil.getDensity(mContext, 2.0F))) {
                            mListener.onRightSpeedSlide(111.0F);
                        }
                    }
                } else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
                    if ((double) var5 > (double) width * 4.0D / 5.0D) {
                        mListener.onRightSlide((var8 - e2.getY(0)) / (float) heigth);
                    } else if ((double) var5 < (double) width / 5.0D) {
                        mListener.onLeftSlide((var8 - e2.getY(0)) / (float) heigth);
                    }
                }
            }

            firstScroll = false;
            return false;
        }

        public final boolean onDoubleTap(MotionEvent var1) {
            if (mListener != null) {
                mListener.onDoubleTap();
            }
            return super.onDoubleTap(var1);
        }
    }


    class TapGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mListener != null) {
                mListener.onSingleTap();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mListener != null) {
                mListener.onLongPress();
            }
        }
    }

    class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {

        public final boolean onScale(ScaleGestureDetector var1) {
            if (mListener != null) {
                mListener.onScale(var1.getScaleFactor(), 1);
            }

            return true;
        }

        public final void onScaleEnd(ScaleGestureDetector var1) {
            if (mListener != null) {
                mListener.onScale(0.0f, 2);
            }
        }

        public final boolean onScaleBegin(ScaleGestureDetector var1) {
            if (mListener != null) {
                mListener.onScale(0.0f, 0);
            }
            return true;
        }
    }

}
