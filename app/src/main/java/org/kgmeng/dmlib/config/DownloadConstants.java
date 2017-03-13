package org.kgmeng.dmlib.config;

import android.os.Environment;

public class DownloadConstants {

    public static final String SDCARD_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    /**
     * MMAssistant project's root folder path
     */
    public static  String FILE_BASE_PATH = SDCARD_BASE_PATH + "/Movies/SpeedDog";


}
