package android.luna.net.videohelptools.wxapi;

import android.luna.net.videohelper.activity.BaseActivity;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import net.luna.common.util.PreferencesUtils;
import net.luna.common.wechat.WeChatSDKManager;

/**
 * Created by bintou on 16/1/27.
 */
public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeChatSDKManager.getInstanct(this).handIntent(this, getIntent());

    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
            PreferencesUtils.putBoolean(this, GlobalConstant.SP_HAS_SHARE, true);
        }
        finish();
    }
}
