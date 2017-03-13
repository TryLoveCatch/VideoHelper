package com.github.curioustechizen.ago;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.widget.ImageView;

import net.luna.common.debug.LunaLog;

import io.vov.vitamio.R;


/**
 * Created by bintou on 16/2/25.
 */
public class BatteryInfoView extends ImageView {

    private IntentFilter mIntentFilter;
    private Context mContext;


    public BatteryInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BatteryInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContext.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mIntentReceiver);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                String statusString = "";
                setImageResource(R.drawable.battery_list);
                getDrawable().setLevel(level);
//                switch (status) {
//                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
//                        statusString = "unknown";
//                        break;
//                    case BatteryManager.BATTERY_STATUS_CHARGING:
//                        statusString = "charging";
//                        setImageResource(R.drawable.battery_list);
//                        getDrawable().setLevel(level);
//                        break;
//                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
//                        statusString = "discharging";
//                        setImageResource(R.drawable.battery_list);
//                        getDrawable().setLevel(level);
//                        break;
//                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//                        statusString = "not charging";
//                        setImageResource(R.drawable.battery_list);
//                        getDrawable().setLevel(level);
//                        break;
//                    case BatteryManager.BATTERY_STATUS_FULL:
//                        statusString = "full";
//
//                        break;
//                }
            }
        }
    };

}
