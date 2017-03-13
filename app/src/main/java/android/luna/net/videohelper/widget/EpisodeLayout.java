package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelper.anim.AnimationCollector;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;

/**
 * Created by bintou on 15/12/1.
 */
public class EpisodeLayout extends RelativeLayout {

    private AnimationCollector animationCollector;

    public EpisodeLayout(Context context) {
        super(context);
    }

    public EpisodeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EpisodeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setVisibility(int visibility) {
        int width = getWidth();

        if (animationCollector == null) {
            animationCollector = new AnimationCollector(getContext());
        }
        if (getVisibility() != visibility) {
            if (visibility == View.VISIBLE) {
                animationCollector.episodeVisibleAnim(this, width);
                super.setVisibility(visibility);
            } else {
                animationCollector.episodeGoneAnim(this, width, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        EpisodeLayout.super.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        }
    }
}
