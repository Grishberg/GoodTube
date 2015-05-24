package com.grishberg.goodtube.gui.fragment;

		import android.support.v4.app.Fragment;
		import android.app.Activity;
		import android.net.Uri;
		import android.os.Bundle;
		import android.view.LayoutInflater;
		import android.view.View;
		import android.view.ViewGroup;
		import android.view.animation.Animation;
		import android.view.animation.AnimationUtils;
		import android.widget.LinearLayout;
		import android.widget.TextView;

		import com.grishberg.goodtube.R;
		import com.grishberg.goodtube.data.containers.ResultPageContainer;
		import com.grishberg.goodtube.data.containers.VideoContainer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoDescriptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoDescriptionFragment extends Fragment
{

	private TextView	mDescriptionTextView;
	private TextView	mDurationTextView;
	private TextView	mViewCountTextView;
	private TextView	mLikesCountTextView;
	private TextView	mDislikesCountTextView;

	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment VideoDescriptionFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static VideoDescriptionFragment newInstance(String param1, String param2)
	{
		VideoDescriptionFragment fragment = new VideoDescriptionFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public VideoDescriptionFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
		{
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_video_description, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mDescriptionTextView	= (TextView) getView().findViewById(R.id.tvDescription);
		mDurationTextView		= (TextView) getView().findViewById(R.id.tvDuraion);
		mViewCountTextView		= (TextView) getView().findViewById(R.id.tvViewCount);
		mLikesCountTextView		= (TextView) getView().findViewById(R.id.tvLikesCount);
		mDislikesCountTextView		= (TextView) getView().findViewById(R.id.tvDislikesCount);

		final Animation translateAnimation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.translate_left);
		LinearLayout descriptionPanel	= (LinearLayout) getView().findViewById(R.id.descriptionPanel);
		descriptionPanel.startAnimation(translateAnimation);
	}

	public void setMainData(VideoContainer data)
	{
		mDescriptionTextView.setText(data.getTitle());
		mDurationTextView.setText("");
		mViewCountTextView.setText("");
		mLikesCountTextView.setText("");
		mDislikesCountTextView.setText("");
	}

	public void setAdditionalData(VideoContainer data)
	{
		mDurationTextView.setText("продолжительность: " + data.getDuration());
		mViewCountTextView.setText(data.getViewCountStr());
		mLikesCountTextView.setText(Long.toString(data.getLikeCount()));
		mDislikesCountTextView.setText(Long.toString(data.getDislikeCount()));
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener
	{
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

}
