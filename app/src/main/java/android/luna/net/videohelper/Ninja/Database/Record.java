package android.luna.net.videohelper.Ninja.Database;

import net.luna.common.debug.LunaLog;

public class Record {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String url;

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    private long time;

    public long getTime() {
        return time;
    }

    private long playTime;

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private boolean shouldDelete = false;

    public boolean isShouldDelete() {
        return shouldDelete;
    }

    public void setShouldDelete(boolean shouldDelete) {
        this.shouldDelete = shouldDelete;
    }

    public Record() {
        this.title = null;
        this.url = null;
        this.time = 0l;
        shouldDelete = false;
    }

    public Record(String title, String url, long time) {
        this.title = title;
        this.url = url;
        this.time = time;
        shouldDelete = false;
    }

    public Record(String title, String url, long playTime, long time) {
        this.title = title;
        this.url = url;
        this.time = time;
        this.playTime = playTime;
        shouldDelete = false;
    }
}
