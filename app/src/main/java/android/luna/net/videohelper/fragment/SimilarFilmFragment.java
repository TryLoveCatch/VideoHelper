package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.luna.net.videohelper.adapter.VideoListAdapter;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/5.
 */
@SuppressLint("ValidFragment")
public class SimilarFilmFragment extends Fragment {

    ArrayList<VideoDetial> mVideoList;

    private VideoListAdapter mVideoListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public SimilarFilmFragment(ArrayList<VideoDetial> videoList) {
        super();
        mVideoList = videoList;
    }

    public SimilarFilmFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_similarfilm, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycleview_film);
        mVideoListAdapter = new VideoListAdapter(getActivity(), mVideoList);
        recyclerView.setAdapter(mVideoListAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        return v;
    }
}
