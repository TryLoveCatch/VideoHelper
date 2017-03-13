package android.luna.net.videohelper.widget;

import android.app.Activity;
import android.content.Context;
import android.luna.net.videohelptools.R;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import net.luna.common.util.PreferencesUtils;
import net.luna.common.view.button.ButtonFlat;
import net.luna.common.view.widget.CustomerDialog;


/**
 * Created by bintou on 15/11/20.
 */
public class DisclaimerDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Context mContext;
    CustomerDialog disclaimerDialog;
    CheckBox checkbox;
    ButtonFlat btn;

    private boolean canDismiss = true;

    public DisclaimerDialog(final Context mContext) {
        this.mContext = mContext;
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_discalimer, null);
        disclaimerDialog = new CustomerDialog(mContext, view);
        btn = (ButtonFlat) view.findViewById(R.id.accept_btn);
        checkbox = (CheckBox) view.findViewById(R.id.checked);
        checkbox.setOnCheckedChangeListener(this);
        btn.setOnClickListener(this);
        disclaimerDialog.setCancelable(false);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canDismiss = isChecked;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDismiss) {
                    PreferencesUtils.putBoolean(mContext, "hasReadDisclaimer", true);
                    dismiss();
                }
            }
        });
    }

    public void show() {
        if (disclaimerDialog != null && !disclaimerDialog.isShowing())
            disclaimerDialog.show();
    }

    public void dismiss() {
        if (disclaimerDialog != null && disclaimerDialog.isShowing()) {
            disclaimerDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}
