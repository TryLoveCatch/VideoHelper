package android.luna.net.videohelper.Ninja.Browser;

import java.util.LinkedList;
import java.util.List;

public class BrowserContainer {
    private static List<android.luna.net.videohelper.Ninja.Browser.AlbumController> list = new LinkedList<>();

    public static android.luna.net.videohelper.Ninja.Browser.AlbumController get(int index) {
        return list.get(index);
    }

    public synchronized static void set(android.luna.net.videohelper.Ninja.Browser.AlbumController controller, int index) {
        if (list.get(index) instanceof android.luna.net.videohelper.Ninja.View.NinjaWebView) {
            ((android.luna.net.videohelper.Ninja.View.NinjaWebView) list.get(index)).destroy();
        }

        list.set(index, controller);
    }

    public synchronized static void add(android.luna.net.videohelper.Ninja.Browser.AlbumController controller) {
        list.add(controller);
    }

    public synchronized static void add(android.luna.net.videohelper.Ninja.Browser.AlbumController controller, int index) {
        list.add(index, controller);
    }

    public synchronized static void remove(int index) {
        if (list.get(index) instanceof android.luna.net.videohelper.Ninja.View.NinjaWebView) {
            ((android.luna.net.videohelper.Ninja.View.NinjaWebView) list.get(index)).destroy();
        }

        list.remove(index);
    }

    public synchronized static void remove(android.luna.net.videohelper.Ninja.Browser.AlbumController controller) {
        if (controller instanceof android.luna.net.videohelper.Ninja.View.NinjaWebView) {
            ((android.luna.net.videohelper.Ninja.View.NinjaWebView) controller).destroy();
        }

        list.remove(controller);
    }

    public static int indexOf(android.luna.net.videohelper.Ninja.Browser.AlbumController controller) {
        return list.indexOf(controller);
    }

    public static List<android.luna.net.videohelper.Ninja.Browser.AlbumController> list() {
        return list;
    }

    public static int size() {
        return list.size();
    }

    public synchronized static void clear() {
        for (android.luna.net.videohelper.Ninja.Browser.AlbumController albumController : list) {
            if (albumController instanceof android.luna.net.videohelper.Ninja.View.NinjaWebView) {
                ((android.luna.net.videohelper.Ninja.View.NinjaWebView) albumController).destroy();
            }
        }

        list.clear();
    }
}
