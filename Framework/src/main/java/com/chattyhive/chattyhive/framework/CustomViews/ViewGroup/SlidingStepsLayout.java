package com.chattyhive.chattyhive.framework.CustomViews.ViewGroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.chattyhive.chattyhive.framework.CustomViews.Listener.OnInflateLayoutListener;
import com.chattyhive.chattyhive.framework.CustomViews.Listener.OnRemoveLayoutListener;
import com.chattyhive.chattyhive.framework.CustomViews.Listener.OnTransitionListener;
import com.chattyhive.chattyhive.framework.CustomViews.View.ShapeArrow;
import com.chattyhive.chattyhive.framework.R;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by Jonathan on 01/09/2014.
 */
public class SlidingStepsLayout extends ViewGroup {

    private static LayoutInflater inflater = null;

    private static DisplayMetrics displayMetrics = null;

    private static int defaultMaxAnimationDuration = 150;
    private static int defaultButtonPressedAnimationDuration = 150;
    private static int defaultFlingSpeedThreshold = 800;

    private VelocityTracker velocityTracker;
    private Scroller scroller;

    private Boolean showActionBar;
    private Boolean allowSwipeToChangeStep;

    private float actionBarHeight;
    private float actionBarArrowPointWidth;

    private int actionBarUnselectedTitleColor;
    private int actionBarSelectedTitleColor;
    private int actionBarUnselectedSubtitleColor;
    private int actionBarSelectedSubtitleColor;
    private Drawable actionBarBackground;
    private int actionBarMarkerBackgroundColor;
    private int actionBarSelectedBorderColor;
    private float actionBarSelectedBorderThick;

    private float actionBarTitleTextSize;
    private float actionBarSubTitleTextSize;
    private float actionBarTitleMarginBottom;
    private float actionBarLabelMarginLeft;
    private float actionBarLabelMarginRight;

    private float labelWidth;

    private View actionBarView;
    private View actionBarMarkerView;

    private int numberSteps;
    private int actualStep;
    private int unloadingStep;

    private float actualPosition;

    private int maxAnimationDuration;
    private int buttonPressedAnimationDuration;
    private int flingSpeedThreshold;
    private boolean scrolling = false;

    private boolean actionBarHasToScroll;
    private int actionBarTagsPerScreen;
    private int leftmostActionBarTag = -1;
    private int actualSelectedActionBarTag = -1;

    private OnInflateLayoutListener inflateLayoutListener;
    public void setOnInflateLayoutListener(OnInflateLayoutListener listener) {
        this.inflateLayoutListener = listener;
    }

    private OnRemoveLayoutListener removeLayoutListener;
    public void setOnRemoveLayoutListener(OnRemoveLayoutListener listener) {
        this.removeLayoutListener = listener;
    }

    private OnTransitionListener transitionListener;
    public void setOnTransitionListener(OnTransitionListener listener) {
        this.transitionListener = listener;
    }

    private TreeSet<LayoutParams> layouts;
    public void addLayout (LayoutParams layout) {
        this.layouts.add(layout);
        this.numberSteps++;
        //TODO: check if actualStep changed
        //TODO: Measure and update layout
    }
    public void removeLayout(int layoutPosition) {
        if (layoutPosition < numberSteps)
            this.removeLayout(this.layouts.toArray(new LayoutParams[this.layouts.size()])[layoutPosition]);
    }
    public void removeLayout(LayoutParams layout) {
        this.layouts.remove(layout);
        this.numberSteps--;
        //TODO: check if actualStep changed
        //TODO: Measure and update layout
    }

    private HashMap<Integer,Object> values;
    public void setValue (int view,Object value) {
        this.values.put(view,value);
    }
    public Object getValue (int view) {
        return this.values.get(view);
    }
    public void removeValue (int view) {
        this.values.remove(view);
    }

    public SlidingStepsLayout(Context context) {
        this(context, null);
    }
    public SlidingStepsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.slidingStepsLayoutStyle);
    }
    public SlidingStepsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);

        this.velocityTracker = VelocityTracker.obtain();
        this.scroller = new Scroller(context);

        this.values = new HashMap<Integer, Object>();

        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        }

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.SlidingStepsLayout, defStyle, 0);

        this.showActionBar = a.getBoolean(R.styleable.SlidingStepsLayout_showActionBar, false);
        this.allowSwipeToChangeStep = a.getBoolean(R.styleable.SlidingStepsLayout_allowSwipeToChangeStep, false);

        this.actionBarHeight = a.getDimension(R.styleable.SlidingStepsLayout_actionBarHeight, 48*displayMetrics.scaledDensity);
        this.actionBarArrowPointWidth = a.getDimension(R.styleable.SlidingStepsLayout_actionBarArrowPointWidth, 24*displayMetrics.scaledDensity);

        this.actionBarBackground = a.getDrawable(R.styleable.SlidingStepsLayout_actionBarBackground);

        this.actionBarUnselectedTitleColor = a.getColor(R.styleable.SlidingStepsLayout_actionBarUnselectedTitleColor, Color.parseColor("#eeeeee"));
        this.actionBarUnselectedSubtitleColor = a.getColor(R.styleable.SlidingStepsLayout_actionBarUnselectedSubtitleColor,Color.parseColor("#eeeeee"));
        this.actionBarSelectedTitleColor = a.getColor(R.styleable.SlidingStepsLayout_actionBarSelectedTitleColor,Color.parseColor("#111111"));
        this.actionBarSelectedSubtitleColor = a.getColor(R.styleable.SlidingStepsLayout_actionBarSelectedSubtitleColor,Color.parseColor("#111111"));
        this.actionBarMarkerBackgroundColor = a.getColor(R.styleable.SlidingStepsLayout_actionBarMarkerBackgroundColor,Color.parseColor("#eeeeee"));

        this.actionBarTitleTextSize = a.getDimension(R.styleable.SlidingStepsLayout_actionBarTitleTextSize,displayMetrics.scaledDensity * 16);
        this.actionBarSubTitleTextSize = a.getDimension(R.styleable.SlidingStepsLayout_actionBarSubTitleTextSize,(float)(displayMetrics.scaledDensity * 13.5));
        this.actionBarTitleMarginBottom = a.getDimension(R.styleable.SlidingStepsLayout_actionBarTitleMarginBottom,0);
        this.actionBarLabelMarginLeft = a.getDimension(R.styleable.SlidingStepsLayout_actionBarLabelMarginLeft,0);
        this.actionBarLabelMarginRight = a.getDimension(R.styleable.SlidingStepsLayout_actionBarLabelMarginRight,0);

        this.maxAnimationDuration = a.getInteger(R.styleable.SlidingStepsLayout_maxTransitionAnimationDuration,defaultMaxAnimationDuration);
        this.buttonPressedAnimationDuration = a.getInteger(R.styleable.SlidingStepsLayout_buttonPressedTransitionAnimationDuration,defaultButtonPressedAnimationDuration);
        this.flingSpeedThreshold = a.getInteger(R.styleable.SlidingStepsLayout_flingSpeedDetectionThreshold,defaultFlingSpeedThreshold);

        a.recycle();

        setFocusable(true);
        setFocusableInTouchMode(true);

        if (inflater == null)
            inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    protected void loadChildren() {
        if (this.layouts != null) return;
        this.layouts = new TreeSet<LayoutParams>();

        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
            if (layoutParams instanceof LayoutParams)
                this.layouts.add((LayoutParams)layoutParams);
        }
        this.numberSteps = this.layouts.size();
        this.actualStep = 0;
        this.unloadingStep = -1;

        this.inflateChild(this.actualStep);
        this.inflateChild(this.actualStep+1);
        invalidate();
        requestLayout();
    }

    protected void inflateChild(int childPosition) {
        if (childPosition < numberSteps) {
            LayoutParams childLayoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()])[childPosition];
            //View child = inflate(this.getContext(),childLayoutParams.getLayout(),this);
            View child = inflater.inflate(childLayoutParams.getLayout(),this,false);

            //this.attachViewToParent(child,this.getChildCount(),child.getLayoutParams());
            this.addView(child);

            if (child.getId() == -1)
                child.setId(childLayoutParams.getLayout());

            childLayoutParams.setViewID(child.getId());

            if (this.inflateLayoutListener != null)
                this.inflateLayoutListener.OnInflate(child);

            //TODO: Fill child fields

            if (childLayoutParams.getNextStepButton() != null) {
                Button nextButton = (Button)child.findViewById(childLayoutParams.getNextStepButton());
                nextButton.setOnClickListener(this.nextButtonClick);
            }
            if (childLayoutParams.getPreviousStepButton() != null) {
                Button previousButton = (Button)child.findViewById(childLayoutParams.getPreviousStepButton());
                previousButton.setOnClickListener(this.backButtonClick);
            }
        }
    }
    protected void removeChild(int childPosition) {
        if (childPosition < numberSteps) {
            LayoutParams childLayoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()])[childPosition];
            final int childCount = this.getChildCount();
            for (int i = 0; i < childCount; i++)
                if ((this.getChildAt(i) != null) && (this.getChildAt(i).getId() == childLayoutParams.getViewID()))
                    this.internalRemoveChild(this.getChildAt(i), childPosition);
        }
    }
    protected void removeChild(View child) {
        for (int i=0; i < this.numberSteps; i++)
            if (child.getId() == this.layouts.toArray(new LayoutParams[this.layouts.size()])[i].getViewID())
                this.internalRemoveChild(child, i);
    }
    private void internalRemoveChild(View child, int childPosition) {
        this.unloadingStep = childPosition;

        if (this.removeLayoutListener != null)
            this.removeLayoutListener.OnRemove(child);

        //TODO: Save child fields

        this.removeView(child);
        LayoutParams childLayoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()])[childPosition];
        //childLayoutParams.setViewID(-1);
        this.unloadingStep = -1;
    }

    public View getViewByStep(int step) {
        return this.findViewById(this.layouts.toArray(new LayoutParams[this.layouts.size()])[step].getViewID());
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widestChildWidth = 0;
        int highestChildHeight = 0;
        int actionBarHeight = (int)((this.showActionBar)?this.actionBarHeight:0);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - actionBarHeight, MeasureSpec.getMode(heightMeasureSpec));

        this.loadChildren();

        for (LayoutParams layout : layouts) {
            View child = this.findViewById(layout.getViewID());
            boolean removeChild = false;
            if (child == null) {
                child = inflater.inflate(layout.getLayout(), this, false);
                removeChild = true;
            }
            //View child = inflate(this.getContext(),layout.getLayout(),this);
            //this.getResources().getLayout(layout.getLayout());
            child.measure(widthMeasureSpec, childHeightMeasureSpec);
            int childMeasuredWidth = child.getMeasuredWidth();
            int childMeasuredHeight = child.getMeasuredHeight();
            if (childMeasuredWidth > widestChildWidth)
                widestChildWidth = childMeasuredWidth;
            if (childMeasuredHeight > highestChildHeight)
                highestChildHeight = childMeasuredHeight;

            if (removeChild)
                this.removeView(child);
        }

        widestChildWidth += this.getPaddingLeft() + this.getPaddingRight();
        highestChildHeight += this.getPaddingTop() + this.getPaddingBottom() + actionBarHeight;

        this.setMeasuredDimension(resolveSize(widestChildWidth,widthMeasureSpec),resolveSize(highestChildHeight, heightMeasureSpec));

        this.calculateActionBarShowingMode();
    }

    private void calculateActionBarShowingMode() {
        TextView textView = new TextView(this.getContext());
        int widestLabelWidth = 0;
        Rect bounds;

        for (LayoutParams layout : this.layouts) {
            if (layout.getStepTitle() != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,this.actionBarTitleTextSize);
                bounds = new Rect();
                textView.getPaint().getTextBounds(layout.getStepTitle(),0,layout.getStepTitle().length(),bounds);
                if (bounds.width() > widestLabelWidth)
                    widestLabelWidth = bounds.width();
            }
            if (layout.getStepSubtitle() != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,this.actionBarSubTitleTextSize);
                bounds = new Rect();
                textView.getPaint().getTextBounds(layout.getStepSubtitle(),0,layout.getStepSubtitle().length(),bounds);
                if (bounds.width() > widestLabelWidth)
                    widestLabelWidth = bounds.width();
            }
        }

        int availableWidth = this.getWidth()-this.getPaddingLeft()-this.getPaddingRight();
        widestLabelWidth += this.actionBarLabelMarginLeft + this.actionBarLabelMarginRight;
        boolean tagsFitInScreen = (availableWidth >= ((widestLabelWidth*this.numberSteps)+(this.actionBarArrowPointWidth*(this.numberSteps-1))));

        if (!tagsFitInScreen) {
            this.actionBarHasToScroll = true;
            this.actionBarTagsPerScreen = (int)Math.floor(availableWidth / (widestLabelWidth+this.actionBarArrowPointWidth));
            this.actionBarTagsPerScreen = Math.max(this.actionBarTagsPerScreen,1);
            if ( (this.numberSteps / (float)this.actionBarTagsPerScreen) > 2) {
                this.actionBarTagsPerScreen = (int) Math.floor((availableWidth - this.actionBarArrowPointWidth) / (widestLabelWidth + this.actionBarArrowPointWidth));
                this.actionBarTagsPerScreen = Math.max(this.actionBarTagsPerScreen,1);
                this.labelWidth = (availableWidth - (this.actionBarArrowPointWidth * (this.actionBarTagsPerScreen + 1)))/(float)this.actionBarTagsPerScreen;
            } else {
                this.labelWidth = (availableWidth - (this.actionBarArrowPointWidth * this.actionBarTagsPerScreen))/(float)this.actionBarTagsPerScreen;
            }
        } else {
            this.actionBarHasToScroll = false;
            this.actionBarTagsPerScreen = this.numberSteps;
            this.actionBarTagsPerScreen = Math.max(this.actionBarTagsPerScreen,1);
            this.labelWidth = (availableWidth - (this.actionBarArrowPointWidth * (this.actionBarTagsPerScreen - 1)))/(float)this.actionBarTagsPerScreen;
        }

        this.computeTitlePosition();
    }
    private void computeTitlePosition() {
        if (!this.actionBarHasToScroll) {
            this.leftmostActionBarTag = 0;
            this.actualSelectedActionBarTag = this.actualStep;
        } else if (this.actionBarTagsPerScreen == 1) {
            this.leftmostActionBarTag = this.actualStep;
            this.actualSelectedActionBarTag = 0;
        } else if (this.actualStep == 0) {
            this.leftmostActionBarTag = 0;
            this.actualSelectedActionBarTag = 0;
        } else if (this.actualStep == (this.numberSteps-1)) {
            this.leftmostActionBarTag = this.numberSteps - this.actionBarTagsPerScreen;
            this.actualSelectedActionBarTag = this.actionBarTagsPerScreen-1;
        } else if (this.actualStep > (this.leftmostActionBarTag + this.actualSelectedActionBarTag)) {
            this.leftmostActionBarTag = Math.min(this.actualStep + 2 - this.actionBarTagsPerScreen,0);
            this.actualSelectedActionBarTag = this.actualStep - this.leftmostActionBarTag;
        } else if (this.actualStep < (this.leftmostActionBarTag + this.actualSelectedActionBarTag)) {
            this.leftmostActionBarTag = Math.min(this.actualStep - 1, this.numberSteps - this.actionBarTagsPerScreen);
            this.actualSelectedActionBarTag = this.actualStep - this.leftmostActionBarTag;
        }
    }
    private void drawActionBar(){
        int availableWidth = this.getWidth()-this.getPaddingLeft()-this.getPaddingRight();
        Context context = this.getContext();
        LayoutParams[] layoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()]);
        //Create frame layout for background
        boolean frameLayoutCreated = false;
        if (this.actionBarView == null) {
            this.actionBarView = new FrameLayout(context);
            this.actionBarView.setLayoutParams(new ViewGroup.LayoutParams(availableWidth,((int)this.actionBarHeight)));
            //this.actionBarView.setBackgroundColor(this.actionBarBackground);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                this.actionBarView.setBackground(this.actionBarBackground);
            else
                this.actionBarView.setBackgroundDrawable(this.actionBarBackground);
            frameLayoutCreated = true;
        }
        //Create horizontal layout for background
        LinearLayout actionBarSupportLayout = new LinearLayout(context);
        actionBarSupportLayout.setLayoutParams(new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        actionBarSupportLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarSupportLayout.setBackgroundColor(Color.parseColor("#00000000"));
        //Create tags
        for (int i = this.leftmostActionBarTag; i < (this.leftmostActionBarTag+this.actionBarTagsPerScreen); i++) {
            if (i>0) {
                //Add an ArrowPoint at left
                FrameLayout arrowPointSpace = new FrameLayout(context);
                arrowPointSpace.setLayoutParams(new MarginLayoutParams((int)this.actionBarArrowPointWidth,ViewGroup.LayoutParams.MATCH_PARENT));
                arrowPointSpace.setBackgroundColor(Color.parseColor("#00000000"));
                actionBarSupportLayout.addView(arrowPointSpace);
            }
            //Add the tag
            LinearLayout tagLayout = new LinearLayout(context);
            tagLayout.setOrientation(LinearLayout.VERTICAL);
            MarginLayoutParams tagLayoutParams = new MarginLayoutParams((int)this.labelWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            tagLayoutParams.setMargins((int)this.actionBarLabelMarginLeft,0,(int)this.actionBarLabelMarginRight,0);
            tagLayout.setLayoutParams(tagLayoutParams);
            tagLayout.setGravity(Gravity.CENTER);
            tagLayout.setBackgroundColor(Color.parseColor("#00000000"));

            TextView tagTitle = new TextView(context);
            MarginLayoutParams tagTitleLayoutParams = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagTitleLayoutParams.setMargins(0,0,0,(int)this.actionBarTitleMarginBottom);
            tagTitle.setLayoutParams(tagTitleLayoutParams);
            tagTitle.setBackgroundColor(Color.parseColor("#00000000"));
            tagTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,this.actionBarTitleTextSize);
            tagTitle.setTextColor((i==this.actualSelectedActionBarTag)?this.actionBarSelectedTitleColor:this.actionBarUnselectedTitleColor);
            tagTitle.setText(layoutParams[i+this.leftmostActionBarTag].getStepTitle());
            tagLayout.addView(tagTitle);

            TextView tagSubtitle = new TextView(context);
            tagSubtitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tagSubtitle.setBackgroundColor(Color.parseColor("#00000000"));
            tagSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.actionBarSubTitleTextSize);
            tagSubtitle.setTextColor((i == this.actualSelectedActionBarTag) ? this.actionBarSelectedSubtitleColor : this.actionBarUnselectedSubtitleColor);
            tagSubtitle.setText(layoutParams[i + this.leftmostActionBarTag].getStepSubtitle());
            tagLayout.addView(tagSubtitle);

            actionBarSupportLayout.addView(tagLayout);
        }

        if ((this.leftmostActionBarTag+this.actionBarTagsPerScreen) < this.numberSteps) {
            //Add an ArrowPoint at right
            FrameLayout arrowPointSpace = new FrameLayout(context);
            arrowPointSpace.setLayoutParams(new MarginLayoutParams((int)this.actionBarArrowPointWidth,ViewGroup.LayoutParams.MATCH_PARENT));
            arrowPointSpace.setBackgroundColor(Color.parseColor("#00000000"));
            actionBarSupportLayout.addView(arrowPointSpace);
        }

        //Show arrow | marker

        //ImageView actionBarMarker = new ImageView(context);
        ShapeArrow actionBarMarker = new ShapeArrow(context);
        actionBarMarker.setArrowPointWidth(Math.round(this.actionBarArrowPointWidth));
        actionBarMarker.setShapeFillColor(this.actionBarMarkerBackgroundColor);
        LinearLayout.LayoutParams actionBarMarkerLayoutParams = new LinearLayout.LayoutParams((int)(this.labelWidth+(2*this.actionBarArrowPointWidth)), ViewGroup.LayoutParams.MATCH_PARENT);

        int leftMargin = (this.leftmostActionBarTag == 0)?-(int)this.actionBarArrowPointWidth:0;
        leftMargin += (int)(this.actualSelectedActionBarTag*(this.labelWidth+this.actionBarArrowPointWidth));
        actionBarMarkerLayoutParams.setMargins(leftMargin, 0, 0, 0);
        actionBarMarker.setLayoutParams(actionBarMarkerLayoutParams);
        actionBarMarker.setBackgroundColor(Color.parseColor("#00000000"));

        LinearLayout auxiliaryMarkerLayout = new LinearLayout(context);
        auxiliaryMarkerLayout.setOrientation(LinearLayout.HORIZONTAL);
        auxiliaryMarkerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        auxiliaryMarkerLayout.setBackgroundColor(Color.parseColor("#00000000"));
        auxiliaryMarkerLayout.addView(actionBarMarker);

        //Update views in background frame layout
        if (((FrameLayout)this.actionBarView).getChildCount() > 0)
            ((FrameLayout)this.actionBarView).removeAllViews();

        ((FrameLayout)this.actionBarView).addView(auxiliaryMarkerLayout);
        ((FrameLayout)this.actionBarView).addView(actionBarSupportLayout);
        ((FrameLayout)this.actionBarView).bringChildToFront(actionBarSupportLayout);

        this.actionBarView.measure(MeasureSpec.makeMeasureSpec(availableWidth,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec((int)this.actionBarHeight,MeasureSpec.EXACTLY));
        if (frameLayoutCreated)
            this.addView(this.actionBarView);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = this.getChildCount();
        final int paddingLeft = this.getPaddingLeft();
        final int paddingRight = this.getPaddingRight();
        final int paddingTop = this.getPaddingTop();
        final int paddingBottom = this.getPaddingBottom();

        final int width = this.getMeasuredWidth();

        //this.loadChildren();

        LayoutParams[] layoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()]);

        final LayoutParams previousStep = (this.actualStep>0)?layoutParams[this.actualStep-1]:null;
        final LayoutParams actualStep = (this.actualStep<this.numberSteps)?layoutParams[this.actualStep]:null;
        final LayoutParams nextStep = (this.actualStep<(this.numberSteps-1))?layoutParams[this.actualStep+1]:null;

        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            if ((unloadingStep > -1) && (child.getId() == layoutParams[this.unloadingStep].layout)) continue;
            ViewGroup.LayoutParams vLayoutParams = child.getLayoutParams();
            if (!(vLayoutParams instanceof MarginLayoutParams)) continue;
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) vLayoutParams;
            final int childMarginLeft = marginLayoutParams.leftMargin;
            final int childMarginRight = marginLayoutParams.rightMargin;
            final int childMarginTop = marginLayoutParams.topMargin;
            final int childMarginBottom = marginLayoutParams.bottomMargin;

            int childLeft = l+childMarginLeft + paddingLeft;
            int childTop = t+childMarginTop + paddingTop + ((int)((this.showActionBar)?this.actionBarHeight:0));
            int childRight = r-childMarginRight - paddingRight;
            int childBottom = b-childMarginBottom - paddingBottom;

            boolean willLayout = false;

            if ((previousStep != null) && (child.getId() == previousStep.getViewID())) {
                childLeft -= (int)(width+actualPosition);
                childRight-= (int)(width+actualPosition);
                willLayout=true;
            } else if (child.getId() == actualStep.getViewID()) {
                childLeft -= (int)actualPosition;
                childRight-= (int)actualPosition;
                willLayout=true;
            } else if ((nextStep != null) && (child.getId() == nextStep.getViewID())) {
                childLeft += (int)(width-actualPosition);
                childRight+= (int)(width-actualPosition);
                willLayout=true;
            }

            if (willLayout)
                child.layout(childLeft,childTop,childRight,childBottom);
        }

        if (this.showActionBar) {
            this.drawActionBar();
            this.actionBarView.layout(l + paddingLeft, t + paddingTop, r - paddingRight, (int) (t + paddingTop + this.actionBarHeight));
        }
    }

    public OnClickListener nextButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            openNext();
        }
    };
    public OnClickListener backButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            openPrevious();
        }
    };

    @Override
    public void computeScroll() {
        if (this.scrolling) {
            if (this.scroller.computeScrollOffset()) {
                setCurrentPosition(this.scroller.getCurrX());
            } else {
                int previousStep = this.actualStep;
                this.scrolling = false;
                if (this.actualPosition > 0) {
                    this.actualStep++;

                    if (this.transitionListener != null)
                        this.transitionListener.OnEndTransition(this.actualStep,previousStep);

                    if (this.actualStep > 1)
                        this.removeChild(this.actualStep-2);
                    if (this.actualStep < (this.numberSteps-1))
                        this.inflateChild(this.actualStep+1);
                }
                else if (this.actualPosition < 0) {
                    this.actualStep--;

                    if (this.transitionListener != null)
                        this.transitionListener.OnEndTransition(this.actualStep,previousStep);

                    if (this.actualStep < (this.numberSteps-2))
                        this.removeChild(this.actualStep+2);
                    if (this.actualStep > 0)
                        this.inflateChild(this.actualStep-1);
                }
                this.setCurrentPosition(0);
            }
        }
    }
    protected void setCurrentPosition(float newPosition) {
        this.movePanels(newPosition - this.actualPosition);
    }
    protected void movePanels (float distance) {
        this.actualPosition = saturateNewPosition(this.actualPosition+distance);
        invalidate();
        requestLayout();
    }
    protected float saturateNewPosition(float newPosition) {
        LayoutParams[] layoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()]);
        float leftBound = 0;
        float rightBound = 0;
        if (this.actualStep > 0)
            leftBound = -(this.getMeasuredWidth() + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep-1].getViewID()).getLayoutParams()).leftMargin + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep - 1].getViewID()).getLayoutParams()).rightMargin);
        if (this.actualStep < (this.numberSteps-1))
            rightBound = this.getMeasuredWidth() + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep+1].getViewID()).getLayoutParams()).leftMargin + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep+1].getViewID()).getLayoutParams()).rightMargin;

        return Math.max(Math.min(newPosition,rightBound),leftBound);
    }
    protected void movePanels (float distance, int duration) {
        int animationDuration = Math.min(duration, this.maxAnimationDuration);
        float finalDistance = saturateNewPosition(this.actualPosition+distance) - this.actualPosition;
        this.scroller.abortAnimation();
        this.scrolling = true;
        this.scroller.startScroll(Math.round(this.actualPosition), 0, Math.round(finalDistance), 0, animationDuration);
        invalidate();
    }

    public void openPrevious() {
        openPrevious(this.buttonPressedAnimationDuration);
    }
    protected void openPrevious(int animationDuration) {
        if (this.actualStep <= 0) return;
        if ((this.transitionListener != null) && (!this.transitionListener.OnBeginTransition(this.actualStep,this.actualStep-1))) return;
        LayoutParams[] layoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()]);
        float leftBound = this.getMeasuredWidth() + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep-1].getViewID()).getLayoutParams()).leftMargin + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep - 1].getViewID()).getLayoutParams()).rightMargin;
        float distance = actualPosition-leftBound;
        movePanels(distance, animationDuration);
    }
    public void openNext() {
        openNext(this.buttonPressedAnimationDuration);
    }
    protected void openNext(int animationDuration) {
        if (this.actualStep >= (this.numberSteps-1)) return;
        if ((this.transitionListener != null) && (!this.transitionListener.OnBeginTransition(this.actualStep,this.actualStep+1))) return;
        LayoutParams[] layoutParams = this.layouts.toArray(new LayoutParams[this.layouts.size()]);
        float rightBound = -(this.getMeasuredWidth() + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep+1].getViewID()).getLayoutParams()).leftMargin + ((MarginLayoutParams)this.findViewById(layoutParams[this.actualStep+1].getViewID()).getLayoutParams()).rightMargin);
        float distance = actualPosition-rightBound;
        movePanels(distance, animationDuration);
    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams implements Comparable {

        private int layout = -1;
        public void setLayout(int layout) {
            this.layout = layout;
        }
        public int getLayout() {
            return  this.layout;
        }

        private int stepOrder = -1;
        public void setStepOrder(int stepOrder) {
            this.stepOrder = stepOrder;
        }
        public int getStepOrder() {
            return this.stepOrder;
        }

        private Integer previousStepButton = -1;
        public void setPreviousStepButton(Integer previousStepButton) {
            this.previousStepButton = previousStepButton;
        }
        public Integer getPreviousStepButton () {
            return this.previousStepButton;
        }

        private Integer nextStepButton = -1;
        public void setNextStepButton(Integer nextStepButton) {
            this.nextStepButton = nextStepButton;
        }
        public Integer getNextStepButton(){
            return this.nextStepButton;
        }

        private String stepTitle;
        public void setStepTitle(String stepTitle) {
            this.stepTitle = stepTitle;
        }
        public String getStepTitle() {
            return this.stepTitle;
        }

        private String stepSubtitle;
        public void setStepSubtitle(String stepSubtitle) {
            this.stepSubtitle = stepSubtitle;
        }
        public String getStepSubtitle() {
            return this.stepSubtitle;
        }

        private int viewID = -2;
        public void setViewID(int viewID) {
            this.viewID = viewID;
        }
        public int getViewID() {
            return this.viewID;
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            int layout = -1;
            int stepOrder = -1;
            int previousStepButton = -1;
            int nextStepButton = -1;

            String stepTitle = null;
            String stepSubtitle = null;

            TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.SlidingStepsLayout_Child, 0, 0);

            final int indexCount = a.getIndexCount();
            for (int index = 0; index < indexCount; index++) {
                switch (a.getIndex(index)) {
                    case R.styleable.SlidingStepsLayout_Child_layout:
                        layout = a.getResourceId(R.styleable.SlidingStepsLayout_Child_layout,-1);
                        break;
                    case R.styleable.SlidingStepsLayout_Child_stepOrder:
                        stepOrder = a.getInt(R.styleable.SlidingStepsLayout_Child_stepOrder,-1);
                        break;
                    case R.styleable.SlidingStepsLayout_Child_previousStepButton:
                        previousStepButton = a.getResourceId(R.styleable.SlidingStepsLayout_Child_previousStepButton,-1);
                        break;
                    case R.styleable.SlidingStepsLayout_Child_nextStepButton:
                        nextStepButton = a.getResourceId(R.styleable.SlidingStepsLayout_Child_nextStepButton,-1);
                        break;
                    case R.styleable.SlidingStepsLayout_Child_stepTitle:
                        stepTitle = a.getString(R.styleable.SlidingStepsLayout_Child_stepTitle);
                        break;
                    case R.styleable.SlidingStepsLayout_Child_stepSubtitle:
                        stepSubtitle = a.getString(R.styleable.SlidingStepsLayout_Child_stepSubtitle);
                        break;
                }
            }

            if ((layout >= 0) && (stepOrder >= 0)) {
                this.setLayout(layout);
                this.setStepOrder(stepOrder);
                this.setPreviousStepButton((previousStepButton >= 0) ? previousStepButton : null);
                this.setNextStepButton((nextStepButton >= 0) ? nextStepButton : null);
                this.setStepTitle((stepTitle != null) ? stepTitle : "");
                this.setStepSubtitle((stepSubtitle != null) ? stepSubtitle : "");
            }
            a.recycle();
        }
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int layout, int stepOrder, Integer previousStepButton, Integer nextStepButton, String stepTitle, String stepSubtitle) {
            super(width, height);

            this.setLayout(layout);
            this.setStepOrder(stepOrder);
            this.setPreviousStepButton(previousStepButton);
            this.setNextStepButton(nextStepButton);
            this.setStepTitle(stepTitle);
            this.setStepSubtitle(stepSubtitle);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);

            if (layoutParams instanceof LayoutParams) {
                this.setLayout(((LayoutParams) layoutParams).getLayout());
                this.setStepOrder(((LayoutParams) layoutParams).getStepOrder());
                this.setPreviousStepButton(((LayoutParams)layoutParams).getPreviousStepButton());
                this.setNextStepButton(((LayoutParams)layoutParams).getNextStepButton());
                this.setStepTitle(((LayoutParams)layoutParams).getStepTitle());
                this.setStepSubtitle(((LayoutParams)layoutParams).getStepSubtitle());
            }
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof LayoutParams)) throw new InvalidParameterException("Specified object is not an instance of SlidingStepsLayout.LayoutParams.");
            return this.getStepOrder() - ((LayoutParams) o).getStepOrder();
        }
    }
}