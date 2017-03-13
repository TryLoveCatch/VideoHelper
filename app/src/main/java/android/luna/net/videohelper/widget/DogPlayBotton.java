package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelper.anim.AnimationCollector;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bintou on 15/12/1.
 */
public class DogPlayBotton extends AppCompatImageButton {

    private AnimationCollector animationCollector;

    public DogPlayBotton(Context context) {
        super(context);
    }

    public DogPlayBotton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DogPlayBotton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setVisibility(int visibility) {
        if (animationCollector == null) {
            animationCollector = new AnimationCollector(getContext());
        }
        if (getVisibility() != visibility) {
            if (visibility == View.VISIBLE) {
                animationCollector.dogVisibleAnim(this);
            } else {
//                animationCollector.dogGoneAnim(this);
            }
        }
        super.setVisibility(visibility);
    }
}
