package android.luna.net.videohelper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by bintou on 15/12/10.
 */
public class GridviewInScroll extends GridView {
    public GridviewInScroll(Context context) {
        super(context);
    }

    public GridviewInScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridviewInScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
