package android.luna.net.videohelper.Ninja.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.luna.net.videohelper.Ninja.Unit.RecordUnit;
import android.support.annotation.NonNull;

import org.kgmeng.dmlib.database.FileInfoUnit;

public class RecordHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Ninja3.db";
    private static final int DATABASE_VERSION = 3;

    public RecordHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(android.luna.net.videohelper.Ninja.Unit.RecordUnit.CREATE_BOOKMARKS);
        database.execSQL(android.luna.net.videohelper.Ninja.Unit.RecordUnit.CREATE_HISTORY);
        database.execSQL(android.luna.net.videohelper.Ninja.Unit.RecordUnit.CREATE_WHITELIST);
        database.execSQL(android.luna.net.videohelper.Ninja.Unit.RecordUnit.CREATE_GRID);
        database.execSQL(RecordUnit.CREATE_PLAY_RECORD);
    }

    // UPGRADE ATTENTION!!!
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + RecordUnit.TABLE_BOOKMARKS);
        database.execSQL("DROP TABLE IF EXISTS " + RecordUnit.TABLE_HISTORY);
        database.execSQL("DROP TABLE IF EXISTS " + RecordUnit.TABLE_WHITELIST);
        database.execSQL("DROP TABLE IF EXISTS " + RecordUnit.TABLE_GRID);
        database.execSQL("DROP TABLE IF EXISTS " + RecordUnit.TABLE_PLAY_RECORD);
        onCreate(database);
    }

    // UPGRADE ATTENTION!!!
    private boolean isTableExist(@NonNull String tableName) {
        return false;
    }
}
