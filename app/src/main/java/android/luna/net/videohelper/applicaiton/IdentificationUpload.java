package android.luna.net.videohelper.applicaiton;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.luna.net.videohelper.bean.user.Identification;
import android.os.Build;

import net.luna.common.util.ListUtils;
import net.luna.common.util.NetWorkUtil;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.ThreadUtils;

import java.util.Calendar;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by bintou on 15/11/26.
 */
public class IdentificationUpload {

    Context mContext;

    public IdentificationUpload(Context mContext) {
        this.mContext = mContext;
    }

    Identification identification;

    public void checkUserIdentification() {

        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String curData = Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DATE);
                    String lastData = PreferencesUtils.getString(mContext, "curdata", "none");
                    if (!curData.equals(lastData)) {
                        identification = new Identification();
                        String imei = NetWorkUtil.getImei();
                        String model = Build.MODEL;
                        identification.setImei(imei);
                        identification.setModel(model);
                        identification.setMac(NetWorkUtil.getMacAddress(mContext.getApplicationContext()));
                        identification.setIp(NetWorkUtil.getNetIp(mContext));
                        ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
                                PackageManager.GET_META_DATA);
                        String channel = appInfo.metaData.getString("UMENG_CHANNEL", "");
                        identification.setChannel(channel);
                        BmobQuery<Identification> query = new BmobQuery<Identification>();
                        query.addWhereEqualTo("imei", imei);
                        query.findObjects(mContext, new FindListener<Identification>() {
                            @Override
                            public void onSuccess(List<Identification> list) {
                                if (ListUtils.getSize(list) > 0) {
                                    identification.update(mContext, list.get(0).getObjectId(), new UpdateListener() {
                                        @Override
                                        public void onSuccess() {
                                            PreferencesUtils.putString(mContext, "curdata", curData);
                                        }

                                        @Override
                                        public void onFailure(int i, String s) {
                                        }
                                    });
                                } else {
                                    identification.save(mContext);
                                }
                            }

                            @Override
                            public void onError(int i, String s) {
                                identification.save(mContext, new SaveListener() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFailure(int i, String s) {
                                    }
                                });
                                PreferencesUtils.putString(mContext, "curdata", curData);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
