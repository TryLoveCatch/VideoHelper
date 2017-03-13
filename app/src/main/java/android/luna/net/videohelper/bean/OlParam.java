package android.luna.net.videohelper.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by bintou on 16/1/15.
 */
public class OlParam extends BmobObject {

    private String key;

    private String value;

    private int version;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
