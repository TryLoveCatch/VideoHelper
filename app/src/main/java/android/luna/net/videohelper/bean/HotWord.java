package android.luna.net.videohelper.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by bintou on 15/12/10.
 */
public class HotWord extends BmobObject {

    private String word;
    private String url;
    private String img;
    private String content;
    private int cid;
    private String word_zh;
    private String content_zh;

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

    public String getContent_zh() {
        return content_zh;
    }

    public void setContent_zh(String content_zh) {
        this.content_zh = content_zh;
    }

    public String getWord_zh() {
        return word_zh;
    }

    public void setWord_zh(String word_zh) {
        this.word_zh = word_zh;
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

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }
}
