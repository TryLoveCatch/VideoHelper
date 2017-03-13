package io.vov.vitamio.interfaces;

/**
 * Created by bintou on 16/3/15.
 */
public interface MediaPlayerControl {

    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long var1);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    void setVideoQuality(int var1);

    void scale(float var1);

    int getAudioSessionId();

    void setHardCode(boolean bool);


}
