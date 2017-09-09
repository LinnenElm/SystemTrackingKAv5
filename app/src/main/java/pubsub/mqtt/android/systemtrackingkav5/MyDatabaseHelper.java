package pubsub.mqtt.android.systemtrackingkav5;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LocationRecords";

    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String TABLE_CREATE00 = "create table Unit00" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE01 = "create table Unit01" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE02 = "create table Unit02" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE03 = "create table Unit03" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE04 = "create table Unit04" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE05 = "create table Unit05" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE06 = "create table Unit06" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE07 = "create table Unit07" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE08 = "create table Unit08" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE09 = "create table Unit09" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";

    private static final String TABLE_CREATE10 = "create table Unit10" +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "SENT LONG NOT NULL," +
            "RECEIVED LONG NOT NULL," +
            "LONGITUDE DOUBLE NOT NULL," +
            "LATITUDE DOUBLE NOT NULL)";


    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void createRecords(String UNIT_TABLE, Double longitude, Double latitude,long sentTime, long recvTime){
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("LONGITUDE", longitude);
        values.put("LATITUDE", latitude);
        values.put("SENT", recvTime);
        values.put("RECEIVED", sentTime);
        database.insert(UNIT_TABLE, null, values);
        Log.e(GLocation.TAG, "Write phase completed. LONGITUDE: "+longitude+" LATITUDE: "+latitude+" SENT: "+recvTime+" RECEIVED: "+sentTime);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(MyDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS LocationRecords");
        onCreate(database);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE00);
        database.execSQL(TABLE_CREATE01);
        database.execSQL(TABLE_CREATE02);
        database.execSQL(TABLE_CREATE03);
        database.execSQL(TABLE_CREATE04);
        database.execSQL(TABLE_CREATE05);
        database.execSQL(TABLE_CREATE06);
        database.execSQL(TABLE_CREATE07);
        database.execSQL(TABLE_CREATE08);
        database.execSQL(TABLE_CREATE09);
        database.execSQL(TABLE_CREATE10);
    }
}
