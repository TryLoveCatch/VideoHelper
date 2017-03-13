package android.luna.net.videohelper.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;

/**
 * Created by bintou on 16/4/5.
 */
public class CloudVideo extends BmobObject {

    public String webUrl;

    public String name;

    public String cover;

    public String desc;
    public String actors;
    public String area;
    public String directors;
    public String rating;
    public String genres;
    public String year;
    public String episodes;

    public String source;
    public String playLinks;
    public BmobDate time;
    public boolean canPlay;

    public int cid;


}
