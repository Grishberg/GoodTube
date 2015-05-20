package com.grishberg.goodtube.data.containers;

import java.util.List;

/**
 * Created by G on 18.05.15.
 */
public class ResultPageContainer
{
	private String nextPageToken;
	private List<VideoContainer> items;

	public ResultPageContainer(List<VideoContainer> items, String nextPageToken)
	{
		this.items 			= items;
		this.nextPageToken	= nextPageToken;
	}

	public String getNextPageToken()
	{
		return nextPageToken;
	}

	public List<VideoContainer> getItems()
	{
		return items;
	}
}
