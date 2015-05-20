package com.grishberg.goodtube.gui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.grishberg.goodtube.R;
import com.grishberg.goodtube.data.adapters.VideoListAdapter;
import com.grishberg.goodtube.data.containers.ResultPageContainer;
import com.grishberg.goodtube.data.containers.VideoContainer;
import com.grishberg.goodtube.data.models.YoutubeDataModel;
import com.grishberg.goodtube.gui.layout.PlayerLayout;
import com.grishberg.goodtube.gui.listeners.GetVideoListListener;
import com.grishberg.goodtube.gui.listeners.InfinityScrollListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class VideoListActivityFragment extends Fragment implements YouTubePlayer.OnInitializedListener
{
	public static final String		TAG				= "YoutubeApp";
	public static final long		ITEMS_PER_PAGE	= 10;
	private ListView 				mListView;
	private List<VideoContainer> 	mVideoList;
	private EditText				mSearchEdit;

	private TextView				mDescriptionTextView;
	private TextView				mDurationTextView;
	private TextView				mViewCountTextView;


	private VideoListAdapter 		mVideoListAdapter;
	private YoutubeDataModel 		mDataModel;
	private String					mNextPageToken;
	private String					mSearchKeyword;
	private boolean					getMostPopularMode;
	private ProgressBar				mProgressBar;
	private int 					mShortAnimationDuration;
	private boolean					mIsProgressBarVisible;
	private YouTubePlayerSupportFragment	mYoutubePlayerFragment;
	private YouTubePlayer 			mPlayer;
	private PlayerLayout 			mYoutubeLayout;
	public VideoListActivityFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view	= inflater.inflate(R.layout.fragment_video_list, container, false);

		// элементы управления детального просмотра видео
		mYoutubeLayout			= (PlayerLayout) view.findViewById(R.id.dragLayout);

		mProgressBar	= (ProgressBar) view.findViewById(R.id.video_list_progress);
		mListView		= (ListView) view.findViewById(R.id.video_list_view);
		//mListView.setEmptyView(mProgressBar);
		mSearchEdit		= (EditText) view.findViewById(R.id.searchTextEdit);

		mDescriptionTextView	= (TextView) view.findViewById(R.id.tvDescription);
		mDurationTextView		= (TextView) view.findViewById(R.id.tvDuraion);
		mViewCountTextView		= (TextView) view.findViewById(R.id.tvViewCount);

		mDataModel		= new YoutubeDataModel(getActivity());

		getMostPopularMode	= true;
		mVideoList			= new ArrayList<VideoContainer>();
		mVideoListAdapter	= new VideoListAdapter(getActivity(), mVideoList);
		mListView.setAdapter(mVideoListAdapter);



		//----------------- реализация inifinite scroll ------------------------
		mListView.setOnScrollListener(new InfinityScrollListener(ITEMS_PER_PAGE)
		{
			// событие возникает во время того как скроллинг дойдет до конца
			@Override
			public void loadMore(int page, int totalItemsCount)
			{
				// загрузить еще данных
				getNextPage();
			}
		});

		mSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				final boolean isEnterEvent = event != null
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
				final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
				final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

				if (actionId == EditorInfo.IME_ACTION_DONE || isEnterUpEvent)
				{
					// Do your action here
					getMostPopularMode = false;
					mNextPageToken = null;
					mVideoList.clear();
					mSearchKeyword = v.getText().toString();
					showProgressBar();
					getNextPage();
					return true;
				} else if (isEnterDownEvent)
				{
					// Capture this event to receive ACTION_UP
					return true;
				} else
				{
					// We do not care on other actions
					return false;
				}
			}
		});

		// настройка анимации перехода
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);

		setupYoutubeFragment();

		showProgressBar();

		getNextPage();


		// обработчик нажатия на элемент ListView
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				VideoContainer item = (VideoContainer) mListView.getAdapter().getItem(i);
				mDataModel.getVideoInfo(item.getId(), new GetVideoListListener()
				{
					@Override
					public void onResult(ResultPageContainer result)
					{
						if (result == null) return;
						mDurationTextView.setText("продолжительность: " + result.getItems().get(0).getDuration());
						mViewCountTextView.setText("количество просмотров: " + result.getItems().get(0).getViewCount());
					}
				});
				mPlayer.loadVideo(item.getId());
				mPlayer.play();
				mDescriptionTextView.setText(item.getTitle());
				mDurationTextView.setText("");
				mViewCountTextView.setText("");



				mYoutubeLayout.setVisibility(View.VISIBLE);
				mYoutubeLayout.maximize();
			}
		});
		mYoutubeLayout.minimize();
		mYoutubeLayout.setVisibility(View.GONE);
		return view;
	}

	// инициализация плеера
	private void setupYoutubeFragment()
	{

		mYoutubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
		mYoutubePlayerFragment.initialize(YoutubeDataModel.YOUTUBE_API_KEY, this);
		getChildFragmentManager().beginTransaction().replace(R.id.fragment_youtube_player, mYoutubePlayerFragment).commit();

	}

	public void showProgressBar()
	{
		mIsProgressBarVisible	= true;
		mListView.setAlpha(1f);
		mProgressBar.setAlpha(1f);
		mListView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	// скрытие прогрессбара и отображение списка
	private void hideProgressBar()
	{
		if(!mIsProgressBarVisible) return;
		mListView.setAlpha(0f);
		mListView.setVisibility(View.VISIBLE);

		mListView.animate()
				.alpha(1f)
				.setDuration(mShortAnimationDuration)
				.setListener(null);

		mProgressBar.animate()
				.alpha(0f)
				.setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						mProgressBar.setVisibility(View.GONE);
						mIsProgressBarVisible	= false;
					}
				});
	}

	// извлечь следующую страницу
	public void getNextPage()
	{
		if (getMostPopularMode)
		{
			mDataModel.getMostPopularResult(mNextPageToken, new GetVideoListListener()
			{
				@Override
				public void onResult(ResultPageContainer result)
				{
					onGetPageResult(result);
				}
			});
		}
		else
		{
			mDataModel.getSearchResult(mSearchKeyword, mNextPageToken, new GetVideoListListener()
			{
				@Override
				public void onResult(ResultPageContainer result)
				{
					onGetPageResult(result);
				}
			});
		}

	}

	public void onGetPageResult(ResultPageContainer result)
	{
		hideProgressBar();
		if(result == null) return;
		mNextPageToken	= result.getNextPageToken();
		mVideoList.addAll(result.getItems());
		mVideoListAdapter.notifyDataSetChanged();
	}

	//-------------------- инициализация Youtube fragment
	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
										boolean wasRestored)
	{
		mPlayer	= player;
		mYoutubeLayout.setYoutubePlayer(player);
		if (!wasRestored)
		{
			//player.cueVideo("nCgQDjiotG0");
			//player.play();
		}
	}

	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider,
										YouTubeInitializationResult result)
	{
		if (result.isUserRecoverableError()) {
			result.getErrorDialog(this.getActivity(),1).show();
		} else {
			Toast.makeText(this.getActivity(),
					"YouTubePlayer.onInitializationFailure(): " + result.toString(),
					Toast.LENGTH_LONG).show();
		}
	}

}
