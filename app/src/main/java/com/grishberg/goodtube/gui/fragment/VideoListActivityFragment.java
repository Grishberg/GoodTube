package com.grishberg.goodtube.gui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.media.Image;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggableView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
//import com.google.android.youtube.player.YouTubePlayerFragment;

import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import com.grishberg.goodtube.R;
import com.grishberg.goodtube.data.adapters.VideoListAdapter;
import com.grishberg.goodtube.data.containers.ResultPageContainer;
import com.grishberg.goodtube.data.containers.VideoContainer;
import com.grishberg.goodtube.data.models.YoutubeDataModel;

import com.grishberg.goodtube.gui.listeners.GetVideoListListener;
import com.grishberg.goodtube.gui.listeners.InfinityScrollListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A placeholder fragment containing a simple view.
 */
public class VideoListActivityFragment extends Fragment
{
	public static final String		TAG						= "YoutubeAppFragment";
	public static final String		PLAYER_PLAY_ENABLED		= "playerPlayEnabled";
	public static final String		PLAYER_PLAY_OFFSET		= "playerPlayOffset";
	public static final String		MOSTPOPULAR_MODE_STATUS	= "mostpopularModeStatus";
	public static final String		PLAYER_FULLSCREEN_STATE	= "playerFullscreenState";
	public static final String		PLAYER_VIDEO_ID			= "playerVideoId";
	public static final String		SEARCH_KEYWORD			= "searchKeyword";
	private final int REQ_CODE_SPEECH_INPUT = 100;


	public static final long		ITEMS_PER_PAGE	= 10;

	DraggableView 					mDraggableView;
	private ListView 				mListView;

	private List<VideoContainer> 	mVideoList;
	private EditText				mSearchEdit;
	private ImageButton				mSpeachButton;

	private VideoListAdapter 				mVideoListAdapter;
	private YoutubeDataModel 				mDataModel;
	private String							mNextPageToken;
	private String							mPrevPageToken;

	private String							mSearchKeyword;
	private ProgressBar						mProgressBar;
	private int 							mShortAnimationDuration;
	private boolean							mIsProgressBarVisible;
	private VideoDescriptionFragment		mVideoDescriptionFragment;
	private YouTubePlayerSupportFragment	mYoutubeContainer;
	private YouTubePlayer 					mYoutubePlayer;
	private String							mCurrentVideoId;

	private boolean							mGetMostPopularMode;
	private int mPlayOffset					= 0;
	private int mFullscreenState			= 0;
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
		mSpeachButton		= (ImageButton) getView().findViewById(R.id.speach_button);
		mProgressBar		= (ProgressBar) getView().findViewById(R.id.video_list_progress);
		mListView			= (ListView) getView().findViewById(R.id.video_list_view);
		mSearchEdit			= (EditText) getView().findViewById(R.id.searchTextEdit);
		mDataModel			= new YoutubeDataModel(getActivity());

		// узнать размер экрана
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

		float dpHeight		= displayMetrics.heightPixels / displayMetrics.density;
		float dpWidth		= displayMetrics.widthPixels / displayMetrics.density;
		float scaleFactor	= dpWidth / 200.0f;
		mDraggableView.setXTopViewScaleFactor(3);
		mDraggableView.setYTopViewScaleFactor(3);

		mGetMostPopularMode	= true;
		mVideoList			= new ArrayList<VideoContainer>();
		mVideoListAdapter	= new VideoListAdapter(getActivity(), mVideoList);

		// настройка анимации перехода из прогресс бара на список видео
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_mediumAnimTime);

		mSpeachButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				promptSpeechInput();
			}
		});

		// если было восстановление после поворота
		if (savedInstanceState != null)
		{
			mPlayerPlayModeEnabled	= savedInstanceState.getBoolean(PLAYER_PLAY_ENABLED);
			mGetMostPopularMode		= savedInstanceState.getBoolean(MOSTPOPULAR_MODE_STATUS);
			mSearchKeyword			= savedInstanceState.getString(SEARCH_KEYWORD);
			if(mPlayerPlayModeEnabled)
			{
				mPlayOffset			= savedInstanceState.getInt(PLAYER_PLAY_OFFSET);
				mFullscreenState		= savedInstanceState.getInt(PLAYER_FULLSCREEN_STATE);
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

		if (!mPlayerPlayModeEnabled)
		{
			mDraggableView.setVisibility(View.GONE);
		}
		showProgressBar();

		getNextPage();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// Save the values you need from your textview into "outState"-object
		super.onSaveInstanceState(outState);
		if(mYoutubePlayer != null)
		{
			outState.putBoolean(PLAYER_PLAY_ENABLED, mYoutubePlayer.isPlaying());
			if (mYoutubePlayer.isPlaying())
			{
				outState.putInt(PLAYER_PLAY_OFFSET, mYoutubePlayer.getCurrentTimeMillis());
				outState.putInt(PLAYER_FULLSCREEN_STATE, mYoutubePlayer.getFullscreenControlFlags());
				outState.putString(PLAYER_VIDEO_ID, mCurrentVideoId);
			}
			outState.putBoolean(MOSTPOPULAR_MODE_STATUS, mGetMostPopularMode);
			outState.putString(SEARCH_KEYWORD, mSearchKeyword);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_video_list, container, false);

		// обработка нажатия кнопки Назад
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_BACK)
				{
					if (mDraggableView != null && mDraggableView.isMaximized())
					{
						mDraggableView.minimize();
						return true;
					} else
					{
						getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						return false;
					}

				} else
				{
					return false;
				}
			}
		});
		return view;

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
					mSearchKeyword = v.getText().toString();
					searchVideo(mSearchKeyword);
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

	private void searchVideo(String keyword)
	{
		// Do your action here
		mGetMostPopularMode = false;
		mNextPageToken		= null;
		mPrevPageToken		= null;
		mVideoList.clear();

		showProgressBar();
		getNextPage();
	}

	// инициализация плеера
	private void initiliazeYoutubeFragment()
	{
		mYoutubeContainer	= YouTubePlayerSupportFragment.newInstance();
		mYoutubeContainer.initialize(YoutubeDataModel.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener()
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

							Log.d(TAG,"on loading");

						}

						@Override
						public void onLoaded(String s)
						{
							Log.d(TAG, "on loaded " + s);
							//mYoutubePlayer.seekToMillis(mPlayOffset);
							//mYoutubePlayer.play();
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
							//mYoutubePlayer.play();
							Log.d(TAG, "on error " + errorReason.toString());
						}
					});
				}
				if (mPlayerPlayModeEnabled)
				{
					mYoutubePlayer.loadVideo(mCurrentVideoId);
					//mYoutubePlayer.seekToMillis((mPlayOffset/1000)*1000);
					//mYoutubePlayer.play();
					//mYoutubePlayer.setFullscreen(mFullscreenStatus);
				}
			}

			@Override
			public void onInitializationFailure(YouTubePlayer.Provider provider,
												YouTubeInitializationResult error)
			{
			}
		});

		getChildFragmentManager().beginTransaction().replace(R.id.fragment_youtube_player, mYoutubeContainer).commit();

		mVideoDescriptionFragment	= new VideoDescriptionFragment();
		getChildFragmentManager().beginTransaction().replace(R.id.bottom_panel, mVideoDescriptionFragment).commit();

	}

	private void initializeDraggablePanel()
	{
		hookDraggableViewListener();
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
		// для предотвращения цикличной загрузки страниц
		if (mNextPageToken == null && mPrevPageToken != null) return;

		if (mGetMostPopularMode)
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
		if(result == null)
		{
			Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.search_exception),
					Toast.LENGTH_SHORT).show();
			return;
		}
		mNextPageToken	= result.getNextPageToken();
		mPrevPageToken	= result.getPrevPageToken();
		if(mNextPageToken == null && mPrevPageToken == null)
		{
			mPrevPageToken = "";
		}
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
				if(mYoutubePlayer!= null && !mYoutubePlayer.isPlaying())
				{
					//startVideo();
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
		{
			mYoutubePlayer.pause();
		}
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


// -------- обработка речи --------------------------
	/**
	 * Showing google speech input dialog
	 * */
	private void promptSpeechInput()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a)
		{
			Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Receiving speech input
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
			case REQ_CODE_SPEECH_INPUT:
			{
				if (resultCode == Activity.RESULT_OK && null != data)
				{
					ArrayList<String> result = data
							.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					mSearchKeyword = result.get(0);
					if(mSearchKeyword != null && mSearchKeyword.length() > 0)
					{
						mSearchEdit.setText(mSearchKeyword);
						searchVideo(mSearchKeyword);
					}
				}
				break;
			}

		}
	}
}
