package android.luna.net.videohelper.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by bintou on 15/12/10.
 */
public class Recommend extends BmobObject {

    private String word;
    private String url;
    private String img;
    private String content;
    private String vid;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }
}
