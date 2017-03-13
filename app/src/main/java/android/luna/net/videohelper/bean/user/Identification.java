package android.luna.net.videohelper.bean.user;

import cn.bmob.v3.BmobObject;

/**
 * Created by bintou on 15/11/23.
 */
public class Identification extends BmobObject {

    private String imei;
    private String mac;
    private String ip;
    private String channel;
    private String model;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
