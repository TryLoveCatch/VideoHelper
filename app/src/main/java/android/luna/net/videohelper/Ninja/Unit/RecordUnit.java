package android.luna.net.videohelper.Ninja.Unit;

public class RecordUnit {
    public static final String TABLE_BOOKMARKS = "BOOKMARKS";
    public static final String TABLE_HISTORY = "HISTORY";
    public static final String TABLE_WHITELIST = "WHITELIST";
    public static final String TABLE_GRID = "GRID";
    public static final String TABLE_PLAY_RECORD = "PLAYRECORD";

    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_URL = "URL";
    public static final String COLUMN_TIME = "TIME";
    public static final String COLUMN_PLAY_TIME = "PLAY_TIME";
    public static final String COLUMN_DOMAIN = "DOMAIN";
    public static final String COLUMN_FILENAME = "FILENAME";
    public static final String COLUMN_ORDINAL = "ORDINAL";

    public static final String CREATE_HISTORY = "CREATE TABLE "
            + TABLE_HISTORY
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_TIME + " integer"
            + ")";

    public static final String CREATE_BOOKMARKS = "CREATE TABLE "
            + TABLE_BOOKMARKS
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_TIME + " integer"
            + ")";

    public static final String CREATE_PLAY_RECORD = "CREATE TABLE "
            + TABLE_PLAY_RECORD
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_PLAY_TIME + " integer,"
            + " " + COLUMN_TIME + " integer"
            + ")";

    public static final String CREATE_WHITELIST = "CREATE TABLE "
            + TABLE_WHITELIST
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")";

    public static final String CREATE_GRID = "CREATE TABLE "
            + TABLE_GRID
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_FILENAME + " text,"
            + " " + COLUMN_ORDINAL + " integer"
            + ")";

    private static android.luna.net.videohelper.Ninja.Database.Record holder;

    public static android.luna.net.videohelper.Ninja.Database.Record getHolder() {
        return holder;
    }

    public synchronized static void setHolder(android.luna.net.videohelper.Ninja.Database.Record record) {
        holder = record;
    }
}
