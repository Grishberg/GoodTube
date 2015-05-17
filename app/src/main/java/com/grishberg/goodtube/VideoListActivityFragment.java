package com.grishberg.goodtube;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.grishberg.goodtube.gui.PlayerLayout;


/**
 * A placeholder fragment containing a simple view.
 */
public class VideoListActivityFragment extends Fragment
{

	public VideoListActivityFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view	= inflater.inflate(R.layout.fragment_video_list, container, false);

		final TextView viewHeader			= (TextView) view.findViewById(R.id.header);
		final PlayerLayout youtubeLayout	= (PlayerLayout) view.findViewById(R.id.dragLayout);
		final ListView listView				= (ListView) view.findViewById(R.id.listView);

		// обработчик нажатия на элемент ListView
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				viewHeader.setText(listView.getAdapter().getItem(i).toString());
				youtubeLayout.setVisibility(View.VISIBLE);
				youtubeLayout.maximize();
			}
		});

		// установка адаптера ListView
		listView.setAdapter(new BaseAdapter()
		{
			@Override
			public int getCount()
			{
				return 50;
			}

			@Override
			public String getItem(int i)
			{
				return "object" + i;
			}

			@Override
			public long getItemId(int i)
			{
				return i;
			}

			@Override
			public View getView(int i, View rView, ViewGroup viewGroup)
			{
				View view = rView;
				if (view == null)
				{
					view = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, viewGroup, false);
				}
				((TextView) view.findViewById(android.R.id.text1)).setText(getItem(i));
				return view;
			}
		});

		return view;
	}
}
