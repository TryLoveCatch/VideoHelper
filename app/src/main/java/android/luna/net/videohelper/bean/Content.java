
package android.luna.net.videohelper.bean;


import android.luna.net.videohelper.bean.user.User;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

/**
 * APP内容类，每天更新的内容，可以是文章，音乐，视频等
 *
 * @author bintou
 * @version 创建时间：2015年7月1日 上午11:06:45
 */
public class Content extends BmobObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String title;

    private String titleImage;

    private User author;

    /*
     * 文章，建议使用HTML类容，也可以纯文本，图片插入  |*img*|
     *	,视频插入 |*video*| ,音乐插入 |*music*|
     */
    private String article;

    private ArrayList<String> images;

    /*
    视频地址
     */
    private String video;

    /*
    音乐地址
     */
    private String music;

    /*
    发表日期
     */
    private String pushDate;

    public String getTitle() {
        return title;
    }

    public String getTitleImage() {
        return titleImage;
    }

    public User getAuthor() {
        return author;
    }

    public String getArticle() {
        return article;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getVideo() {
        return video;
    }

    public String getMusic() {
        return music;
    }

    public String getPushDate() {
        return pushDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleImage(String titleImage) {
        this.titleImage = titleImage;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setPushDate(String pushDate) {
        this.pushDate = pushDate;
    }


}

