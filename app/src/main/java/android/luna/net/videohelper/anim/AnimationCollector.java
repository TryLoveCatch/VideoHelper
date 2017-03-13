package android.luna.net.videohelper.anim;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import net.luna.common.util.NetWorkUtil;
import net.luna.common.util.ScreenUtils;

/**
 * Created by bintou on 15/10/30.
 * 使用objectanimation更加方便和有效率。
 */
public class AnimationCollector {

    Context mContext;
    float screenWidth;
    float screenHeigh;

    public AnimationCollector(Context context) {
        mContext = context;
        screenWidth = ScreenUtils.widthPixels(mContext);
        screenHeigh = ScreenUtils.heightPixels(mContext);
    }

    /**
     * App开场专用动画
     * -
     *
     * @param parent
     * @param bgView
     */
    public void specialPrologue(Context context, final View parent, View bgView, Animator.AnimatorListener listener) {
        if (Build.VERSION.SDK_INT >= 14) {
            try {
                int showTime = 4500;
                if (!NetWorkUtil.IsNetWorkEnable(context)) {
                    showTime = 2000;
                }
                //开场页
                bgView.setPivotY(0);
                ObjectAnimator bsy = ObjectAnimator.ofFloat(bgView, "scaleY", 1f, 1.05f).setDuration(showTime);
                ObjectAnimator bsx = ObjectAnimator.ofFloat(bgView, "scaleX", 1f, 1.05f).setDuration(showTime);
                ObjectAnimator sy = ObjectAnimator.ofFloat(parent, "scaleY", 1f, 0.22f).setDuration(500);
                sy.setInterpolator(new DecelerateInterpolator());
                sy.setStartDelay(showTime);
                ObjectAnimator bgAlpha = ObjectAnimator.ofFloat(parent, "alpha", 1f, 0f).setDuration(500);
                bgAlpha.setStartDelay(showTime);


                AnimatorSet mainAnimatorSet = new AnimatorSet();
                mainAnimatorSet.playTogether(bsx, bsy, bgAlpha);
//                mainAnimatorSet.playTogether( bgAlpha);

                mainAnimatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        parent.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                if (listener != null) {
                    mainAnimatorSet.addListener(listener);
                }
                mainAnimatorSet.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    parent.setVisibility(View.GONE);
                }
            }, 2000);

        }
    }

    public void dogVisibleAnim(View view) {
        ObjectAnimator ty = ObjectAnimator.ofFloat(view, "TranslationY", 400f, 0f).setDuration(400);
        ty.setInterpolator(new AccelerateInterpolator(1f));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ty);
        animatorSet.start();
    }

    public void dogGoneAnim(final View view, Animator.AnimatorListener listener) {
        ObjectAnimator ty = ObjectAnimator.ofFloat(view, "TranslationY", 0f, 300f).setDuration(300);
        ty.setInterpolator(new AccelerateInterpolator(1f));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ty);
        animatorSet.addListener(listener);
        animatorSet.start();
    }


    public void episodeVisibleAnim(View view, int width) {
        if (width <= 0) {
            width = 512;
        }
        ObjectAnimator ty = ObjectAnimator.ofFloat(view, "TranslationX", width, 0f).setDuration(400);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ty);
        animatorSet.start();
    }

    public void episodeGoneAnim(final View view, int width, Animator.AnimatorListener listener) {
        if (width <= 0) {
            width = 512;
        }
        ObjectAnimator ty = ObjectAnimator.ofFloat(view, "TranslationX", 0f, width).setDuration(400);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ty);
        animatorSet.addListener(listener);
        animatorSet.start();
    }

    public void pointVisiable(final View view) {
        ObjectAnimator sy = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f).setDuration(400);
        ObjectAnimator sx = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f).setDuration(400);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(sy, sx);
        animatorSet.start();
    }

}
