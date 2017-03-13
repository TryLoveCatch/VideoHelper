package android.luna.net.videohelper.Ninja.Activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.luna.net.videohelper.Ninja.Browser.AdBlock;
import android.luna.net.videohelper.Ninja.Browser.AlbumController;
import android.luna.net.videohelper.Ninja.Browser.BrowserContainer;
import android.luna.net.videohelper.Ninja.Browser.BrowserController;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.IntentUnit;
import android.luna.net.videohelper.Ninja.Unit.ViewUnit;
import android.luna.net.videohelper.Ninja.View.CompleteAdapter;
import android.luna.net.videohelper.Ninja.View.DialogAdapter;
import android.luna.net.videohelper.Ninja.View.FullscreenHolder;
import android.luna.net.videohelper.Ninja.View.NinjaRelativeLayout;
import android.luna.net.videohelper.Ninja.View.NinjaWebView;
import android.luna.net.videohelper.Ninja.View.RecordAdapter;
import android.luna.net.videohelper.Ninja.View.SwipeToBoundListener;
import android.luna.net.videohelper.activity.BookmarkActivity;
import android.luna.net.videohelper.activity.DownloadActivity;
import android.luna.net.videohelper.activity.GuideMoreActivity;
import android.luna.net.videohelper.activity.SearchActivity;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.anim.AnimationCollector;
import android.luna.net.videohelper.applicaiton.IdentificationUpload;
import android.luna.net.videohelper.applicaiton.VideoHelperApplication;
import android.luna.net.videohelper.bean.OlParam;
import android.luna.net.videohelper.download.VideosDownloadService;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.SiteRegex;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.widget.DefinitionDialog;
import android.luna.net.videohelper.widget.HomeContent;
import android.luna.net.videohelper.widget.ParserLayout;
import android.luna.net.videohelper.widget.ShareDialog;
import android.luna.net.videohelper.widget.SplashPageView;
import android.luna.net.videohelptools.R;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputType;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.dtr.zxing.activity.CaptureActivity;
import com.nineoldandroids.animation.Animator;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.util.ToastUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;
import io.vov.vitamio.Vitamio;


/**
 * edit by huangjianbin
 */
public class BrowserActivity extends Activity implements BrowserController, View.OnClickListener {
    // Sync with NinjaToast.show() 2000ms delay
    private static final int DOUBLE_TAPS_QUIT_DEFAULT = 2000;

    private Context context;


    private String site;
    private ImageButton forwardBtn, backwardBtn;

    private ParserLayout parserLayout;
    private TextView webNameTv;


    private RelativeLayout omnibox;
    private AutoCompleteTextView inputBox;
    private ImageButton omniboxBookmark, omniboxSearch;
    private ProgressBar progressBar;

    private RelativeLayout contentFrame;

    private LinearLayout bottomLayour;


    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }
    }

    private FullscreenHolder fullscreenHolder;
    private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private ValueCallback<Uri> uploadMsg = null;
    private ValueCallback<Uri[]> filePathCallback = null;

    private static boolean quit = false;
    private boolean create = true;

    private int shortAnimTime = 0;
    private int mediumAnimTime = 0;
    private int longAnimTime = 0;
    private AlbumController currentAlbumController = null;

    private HomeContent homeContent;
    private ShareDialog shareDialog;
    private DefinitionDialog defDialog;

    private LinearLayout rightBtn;
    private ImageButton recordBtn;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (resultCode == GlobalConstant.INDEX_BOOKMARK_RETURN) {
                String url = intent.getStringExtra(IntentUnit.URL);
                updateAlbum(url);
            } else if (requestCode == GlobalConstant.INDEX_VIDEO_RETURN) {
                if (site != null && site.equals(GlobalConstant.SITE_IQIYI)) {
                    refresh();
                }
                int visit_time = PreferencesUtils.getInt(this, GlobalConstant.KEY_VIDEO_VISIT_TIMES, 0);
                LunaLog.d("visit_time: " + visit_time);
                if (visit_time > 4) {
                    ShareDialog shareDialog = new ShareDialog(context);
                    shareDialog.showShareDialog("");
                    PreferencesUtils.putInt(this, GlobalConstant.KEY_VIDEO_VISIT_TIMES, 0);
                }
            } else if (resultCode == GlobalConstant.INDEX_EDIT_CATELOGUE_RETURN) {
                homeContent.updateCatelogue();
            } else if (resultCode == GlobalConstant.INDEX_SEARCH_RETURN) {
                dispatchIntent(intent);
            }

            if (resultCode == GlobalConstant.RESULT_VISIT_SUCCESS) {
                if (intent != null) {
                    long playTime = intent.getLongExtra("play_time", 0l);
                    String title = intent.getStringExtra("name");
                    String url = intent.getStringExtra("url");
                    Record record = new Record(title, url, playTime, System.currentTimeMillis());
                    RecordAction recordAction = RecordAction.getInstance(context);
                    recordAction.open(true);
                    if (recordAction.checkPlayRecord(url)) {
                        recordAction.updatePlayRecord(record);
                    } else {
                        recordAction.addPlayRecord(record);
                    }
                    recordAction.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(getApplicationContext());
        context = this;
        setContentView(R.layout.activity_browser);
        initPrologue();

        ((VideoHelperApplication) getApplication()).setWebHandler(webviewHandler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                    getResources().getColor(R.color.background)
            );
            setTaskDescription(description);
        }


        StatService.trackCustomEvent(this, "onCreate", "");
        create = true;
        shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        longAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

//        iqiyiParser = new IqiyiParser(context, webviewHandler);

        initControlView();
        initOmnibox();
        homeContent = new HomeContent(this, webviewHandler);
        contentFrame = (RelativeLayout) findViewById(R.id.main_content);

        shareDialog = new ShareDialog(context);

        dispatchIntent(getIntent());

        new AdBlock(this); // For AdBlock cold boot
        //检查更新

        ThreadUtils.runInUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                //防止GP更新出问题。
                if (getApplicationContext().getPackageName().equals("android.luna.net.videohelptools"))
                    UmengUpdateAgent.update(context);
//                showTipPage();
            }
        }, 1000);

//        if (!PreferencesUtils.getBoolean(context, GlobalConstant.SP_SHOW_AD_DECLARATION)) {
//            ThreadUtils.runInUiThreadDelay(new Runnable() {
//                @Override
//                public void run() {
//                    final Dialog dialog = new Dialog(context, getResources().getString(R.string.disclaimer), GlobalConstant.AD_DECLARATION);
//                    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            dialog.dismiss();
//                            PreferencesUtils.putBoolean(context, GlobalConstant.SP_SHOW_AD_DECLARATION, true);
//                        }
//                    });
//                    dialog.show();
//                }
//            }, 4000);
//
//        }

        IdentificationUpload iu = new IdentificationUpload(context);
        iu.checkUserIdentification();

        BmobQuery<OlParam> query = new BmobQuery<>();
        query.getObject(context, "bX90BBBE", new GetListener<OlParam>() {
            @Override
            public void onSuccess(OlParam olParam) {
                if (olParam != null && !StringUtils.isBlank(olParam.getValue())) {
                    PreferencesUtils.putString(context, GlobalConstant.SP_IQIYI_VIP_DOMAIN, olParam.getValue());
                }
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });

        BmobQuery<OlParam> query1 = new BmobQuery<>();
        query1.getObject(context, "UJaQ222J", new GetListener<OlParam>() {
            @Override
            public void onSuccess(OlParam olParam) {
                if (olParam != null && !StringUtils.isBlank(olParam.getValue())) {
                    PreferencesUtils.putString(context, GlobalConstant.SP_IQIYI_NORMAL_M3U8, olParam.getValue());
                }
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });

        BmobQuery<OlParam> query2 = new BmobQuery<>();
        query1.getObject(context, "mWnUCCC9", new GetListener<OlParam>() {
            @Override
            public void onSuccess(OlParam olParam) {
                if (olParam != null && !StringUtils.isBlank(olParam.getValue())) {
                    PreferencesUtils.putString(context, GlobalConstant.SP_BAIKAN_API, olParam.getValue());
                }
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });

    }

    private void initPrologue() {
        if (PreferencesUtils.getBoolean(context, GlobalConstant.KEY_IS_FIRST_IN, true)) {
            PreferencesUtils.putBoolean(context, GlobalConstant.KEY_IS_FIRST_IN, false);
            findViewById(R.id.prologue_bg).setVisibility(View.GONE);
            RelativeLayout view = (RelativeLayout) findViewById(R.id.first_prologue);
            view.setVisibility(View.VISIBLE);
            SplashPageView splashPageView = new SplashPageView(context, view);
            splashPageView.showGuideSplash();
        } else {
            try {
                //准备展示开屏广告的容器
                FrameLayout adContainer = (FrameLayout) this
                        .findViewById(R.id.layout_splash_ad);

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_prologure);
                ImageView bg = (ImageView) findViewById(R.id.prologue_bg);
                AnimationCollector animationCollector = new AnimationCollector(context);
                animationCollector.specialPrologue(context, layout, bg, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        String titles = PreferencesUtils.getString(context, GlobalConstant.SP_CATELOGUE_TITLES, "");
                        if (StringUtils.isBlank(titles)) {
                            Intent intent = new Intent(context, GuideMoreActivity.class);
                            intent.putExtra("first", true);
                            startActivityForResult(intent, GlobalConstant.INDEX_EDIT_CATELOGUE_RETURN);
                        }
                        BmobQuery<OlParam> query1 = new BmobQuery<>();
                        query1.getObject(context, "IpNSCCCF", new GetListener<OlParam>() {

                            @Override
                            public void onSuccess(OlParam olParam) {
                                if (olParam.getKey().equals("1")) {
                                    PreferencesUtils.putBoolean(context, GlobalConstant.SP_SHOW_AD_SWITCH, true);
                                } else {
                                    PreferencesUtils.putBoolean(context, GlobalConstant.SP_SHOW_AD_SWITCH, false);
                                }
                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initControlView() {
        bottomLayour = (LinearLayout) findViewById(R.id.bottom_bar);
        parserLayout = (ParserLayout) findViewById(R.id.parse_layout);
        webNameTv = (TextView) findViewById(R.id.video_detect_name);
        forwardBtn = (ImageButton) findViewById(R.id.bottom_forward);
        backwardBtn = (ImageButton) findViewById(R.id.bottom_backward);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (getIntent().getBooleanExtra(GlobalConstant.EXTRA_INTENT_IS_SEARCH, false)) {
                String url = getIntent().getStringExtra(IntentUnit.URL);
                if (!StringUtils.isBlank(url)) {
                    dispatchIntent(getIntent());
                }
            }

            MobclickAgent.onResume(this);
            IntentUnit.setContext(this);
            if (create) {
                return;
            }
//        dispatchIntent(getIntent());

            if (IntentUnit.isDBChange()) {
                updateBookmarks();
                updateAutoComplete();
                IntentUnit.setDBChange(false);
            }
            if (IntentUnit.isSPChange()) {
                for (AlbumController controller : BrowserContainer.list()) {
                    if (controller instanceof NinjaWebView) {
                        ((NinjaWebView) controller).initPreferences();
                    }
                }
                IntentUnit.setSPChange(false);
            }

            updateAutoComplete();

            if (currentAlbumController != null && currentAlbumController instanceof NinjaWebView) {
                ((NinjaWebView) currentAlbumController).resume();
                updateBookmarks();
            }

            if (homeContent != null) {
                homeContent.onResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //打开后的intent操作
    private void dispatchIntent(Intent intent) {

        if (intent != null && intent.hasExtra(IntentUnit.OPEN)) { // From HolderActivity's menu
            pinAlbums(intent.getStringExtra(IntentUnit.OPEN));
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_WEB_SEARCH)) { // From ActionMode and some others
            pinAlbums(intent.getStringExtra(SearchManager.QUERY));
        } else if (intent != null && intent.hasExtra(IntentUnit.URL)) { //打开网页
            String url = intent.getStringExtra(IntentUnit.URL);
            pinAlbums(url);
        } else {
            //可以替换成首页地址
            pinAlbums(null);
//            pinAlbums("http://app.cl-c.cc/");
        }
    }

    @Override
    public void onPause() {
        hideSoftInput(inputBox);
        create = false;
        inputBox.clearFocus();
        if (currentAlbumController != null && currentAlbumController instanceof NinjaRelativeLayout) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
            }
        } else if (currentAlbumController != null && currentAlbumController instanceof NinjaWebView) {
            ((NinjaWebView) currentAlbumController).pause();
        }

        IntentUnit.setContext(this);
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    public void onDestroy() {

        BrowserContainer.clear();
        IntentUnit.setContext(null);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        hideSoftInput(inputBox);
        hideSearchPanel();
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // When video fullscreen, first close it
            if (fullscreenHolder != null || customView != null || videoView != null) {
                return onHideCustomView();
            }
            return onKeyCodeBack(true, true);
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // When video fullscreen, just control the sound
        if (fullscreenHolder != null || customView != null || videoView != null) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));
            if (vc != 2) {
                return true;
            }
        }

        return false;
    }

    Filter filter;

    private void initOmnibox() {
        omnibox = (RelativeLayout) findViewById(R.id.main_omnibox);
        inputBox = (AutoCompleteTextView) findViewById(R.id.main_omnibox_input);

        try {
            Class autoCompleteTextView = Class.forName("android.widget.AutoCompleteTextView");
            Field threshold = autoCompleteTextView.getDeclaredField("mThreshold");
            threshold.setAccessible(true);
            threshold.set(inputBox, 0);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        rightBtn = (LinearLayout) findViewById(R.id.main_omnibox_right_btn);
        rightBtn.setVisibility(View.GONE);
        recordBtn = (ImageButton) findViewById(R.id.main_omnibox_record);

        omniboxBookmark = (ImageButton) findViewById(R.id.main_omnibox_bookmark);
        omniboxBookmark.setVisibility(View.GONE);
        omniboxSearch = (ImageButton) findViewById(R.id.main_omnibox_icon_search);
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        inputBox.setOnTouchListener(new SwipeToBoundListener(omnibox, new SwipeToBoundListener.BoundCallback() {
            private KeyListener keyListener = inputBox.getKeyListener();

            @Override
            public boolean canSwipe() {
                if (!inputBox.isPopupShowing()) {
                    if (filter != null && StringUtils.isBlank(inputBox.getText().toString())) {
//                        filter.filter("soku", inputBox);
//                        filter.filter("http://m.iqiyi.com/search.html?source=", inputBox);
                    }
                    inputBox.showDropDown();
                }

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this);
                boolean ob = sp.getBoolean(getString(R.string.sp_omnibox_control), true);
                return ob;
            }

            @Override
            public void onSwipe() {
                inputBox.setKeyListener(null);
                inputBox.setFocusable(false);
                inputBox.setFocusableInTouchMode(false);
                inputBox.clearFocus();
            }

            @Override
            public void onBound(boolean canSwitch, boolean left) {
                inputBox.setKeyListener(keyListener);
                inputBox.setFocusable(true);
                inputBox.setFocusableInTouchMode(true);
                inputBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                inputBox.clearFocus();

                if (canSwitch) {
                    AlbumController controller = nextAlbumController(left);
                    showAlbum(controller, false, false, true);
                    ToastUtils.show(context, controller.getAlbumTitle());
                }
            }
        }));

        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (currentAlbumController == null) { // || !(actionId == EditorInfo.IME_ACTION_DONE)
                    return false;
                }
                String query = inputBox.getText().toString().trim();
                if (StringUtils.isBlank(query)) {
//                    query = "http://www.soku.com/m/y/index.html#loaded";
                    query = "http://m.iqiyi.com/search.html?source=input&vfrm=2-3-0-1&key=";
                }
                if (!BrowserUnit.isURL(query)) {
                    try {
//                        query = URLEncoder.encode(query, BrowserUnit.URL_ENCODING);
//                        query = BrowserUnit.SEARCH_ENGINE_IQIYI + query;
                        Intent intent = new Intent(context, SearchActivity.class);
                        intent.putExtra("search", query);
                        startActivityForResult(intent, 0);
//                        //录入关键词
                        RecordAction action = RecordAction.getInstance(context);
                        action.open(true);
                        action.addHistory(new Record(query, query, System.currentTimeMillis()));
                        action.close();
                        updateAutoComplete();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    updateAlbum(query);
                }

                UploadEventRecord.recordEvent(context, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_SEARCH, "顶部输入框");
                UploadEventRecord.recordEvent(context, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_SEARCH_CONTENT, query);
                UploadEventRecord.recordEventinternal(context, GlobalConstant.P_SEARCH_CONTENT, query);

                hideSoftInput(inputBox);
                return false;
            }
        });
        updateBookmarks();
        updateAutoComplete();

    }


    private void initBHList(final NinjaRelativeLayout layout, boolean update) {
        if (update) {
            updateProgress(BrowserUnit.PROGRESS_MIN);
        }

        RecordAction action = RecordAction.getInstance(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this);
        action.open(false);
        final List<Record> list;
        if (layout.getFlag() == BrowserUnit.FLAG_BOOKMARKS) {
            list = action.listBookmarks();
            Collections.sort(list, new Comparator<Record>() {
                @Override
                public int compare(Record first, Record second) {
                    return first.getTitle().compareTo(second.getTitle());
                }
            });
        } else if (layout.getFlag() == BrowserUnit.FLAG_HISTORY) {
            list = action.listHistory();
        } else {
            list = new ArrayList<>();
        }
        action.close();

        ListView listView = (ListView) layout.findViewById(R.id.record_list);
        TextView textView = (TextView) layout.findViewById(R.id.record_list_empty);
        listView.setEmptyView(textView);

        final RecordAdapter adapter = new RecordAdapter(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this, R.layout.record_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /* Wait for adapter.notifyDataSetChanged() */
        if (update) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                    updateProgress(BrowserUnit.PROGRESS_MAX);
                }
            }, shortAnimTime);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateAlbum(list.get(position).getURL());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showListMenu(adapter, list, position);
                return true;
            }
        });
    }

    private synchronized void addAlbum(int flag) {
        final AlbumController holder;
        if (flag == BrowserUnit.FLAG_BOOKMARKS) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.record_list, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_BOOKMARKS);
//            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_bookmarks));
            holder = layout;
            initBHList(layout, false);
        } else if (flag == BrowserUnit.FLAG_HISTORY) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.record_list, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_HISTORY);
//            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_history));
            holder = layout;
            initBHList(layout, false);
        } else if (flag == BrowserUnit.FLAG_HOME) {
            holder = homeContent.getContainView();
        } else {
            return;
        }
        BrowserContainer.add(holder);
        showAlbum(holder, false, true, true);
    }

    private synchronized void addAlbum(String title, final String url, final boolean foreground, final Message resultMsg) {
        final NinjaWebView webView = new NinjaWebView(this, webviewHandler);
        webView.setBrowserController(this);
        webView.setFlag(BrowserUnit.FLAG_NINJA);
//        webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        webView.setAlbumTitle(title);
        ViewUnit.bound(this, webView);
//        iqiyiParser.addJsInterface(webView);

        final View albumView = webView.getAlbumView();
        if (currentAlbumController != null && (currentAlbumController instanceof NinjaWebView) && resultMsg != null) {
            int index = BrowserContainer.indexOf(currentAlbumController) + 1;
            BrowserContainer.add(webView, index);
        } else {
            BrowserContainer.add(webView);
        }

        if (!foreground) {
            ViewUnit.bound(this, webView);
            webView.loadUrl(url);
            webView.deactivate();

            albumView.setVisibility(View.VISIBLE);
            return;
        }

        albumView.setVisibility(View.INVISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.album_slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showAlbum(webView, false, true, false);

                if (url != null && !url.isEmpty()) {
                    webView.loadUrl(url);
                } else if (resultMsg != null) {
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(webView);
                    resultMsg.sendToTarget();
                }
            }
        });
        albumView.startAnimation(animation);
    }

    private synchronized void pinAlbums(String url) {
        try {
            hideSoftInput(inputBox);
            hideSearchPanel();
            for (AlbumController controller : BrowserContainer.list()) {
                if (controller instanceof NinjaWebView) {
                    ((NinjaWebView) controller).setBrowserController(this);
                } else if (controller instanceof NinjaRelativeLayout) {
                    ((NinjaRelativeLayout) controller).setBrowserController(this);
                }
                controller.getAlbumView().setVisibility(View.VISIBLE);
                controller.deactivate();
            }
            if (BrowserContainer.size() < 1 && url == null) {
                addAlbum(BrowserUnit.FLAG_HOME);
            } else if (BrowserContainer.size() >= 1 && url == null) {
                if (currentAlbumController != null) {
                    currentAlbumController.activate();
                    return;
                }
                int index = BrowserContainer.size() - 1;
                currentAlbumController = BrowserContainer.get(index);
                contentFrame.removeAllViews();
                if (currentAlbumController != null && ((View) currentAlbumController).getParent() != null) {
                    ((ViewGroup) ((View) currentAlbumController).getParent()).removeView((View) currentAlbumController);
                }
                contentFrame.addView((View) currentAlbumController);
                currentAlbumController.activate();

                updateOmnibox();

            } else { // When url != null
                //这里刷新界面网页会出问题，要查一下。。。
                NinjaWebView webView = new NinjaWebView(this, webviewHandler);
                webView.setBrowserController(this);
                webView.setFlag(BrowserUnit.FLAG_NINJA);
//            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                webView.setAlbumTitle(getString(R.string.album_untitled));
                ViewUnit.bound(this, webView);
//                iqiyiParser.addJsInterface(webView);
                webView.loadUrl(url);

                BrowserContainer.add(webView);
                final View albumView = webView.getAlbumView();
                albumView.setVisibility(View.VISIBLE);
                contentFrame.removeAllViews();
                if (webView != null && webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                contentFrame.addView(webView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                if (currentAlbumController != null) {
                    currentAlbumController.deactivate();
                }
                currentAlbumController = webView;
                currentAlbumController.activate();

                updateOmnibox();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void showAlbum(AlbumController controller, boolean anim, final boolean expand, final boolean capture) {
        if (controller == null || controller == currentAlbumController) {
            return;
        }

        if (currentAlbumController != null && anim) {
            currentAlbumController.deactivate();
            final View rv = (View) currentAlbumController;
            final View av = (View) controller;

            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.album_fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {
                    contentFrame.removeAllViews();
                    if (av != null && av.getParent() != null) {
                        ((ViewGroup) av.getParent()).removeView(av);
                    }
                    contentFrame.addView(av, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            });
            rv.startAnimation(fadeOut);
        } else {
            if (currentAlbumController != null) {
                currentAlbumController.deactivate();
            }
            contentFrame.removeAllViews();
            if (controller != null && ((View) controller).getParent() != null) {
                ((ViewGroup) ((View) controller).getParent()).removeView((View) controller);
            }
            contentFrame.addView((View) controller, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        currentAlbumController = controller;
        currentAlbumController.activate();
        updateOmnibox();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                if (capture) {
//                    currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
//                }
//            }
//        }, shortAnimTime);
    }

    private synchronized void updateAlbum() {
        try {
            if (currentAlbumController == null) {
                return;
            }
            if (parserLayout != null && parserLayout.getVisibility() == View.VISIBLE) {
                parserLayout.setVisibility(View.GONE);
                bottomLayour.setVisibility(View.VISIBLE);
            }
            omniboxSearch.setVisibility(View.VISIBLE);
            omniboxBookmark.setVisibility(View.GONE);
            rightBtn.setVisibility(View.GONE);
            recordBtn.setVisibility(View.VISIBLE);

            NinjaRelativeLayout layout = homeContent.getContainView();

            currentAlbumController.deactivate();
            contentFrame.removeAllViews();
            if (layout != null && layout.getParent() != null) {
                ((ViewGroup) layout.getParent()).removeView(layout);
            }
            contentFrame.addView(layout);
//        BrowserContainer.set(layout, index);
            currentAlbumController = layout;
            updateOmnibox();
            backwardBtn.setImageResource(R.mipmap.tab_ic_back_disable);
            forwardBtn.setImageResource(R.mipmap.tab_ic_go_disable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAlbum(String url) {
        try {
            if (currentAlbumController == null) {
                return;
            }
            omniboxSearch.setVisibility(View.GONE);
            omniboxBookmark.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.VISIBLE);
            recordBtn.setVisibility(View.GONE);
            if (currentAlbumController instanceof NinjaWebView) {
                ((NinjaWebView) currentAlbumController).loadUrl(url);
                updateOmnibox();
            } else if (currentAlbumController instanceof NinjaRelativeLayout) {

                NinjaWebView webView = new NinjaWebView(this, webviewHandler);
                webView.setBrowserController(this);
                webView.setFlag(BrowserUnit.FLAG_NINJA);
//            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                webView.setAlbumTitle(getString(R.string.album_untitled));
//                iqiyiParser.addJsInterface(webView);
                webView.addCheckFileJsInterface(webviewHandler);

//            int index = switcherContainer.indexOfChild(currentAlbumController.getAlbumView());
                currentAlbumController.deactivate();
                contentFrame.removeAllViews();
                if (webView != null && webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                contentFrame.addView(webView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            BrowserContainer.set(webView, index);
                currentAlbumController = webView;
                webView.activate();

                webView.loadUrl(url);

                updateOmnibox();
                backwardBtn.setImageResource(R.drawable.ic_backward);

            } else {
                ToastUtils.show(context, R.string.toast_load_error);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void removeAlbum(AlbumController controller) {
        if (currentAlbumController == null || BrowserContainer.size() <= 1) {
            BrowserContainer.remove(controller);
            addAlbum(BrowserUnit.FLAG_HOME);
            return;
        }

        if (controller != currentAlbumController) {
            BrowserContainer.remove(controller);
        } else {
            int index = BrowserContainer.indexOf(controller);
            BrowserContainer.remove(controller);
            if (index >= BrowserContainer.size()) {
                index = BrowserContainer.size() - 1;
            }
            showAlbum(BrowserContainer.get(index), false, false, false);
        }
    }

    @Override
    public void updateAutoComplete() {
        RecordAction action = RecordAction.getInstance(this);
        action.open(false);
        List<Record> list = action.listBookmarks();
        list.addAll(action.listHistory());
        action.close();
        final CompleteAdapter adapter = new CompleteAdapter(this, R.layout.complete_item, list, true);
        inputBox.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        filter = adapter.getFilter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputBox.setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen.layout_height_6dp));
        }
        inputBox.setDropDownWidth(ViewUnit.getWindowWidth(this));
        inputBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    TextView tv = (TextView) view.findViewById(R.id.complete_item_url);
                    if (tv != null) {
                        String url = tv.getText().toString();
                        url = StringUtils.nullStrToEmpty(url);
                        if (!BrowserUnit.isURL(url)) {
                            Intent intent = new Intent(context, SearchActivity.class);
                            intent.putExtra("search", url);
                            startActivityForResult(intent, 0);
                        } else {
                            inputBox.setText(Html.fromHtml(BrowserUnit.urlWrapper(url)), EditText.BufferType.SPANNABLE);
                            inputBox.setSelection(url.length());
                            updateAlbum(url);
                        }
                        hideSoftInput(inputBox);
                        UploadEventRecord.recordEvent(context, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_SEARCH, "drowlist_click");
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void updateBookmarks() {
        if (currentAlbumController == null || !(currentAlbumController instanceof NinjaWebView)) {
            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.ic_title_bm));
            return;
        }

        RecordAction action = RecordAction.getInstance(this);
        action.open(false);
        String url = ((NinjaWebView) currentAlbumController).getUrl();
        if (action.checkBookmark(url)) {
            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.mipmap.spzd_nav_ic_collect_down));
        } else {
            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.ic_title_bm));
        }
        action.close();
    }

    @Override
    public void updateInputBox(String query) {
        if (query != null) {
            inputBox.setText(Html.fromHtml(BrowserUnit.urlWrapper(query)), EditText.BufferType.SPANNABLE);
        } else {
            inputBox.setText(null);
        }
        inputBox.clearFocus();
    }

    private void updateOmnibox() {
        if (currentAlbumController == null) {
            return;
        }

        if (currentAlbumController instanceof NinjaRelativeLayout) {
            updateProgress(BrowserUnit.PROGRESS_MAX);
            updateBookmarks();
            updateInputBox(null);
        } else if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            updateProgress(ninjaWebView.getProgress());
            updateBookmarks();
            if (ninjaWebView.getUrl() == null && ninjaWebView.getOriginalUrl() == null) {
                updateInputBox(null);
            } else if (ninjaWebView.getUrl() != null) {
                updateInputBox(ninjaWebView.getUrl());
            } else {
                updateInputBox(ninjaWebView.getOriginalUrl());
            }
        }
    }

    @Override
    public synchronized void updateProgress(int progress) {
        if (progress > progressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", progress);
            animator.setDuration(shortAnimTime);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else if (progress < progressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, progress);
            animator.setDuration(shortAnimTime);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }

        updateBookmarks();
        if (progress < BrowserUnit.PROGRESS_MAX) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        // Because Activity launchMode is singleInstance,
        // so we can not get result from onActivityResult when Android 4.X,
        // what a pity
        //
        // this.uploadMsg = uploadMsg;
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.setType("*/*");
        // startActivityForResult(Intent.createChooser(intent, getString(R.string.main_file_chooser)), IntentUnit.REQUEST_FILE_16);
        uploadMsg.onReceiveValue(null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_desc, null, false);
        TextView textView = (TextView) layout.findViewById(R.id.dialog_desc);
        textView.setText(R.string.dialog_content_upload);

        builder.setView(layout);
        builder.create().show();
    }

    @Override
    public void onCreateView(WebView view, final Message resultMsg) {
        if (resultMsg == null) {
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addAlbum(getString(R.string.album_untitled), null, true, resultMsg);
            }
        }, shortAnimTime);
    }

    @Override
    public boolean onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
        return onShowCustomView(view, callback);
    }

    @Override
    public boolean onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view == null) {
            return false;
        }
        try {
            if (customView != null && callback != null) {
                callback.onCustomViewHidden();
                return false;
            }

            customView = view;
            originalOrientation = getRequestedOrientation();

            fullscreenHolder = new FullscreenHolder(this);
            if (customView != null && customView.getParent() != null) {
                ((ViewGroup) customView.getParent()).removeView(customView);
            }
            fullscreenHolder.addView(
                    customView,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    ));

            FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
            if (fullscreenHolder != null && fullscreenHolder.getParent() != null) {
                ((ViewGroup) fullscreenHolder.getParent()).removeView(fullscreenHolder);
            }
            decorView.addView(
                    fullscreenHolder,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    ));

            customView.setKeepScreenOn(true);
            ((View) currentAlbumController).setVisibility(View.GONE);
            setCustomFullscreen(true);

            if (view instanceof FrameLayout) {
                if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                    videoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                    videoView.setOnErrorListener(new VideoCompletionListener());
                    videoView.setOnCompletionListener(new VideoCompletionListener());
                }
            }
            customViewCallback = callback;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Auto landscape when video shows
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onHideCustomView() {
        if (customView == null || customViewCallback == null || currentAlbumController == null) {
            return false;
        }
        try {
            FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
            if (fullscreenHolder != null && decorView != null) {
                decorView.removeView(fullscreenHolder);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                try {
                    customViewCallback.onCustomViewHidden();
                } catch (Throwable t) {
                }
            }

            customView.setKeepScreenOn(false);
            ((View) currentAlbumController).setVisibility(View.VISIBLE);
            setCustomFullscreen(false);

            fullscreenHolder = null;
            customView = null;
            if (videoView != null) {
                videoView.setOnErrorListener(null);
                videoView.setOnCompletionListener(null);
                videoView = null;
            }
            setRequestedOrientation(originalOrientation);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onLongPress(String url) {
        WebView.HitTestResult result;
        if (!(currentAlbumController instanceof NinjaWebView)) {
            return;
        }
        result = ((NinjaWebView) currentAlbumController).getHitTestResult();

        final List<String> list = new ArrayList<>();
        list.add(getString(R.string.main_menu_new_tab));
        list.add(getString(R.string.main_menu_copy_link));
        if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
            list.add(getString(R.string.main_menu_save));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_text_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        final AlertDialog dialog = builder.create();
        if (url != null || (result != null && result.getExtra() != null)) {
            if (url == null) {
                url = result.getExtra();
            }
            dialog.show();
        }

        final String target = url;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = list.get(position);
                if (s.equals(getString(R.string.main_menu_new_tab))) { // New tab
                    addAlbum(getString(R.string.album_untitled), target, false, null);
                    ToastUtils.show(context, R.string.toast_new_tab_successful);
                } else if (s.equals(getString(R.string.main_menu_copy_link))) { // Copy link
                    BrowserUnit.copyURL(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this, target);
                } else if (s.equals(getString(R.string.main_menu_save))) { // Save
                    BrowserUnit.download(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this, target, target, BrowserUnit.MIME_TYPE_IMAGE);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }


    private boolean onKeyCodeBack(boolean douQ, boolean willFinish) {
        hideSoftInput(inputBox);
        if (currentAlbumController == null) {
            finish();
        } else if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            if (ninjaWebView.canGoBack()) {
                ninjaWebView.goBack();
            } else {
                updateAlbum();
            }
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            switch (currentAlbumController.getFlag()) {
                case BrowserUnit.FLAG_BOOKMARKS:
                    updateAlbum();
                    break;
                case BrowserUnit.FLAG_HISTORY:
                    updateAlbum();
                    break;
                case BrowserUnit.FLAG_HOME:
                    if (willFinish) {
                        if (douQ) {
                            doubleTapsQuit();
                        } else {
                            finish();
                        }
                    }
                    break;
                default:
                    finish();
                    break;
            }
        } else {
            finish();
        }

        return true;
    }

    private void doubleTapsQuit() {
        final Timer timer = new Timer();
        if (!quit) {
            quit = true;
            ToastUtils.show(context, R.string.toast_double_taps_quit);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false;
                    timer.cancel();
                }
            }, DOUBLE_TAPS_QUIT_DEFAULT);
        } else {
            timer.cancel();
            finish();
        }
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSearchPanel() {
        omnibox.setVisibility(View.VISIBLE);
    }


    private void showListMenu(final RecordAdapter recordAdapter, final List<Record> recordList, final int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        final String[] array = getResources().getStringArray(R.array.list_menu);
        final List<String> stringList = new ArrayList<>();
        stringList.addAll(Arrays.asList(array));
        if (currentAlbumController.getFlag() != BrowserUnit.FLAG_BOOKMARKS) {
            stringList.remove(array[3]);
        }

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter dialogAdapter = new DialogAdapter(this, R.layout.dialog_text_item, stringList);
        listView.setAdapter(dialogAdapter);
        dialogAdapter.notifyDataSetChanged();

        final AlertDialog dialog = builder.create();
        dialog.show();

        final Record record = recordList.get(location);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = stringList.get(position);
                if (s.equals(array[0])) { // New tab
                    addAlbum(getString(R.string.album_untitled), record.getURL(), false, null);
                    ToastUtils.show(context, R.string.toast_new_tab_successful);
                } else if (s.equals(array[1])) { // Copy link
                    BrowserUnit.copyURL(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this, record.getURL());
                } else if (s.equals(array[2])) { // Share
                    IntentUnit.share(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this, record.getTitle(), record.getURL());
                } else if (s.equals(array[3])) { // Edit
                    showEditDialog(recordAdapter, recordList, location);
                } else if (s.equals(array[4])) { // Delete
                    RecordAction action = RecordAction.getInstance(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this);
                    action.open(true);
                    if (currentAlbumController.getFlag() == BrowserUnit.FLAG_BOOKMARKS) {
                        action.deleteBookmark(record);
                    } else if (currentAlbumController.getFlag() == BrowserUnit.FLAG_HISTORY) {
                        action.deleteHistory(record);
                    }
                    action.close();

                    recordList.remove(location);
                    recordAdapter.notifyDataSetChanged();

                    updateBookmarks();
                    updateAutoComplete();

                    ToastUtils.show(context, R.string.toast_delete_successful);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }

    private void showEditDialog(final RecordAdapter recordAdapter, List<Record> recordList, int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_edit, null, false);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final Record record = recordList.get(location);
        final EditText editText = (EditText) layout.findViewById(R.id.dialog_edit);
        editText.setHint(R.string.dialog_title_hint);
        editText.setText(record.getTitle());
        editText.setSelection(record.getTitle().length());
        hideSoftInput(inputBox);
        showSoftInput(editText);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_DONE) {
                    return false;
                }

                String text = editText.getText().toString().trim();
                if (text.isEmpty()) {
                    ToastUtils.show(context, R.string.toast_input_empty);
                    return true;
                }

                RecordAction action = RecordAction.getInstance(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this);
                action.open(true);
                record.setTitle(text);
                action.updateBookmark(record);
                action.close();

                recordAdapter.notifyDataSetChanged();
                hideSoftInput(editText);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        dialog.dismiss();
                    }
                }, longAnimTime);
                return false;
            }
        });
    }

    private boolean prepareRecord() {
        if (currentAlbumController == null || !(currentAlbumController instanceof NinjaWebView)) {
            return false;
        }

        NinjaWebView webView = (NinjaWebView) currentAlbumController;
        String title = webView.getTitle();
        String url = webView.getUrl();
        if (title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
            return false;
        }
        return true;
    }

    private void setCustomFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        /*
         * Can not use View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
         * so we can not hide NavigationBar :(
         */
        int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        if (fullscreen) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
            if (customView != null) {
                customView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                contentFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        getWindow().setAttributes(layoutParams);
    }

    private AlbumController nextAlbumController(boolean next) {
        if (BrowserContainer.size() <= 1) {
            return currentAlbumController;
        }

        List<AlbumController> list = BrowserContainer.list();
        int index = list.indexOf(currentAlbumController);
        if (next) {
            index++;
            if (index >= list.size()) {
                index = 0;
            }
        } else {
            index--;
            if (index < 0) {
                index = list.size() - 1;
            }
        }

        return list.get(index);
    }


    @Override
    public void onClick(View v) {
        try {
            NinjaWebView ninjaWebView;
            switch (v.getId()) {
                case R.id.btn_play_web:
                    if (currentAlbumController instanceof NinjaWebView) {
                        MobclickAgent.onEvent(context, GlobalConstant.DOG_CLICK_EVENT);
                        UploadEventRecord.recordEvent(context, GlobalConstant.DOG_CLICK_EVENT, GlobalConstant.P_VIDEO_CLICK, "观看");

                        ninjaWebView = (NinjaWebView) currentAlbumController;
                        String webTitle = ninjaWebView.getTitle();
                        LunaLog.d("title: " + webTitle);
                        UploadEventRecord.recordEventinternal(context, GlobalConstant.ACTION_PLAY_VIDEO_WEB, webTitle);
                        String webUrl = ninjaWebView.getUrl();
                        String content = ninjaWebView.getWebContent();
                        //正则表达式暂时不全，先用字符串判断
                        site = BrowserUnit.checkSite(webUrl);
//                        if (webUrl.contains("m.iqiyi.com")) {
//                            iqiyiParser.setUrl(webUrl);
//                            iqiyiParser.setHandler(webviewHandler);
//                            iqiyiParser.loadJsCode(ninjaWebView);
//                            updateProgress(50);
//                        } else {
                        goToVideoActivity(webUrl, webUrl, webTitle, content);
//                        }
                    }
                    break;
                case R.id.main_omnibox_refresh:
                    refresh();
                    break;
                case R.id.main_omnibox_bookmark:
                    if (currentAlbumController instanceof NinjaWebView) {
                        if (!prepareRecord()) {
                            ToastUtils.show(context, R.string.toast_add_bookmark_failed);
                            return;
                        }

                        ninjaWebView = (NinjaWebView) currentAlbumController;
                        String title = ninjaWebView.getTitle();
                        String url = ninjaWebView.getUrl();

                        RecordAction action = RecordAction.getInstance(android.luna.net.videohelper.Ninja.Activity.BrowserActivity.this);
                        action.open(true);
                        if (action.checkBookmark(url)) {
                            action.deleteBookmark(url);
                            ToastUtils.show(context, R.string.toast_delete_bookmark_successful);
                        } else {
                            action.addBookmark(new Record(title, url, System.currentTimeMillis()));
                            ToastUtils.show(context, R.string.toast_add_bookmark_successful);
                        }
                        action.close();

                        updateBookmarks();
                        updateAutoComplete();
                    }
                    break;
                case R.id.bottom_exit:
                    updateAlbum();
                    break;
                case R.id.bottom_backward:
                    onKeyCodeBack(false, false);
                    break;
                case R.id.bottom_forward:
                    if (currentAlbumController instanceof NinjaWebView) {
                        ninjaWebView = (NinjaWebView) currentAlbumController;
                        if (ninjaWebView.canGoForward()) {
                            ninjaWebView.goForward();
                        }
                    }
                    break;
                case R.id.bottom_bookmark:
                    startActivityForResult(new Intent(context, BookmarkActivity.class), GlobalConstant.INDEX_BOOKMARK_RETURN);
                    break;
                case R.id.main_omnibox_record:
                    Intent recordIntent = new Intent(context, BookmarkActivity.class);
                    recordIntent.putExtra("type", GlobalConstant.ACTIVITY_TYPE_RECORD);
                    startActivityForResult(recordIntent, GlobalConstant.INDEX_BOOKMARK_RETURN);
                    break;
                case R.id.bottom_download:
                    startActivity(new Intent(context, DownloadActivity.class));
                    break;
                case R.id.btn_detection_close:
                    parserLayout.setVisibility(View.GONE);
                    bottomLayour.setVisibility(View.VISIBLE);
                    currentAlbumController.activate();
                    break;
                case R.id.btn_download_web:
                    if (currentAlbumController instanceof NinjaWebView) {
                        if (defDialog == null) {
                            defDialog = new DefinitionDialog(context);
                        }
                        ninjaWebView = (NinjaWebView) currentAlbumController;
                        String webTitle = ninjaWebView.getTitle();
                        String webUrl = ninjaWebView.getUrl();
                        //正则表达式暂时不全，先用字符串判断
                        site = BrowserUnit.checkSite(webUrl);
                        Message msg = new Message();
                        msg.what = 99;
                        msg.obj = webTitle;
                        Bundle bundle = new Bundle();
                        bundle.putString("webTitle", webTitle);
                        bundle.putString("webUrl", webUrl);
                        msg.setData(bundle);
                        downloadHandler.sendMessage(msg);
                        defDialog.showDialog(webUrl, site, "", webTitle, downloadHandler);
                    }
                    break;
                case R.id.btn_qr_code:
                    if (currentAlbumController instanceof NinjaWebView) {
                        ninjaWebView = (NinjaWebView) currentAlbumController;
                        String webUrl = ninjaWebView.getUrl();
                        String webTitle = ninjaWebView.getTitle();
                        site = BrowserUnit.checkSite(webUrl);
                        if (!shareDialog.needShowShareDialog(webTitle)) {
//                            if (webUrl.contains("m.iqiyi.com")) {
//                                Message msg = new Message();
//                                msg.what = 99;
//                                Bundle bundle = new Bundle();
//                                bundle.putString("webTitle", webTitle);
//                                bundle.putString("webUrl", webUrl);
//                                msg.setData(bundle);
//                                captureHandler.sendMessage(msg);
////                                iqiyiParser.setUrl(webUrl);
////                                iqiyiParser.setHandler(captureHandler);
////                                iqiyiParser.loadJsCode(ninjaWebView);
//                                updateProgress(50);
//                            } else {
                            gotoCaptureActivity(webUrl, webTitle);
//                            }
                            UploadEventRecord.recordEvent(context, GlobalConstant.DOG_CLICK_EVENT, GlobalConstant.P_VIDEO_CLICK, "扫码播放");
                        }
                    }
                    break;
                case R.id.parser_guide_pic:
                    findViewById(R.id.parser_guide_pic).setVisibility(View.GONE);
                    break;
                default:
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoCaptureActivity(String url, String webTitle) {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra("webTitle", webTitle);
        intent.putExtra("webUrl", url);
        intent.putExtra("site", site);
        startActivity(intent);
    }


    private void refresh() {
        if (currentAlbumController == null) {
            ToastUtils.show(context, R.string.toast_refresh_failed);
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            if (ninjaWebView.isLoadFinish()) {
                ninjaWebView.reload();
            } else {
                ninjaWebView.stopLoading();
            }
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            final NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
//                        initHomeGrid(layout, true);
                return;
            }
            initBHList(layout, true);
        } else {
            ToastUtils.show(context, R.string.toast_refresh_failed);
        }
    }

    private void goToVideoActivity(String url, String webUrl, String name, String content) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("content", content);
        intent.putExtra("webUrl", webUrl);
        intent.putExtra("name", name);
        intent.putExtra("site", site);
        intent.putExtra("fromWebview", true);
        this.startActivityForResult(intent, GlobalConstant.INDEX_VIDEO_RETURN);
    }

    private Handler captureHandler = new Handler(Looper.getMainLooper()) {
        String title = "";
        String webUrl = "";

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == GlobalConstant.VIDEO_URL_RECEIVE) {
                String url = (String) msg.obj;
                Intent intent = new Intent(context, CaptureActivity.class);
                intent.putExtra("webTitle", title);
                intent.putExtra("webUrl", webUrl);
                LunaLog.d("weburl: " + webUrl);
                intent.putExtra("link", url);
                intent.putExtra("site", site);
                startActivity(intent);
            } else if (msg.what == 99) {
                Bundle bundle = msg.getData();
                webUrl = bundle.getString("webUrl");
                title = bundle.getString("webTitle");
            }
        }
    };

    private Handler downloadHandler = new Handler(Looper.getMainLooper()) {
        String title = "";
        String webUrl = "";

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == GlobalConstant.VIDEO_URL_RECEIVE) {
                String url = (String) msg.obj;
                if (StringUtils.isBlank(url)) {
                    if (msg.arg2 == 1003) {
//                        if (iqiyiParser != null && webUrl.contains("m.iqiyi.com") && currentAlbumController instanceof NinjaWebView) {
//                            iqiyiParser.setUrl(webUrl);
//                            iqiyiParser.setHandler(downloadHandler);
//                            iqiyiParser.loadJsCode((WebView) currentAlbumController);
//                            return;
//                        }
                    }
                    ToastUtils.show(context, getResources().getString(R.string.can_not_get_video_address));
                    return;
                }
                Intent i = new Intent(context, VideosDownloadService.class);
                i.setAction(Intent.ACTION_INSERT);
                Uri uri = Uri.parse(url);
                i.setData(uri);
                i.putExtra(Intent.EXTRA_TITLE, title);
                startService(i);
                UploadEventRecord.recordEvent(context, GlobalConstant.DOG_CLICK_EVENT, GlobalConstant.P_VIDEO_CLICK, "缓存");
            } else if (msg.what == 99) {
                Bundle bundle = msg.getData();
                webUrl = bundle.getString("webUrl");
                title = bundle.getString("webTitle");
            }
        }
    };

    private Handler webviewHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            String url = (String) msg.obj;

            switch (msg.what) {
                case GlobalConstant.VIDEO_URL_RECEIVE:
                    String webTitle = "";
                    String webUrl = "";
                    String content = "";
                    if (!url.equals("error")) {
                        updateProgress(BrowserUnit.PROGRESS_MAX);

                        if (currentAlbumController instanceof NinjaWebView) {
                            webTitle = ((NinjaWebView) currentAlbumController).getTitle();
                            webUrl = ((NinjaWebView) currentAlbumController).getUrl();
                            content = ((NinjaWebView) currentAlbumController).getWebContent();
                        }
                        goToVideoActivity(url, webUrl, webTitle, content);
                    }
                    break;
                case BrowserUnit.MESSAGE_PAGE_RECEIVE_TITLE:
                    changeParserStatus(url);
                    break;
                case BrowserUnit.MESSAGE_PAGE_START:

                    if (!SiteRegex.checkUrlcanParser(url)) {
                        if (parserLayout.getVisibility() == View.VISIBLE) {
                            parserLayout.setVisibility(View.GONE);
                            bottomLayour.setVisibility(View.VISIBLE);
                        }
                    }
                    if (currentAlbumController instanceof NinjaWebView) {
                        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
                        site = BrowserUnit.checkSite(url);
                        if (ninjaWebView.canGoForward()) {
                            forwardBtn.setImageResource(R.drawable.ic_forward);
                        } else {
                            forwardBtn.setImageResource(R.mipmap.tab_ic_go_disable);
                        }
                    }
                    break;
                case BrowserUnit.MESSAGE_PAGE_FINISH:
                    if (currentAlbumController instanceof NinjaWebView) {
                        LunaLog.e("fini url: " + url);
                        if (url.matches(SiteRegex.HUNAN_SITE_REGEX)) {
                            changeParserStatus(url);
                        }
                        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
                        site = BrowserUnit.checkSite(url);
                        ninjaWebView.loadJsWhenFinish(site);
                    }

                    break;
                case BrowserUnit.MESSAGE_LOAD_RESOURCE:
                    if (currentAlbumController instanceof NinjaWebView) {
                        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
                        ninjaWebView.loadCheckVideoJs(site);
                    }
                    break;
                case BrowserUnit.MESSAGE_SHOULDLOAD_URL:
                    break;
                case BrowserUnit.MESSAGE_OPEN_URL:
                    updateAlbum((String) msg.obj);
                    break;
            }
        }
    };

    private void changeParserStatus(String url) {
        String webTitle = "";
        if (currentAlbumController instanceof NinjaWebView) {
            webTitle = ((NinjaWebView) currentAlbumController).getTitle();
        }
        webNameTv.setText(webTitle);
        if (SiteRegex.checkUrlcanParser(url)) {
            boolean isFirstParser = PreferencesUtils.getBoolean(context, GlobalConstant.SP_FIRST_PARSER, true);
            if (isFirstParser) {
                PreferencesUtils.putBoolean(context, GlobalConstant.SP_FIRST_PARSER, false);
                findViewById(R.id.parser_guide_pic).setVisibility(View.VISIBLE);
            }
            if (parserLayout.getVisibility() == View.GONE) {
                parserLayout.setVisibility(View.VISIBLE);
//                bottomLayour.setVisibility(View.GONE);
            }
        } else {
            if (parserLayout.getVisibility() == View.VISIBLE) {
                parserLayout.setVisibility(View.GONE);
                bottomLayour.setVisibility(View.VISIBLE);
            }
        }
    }
}
