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
/*

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_video_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	*/
}
