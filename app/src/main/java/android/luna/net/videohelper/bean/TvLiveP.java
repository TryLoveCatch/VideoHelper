package android.luna.net.videohelper.bean;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by bintou on 16/3/30.
 */
public class TvLiveP implements Serializable {


    public String channelId;
    public String channelName;
    public List<TvSource> sourceList = new ArrayList<>();
    public String icon;
    public String epgs;
    public String city;

}
