package io.vov.vitamio;

import android.text.TextUtils;

import io.vov.vitamio.widget.VideoConstant;

/**
 * Created by bintou on 16/3/16.
 */
public class VideoSegsBean {

    private String site;
    private String m1080p;
    private String m720p;
    private String m480p;
    private String m320p;
    private String m240p;

    private String curDef = "";

    public String getAssignDef(int def) {

        while (true) {
            switch (def) {
                case VideoConstant.DEF_SUPER:
                    if (!TextUtils.isEmpty(m720p)) {
                        curDef = "720P";
                        return m720p;
                    }
                case VideoConstant.DEF_HIGH:
                    if (!TextUtils.isEmpty(m480p)) {
                        curDef = "480P";
                        return m480p;
                    }
                case VideoConstant.DEF_NORMAL:
                    if (!TextUtils.isEmpty(m320p)) {
                        curDef = "320P";
                        return m320p;
                    }
                case VideoConstant.DEF_ORIGINAL:
                    if (!TextUtils.isEmpty(m1080p)) {
                        curDef = "1080P";
                        return m1080p;
                    }
                case VideoConstant.DEF_LOW:
                    if (!TextUtils.isEmpty(m240p)) {
                        curDef = "240P";
                        return m240p;
                    }
                default:
                    def = VideoConstant.DEF_HIGH;
                    curDef = "480P";
            }
        }
    }

    public String getM1080p() {
        return m1080p;
    }

    public void setM1080p(String m1080p) {
        this.m1080p = m1080p;
    }

    public String getM240p() {
        return m240p;
    }

    public void setM240p(String m240p) {
        this.m240p = m240p;
    }

    public String getM320p() {
        return m320p;
    }

    public void setM320p(String m320p) {
        this.m320p = m320p;
    }

    public String getM480p() {
        return m480p;
    }

    public void setM480p(String m480p) {
        this.m480p = m480p;
    }

    public String getM720p() {
        return m720p;
    }

    public void setM720p(String m720p) {
        this.m720p = m720p;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getCurDef() {
        return curDef;
    }

    public void setCurDef(String curDef) {
        this.curDef = curDef;
    }
}
