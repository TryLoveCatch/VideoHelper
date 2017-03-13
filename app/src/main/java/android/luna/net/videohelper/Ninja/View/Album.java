package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.luna.common.util.ToastUtils;


public class Album {
    private Context context;

    private View albumView;

    public View getAlbumView() {
        return albumView;
    }

    private ImageView albumCover;

    public void setAlbumCover(Bitmap bitmap) {
        albumCover.setImageBitmap(bitmap);
    }

    private TextView albumTitle;

    public String getAlbumTitle() {
        return albumTitle.getText().toString();
    }

    public void setAlbumTitle(String title) {
        albumTitle.setText(title);
    }

    private android.luna.net.videohelper.Ninja.Browser.AlbumController albumController;

    public void setAlbumController(android.luna.net.videohelper.Ninja.Browser.AlbumController albumController) {
        this.albumController = albumController;
    }

    private android.luna.net.videohelper.Ninja.Browser.BrowserController browserController;

    public void setBrowserController(android.luna.net.videohelper.Ninja.Browser.BrowserController browserController) {
        this.browserController = browserController;
    }

    public Album(Context context, android.luna.net.videohelper.Ninja.Browser.AlbumController albumController, android.luna.net.videohelper.Ninja.Browser.BrowserController browserController) {
        this.context = context;
        this.albumController = albumController;
        this.browserController = browserController;
        initUI();
    }

    private void initUI() {
        albumView = LayoutInflater.from(context).inflate(R.layout.album, null, false);

        albumView.setOnTouchListener(new android.luna.net.videohelper.Ninja.View.SwipeToDismissListener(
                albumView,
                null,
                new android.luna.net.videohelper.Ninja.View.SwipeToDismissListener.DismissCallback() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        browserController.removeAlbum(albumController);
                    }
                }
        ));

        albumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browserController.showAlbum(albumController, false, false, false);
            }
        });

        albumView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastUtils.show(context, albumTitle.getText().toString());
                return true;
            }
        });

        albumCover = (ImageView) albumView.findViewById(R.id.album_cover);
        albumTitle = (TextView) albumView.findViewById(R.id.album_title);
        albumTitle.setText(context.getString(R.string.album_untitled));
    }

    public void activate() {

    }

    public void deactivate() {
    }
}
