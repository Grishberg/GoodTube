package com.grishberg.goodtube.data.models;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
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
	public void getSearchResult( final String searchKeyword
			, final String pageToken
			, final GetVideoListListener resultListener)
	{
		SearchVideoTask task = new SearchVideoTask();
		task.execute(new SearchParamers()
		{
			@Override
			public String getNextPageToken()
			{
				return pageToken;
			}

			@Override
			public String getKeywords()
			{
				return searchKeyword;
			}

			@Override
			public void onDone(ResultPageContainer result)
			{
				resultListener.onResult(result);
			}
		});
	}

	// асинхронная загрузка популярного видео
	public void getMostPopularResult(final String pageToken
			, final GetVideoListListener resultListener)
	{
		PopularVideoTask task = new PopularVideoTask();
		task.execute(new SearchParamers()
		{
			@Override
			public String getNextPageToken()
			{
				return pageToken;
			}

			@Override
			public String getKeywords()
			{
				return null;
			}

			@Override
			public void onDone(ResultPageContainer result)
			{
				resultListener.onResult(result);
			}
		});
	}

	public void getVideoInfo(final String id, final GetVideoListListener resultListener)
	{
		VideoInfoTask task	= new VideoInfoTask();
		task.execute(new InfoParamers()
		{
			@Override
			public String getVideoId()
			{
				return id;
			}

			@Override
			public void onDone(ResultPageContainer result)
			{
				resultListener.onResult(result);
			}
		});
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
				items.add(item);
			}
			if( items.size() > 0)
			{
				return new ResultPageContainer(items, null);
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
			search.setFields("items(id/videoId,snippet/publishedAt,snippet/title,snippet/description,snippet/thumbnails/default/url)");
			search.setQ(searchKeyword);
			if(pageToken != null && pageToken.length() > 0)
			{
				search.setPageToken(pageToken);
			}

			SearchListResponse response	= search.execute();
			List<SearchResult> results	= response.getItems();
			String nextPageToken 		= response.getNextPageToken();

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
			return new ResultPageContainer(items, nextPageToken);
		}catch(IOException e)
		{
			Log.d(TAG, "Could not search: "+e);
			return null;
		}
	}

	// популярное
	private ResultPageContainer getMostPopular(String pageToken)
	{
		try
		{
			YouTube.Videos.List search	= mYoutube.videos().list("id,snippet,contentDetails");
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
			return new ResultPageContainer(items,nextPageToken) ;
		}catch(IOException e)
		{
			Log.d(TAG, "Could not search: "+e);
			return null;
		}
	}

	//
	interface SearchParamers
	{
		public String 	getNextPageToken();
		public String 	getKeywords();
		public void 	onDone(ResultPageContainer result);
	}
	//---------------------------------------
	// Асинхронное извлечение результатов поиска
	private class SearchVideoTask extends AsyncTask<SearchParamers, Void, ResultPageContainer >
	{
		private SearchParamers inputParam;
		protected ResultPageContainer doInBackground(SearchParamers... params)
		{
			inputParam			= params.length > 0 ? params[0] : null;
			String nexPageToken	= inputParam.getNextPageToken();
			String keyword		= inputParam.getKeywords();


			return search(keyword, nexPageToken);
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			inputParam.onDone(result);
		}
	}

	// Асинхронное извлечение результатов поиска
	private class PopularVideoTask extends AsyncTask<SearchParamers, Void, ResultPageContainer >
	{
		private SearchParamers inputParam;
		protected ResultPageContainer doInBackground(SearchParamers... params)
		{
			try
			{
				TimeUnit.SECONDS.sleep(1);
			}
			catch (Exception e)
			{

			}
			inputParam			= params.length > 0 ? params[0] : null;
			String nexPageToken	= inputParam.getNextPageToken();


			return getMostPopular(nexPageToken);
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			inputParam.onDone(result);
		}
	}

	interface InfoParamers
	{
		public String 	getVideoId();
		public void 	onDone(ResultPageContainer result);
	}
	//---------------------------------------
	// Асинхронное извлечение результатов поиска
	private class VideoInfoTask extends AsyncTask<InfoParamers, Void, ResultPageContainer >
	{
		private InfoParamers inputParam;
		protected ResultPageContainer doInBackground(InfoParamers... params)
		{
			inputParam		= params.length > 0 ? params[0] : null;
			String id		= inputParam.getVideoId();

			return getVideoInfo(id);
		}

		protected void onPostExecute(ResultPageContainer result)
		{
			inputParam.onDone(result);
		}
	}

}
