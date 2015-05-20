package com.grishberg.goodtube.gui.layout;

/**
 * Created by G on 17.05.15.
 */

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.api.services.youtube.YouTube;
import com.grishberg.goodtube.R;

/**
 * Created by G on 17.05.15.
 *  класс для отображения подробного видео
 */
public class PlayerLayout extends FrameLayout//ViewGroup
{
	private static final String		TAG	= "GoodTube.VDH";
	private final ViewDragHelper	mDragHelper;
	//private View					mPlayerView;
	private View					mHeaderView;
	private View					mDescriptionView;
	private float 					mInitialMotionX;
	private float 					mInitialMotionY;
	private int 					mDragRangeY;
	private int						mDragRangeX;
	private int 					mTop;
	private int						mLeft;
	private float 					mDragOffsetY;
	private float 					mDragOffsetX;
	private YouTubePlayer			mYoutubePlayer;
	private int 					mLastWidth;
	private int						mLastHeight;

	public PlayerLayout(Context context)

	{
		this(context, null);

	}

	public PlayerLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public PlayerLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
	}

	public void setYoutubePlayer(YouTubePlayer player)
	{
		mYoutubePlayer	= player;
	}


	@Override
	protected void onFinishInflate()
	{
		mHeaderView			= findViewById(R.id.fragment_youtube_player);
		mDescriptionView	= findViewById(R.id.descriptionPanel);
	}

	public void maximize()
	{
		mHeaderView.setVisibility(View.VISIBLE);
		mHeaderView.setAlpha(1);
		smoothSlideTo(0f);
	}

	public void minimize()
	{
		smoothSlideTo(1f);
	}

	// плавное движение вверх или вниз
	boolean smoothSlideTo(float slideOffset)
	{
		final int topBound = getPaddingTop();
		final int leftBound = getPaddingLeft();

		int y = (int) (topBound + slideOffset * mDragRangeY);
		int x = (int) (leftBound + slideOffset * mDragRangeX);
		if (mDragHelper.smoothSlideViewTo(mHeaderView,x, y))
		{
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}


	@Override
	public void computeScroll()
	{
		if (mDragHelper.continueSettling(true))
		{
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	// обработка касаний и движений
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
		boolean shouldIntercept	= mDragHelper.shouldInterceptTouchEvent(ev);
		return  shouldIntercept && mHeaderView.getVisibility() == View.VISIBLE;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		mDragHelper.processTouchEvent(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();


		boolean isHeaderViewUnder = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);

		switch (action & MotionEventCompat.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:
			{
				mInitialMotionX = x;
				mInitialMotionY = y;

				break;
			}

			case MotionEvent.ACTION_UP:
			{
				// действие при отпускании передвигаемого элемента
				final float dx = x - mInitialMotionX;
				final float dy = y - mInitialMotionY;
				final int slop = mDragHelper.getTouchSlop();

				if (dx * dx + dy * dy < slop * slop && isHeaderViewUnder)
				{
					if (mDragOffsetY == 0)
					{
						smoothSlideTo(1f);
					} else
					{
						smoothSlideTo(0f);
					}
				}
				break;
			}
		}


		return isHeaderViewUnder && isViewHit(mHeaderView, (int) x, (int) y) ||
				isViewHit(mDescriptionView, (int) x, (int) y);
	}


	private boolean isViewHit(View view, int x, int y)
	{
		int[] viewLocation = new int[2];
		view.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
				screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int maxWidth	= MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight	= MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
				resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		mDragRangeY = getHeight() - (mLastHeight-mLastHeight/2);

		float headerLeft	= 0;

		if(mLastHeight > 0)
		{
			headerLeft	= mLastWidth - (float)mLastWidth *  (float)mHeaderView.getHeight() / (float)mLastHeight;
		}
		mDragRangeX		= getWidth() - (mLastWidth / 2);

		//mHeaderView.layout(
		//		(int) headerLeft,
		//		mTop,
		//		(int) headerLeft + mHeaderView.getMeasuredWidth(),
		//		mTop + mHeaderView.getMeasuredHeight());

		mHeaderView.layout(
				mLeft,
				mTop,
				mLeft + mHeaderView.getMeasuredWidth(),
				mTop + mHeaderView.getMeasuredHeight());

		mDescriptionView.layout(
				0,
				mTop + mHeaderView.getMeasuredHeight(),
				r,
				mTop + b);
	}

	//---------------- класс ---------------

	private class DragHelperCallback extends ViewDragHelper.Callback
	{
		public void onViewDragStateChanged(int state)
		{
			switch (state)
			{
				case ViewDragHelper.STATE_DRAGGING:
					if (mDragOffsetY == 0.0 )
					{
						// если перетаскивание сверху вниз
						mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
					}
					// начало перетаскивания
					Log.d(TAG,"STATE_DRAGGING");
					if(mLastWidth == 0)
					{
						mLastHeight		= mHeaderView.getHeight();
						mLastWidth		= mHeaderView.getWidth();
					}
					break;

				case ViewDragHelper.STATE_IDLE:
					// завершилось перетаскивание
					// в зависимости от того вверху или внизу оказался экран, изменить тип плеера
					if(mDragOffsetY == 0.0)
					{
						// наверху
						mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
					}
					if(mDragOffsetY == 1.0 && mDragOffsetX == 0.0)
					{
						mHeaderView.setVisibility(View.GONE);
						mYoutubePlayer.pause();
					}
					Log.d(TAG,"STATE_IDLE mDragOffsetX="+mDragOffsetX+", mDragOffsetY="+mDragOffsetY);
					break;

				case ViewDragHelper.STATE_SETTLING:
					Log.d(TAG,"STATE_SETTLING");
					break;
			}
		}

		@Override
		public boolean tryCaptureView(View child, int pointerId)
		{
			return child == mHeaderView;
		}

		// функция вызывается во время движения
		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
		{
			mTop	= top;
			mLeft	= left;
			mDragOffsetY		= (float) top / mDragRangeY;
			if (mDragRangeX != 0.0) mDragOffsetX		= (float) left / (float)mDragRangeX;
			float multFactor	= 1.0f - mDragOffsetY / 2.0f;


			ViewGroup.LayoutParams param = mHeaderView.getLayoutParams();
			param.height	= (int)(mLastHeight	* multFactor);
			param.width		= (int)(mLastWidth	* multFactor);
			mHeaderView.setLayoutParams(param);

			if(mDragOffsetY == 1.0)
			{
				//mHeaderView.setAlpha(0.8f);
				//Log.d(TAG, "onViewPositionChanged setAlpha ="+mDragOffsetX);
			}
			// скрытие панели с описанием
			mDescriptionView.setAlpha(1 - mDragOffsetY);
			requestLayout();
		}

		// событие при отпускании контрола
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel)
		{
			int top		= getPaddingTop();
			int left	= getPaddingLeft();
			if (yvel > 0 || (yvel == 0 && mDragOffsetY > 0.5f))
			{
				top		+= mDragRangeY;
				left	+= mDragRangeX;
			}
			if( (xvel < 0 && mDragOffsetY == 1.0) || (xvel == 0 && mDragOffsetX > 0.5f && mDragOffsetY == 1.0))
			{
				// скрыть компонент и поставить на паузу
				left = 0;
			}
			Log.d(TAG," x="+xvel+", y="+yvel);
			mDragHelper.settleCapturedViewAt(left, top);
			invalidate();
		}

		@Override
		public int getViewVerticalDragRange(View child)
		{
			return mDragRangeY;
		}

		@Override
		public int getViewHorizontalDragRange(View child)
		{
			return mDragRangeX;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy)
		{
			final int topBound = getPaddingTop();
			final int bottomBound = getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

			final int newTop = Math.min(Math.max(top, topBound), bottomBound);
			return newTop;
		}

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx)
		{
			final int leftBound = getPaddingLeft();
			final int RightBound = getWidth() - mHeaderView.getWidth() - mHeaderView.getPaddingRight();

			final int newLeft = Math.min(Math.max(left, leftBound), RightBound);
			return newLeft;
		}
	}

}
