package com.grishberg.goodtube.gui.listeners;

import android.widget.AbsListView;

/**
 * Created by G on 18.05.15.
 */

public abstract class InfinityScrollListener implements AbsListView.OnScrollListener
{
	private int itemsOffset     	= 5;    // за сколько значений до конца списка начать  отображать
	private long bufferItemCount 	= 10;
	private int currentPage 		= 0;
	private int itemCount 			= 0;
	private boolean isLoading 		= true;


	public InfinityScrollListener(long bufferItemCount)
	{
		this.bufferItemCount = bufferItemCount;
	}

	public abstract void loadMore(int page, int totalItemsCount);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
// Do Nothing
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		if (totalItemCount < itemCount)
		{
			this.itemCount = totalItemCount;
			if (totalItemCount == 0)
			{
				this.isLoading = true;
			}
		}

		if (isLoading && (totalItemCount > itemCount))
		{
			isLoading = false;
			itemCount = totalItemCount;
			currentPage++;
		}

		if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + bufferItemCount + itemsOffset))
		{
			loadMore(currentPage + 1, totalItemCount);
			isLoading = true;
		}
	}
}