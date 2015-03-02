package org.jorge.lbudget.ui.component;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

class ScrollSettleHandler extends Handler {
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private int mSettledScrollY;
    private View mViewToAnimate;

    ScrollSettleHandler(View v) {
        super();
        mViewToAnimate = v;
    }

    void onScroll(int scrollY) {
        if (mSettledScrollY != scrollY) {
            mSettledScrollY = scrollY;
            removeMessages(0);
            sendEmptyMessage(0);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        mViewToAnimate.animate().setInterpolator(mInterpolator)
                .setDuration(TRANSLATE_DURATION_MILLIS)
                .translationY(mSettledScrollY);
    }
}
