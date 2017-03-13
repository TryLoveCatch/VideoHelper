package android.luna.net.videohelper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.luna.net.videohelptools.R;
import android.util.AttributeSet;
import android.view.View;

import net.luna.common.util.DpUtils;



/**
 * Created by Alian on 15-8-18.
 */
public class Pointer extends View {

    private static final String DEFAULT_NUMBER = "1";

    private String mNumber = DEFAULT_NUMBER;

    private float mCirclePaintStrokeWidth = 2f;

    private Paint mCirclePaint;
    private Paint mTextPaint;

    private float mRadius = 12f;
    private float mTextSize = 18f;

    //    private float mSelectedRadius = 22f;
    //    private float mSelectedTextSize = 36f;

    private Rect mBounds = new Rect();
    //    private Rect mSelectedBounds = new Rect();

    //    private Paint mPaint;
    //    private float mRadius;
    //    private Rect mBounds;

    private boolean mPointerSelected = false;

    private boolean mIsShowNumber = false;

    private Context mContext;

    public Pointer(Context context) {
        this(context, null);
    }

    public Pointer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
    }

    private void initPaint() {
        mRadius = DpUtils.dpToPx(4,mContext.getResources());
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(getResources().getColor(R.color.pointer_normal));
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeWidth(mCirclePaintStrokeWidth);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(getResources().getColor(R.color.pointer_normal));
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);

        mTextPaint.getTextBounds(mNumber, 0, mNumber.length(), mBounds);

    }

    public void setNumber(int number) {
        mNumber = String.valueOf(number);
        invalidate();
    }

    @Override
    public void setSelected(boolean selected) {
        mPointerSelected = selected;
        if (mPointerSelected) {
            mCirclePaint.setColor(getResources().getColor(R.color.pointer_selected));
            mCirclePaint.setStyle(Paint.Style.FILL);
        } else {
            mCirclePaint.setColor(getResources().getColor(R.color.pointer_normal));
            mCirclePaint.setStyle(Paint.Style.FILL);
        }
        invalidate();
    }

    public boolean isPointerSelected() {
        return mPointerSelected;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Math.round(
                        mRadius * 2 + getPaddingLeft() + getPaddingRight() + mCirclePaintStrokeWidth * 2),
                Math.round(
                        mRadius * 2 + getPaddingTop() + getPaddingBottom() + mCirclePaintStrokeWidth * 2));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, mRadius, mCirclePaint);

        if (mIsShowNumber) {
            canvas.drawText(mNumber, getMeasuredWidth() / 2 - mBounds.width(),
                    getMeasuredHeight() / 2 + mBounds.height() / 2, mTextPaint);
        }
    }
}
