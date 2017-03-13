package android.luna.net.videohelper.bean.user;

import android.content.Context;
import android.text.TextUtils;


import net.luna.common.debug.LunaLog;
import net.luna.common.util.ToastUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ResetPasswordByCodeListener;
import cn.bmob.v3.listener.ResetPasswordByEmailListener;
import cn.bmob.v3.listener.UpdateListener;

public class UserOperation {

    public static User getCurrentUser(Context context) {
        User user = BmobUser.getCurrentUser(context, User.class);
        if (user != null) {
            LunaLog.d("本地用户信息:objectId = " + user.getObjectId() + ",name = " + user.getUsername() + ",age = "
                    + user.getAge());
        } else {
            LunaLog.d("本地用户为null,请登录。");
        }
        return user;
    }

    public static void logOut(Context context) {
        User.logOut(context);
    }

    /**
     * 更新用户
     */
    public void updateUser(final Context context, User user) {
        final User bmobUser = BmobUser.getCurrentUser(context, User.class);
        if (bmobUser != null) {
            LunaLog.d("getObjectId = " + bmobUser.getObjectId());
            LunaLog.d("getUsername = " + bmobUser.getUsername());
            LunaLog.d("getEmail = " + bmobUser.getEmail());
            LunaLog.d("getCreatedAt = " + bmobUser.getCreatedAt());
            LunaLog.d("getUpdatedAt = " + bmobUser.getUpdatedAt());
            User newUser = user;
            newUser.update(context, bmobUser.getObjectId(), new UpdateListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int code, String msg) {
                    ToastUtils.show(context, "更新用户信息失败:" + msg);
                }
            });
        } else {
            ToastUtils.show(context, "请先登录");
        }
    }

//    public static void updateHeadPhoto(final Context context, String filePath) {
//        BmobProFile.getInstance(context).submitThumnailTask(filePath, 1, new ThumbnailListener() {
//
//            @Override
//            public void onError(int errorCode, String msg) {
//                LunaLog.e("上传头像失败，错误码：" + errorCode + "  " + msg);
//            }
//
//            @Override
//            public void onSuccess(String thumbnailName, String thumbnailUrl) {
//                User user = BmobUser.getCurrentUser(context, User.class);
//                user.setHeadPhoto(thumbnailUrl);
//                user.update(context);
//            }
//        });
//    }

    /**
     * 验证旧密码是否正确
     *
     * @param
     * @return void
     * @throws
     * @Title: updatePassword
     * @Description: TODO
     */
    public static void checkPassword(final Context context, String password) {
        BmobQuery<User> query = new BmobQuery<User>();
        final User bmobUser = BmobUser.getCurrentUser(context, User.class);
        // 如果你传的密码是正确的，那么arg0.size()的大小是1，这个就代表你输入的旧密码是正确的，否则是失败的
        query.addWhereEqualTo("password", "password");
        query.addWhereEqualTo("username", bmobUser.getUsername());
        query.findObjects(context, new FindListener<User>() {

            @Override
            public void onError(int arg0, String arg1) {

            }

            @Override
            public void onSuccess(List<User> arg0) {
                ToastUtils.show(context, "查询密码成功:" + arg0.size());
            }
        });
    }

    /**
     * 重置密码
     */
    public static void ResetPasswrod(final Context context, final String email) {
        BmobUser.resetPasswordByEmail(context, email, new ResetPasswordByEmailListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                ToastUtils.show(context, "重置密码请求成功，请到" + email + "邮箱进行密码重置操作");
            }

            @Override
            public void onFailure(int code, String e) {
                // TODO Auto-generated method stub
                ToastUtils.show(context, "重置密码失败:" + e);
            }
        });
    }

    /**
     * 查询用户
     */
    public static void FindUser(Context context, String userName, FindListener<User> listener) {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", "lucky");
        query.findObjects(context, listener);
    }

    /**
     * 通过短信验证码来重置用户密码
     *
     * @return void 注：整体流程是先调用请求验证码的接口获取短信验证码，随后调用短信验证码重置密码接口来重置该手机号对应的用户的密码
     * @method requestSmsCode
     */
    public static void resetPasswordBySMS(final Context context, String smsCode, String newPassword) {
        if (!TextUtils.isEmpty(smsCode)) {
            // 2、重置的是绑定了该手机号的账户的密码
            BmobUser.resetPasswordBySMSCode(context, smsCode, newPassword, new ResetPasswordByCodeListener() {

                @Override
                public void done(BmobException e) {
                    // TODO Auto-generated method stub
                    if (e == null) {
                        ToastUtils.show(context, "密码重置成功");
                    } else {
                        ToastUtils.show(context, "错误码：" + e.getErrorCode() + ",错误原因：" + e.getLocalizedMessage());
                    }
                }
            });
        } else {
            ToastUtils.show(context, "请填写验证码");
        }
    }

}
