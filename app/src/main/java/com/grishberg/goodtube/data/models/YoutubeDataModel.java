package com.grishberg.goodtube.data.models;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.grishberg.goodtube.R;
import com.grishberg.goodtube.gui.fragment.VideoListActivityFragment;
import com.grishberg.goodtube.gui.listeners.GetVideoListListener;
import com.grishberg.goodtube.data.containers.ResultPageContainer;
import com.grishberg.goodtube.data.containers.VideoContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by G on 18.05.15.
 */
public class YoutubeDataModel
{
	public static final String TAG						= "YoutubeDataModel";
	public static final String YOUTUBE_API_KEY			="AIzaSyD0INVrE2YHbGJqhU3iTjzLSPOFDAuactE";
	public static final String LOCALE_RU				= "RU";
	public static final String CHART_MOST_POPULAR		= "mostPopular";
	public static final String MOSTPOPULAR_PLAYLIST_ID	= "PLgMaGEI-ZiiZ0ZvUtduoDRVXcU5ELjPcI";

	private YouTube mYoutube;

	public YoutubeDataModel(Context context)
	{

		mYoutube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer()
		{
			@Override
			public void initialize(HttpRequest request) throws IOException
			{

			}
		}).setApplicationName(context.getString(R.string.app_name)).build();
	}

	// асинхронный поиск по ключевым словам
	public void getSearchResult(  String searchKeyword
			,  String pageToken
			,  GetVideoListListener resultListener)
	{
		SearchVideoTask task = new SearchVideoTask(pageToken,searchKeyword,resultListener);
		task.execute();
	}

	// асинхронная загрузка популярного видео
	public void getMostPopularResult( String pageToken
			,  GetVideoListListener resultListener)
	{
		PopularVideoTask task = new PopularVideoTask(pageToken,resultListener);
		task.execute();
	}

	public void getVideoInfo( String id, GetVideoListListener resultListener)
	{
		VideoInfoTask task	= new VideoInfoTask(id,resultListener);
		task.execute();
	}

	private ResultPageContainer getVideoInfo(String videoId)
	{
		try
		{
			YouTube.Videos.List search = mYoutube.videos().list("contentDetails,statistics");
			search.setKey(YOUTUBE_API_KEY);
			search.setId(videoId);


			VideoListResponse response = search.execute();
			List<Video> results = response.getItems();

			List<VideoContainer> items = new ArrayList<VideoContainer>();
			for (Video result : results)
			{
				VideoContainer item = new VideoContainer();
				item.setDuration(result.getContentDetails().getDuration());
				item.setViewCount(result.getStatistics().getViewCount().longValue());
				item.setLikeCount(result.getStatistics().getLikeCount().longValue());
				item.setDislikeCount(result.getStatistics().getDislikeCount().longValue());
				items.add(item);
			}
			if( items.size() > 0)
			{
				return new ResultPageContainer(items, null, null);
			}
		}
		catch (Exception e)
		{

		}
		return null;
	}

	// поиск видео
	private ResultPageContainer search(String searchKeyword, String pageToken)
	{
		try
		{
			YouTube.Search.List search	= mYoutube.search().list("id,snippet");
			search.setKey(YOUTUBE_API_KEY);
			search.setType("video");
			//search.setFields("items(id/videoId,snippet/publishedAt,snippet/title,snippet/description,snippet/thumbnails/default/url)");
			search.setMaxResults(VideoListActivityFragment.ITEMS_PER_PAGE);
			search.setQ(searchKeyword);

			if(pageToken != null && pageToken.length() > 0)
			{
				search.setPageToken(pageToken);
			}
			else
			{
			}

			SearchListResponse response	= search.execute();
			List<SearchResult> results	= response.getItems();
			String nextPageToken 		= response.getNextPageToken();
			String prevPageToken		= response.getPrevPageToken();
			List<VideoContainer> items	= new ArrayList<VideoContainer>();
			for(SearchResult result:results)
			{
				VideoContainer item = new VideoContainer();
				item.setTitle(result.getSnippet().getTitle());
				item.setDescription(result.getSnippet().getDescription());
				item.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
				item.setId(result.getId().getVideoId());
				item.setPublishedAt(result.getSnippet().getPublishedAt().getValue());

				items.add(item);
			}
			return new ResultPageContainer(items, prevPageToken, nextPageToken);
		}catch(IOException e)
		{
			Log.d(TAG, "Could not search: "+e);
			return null;
		}
	}


	// популярное playlist
	private ResultPageContainer getPlaylistMostPopular(String pageToken)
	{
		try
		{
			YouTube.PlaylistItems.List search	= mYoutube.playlistItems().list("snippet");
			search.setKey(YOUTUBE_API_KEY);
			search.setPlaylistId(MOSTPOPULAR_PLAYLIST_ID);
			search.setMaxResults(VideoListActivityFragment.ITEMS_PER_PAGE);

			if(pageToken != null)
			{
				search.setPageToken(pageToken);
			}

			PlaylistItemListResponse response	= search.execute();
			String nextPageToken		= response.getNextPageToken();
			String prevPageToken		= response.getPrevPageToken();
			List<PlaylistItem> results 	= response.getItems();

			List<VideoContainer> items = new ArrayList<VideoContainer>();
			for(PlaylistItem result:results)
			{
				VideoContainer item = new VideoContainer();
				item.setTitle(result.getSnippet().getTitle());
				item.setDescription(result.getSnippet().getDescription());
				item.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
				item.setId(result.getSnippet().getResourceId().getVideoId());
				item.setPublishedAt(result.getSnippet().getPublishedAt().getValue());
				items.add(item);
			}
			return new ResultPageContainer(items, prevPageToken	, nextPageToken) ;
		}catch(IOException e)
		{
			Log.d(TAG, "Could not search: "+e);
			return null;
		}
	}
	// video.list
	private ResultPageContainer getMostPopular(String pageToken)
	{
		try
		{
			YouTube.Videos.List search	= mYoutube.videos().list("id,snippet");
			search.setKey(YOUTUBE_API_KEY);
			search.setRegionCode(LOCALE_RU);
			search.setChart(CHART_MOST_POPULAR);
			search.setMaxResults(VideoListActivityFragment.ITEMS_PER_PAGE);

			if(pageToken != null)
			{
				search.setPageToken(pageToken);
			}

			VideoListResponse response	= search.execute();
			String nextPageToken		= response.getNextPageToken();
			String prevPageToken		= response.getPrevPageToken();
			List<Video> results 		= response.getItems();

			List<VideoContainer> items = new ArrayList<VideoContainer>();
			for(Video result:results)
			{
				VideoContainer item = new VideoContainer();
				item.setTitle(result.getSnippet().getTitle());
				item.setDescription(result.getSnippet().getDescription());
				item.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
				item.setId(result.getId());
				item.setPublishedAt(result.getSnippet().getPublishedAt().getValue());
				items.add(item);
			}
			return new ResultPageContainer(items, prevPageToken	, nextPageToken) ;
		}catch(IOException e)
		{
			Log.d(TAG, "Could not search: "+e);
			return null;
		}
	}

	//---------------------------------------
	// Асинхронное извлечение результатов поиска
	private class SearchVideoTask extends AsyncTask<Void, Void, ResultPageContainer >
	{
		private String nextPageToken;
		private String keyword;
		GetVideoListListener handler;

		public SearchVideoTask (String nextPageToken, String keyword, GetVideoListListener handler)
		{
			this.nextPageToken	= nextPageToken;
			this.keyword		= keyword;
			this.handler		= handler;
		}

		protected ResultPageContainer doInBackground(Void... params)
		{
			ResultPageContainer result = null;
			try
			{
				result	= search(keyword, nextPageToken);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return result;
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			handler.onResult(result);
		}
	}

	// Асинхронное извлечение результатов поиска
	private class PopularVideoTask extends AsyncTask<Void, Void, ResultPageContainer >
	{
		private String nextPageToken;
		GetVideoListListener handler;

		public PopularVideoTask (String nextPageToken,GetVideoListListener handler)
		{
			this.nextPageToken	= nextPageToken;
			this.handler		= handler;
		}
		protected ResultPageContainer doInBackground(Void... params)
		{
			ResultPageContainer result = null;

			try
			{
				result = getPlaylistMostPopular(nextPageToken);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return result;
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			handler.onResult(result);
		}
	}


	//---------------------------------------
	// Асинхронное извлечение результатов поиска
	private class VideoInfoTask extends AsyncTask<Void, Void, ResultPageContainer >
	{
		private String id;
		GetVideoListListener handler;

		public VideoInfoTask (String id,GetVideoListListener handler)
		{
			this.id	= id;
			this.handler	= handler;
		}
		protected ResultPageContainer doInBackground(Void... params)
		{
			ResultPageContainer result = null;
			try
			{
				result = getVideoInfo(id);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			handler.onResult(result);
		}
	}

}
