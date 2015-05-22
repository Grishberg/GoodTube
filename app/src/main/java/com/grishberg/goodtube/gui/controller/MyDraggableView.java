package com.grishberg.goodtube.gui.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.github.pedrovgs.DraggableView;
import com.grishberg.goodtube.R;

/**
 * Created by G on 22.05.15.
 */
public class MyDraggableView extends DraggableView
{
	protected View mTopView;
	protected View mBottomView;
	protected boolean isMinimized;
	protected boolean isMaximized;

	public MyDraggableView(Context context)
	{
		super(context);
	}
	public MyDraggableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyDraggableView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTopViewRef(View view)
	{
		mTopView = view;
	}

	public void setBottomViewRef(View view)
	{
		mBottomView = view;
	}

	@Override
	public void maximize()
	{
		RelativeLayout.LayoutParams playerParams =
				(RelativeLayout.LayoutParams) mTopView.getLayoutParams();
		playerParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		playerParams.height = getResources().getDimensionPixelSize(R.dimen.player_height);
		FrameLayout container = (FrameLayout)mTopView.getParent().getParent();
		RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams)container.getLayoutParams();
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
		containerParams.bottomMargin = 0;
		containerParams.rightMargin = 0;
		mTopView.requestLayout();
		container.requestLayout();
		isMinimized = false;



		super.maximize();
	}
	@Override
	public void minimize()
	{
		RelativeLayout.LayoutParams playerParams =
				(RelativeLayout.LayoutParams) mTopView.getLayoutParams();
		playerParams.width = getResources().getDimensionPixelSize(R.dimen.player_minimized_width);
		playerParams.height = getResources().getDimensionPixelSize(R.dimen.player_minimized_height);
		FrameLayout container = (FrameLayout)mTopView.getParent().getParent();
		RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams)container.getLayoutParams();
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		containerParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.player_minimized_margin);
		containerParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.player_minimized_margin);
		mTopView.requestLayout();
		container.requestLayout();
		isMinimized = true;

		super.minimize();
	}

}
