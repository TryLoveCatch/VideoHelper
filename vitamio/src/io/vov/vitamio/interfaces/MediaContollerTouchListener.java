package io.vov.vitamio.interfaces;

/**
 * Created by bintou on 16/3/15.
 */
public interface MediaContollerTouchListener {

    void onGestureBegin();

    void onGestureEnd();

    void onLeftSlide(float var1);

    void onRightSlide(float var1);

    void onSingleTap();

    void onDoubleTap();

    void onScale(float var1, int var2);

    void onLongPress();

    void onLeftSpeedSlide(float var1);

    void onRightSpeedSlide(float var1);
}
