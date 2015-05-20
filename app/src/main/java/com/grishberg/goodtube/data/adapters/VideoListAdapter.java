package com.grishberg.goodtube.data.adapters;

/**
 * Created by G on 19.05.15.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.grishberg.goodtube.R;
import com.grishberg.goodtube.data.containers.VideoContainer;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by G on 18.05.15.
 */
public class VideoListAdapter extends BaseAdapter
{

	private List<VideoContainer> 	mItems;
	private Picasso 				mPicasso;
	private LayoutInflater      	mInflater;
	public VideoListAdapter(Context context,List<VideoContainer> elements)
	{
		this.mItems = elements;
		mInflater   = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPicasso	= Picasso.with(context.getApplicationContext());
	}
	@Override
	public int getCount()
	{
		return mItems.size();
	}

	@Override
	public Object getItem(int location)
	{
		return mItems.get(location);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	// пункт списка
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// используем созданные, но не используемые view
		View view = convertView;
		if (view == null)
		{
			view = mInflater.inflate(R.layout.video_list_cell, parent, false);
		}

		VideoContainer p = (VideoContainer)getItem(position);

		// заполняем View
		((TextView) view.findViewById(R.id.tvTitle)).setText(p.getTitle());
		((TextView) view.findViewById(R.id.tvDate)).setText(p.getPublishedAtString());
		//final ProgressBar progressBar   = (ProgressBar) view.findViewById(R.id.icon_loading_spinner);
		final ImageView img             = (ImageView) view.findViewById(R.id.thumbnail);
		//img.setVisibility(View.GONE);

		if(p.getThumbnailUrl().length() > 0)
		{
			mPicasso.load(p.getThumbnailUrl()).into(img);
		}
		else
		{
			// отображать пустую картинку для таких случаев
			img.setVisibility(View.VISIBLE);
//			img.setImageResource(R.drawable.goodlinelogomini);

		}
		return view;
	}


}
