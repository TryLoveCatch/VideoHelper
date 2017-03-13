package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.luna.common.util.StringUtils;


/**
 * Created by bintou on 15/11/5.
 */
@SuppressLint("ValidFragment")
public class IntroduceFragment extends Fragment {

    VideoDetial mVideoDetial;

    public IntroduceFragment(VideoDetial videoDetial) {
        super();
        mVideoDetial = videoDetial;
    }

    public IntroduceFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_introduce, container, false);
        TextView tv = (TextView) v.findViewById(R.id.introduce_tv);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        if (mVideoDetial != null) {
            if (!StringUtils.isBlank(mVideoDetial.directors)) {
                sb.append("导演：" + mVideoDetial.directors + "\n");
            }
            if (!StringUtils.isBlank(mVideoDetial.actors)) {
                sb.append("主演：" + mVideoDetial.actors + "\n");
            }
            if (!StringUtils.isBlank(mVideoDetial.year)) {
                sb.append("年代：" + mVideoDetial.year + "\n");
            }
            if (!StringUtils.isBlank(mVideoDetial.area)) {
                sb.append("地区：" + mVideoDetial.area + "\n");
            }
            if (!StringUtils.isBlank(mVideoDetial.genres)) {
                sb.append("类型：" + mVideoDetial.genres + "\n");
            }
            if (!StringUtils.isBlank(mVideoDetial.desc)) {
                sb.append("简介：" + mVideoDetial.desc + "\n");
            }
        }
        tv.setText(sb.toString());
        return v;
    }
}
