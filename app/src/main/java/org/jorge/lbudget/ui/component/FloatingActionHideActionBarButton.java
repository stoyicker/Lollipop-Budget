package org.jorge.lbudget.ui.component;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.jorge.lbudget.R;

/**
 * This class and all resources it uses have been adapted from makovkastar's Floating Action
 * Button library,
 * which can be found at https://github.com/makovkastar/FloatingActionButton
 * The purpose of this adaptation is to allow the button to work with a RecylerView instead of
 * with an AbsListView
 */
public class FloatingActionHideActionBarButton extends ImageButton {

    public void setTopPadding(int paddingTop) {
        BASE_TOP_PADDING = paddingTop;
    }

    @IntDef({TYPE_NORMAL, TYPE_MINI})
    public @interface TYPE {
    }

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_MINI = 1;

    protected RecyclerView mRecyclerView;

    private int mScrollY;
    private boolean mVisible;

    private int mColorNormal;
    private int mColorPressed;
    private boolean mShadow;
    private int mType;

    private final ScrollSettleHandler mScrollSettleHandler;
    private Activity mActivity;
    private Boolean mActionBarIsShowingOrShown = Boolean.TRUE;
    private final Object mActionBarLock = new Object();
    private Integer BASE_TOP_PADDING;

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView
            .OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int newScrollY = getListViewScrollY();
            if (newScrollY == mScrollY) {
                return;
            }

            if (newScrollY > mScrollY) {
                // Scrolling up
                hide();
            } else if (newScrollY < mScrollY) {
                // Scrolling down
                show();
            }
            mScrollY = newScrollY;
            onScrolledForActionBar(recyclerView, dx, dy);
        }

        final Integer MIN_SCROLL_TOGGLE_ACTION_BAR = getContext().getResources().getInteger(R.
                integer.min_scroll_toggle_action_bar);

        public void onScrolledForActionBar(RecyclerView recyclerView, int dx, int dy) {
            ActionBar actionBar = mActivity.getActionBar();
            synchronized (mActionBarLock) {
                if (actionBar != null)
                    if (dy > MIN_SCROLL_TOGGLE_ACTION_BAR && mActionBarIsShowingOrShown) {
                        recyclerView.setPadding(0, 0, 0, 0);
                        actionBar.hide();
                        mActionBarIsShowingOrShown = Boolean.FALSE;
                    } else if ((dy < -1 * MIN_SCROLL_TOGGLE_ACTION_BAR || !recyclerView
                            .canScrollVertically(-1)) && !mActionBarIsShowingOrShown) {
                        recyclerView.setPadding(0, BASE_TOP_PADDING, 0, 0);
                        actionBar.show();
                        mActionBarIsShowingOrShown = Boolean.TRUE;
                        if (!recyclerView.canScrollVertically(-1))
                            recyclerView.smoothScrollToPosition(0);
                    }
            }
        }
    };

    public FloatingActionHideActionBarButton(Context context) {
        this(context, null);
    }

    public FloatingActionHideActionBarButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        mScrollSettleHandler = new ScrollSettleHandler(this);
    }

    public FloatingActionHideActionBarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        mScrollSettleHandler = new ScrollSettleHandler(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = getDimension(
                mType == TYPE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
        if (mShadow) {
            int shadowSize = getDimension(R.dimen.fab_shadow_size);
            size += shadowSize * 2;
        }
        setMeasuredDimension(size, size);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mScrollY = mScrollY;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mScrollY = savedState.mScrollY;
            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void init(Context context, AttributeSet attributeSet) {
        mVisible = true;
        mColorNormal = getColor(android.R.color.holo_blue_dark);
        mColorPressed = getColor(android.R.color.holo_blue_light);
        mType = TYPE_NORMAL;
        mShadow = true;
        if (attributeSet != null) {
            initAttributes(context, attributeSet);
        }
        updateBackground();
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet,
                R.styleable.FloatingActionHideActionBarButton);
        if (attr != null) {
            try {
                mColorNormal = attr.getColor(R.styleable
                                .FloatingActionHideActionBarButton_fab_colorNormal,
                        getColor(android.R.color.holo_blue_dark));
                mColorPressed = attr.getColor(R.styleable
                                .FloatingActionHideActionBarButton_fab_colorPressed,
                        getColor(android.R.color.holo_blue_light));
                mShadow = attr.getBoolean(R.styleable
                        .FloatingActionHideActionBarButton_fab_shadow, true);
                mType = attr.getInt(R.styleable.FloatingActionHideActionBarButton_fab_type,
                        TYPE_NORMAL);
            } finally {
                attr.recycle();
            }
        }
    }

    private void updateBackground() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mColorPressed));
        drawable.addState(new int[]{}, createDrawable(mColorNormal));
        setBackgroundCompat(drawable);
    }

    private Drawable createDrawable(int color) {
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);

        if (mShadow) {
            LayerDrawable layerDrawable = new LayerDrawable(
                    new Drawable[]{getResources().getDrawable(R.drawable.shadow),
                            shapeDrawable});
            int shadowSize = getDimension(
                    mType == TYPE_NORMAL ? R.dimen.fab_shadow_size : R.dimen.fab_mini_shadow_size);
            layerDrawable.setLayerInset(1, shadowSize, shadowSize, shadowSize, shadowSize);
            return layerDrawable;
        } else {
            return shapeDrawable;
        }
    }

    private TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    private int getDimension(@DimenRes int id) {
        return getResources().getDimensionPixelSize(id);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    protected int getListViewScrollY() {
        View topChild = mRecyclerView.getChildAt(0);
        return topChild == null ? 0 : ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition() * topChild.getHeight() -
                topChild.getTop();
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public void setColorNormal(int color) {
        if (color != mColorNormal) {
            mColorNormal = color;
            updateBackground();
        }
    }

    public void setColorNormalResId(@ColorRes int colorResId) {
        setColorNormal(getColor(colorResId));
    }

    public int getColorNormal() {
        return mColorNormal;
    }

    public void setColorPressed(int color) {
        if (color != mColorPressed) {
            mColorPressed = color;
            updateBackground();
        }
    }

    public void setColorPressedResId(@ColorRes int colorResId) {
        setColorPressed(getColor(colorResId));
    }

    public int getColorPressed() {
        return mColorPressed;
    }

    public void setShadow(boolean shadow) {
        if (shadow != mShadow) {
            mShadow = shadow;
            updateBackground();
        }
    }

    public boolean hasShadow() {
        return mShadow;
    }

    public void setType(@TYPE int type) {
        if (type != mType) {
            mType = type;
            updateBackground();
        }
    }

    @TYPE
    public int getType() {
        return mType;
    }

    protected RecyclerView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void show() {
        if (!mVisible) {
            mVisible = true;
            mScrollSettleHandler.onScroll(0);
        }
    }

    public void hide() {
        if (mVisible) {
            mVisible = false;
            mScrollSettleHandler.onScroll(getHeight() + getMarginBottom());
        }
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setOnScrollListener(mOnScrollListener);
    }

    /**
     * A {@link android.os.Parcelable} representing the {@link org.jorge.lbudget.ui.util
     * .FloatingActionHideActionBarButton}'s
     * state.
     */
    public static class SavedState extends BaseSavedState {

        private int mScrollY;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mScrollY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mScrollY);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable
                .Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
