package com.grishberg.goodtube.gui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggablePanel;
import com.github.pedrovgs.DraggableView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
//import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.grishberg.goodtube.R;
import com.grishberg.goodtube.data.adapters.VideoListAdapter;
import com.grishberg.goodtube.data.containers.ResultPageContainer;
import com.grishberg.goodtube.data.containers.VideoContainer;
import com.grishberg.goodtube.data.models.YoutubeDataModel;
import com.grishberg.goodtube.gui.listeners.GetVideoListListener;
import com.grishberg.goodtube.gui.listeners.InfinityScrollListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class VideoListActivityFragment extends Fragment
{
	public static final String		TAG						= "YoutubeApp";
	public static final String		PLAYER_PLAY_ENABLED		= "playerPlayEnabled";
	public static final String		PLAYER_PLAY_OFFSET		= "playerPlayOffset";
	public static final String		MOSTPOPULAR_MODE_STATUS	= "mostpopularModeStatus";
	public static final String		PLAYER_FULLSCREEN_STATE	= "playerFullscreenState";
	public static final String		PLAYER_VIDEO_ID			= "playerVideoId";
	public static final String		SEARCH_KEYWORD			= "searchKeyword";



	public static final long		ITEMS_PER_PAGE	= 10;

	DraggableView					mDraggableView;
	private ListView 				mListView;

	private List<VideoContainer> 	mVideoList;
	private EditText				mSearchEdit;


	private VideoListAdapter 				mVideoListAdapter;
	private YoutubeDataModel 				mDataModel;
	private String							mNextPageToken;
	private String							mSearchKeyword;
	private boolean							getMostPopularMode;
	private ProgressBar						mProgressBar;
	private int 							mShortAnimationDuration;
	private boolean							mIsProgressBarVisible;
	private VideoDescriptionFragment		mVideoDescriptionFragment;
//	private YouTubePlayerFragment			mYoutubeFragment;
	private YouTubePlayerView				mYoutubeView;
	//private YouTubePlayerSupportFragment	mYoutubeFragment;
	private YouTubePlayer 					mYoutubePlayer;
	private String							mCurrentVideoId;
	private int playOffset				= 0;
	private int fullscreenState			= 0;
	private boolean mPlayerPlayModeEnabled;
	private boolean mFullscreenStatus;
	//private PlayerLayout 			mYoutubeLayout;
	public VideoListActivityFragment()
	{
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		mSearchKeyword		= "";
		// перемещаемая панель
		mDraggableView		= (DraggableView)  getView().findViewById(R.id.draggable_view);
		mYoutubeView		= (YouTubePlayerView) getView().findViewById(R.id.youtube_view);

		mProgressBar		= (ProgressBar) getView().findViewById(R.id.video_list_progress);
		mListView			= (ListView) getView().findViewById(R.id.video_list_view);
		mSearchEdit			= (EditText) getView().findViewById(R.id.searchTextEdit);

		mDataModel			= new YoutubeDataModel(getActivity());

		getMostPopularMode	= true;
		mVideoList			= new ArrayList<VideoContainer>();
		mVideoListAdapter	= new VideoListAdapter(getActivity(), mVideoList);

		// настройка анимации перехода из прогресс бара на список видео
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_mediumAnimTime);

		if (savedInstanceState != null)
		{
			mPlayerPlayModeEnabled	= savedInstanceState.getBoolean(PLAYER_PLAY_ENABLED);
			getMostPopularMode		= savedInstanceState.getBoolean(MOSTPOPULAR_MODE_STATUS);
			mSearchKeyword			= savedInstanceState.getString(SEARCH_KEYWORD);
			if(mPlayerPlayModeEnabled)
			{
				playOffset 			= savedInstanceState.getInt(PLAYER_PLAY_OFFSET);
				fullscreenState		= savedInstanceState.getInt(PLAYER_FULLSCREEN_STATE);
				mCurrentVideoId		= savedInstanceState.getString(PLAYER_VIDEO_ID);
			}
		}
		else
		{
			Log.d(TAG,"OnCreateView->SavedInstanceState null");
		}

		initiliazeVideListView();
		initiliazeSearchEdit();
		initiliazeYoutubeFragment();
		initializeDraggablePanel();

		mDraggableView.setVisibility(View.GONE);

		showProgressBar();

		getNextPage();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// Save the values you need from your textview into "outState"-object
		super.onSaveInstanceState(outState);
		outState.putBoolean(PLAYER_PLAY_ENABLED, mYoutubePlayer.isPlaying());
		if(mYoutubePlayer.isPlaying())
		{
			outState.putInt(PLAYER_PLAY_OFFSET, mYoutubePlayer.getCurrentTimeMillis());
			outState.putInt(PLAYER_FULLSCREEN_STATE,mYoutubePlayer.getFullscreenControlFlags());
			outState.putString(PLAYER_VIDEO_ID, mCurrentVideoId);
		}
		outState.putBoolean(MOSTPOPULAR_MODE_STATUS, getMostPopularMode);
		outState.putString(SEARCH_KEYWORD, mSearchKeyword);

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_video_list, container, false);

	}

	// инициализация списка видео
	private void initiliazeVideListView()
	{
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

		// обработчик нажатия на элемент ListView
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if (mDraggableView.getVisibility() != View.VISIBLE)
					mDraggableView.setVisibility(View.VISIBLE);
				VideoContainer item = (VideoContainer) mListView.getAdapter().getItem(i);
				mCurrentVideoId = item.getId();
				mDataModel.getVideoInfo(item.getId(), new GetVideoListListener()
				{
					@Override
					public void onResult(ResultPageContainer result)
					{
						if (result == null) return;
						if (mVideoDescriptionFragment != null)
						{
							mVideoDescriptionFragment.setAdditionalData(result.getItems().get(0));
						}
					}
				});
				if (mYoutubePlayer != null)
				{
					mYoutubePlayer.loadVideo(item.getId());
				}
				if (mVideoDescriptionFragment != null)
				{
					mVideoDescriptionFragment.setMainData(item);
				}

				mDraggableView.maximize();
			}
		});
	}

	// инициализация поля-ввода для поиска
	private void initiliazeSearchEdit()
	{
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
	}

	// инициализация плеера
	private void initiliazeYoutubeFragment()
	{

		//mYoutubeFragment = YouTubePlayerSupportFragment.newInstance();
		//mYoutubeFragment = YouTubePlayerSupportFragment.newInstance();

		mYoutubeView.initialize(YoutubeDataModel.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener()
				//mYoutubeFragment.initialize(YoutubeDataModel.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener()
		{

			@Override
			public void onInitializationSuccess(YouTubePlayer.Provider provider,
												final YouTubePlayer player, boolean wasRestored)
			{
				if (!wasRestored)
				{
					mYoutubePlayer = player;
					mYoutubePlayer.setShowFullscreenButton(false);
					mYoutubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener()
					{
						@Override
						public void onLoading()
						{

						}

						@Override
						public void onLoaded(String s)
						{

						}

						@Override
						public void onAdStarted()
						{

						}

						@Override
						public void onVideoStarted()
						{

						}

						@Override
						public void onVideoEnded()
						{
							mPlayerPlayModeEnabled = false;
						}

						@Override
						public void onError(YouTubePlayer.ErrorReason errorReason)
						{
							mYoutubePlayer.play();
							Log.d(TAG, "on error "+errorReason.toString());
						}
					});
				}
				if (mPlayerPlayModeEnabled)
				{
					mYoutubePlayer.loadVideo(mCurrentVideoId);
					mYoutubePlayer.seekToMillis(playOffset);
					mYoutubePlayer.setFullscreen(mFullscreenStatus);
				}
			}

			@Override
			public void onInitializationFailure(YouTubePlayer.Provider provider,
												YouTubeInitializationResult error)
			{
			}
		});
		//getChildFragmentManager().beginTransaction().replace(R.id.youtube_view, mYoutubeFragment).commit();

//		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
//		transaction.replace(R.id.top_panel, mYoutubeFragment);
//		transaction.addToBackStack(null);	// запрет отрабатывать кнопку назад

		// Commit the transaction
//		transaction.commit();
	}

	private void initializeDraggablePanel()
	{
		//mDraggableView.setXScaleFactor(2.5f);
		//mDraggableView.setYScaleFactor(3f);
		//mDraggableView.setTopFragmentMarginRight(10);
		//mDraggableView.setTopFragmentMarginBottom(10);
		//mDraggableView.setFragmentManager(getActivity().getSupportFragmentManager());
		//mDraggableView.attachTopFragment(mYoutubeFragment);
		//mDraggableView.setTopViewResize(true);
/*
		mVideoDescriptionFragment		= new VideoDescriptionFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.bottom_panel, mVideoDescriptionFragment);
		transaction.addToBackStack(null);	// запрет отрабатывать кнопку назад

		// Commit the transaction
		transaction.commit();
*/
		hookDraggableViewListener();
		//mDraggableView.initializeView();
/*
		draggableView = (DraggableView) findViewById(R.id.draggable_view);
		draggableView.setTopViewHeight(topFragmentHeight);
		draggableView.setFragmentManager(fragmentManager);
		draggableView.attachTopFragment(topFragment);
		draggableView.setXTopViewScaleFactor(xScaleFactor);
		draggableView.setYTopViewScaleFactor(yScaleFactor);
		draggableView.setTopViewMarginRight(topFragmentMarginRight);
		draggableView.setTopViewMarginBottom(topFragmentMarginBottom);
		draggableView.attachBottomFragment(bottomFragment);
		draggableView.setDraggableListener(draggableListener);
		draggableView.setHorizontalAlphaEffectEnabled(enableHorizontalAlphaEffect);
		draggableView.setClickToMaximizeEnabled(enableClickToMaximize);
		draggableView.setClickToMinimizeEnabled(enableClickToMinimize);
		draggableView.setTouchEnabled(enableTouchListener);
		draggableView.setTopViewResize(topViewResize);
*/
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
						mIsProgressBarVisible = false;
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


	//------------ Draggeble panel --------------------------------------------------------

	/*
	 * Hook DraggableListener to draggableView to pause or resume VideoView.
	 */
	private void hookDraggableViewListener()
	{
		mDraggableView.setDraggableListener(new DraggableListener()
		{
			@Override
			public void onMaximized()
			{
				if(!mYoutubePlayer.isPlaying())
				{
					startVideo();
				}
				if (mYoutubePlayer != null)
					mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
			}

			//Empty
			@Override
			public void onMinimized()
			{
				if (mYoutubePlayer != null)
					mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
			}

			@Override
			public void onClosedToLeft()
			{
				pauseVideo();
			}

			@Override
			public void onClosedToRight()
			{
				pauseVideo();
			}
		});
	}

	/**
	 * Pause the VideoView content.
	 */
	private void pauseVideo()
	{
		if(mYoutubePlayer != null && mYoutubePlayer.isPlaying())
			mYoutubePlayer.pause();
	}

	/**
	 * Resume the VideoView content.
	 */
	private void startVideo()
	{
		if(mYoutubePlayer != null && !mYoutubePlayer.isPlaying())
		{
			mYoutubePlayer.play();
		}
	}

}
