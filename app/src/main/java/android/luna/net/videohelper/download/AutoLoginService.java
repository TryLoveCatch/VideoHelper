package android.luna.net.videohelper.download;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import net.luna.common.debug.LunaLog;

import java.util.ArrayList;


public class AutoLoginService extends AccessibilityService {
    private static final String TAG = "AutoInstallService";


    private ArrayList<AccessibilityNodeInfo> mNodeInfoList = new ArrayList<AccessibilityNodeInfo>();

    private AccessibilityNodeInfo loginNode;

    private boolean isContainMy = false;
    private boolean isContainNoLogin = false;
    private boolean isContainAutoLogin = false;
    private boolean isInputUsername = false;
    private boolean isInputPsw = false;
    private boolean isTouchLogin = false;

    private ClipboardManager mClipboardManager;

    private String username = "";
    private String password = "";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                log("window state changed");
                AccessibilityNodeInfo nodeInfo = event.getSource();

                if (null != nodeInfo) {
                    mNodeInfoList.clear();
                    traverseNode(nodeInfo);
                    LunaLog.d("isInputPsw :" + isInputPsw + "      isInputUsername:" + isInputUsername);
                    performClickAction();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                AccessibilityNodeInfo scrolledInfo = event.getSource();

                if (null != scrolledInfo) {
                    mNodeInfoList.clear();
                    traverseNode(scrolledInfo);
                    performClickAction();
                }
                break;

            default:
                break;
        }
    }


    synchronized private void traverseNode(AccessibilityNodeInfo node) {
        if (null == node) return;

        final int count = node.getChildCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                traverseNode(childNode);
            }
        } else {

            CharSequence text = node.getText();
            if (null != text && text.length() > 0) {
                String str = text.toString();

                if (node.getClassName().equals("android.widget.EditText")) {
                    log(node.getText());
                    if (str.equals(username)) {
                        isInputUsername = true;
                    }
                }
//                node.findAccessibilityNodeInfosByViewId();
                if (str.contains("未登录")) {
                    log("遍历到了未登录");
                    isContainNoLogin = true;
                    AccessibilityNodeInfo cellNode = node.getParent();
                    if (null != cellNode) mNodeInfoList.add(cellNode);
                    return;
                }

                if (str.contains("取消")) {
                    isContainAutoLogin = true;
                    log("遍历到对话框");
                    mNodeInfoList.add(node);
                }

                if (str.contains("手机号或邮箱")) {
                    if (node.getClassName().equals("android.widget.EditText")) {
//                        log("遍历到账号框");
                        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        ClipData clip = ClipData.newPlainText("label", username);
                        if (mClipboardManager == null) {
                            mClipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        }
                        mClipboardManager.setPrimaryClip(clip);
                        node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                        isInputUsername = true;
                    }
                }


                if (str.equals("登录") && node.getParent().getClassName().equals("android.widget.RelativeLayout")) {
                    AccessibilityNodeInfo cellNode = node.getParent();
                    if (null != cellNode) {
//                        mNodeInfoList.add(cellNode);
                        loginNode = cellNode;
//                        log("遍历到登录按钮");
                    }
                }
            } else {
                if (node.getClassName().equals("android.widget.EditText")) {
                    if (node.getText() == null) {
                        log("遍历到密码框");
                        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        ClipData clip = ClipData.newPlainText("label", password);
                        if (mClipboardManager == null) {
                            mClipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        }
                        mClipboardManager.setPrimaryClip(clip);
                        node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                        isInputPsw = true;
                        if (isInputUsername) {
                            loginNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

    private void log(Object obj) {
        Log.d(TAG, ">>>" + obj);
    }

    private void performClickAction() {
        if (isContainNoLogin || isContainAutoLogin || (isInputPsw && isInputUsername)) {
            log("执行点击");
            int size = mNodeInfoList.size();
            if (size < 1) {
                return;
            }
            AccessibilityNodeInfo cellNode = mNodeInfoList.get(size - 1);
            cellNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (isContainAutoLogin) {
                isContainAutoLogin = false;
            } else {
                isContainMy = false;
            }
        }
    }
}
