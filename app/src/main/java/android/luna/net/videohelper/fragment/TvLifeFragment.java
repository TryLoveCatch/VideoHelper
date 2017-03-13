package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.luna.net.videohelper.adapter.TvLivesListAdapter;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.widget.DividerItemDecoration;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.luna.common.util.ListUtils;
import net.luna.common.util.ThreadUtils;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/5.
 */
@SuppressLint("ValidFragment")
public class TvLifeFragment extends Fragment {

    ArrayList<TvLiveP> mTvLiveArrayList;

    private TvLivesListAdapter mTvLIveListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout progressDialog;
    private String mType;

    public TvLifeFragment(String type) {
        super();
        mType = type;
    }

    public TvLifeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_similarfilm, container, false);
        progressDialog = (LinearLayout) v.findViewById(R.id.progressbar);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycleview_film);
        mTvLIveListAdapter = new TvLivesListAdapter(getActivity(), mTvLiveArrayList, mType);
        recyclerView.setAdapter(mTvLIveListAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        if (ListUtils.getSize(mTvLiveArrayList) <= 0) {
            ThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    mTvLiveArrayList = (ArrayList<TvLiveP>) VideoCatchManager.getInstanct(getContext()).getTvLiveBeans(mType);
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ListUtils.getSize(mTvLiveArrayList) > 0) {
                                mTvLIveListAdapter.updateData(mTvLiveArrayList);
                            }
                            progressDialog.setVisibility(View.GONE);
                        }
                    });
                }
            });
        } else {
            progressDialog.setVisibility(View.GONE);
        }

        return v;
    }
}
