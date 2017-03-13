package org.kgmeng.dmlib.database;

import org.kgmeng.dmlib.model.FileInfo;

/**
 * Created by bintou on 16/3/8.
 */
public class FileInfoUnit {

    public static final String TABLE_DOWNLOADS = "DOWNLOAD";

    public static final String COLUMN_FILENAME = "FILENAME";
    public static final String COLUMN_URL = "DOWNLOADURL";
    public static final String COLUMN_COMPLETE_SIZE = "FILESIEZE";
    public static final String COLUMN_CUR_SIZE = "CURSIZE";
    public static final String COLUMN_PERCENT = "PERCENT";
    public static final String COLUMN_STATUS = "STATUS";

    public static final String CREATE_DOWNLOADS = "CREATE TABLE "
            + TABLE_DOWNLOADS
            + " ("
            + " " + COLUMN_FILENAME + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_COMPLETE_SIZE + " long,"
            + " " + COLUMN_CUR_SIZE + " long,"
            + " " + COLUMN_PERCENT + " integer,"
            + " " + COLUMN_STATUS + " integer"
            + ")";

    public static final String DROP_TABLE_DOWNLOADS =
            "DROP TABLE IF EXISTS " + TABLE_DOWNLOADS;

    private static FileInfo holder;

    public static FileInfo getHolder() {
        return holder;
    }

    public synchronized static void setHolder(FileInfo fileInfo) {
        holder = fileInfo;
    }


}
