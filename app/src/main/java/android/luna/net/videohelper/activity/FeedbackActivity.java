package android.luna.net.videohelper.activity;

import android.luna.net.videohelper.bean.Feedback;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.luna.common.util.StringUtils;
import net.luna.common.util.ToastUtils;
import net.luna.common.view.progress.ProgressBarCircularIndeterminate;

import cn.bmob.v3.listener.SaveListener;


/**
 * Created by Alian on 15-7-29.
 */
public class FeedbackActivity extends BaseActivity {
    public static final String FRAGMENT_FEEDBACK_TAG = "FragmentFeedback";
    EditText editText;
    Button button;
    ProgressBarCircularIndeterminate progressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFeedback();
    }

    private void initFeedback() {
//        editText = (EditText) findViewById(R.id.menu_feedback_input);
//        button = (Button) findViewById(R.id.menu_feedback_send);
//        progressbar = (ProgressBarCircularIndeterminate) findViewById(R.id.menu_feedback_progressbar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feebbackText = editText.getText().toString();
                if (StringUtils.isBlank(feebbackText)) {
                    ToastUtils.show(mContext, "请先填写内容");
                    return;
                }
                Feedback feedback = new Feedback();
                feedback.setArticle(feebbackText);
                progressbar.setVisibility(View.VISIBLE);
                feedback.save(mContext, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        progressbar.setVisibility(View.GONE);
                        ToastUtils.show(mContext, "反馈提交成功");
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtils.show(mContext, "反馈提交失败：" + s);
                        progressbar.setVisibility(View.GONE);
                    }
                });

            }
        });
    }

}
