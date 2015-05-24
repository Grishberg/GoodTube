package com.grishberg.goodtube.gui.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.grishberg.goodtube.R;
import com.grishberg.goodtube.gui.fragment.VideoListActivityFragment;


public class VideoListActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_list);
	}

	@Override
	public void onBackPressed()
	{
		final VideoListActivityFragment fragment = (VideoListActivityFragment) getSupportFragmentManager().findFragmentByTag("VideoListFragment");

		if (fragment != null && fragment.allowBackPressed())
		{ // and then you define a method allowBackPressed with the logic to allow back pressed or not

		}
		else
		{
			super.onBackPressed();
		}
	}

}
