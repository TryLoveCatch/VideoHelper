package android.luna.net.videohelper.bean;

import java.io.Serializable;

/**
 * Created by bintou on 15/10/15.
 */
public class Catelogue implements Serializable {

    public int icon;
    public String site;
    public String url;
    public String title = "未知";

    public boolean isSelected = false;

    /*
    1为网页，2为activity
     */
    public int type = 1;

    public Class tartget;
    public int cid;

    public boolean showPoint = false;

}
