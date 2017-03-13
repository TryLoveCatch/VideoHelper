package android.luna.net.videohelper.bean;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by bintou on 15/11/2.
 */
public class VideoDetial implements Serializable{

    public int cid = 0;
    public String vid;
    public String desc;
    public String cover;
    public String name;
    public String actors;
    public String area;
    public String directors;
    public String rating;
    public String genres;
    public String year;
    public String episodes;
    public String icon;
    public String videoUrl;
    public ArrayList<VideoSource> sources;


}
