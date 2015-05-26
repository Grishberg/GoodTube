package com.grishberg.goodtube.gui.animation;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout.LayoutParams;

/**
 * Created by G on 25.05.15.
 */
public class ExpandAnimation extends Animation {
	private View mAnimatedView;
	private LayoutParams mViewLayoutParams;
	private int mMarginStart, mMarginEnd, mHeightStart, mHeightEnd;
	private boolean mIsVisibleAfter = false;
	private boolean mWasEndedAlready = false;
	private int		mOldHeight;

	/**
	 * Initialize the animation
	 * @param view The layout we want to animate
	 * @param duration The duration of the animation, in ms
	 */
	public ExpandAnimation(View view, int duration) {

		setDuration(duration);
		mAnimatedView = view;
		mViewLayoutParams = (LayoutParams) view.getLayoutParams();

		// decide to show or hide the view
		mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

		mMarginStart = mViewLayoutParams.bottomMargin;
		mMarginEnd = (mMarginStart == 0 ? (0- view.getHeight()) : 0);


		view.setVisibility(View.VISIBLE);
		int h = view.getHeight();
		mHeightStart 	= 0;
		if(mIsVisibleAfter)mHeightStart = view.getHeight();
		mHeightEnd 		= (mHeightStart == 0 ? (mViewLayoutParams.height) : 0);
		mOldHeight		= mHeightStart;

		mViewLayoutParams.height	= mHeightStart;

	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);

		if (interpolatedTime < 1.0f) {

			// Calculating the new bottom margin, and setting it
			mViewLayoutParams.height		= mHeightStart
					+ (int) ((mHeightEnd - mHeightStart) * interpolatedTime);


			//mViewLayoutParams.bottomMargin	= mMarginStart
			//		+ (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

			// Invalidating the layout, making us seeing the changes we made
			mAnimatedView.requestLayout();

			// Making sure we didn't run the ending before (it happens!)
		} else if (!mWasEndedAlready) {
			mViewLayoutParams.height = mHeightEnd;


			//mViewLayoutParams.bottomMargin = mMarginEnd;
			mAnimatedView.requestLayout();

			if (mIsVisibleAfter) {
				mViewLayoutParams.height = mOldHeight;
				mAnimatedView.setLayoutParams(mViewLayoutParams);
				mAnimatedView.setVisibility(View.GONE);
			}
			mWasEndedAlready = true;
		}
	}
}
