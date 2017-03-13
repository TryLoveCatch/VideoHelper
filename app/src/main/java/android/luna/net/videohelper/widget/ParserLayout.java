package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelper.anim.AnimationCollector;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;

import net.luna.common.debug.LunaLog;

/**
 * Created by bintou on 15/12/1.
 */
public class ParserLayout extends RelativeLayout {

    private AnimationCollector animationCollector;

    public ParserLayout(Context context) {
        super(context);
    }

    public ParserLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParserLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setVisibility(int visibility) {
        if (Build.VERSION.SDK_INT >=19) {
            if (animationCollector == null) {
                animationCollector = new AnimationCollector(getContext());
            }
            if (getVisibility() != visibility) {
                if (visibility == View.VISIBLE) {
                    animationCollector.dogVisibleAnim(this);
                    super.setVisibility(visibility);
                } else {
                    animationCollector.dogGoneAnim(this, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ParserLayout.super.setVisibility(View.GONE);
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
        } else {
            super.setVisibility(visibility);
        }
    }
}
