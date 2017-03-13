package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import net.luna.common.util.CompatUtils;

public class FullscreenHolder extends FrameLayout {
    public FullscreenHolder(Context context) {
        super(context);
        this.setBackgroundColor(CompatUtils.getColor(context, android.R.color.black));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
