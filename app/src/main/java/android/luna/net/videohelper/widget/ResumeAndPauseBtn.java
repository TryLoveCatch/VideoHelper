package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelptools.R;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by bintou on 16/2/19.
 */
public class ResumeAndPauseBtn extends ImageButton {

    private boolean isPlaying = false;

    private final int pauseSrcId = R.drawable.ic_download_pause;
    private final int playSrcId = R.drawable.ic_download_play;

    public ResumeAndPauseBtn(Context context) {
        super(context);
        init();
    }

    public ResumeAndPauseBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResumeAndPauseBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ResumeAndPauseBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setPauseStatus();
    }

    public void setPauseStatus() {
        setImageResource(pauseSrcId);
        isPlaying = false;
    }

    public void setPlayingStatus() {
        setImageResource(playSrcId);
        isPlaying = true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

}
