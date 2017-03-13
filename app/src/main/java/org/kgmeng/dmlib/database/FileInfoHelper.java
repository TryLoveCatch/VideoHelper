package org.kgmeng.dmlib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import net.luna.common.debug.LunaLog;

public class FileInfoHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lunadownloads.db";
    private static final int DATABASE_VERSION = 4;

    public FileInfoHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(FileInfoUnit.CREATE_DOWNLOADS);
    }

    // UPGRADE ATTENTION!!!
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(FileInfoUnit.DROP_TABLE_DOWNLOADS);
        onCreate(database);
    }

    // UPGRADE ATTENTION!!!
    private boolean isTableExist(@NonNull String tableName) {
        return false;
    }
}
