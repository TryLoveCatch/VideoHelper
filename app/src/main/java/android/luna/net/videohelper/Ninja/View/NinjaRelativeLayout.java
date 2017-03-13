package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.luna.net.videohelptools.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;


public class NinjaRelativeLayout extends RelativeLayout implements android.luna.net.videohelper.Ninja.Browser.AlbumController {
    private Context context;
    private android.luna.net.videohelper.Ninja.View.Album album;
    private int flag = 0;

    private android.luna.net.videohelper.Ninja.Browser.BrowserController controller;

    public void setBrowserController(android.luna.net.videohelper.Ninja.Browser.BrowserController controller) {
        this.controller = controller;
        this.album.setBrowserController(controller);
    }

    public NinjaRelativeLayout(Context context) {
        this(context, null);
    }

    public NinjaRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NinjaRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.album = new android.luna.net.videohelper.Ninja.View.Album(context, this, this.controller);
        initUI();
    }

    private void initUI() {
        album.setAlbumCover(null);
        album.setAlbumTitle(context.getString(R.string.album_untitled));
        album.setBrowserController(controller);
    }

    @Override
    public int getFlag() {
        return flag;
    }

    @Override
    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public View getAlbumView() {
        return album.getAlbumView();
    }

    @Override
    public void setAlbumCover(Bitmap bitmap) {
        album.setAlbumCover(bitmap);
    }

    @Override
    public String getAlbumTitle() {
        return album.getAlbumTitle();
    }

    @Override
    public void setAlbumTitle(String title) {
        album.setAlbumTitle(title);
    }

    @Override
    public void activate() {
        album.activate();
    }

    @Override
    public void deactivate() {
        album.deactivate();
    }
}
