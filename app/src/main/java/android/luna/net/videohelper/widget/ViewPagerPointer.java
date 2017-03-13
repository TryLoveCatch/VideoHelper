package android.luna.net.videohelper.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alian on 15-8-18.
 */
public class ViewPagerPointer extends LinearLayout {

    private static final int DEFAULT_POINTER_NUMBER = 2;
    private static final int DEFAULT_SELECTED_ITEM = 0;

    private int mPointerNumber = DEFAULT_POINTER_NUMBER;

    private int mCurrentSelectedItem = DEFAULT_SELECTED_ITEM;

    private List<Pointer> mPointers;

    private float mOriginalSize = 0.8f;
    private float mScaleSize = 1.0f;
    private long mAnimationTime = 200l;

    public ViewPagerPointer(Context context) {
        this(context, null);
    }

    public ViewPagerPointer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindViewPager(final ViewPager viewPager, int count) {
        if (viewPager == null) {
            throw new NullPointerException("viewPager == null");
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPointerNumber > 0) {
                    mCurrentSelectedItem = (position) % mPointerNumber;
                    setCurrentSelectedItem(mCurrentSelectedItem);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mPointerNumber > 0) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
//                        setCurrentSelectedItem(mCurrentSelectedItem);
//                        LunaLog.d("mCurrentSelectedItem: " + mCurrentSelectedItem);
                    }
                }
            }
        });
        mPointerNumber = count;
//        mPointerNumber = viewPager.getAdapter().getCount();
        if (mPointerNumber > 0) {
            mCurrentSelectedItem = viewPager.getCurrentItem() % mPointerNumber;
        } else {
            mCurrentSelectedItem = 0;
        }
        initPointers();
    }

    private void setCurrentSelectedItem(int currentSelectedItem) {
        if (currentSelectedItem < 0 || currentSelectedItem > mPointerNumber - 1) {
            throw new IllegalArgumentException("currentSelectedItem is invalid.");
        }

        for (int i = 0; i < mPointers.size(); i++) {
            Pointer pointer = mPointers.get(i);
            if (pointer.isPointerSelected()) {
                showUnselectedAnimation(pointer);
                break;
            }
        }

        for (int i = 0; i < mPointers.size(); i++) {
            Pointer pointer = mPointers.get(i);
            if (currentSelectedItem == i) {
                showSelectedAnimation(pointer);
                break;
            }
        }
    }

    private void showSelectedAnimation(final Pointer pointer) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mOriginalSize, mScaleSize);
        valueAnimator.setDuration(mAnimationTime);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (pointer != null) {
                    pointer.setScaleX((float) animation.getAnimatedValue());
                    pointer.setScaleY((float) animation.getAnimatedValue());
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (pointer != null) {
                    pointer.setSelected(true);
                }
            }
        });
        valueAnimator.start();
    }

    private void showUnselectedAnimation(final Pointer pointer) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mScaleSize, mOriginalSize);
        valueAnimator.setDuration(mAnimationTime);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (pointer != null) {
                    pointer.setScaleX((float) animation.getAnimatedValue());
                    pointer.setScaleY((float) animation.getAnimatedValue());
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (pointer != null) {
                    pointer.setSelected(false);
                }
            }
        });
        valueAnimator.start();
    }

    private void initPointers() {
        if (mPointers == null) {
            mPointers = new ArrayList<>();
        }
        mPointers.clear();

        for (int i = 0; i < mPointerNumber; i++) {
            Pointer pointer = generateOnePointer(i);
            if (i == mCurrentSelectedItem) {
                pointer.setSelected(true);
                pointer.setScaleX(mScaleSize);
                pointer.setScaleY(mScaleSize);
            }
            pointer.setNumber(i + 1);
            mPointers.add(pointer);

            addView(pointer);
        }
    }

    private Pointer generateOnePointer(int position) {
        Pointer pointer = new Pointer(getContext());
        pointer.setPadding(3, 3, 3, 3);

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;

        if (position != mPointerNumber - 1) {
            layoutParams.rightMargin = 12;
        }
        pointer.setLayoutParams(layoutParams);

        return pointer;
    }

    public void updateCount(int count) {
        mPointerNumber = count;

        initPointers();
    }
}
