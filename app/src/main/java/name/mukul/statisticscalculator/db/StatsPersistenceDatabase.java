package name.mukul.statisticscalculator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
   A persistence DB implementation for this application.
 */
public class StatsPersistenceDatabase {

    // constants describing column names of the database tables
    public static final String KEY_ID_COLUMN = "_id";
    public static final String DATA_SET_COLUMN = "DATA_SET_VAL";
    public static final String AR_MEAN_COLUMN = "AR_MEAN_VAL";  // arithmetic mean
    public static final String GM_MEAN_COLUMN = "GM_MEAN_VAL";  // geometric mean
    public static final String HM_MEAN_COLUMN = "HM_MEAN_VAL";  // harmonic mean
    public static final String MEDIAN_COLUMN = "MEDIAN_VAL";
    public static final String MODE_COLUMN = "MODE_VAL";
    //public static final String COS_COLUMN = "COS_VAL";  // coefficient of skewness
    public static final String AVG_DEVIATION_COLUMN = "AVG_DEVIATION_VAL";
    public static final String STD_DEVIATION_COLUMN = "STD_DEVIATION_VAL";
    // columns for coefficient of correlation
    public static final String DATA_SET_COLUMN1 = "DATA_SET_VAL1";
    public static final String DATA_SET_COLUMN2 = "DATA_SET_VAL2";
    public static final String COEF_OF_CORR_COLUMN = "COEF_OF_CORR_VAL";

    // database open/upgrade helper
    private StatsDBOpenHelper statsDBOpenHelper;

    // class constructor
    public StatsPersistenceDatabase(Context context) {
        statsDBOpenHelper = new StatsDBOpenHelper(context, StatsDBOpenHelper.DATABASE_NAME, null, StatsDBOpenHelper.DATABASE_VERSION);
    }

    // Called when the database access is not needed
    public void closeDatabase() {
        statsDBOpenHelper.close();
    }

    // Return a Cursor object representing the result of query. If a primary key value is passed then this query returns 1 record
    // corresponding to that, otherwise (in case null is passed) all table records are returned.
    // 1) returning all records are necessary, when populating the history list
    // 2) returning 1 record is necessary, when opening a specific history record
    public Cursor getDbRecords(Integer keyVal) {
        // components of the query string
        String[] result_columns = new String[] {KEY_ID_COLUMN, DATA_SET_COLUMN, AR_MEAN_COLUMN, MEDIAN_COLUMN, MODE_COLUMN,
                                                AVG_DEVIATION_COLUMN, STD_DEVIATION_COLUMN, DATA_SET_COLUMN1, DATA_SET_COLUMN2,
                                                COEF_OF_CORR_COLUMN, GM_MEAN_COLUMN, HM_MEAN_COLUMN};
        String where = (keyVal == null) ? null : KEY_ID_COLUMN + "=" + keyVal.intValue();
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = statsDBOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(statsDBOpenHelper.DATABASE_TABLE,
                                 result_columns, where, whereArgs,
                                 groupBy, having, order);

        return cursor;
    }

    // add a new record to the database
    public void addNewRecord(String dataSet, String arVal, String medianVal, String modeVal, String avgDeviation,
                             String stdDeviation, String coefDataSet1, String coefDataSet2, String coc,
                             String gMean, String hMean) {
        // Create a new row of values to insert
        ContentValues newValues = new ContentValues();

        // Assign values for the row
        newValues.put(DATA_SET_COLUMN, dataSet);
        newValues.put(AR_MEAN_COLUMN, arVal);
        newValues.put(MEDIAN_COLUMN, medianVal);
        newValues.put(MODE_COLUMN, modeVal);
        newValues.put(AVG_DEVIATION_COLUMN, avgDeviation);
        newValues.put(STD_DEVIATION_COLUMN, stdDeviation);
        newValues.put(GM_MEAN_COLUMN, gMean);
        newValues.put(HM_MEAN_COLUMN, hMean);
        //newValues.put(COS_COLUMN, cos);
        // in case coc is added, the other column values above will be ''
        newValues.put(DATA_SET_COLUMN1, coefDataSet1);
        newValues.put(DATA_SET_COLUMN2, coefDataSet2);
        newValues.put(COEF_OF_CORR_COLUMN, coc);

        // Insert row into the table
        SQLiteDatabase db = statsDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.insert(statsDBOpenHelper.DATABASE_TABLE, null, newValues);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    /*
       Delete all records from the table. This will be used when doing the "clear history" task.
     */
    public void deleteAllRecords() {
        // Specify a where clause that determines which row(s) to delete. Specify where arguments as necessary.
        String where = null;
        String whereArgs[] = null;

        // Delete the rows that match the where clause
        SQLiteDatabase db = statsDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(StatsDBOpenHelper.DATABASE_TABLE, where, whereArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * An inner-class implementing an SQLite Open Helper.
     */
    private static class StatsDBOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "statsDatastore.db";    // the physical file name of the database
        private static final String DATABASE_TABLE = "StatsSessionData";
        private static final int DATABASE_VERSION = 1;

        // SQL Statement to create a new database
        private static final String DATABASE_CREATE = "create table " +
                DATABASE_TABLE + " (" + KEY_ID_COLUMN +
                " integer primary key autoincrement, " +
                DATA_SET_COLUMN + " text, " +               // this is a comma-separated list
                AR_MEAN_COLUMN + " text, " +
                MEDIAN_COLUMN + " text, " +
                MODE_COLUMN + " text, " +
                AVG_DEVIATION_COLUMN + " text, " +
                STD_DEVIATION_COLUMN + " text, " +
                DATA_SET_COLUMN1 + " text, " +
                DATA_SET_COLUMN2 + " text, " +
                COEF_OF_CORR_COLUMN + " text, " +
                GM_MEAN_COLUMN + " text, " +
                HM_MEAN_COLUMN + " text);";

        // class constructor
        public StatsDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Called when no database exists in disk and the helper class needs to create a new one.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        // Called when there is a database version mismatch, meaning that the version of the database
        // on disk needs to be upgraded to the current version.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade
            Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

            // Upgrade the existing database to conform to the new version.
            // Multiple previous versions can be handled by comparing oldVersion and newVersion values.

            // The simplest case is to drop the old table and create a new one
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
            // Create a new one
            onCreate(db);
        }
    }

}
