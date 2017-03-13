package org.kgmeng.dmlib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.kgmeng.dmlib.model.FileInfo;

import java.util.LinkedList;
import java.util.List;

public class FileInfoAction {

    private static volatile FileInfoAction instance;

    private SQLiteDatabase database;
    private FileInfoHelper helper;

    public static FileInfoAction getInstance(Context context) {
        if (instance == null) {
            synchronized (FileInfoAction.class) {
                if (instance == null) {
                    instance = new FileInfoAction(context);
                }
            }
        }
        return instance;
    }

    private FileInfoAction(Context context) {
        this.helper = new FileInfoHelper(context);
        open(true);
    }

    public void open(boolean rw) {
        if (rw) {
            database = helper.getWritableDatabase();
        } else {
            database = helper.getReadableDatabase();
        }
    }


    public void close() {
        helper.close();
    }


    public boolean addFileInfo(FileInfo item) {
        try {
            if (item == null
                    || item.FILENAME == null
                    || item.DOWNLOADURL == null
                    || item.DOWNLOADURL.trim().isEmpty()) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(FileInfoUnit.COLUMN_FILENAME, item.FILENAME.trim());
            values.put(FileInfoUnit.COLUMN_URL, item.DOWNLOADURL.trim());
            values.put(FileInfoUnit.COLUMN_COMPLETE_SIZE, item.FILESIZE);
            values.put(FileInfoUnit.COLUMN_CUR_SIZE, item.cur_size);
            values.put(FileInfoUnit.COLUMN_PERCENT, item.percent);
            values.put(FileInfoUnit.COLUMN_STATUS, item.curStatus.getValue());
            database.insert(FileInfoUnit.TABLE_DOWNLOADS, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFileInfo(FileInfo item) {
        try {
            if (item == null
                    || item.FILENAME == null
                    || item.DOWNLOADURL == null
                    || item.DOWNLOADURL.trim().isEmpty()) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(FileInfoUnit.COLUMN_FILENAME, item.FILENAME.trim());
            values.put(FileInfoUnit.COLUMN_URL, item.DOWNLOADURL.trim());
            if (item.FILESIZE > 0) {
                values.put(FileInfoUnit.COLUMN_COMPLETE_SIZE, item.FILESIZE);
            }
            if (item.cur_size > 0) {
                values.put(FileInfoUnit.COLUMN_CUR_SIZE, item.cur_size);
            }
            if (item.percent > 0) {
                values.put(FileInfoUnit.COLUMN_PERCENT, item.percent);
            }
            values.put(FileInfoUnit.COLUMN_STATUS, item.curStatus.getValue());
            database.update(FileInfoUnit.TABLE_DOWNLOADS, values, FileInfoUnit.COLUMN_URL + "=?", new String[]{item.DOWNLOADURL});
//        LunaLog.d(item.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkFileInfo(FileInfo item) {
        try {
            if (item == null
                    || item.FILENAME == null
                    || item.DOWNLOADURL == null
                    || item.DOWNLOADURL.trim().isEmpty()) {
                return false;
            }

            Cursor cursor = database.query(
                    FileInfoUnit.TABLE_DOWNLOADS,
                    new String[]{FileInfoUnit.COLUMN_URL},
                    FileInfoUnit.COLUMN_URL + "=?",
                    new String[]{item.DOWNLOADURL.trim()},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                boolean result = false;
                if (cursor.moveToFirst()) {
                    result = true;
                }
                cursor.close();

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkFileInfo(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return false;
            }

            Cursor cursor = database.query(
                    FileInfoUnit.TABLE_DOWNLOADS,
                    new String[]{FileInfoUnit.COLUMN_URL},
                    FileInfoUnit.COLUMN_URL + "=?",
                    new String[]{url.trim()},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                boolean result = false;
                if (cursor.moveToFirst()) {
                    result = true;
                }
                cursor.close();

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFileInfo(FileInfo item) {
        try {
            if (item == null || item.DOWNLOADURL == null || item.DOWNLOADURL.isEmpty()) {
                return false;
            }
            database.execSQL("DELETE FROM " + FileInfoUnit.TABLE_DOWNLOADS + " WHERE " + FileInfoUnit.COLUMN_URL + " = " + "\"" + item.DOWNLOADURL.trim() + "\"");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearFileInfo() {
        try {
            database.execSQL("DELETE FROM " + FileInfoUnit.TABLE_DOWNLOADS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private FileInfo getFileInfo(Cursor cursor) {
        try {
            FileInfo item = new FileInfo();
            item.FILENAME = (cursor.getString(0));
            item.DOWNLOADURL = (cursor.getString(1));
            item.FILESIZE = (cursor.getLong(2));
            item.cur_size = (cursor.getLong(3));
            item.percent = (cursor.getInt(4));
            item.setCurStatus(cursor.getInt(5));

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<FileInfo> listFileinfos() {
        try {
            List<FileInfo> list = new LinkedList<>();

            Cursor cursor = database.query(
                    FileInfoUnit.TABLE_DOWNLOADS,
                    new String[]{
                            FileInfoUnit.COLUMN_FILENAME,
                            FileInfoUnit.COLUMN_URL,
                            FileInfoUnit.COLUMN_COMPLETE_SIZE,
                            FileInfoUnit.COLUMN_CUR_SIZE,
                            FileInfoUnit.COLUMN_PERCENT,
                            FileInfoUnit.COLUMN_STATUS
                    },
                    null,
                    null,
                    null,
                    null,
                    FileInfoUnit.COLUMN_FILENAME
            );

            if (cursor == null) {
                return list;
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(getFileInfo(cursor));
                cursor.moveToNext();
            }
            cursor.close();

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
