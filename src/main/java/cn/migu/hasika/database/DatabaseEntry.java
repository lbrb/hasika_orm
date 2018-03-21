package cn.migu.hasika.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.migu.hasika.database.annotation.DatabaseField;
import cn.migu.hasika.database.annotation.DatabaseTable;
import cn.migu.hasika.database.util.SerializableUtil;

/**
 * do not use id field which is used by {@link DatabaseEntry} for primary key
 * field type {@link DatabaseColumnInfo.ColumnTypeConstant}
 * example please {@link cn.migu.hasika.database.example.ExampleEntry}
 *
 * Author: hasika
 * Time: 2018/3/21
 * Any questions can send email to lbhasika@gmail.com
 */

public class DatabaseEntry {
    private final static String TAG = DatabaseEntry.class.getCanonicalName();

    private String mTableName;
    private HashMap<DatabaseColumnInfo, Field> mColumnInfo2Field;
    private SQLiteDatabase db;
    private long mId;

    public DatabaseEntry(Context context) {

        db = DatabaseManager.getInstance(context).getDb();
        mTableName = getTableName(this.getClass());
        mColumnInfo2Field = getFieldsMap(this.getClass());

        if (!isExistTable(mTableName, db)) {
            createTable();
        }

    }

    public void setId(long id) {
        mId = id;
    }


    private static <T extends DatabaseEntry> HashMap<DatabaseColumnInfo, Field> getFieldsMap(Class<T> clazz) {

        HashMap<DatabaseColumnInfo, Field> tableName2Field = new HashMap<>();

        //field
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field :
                declaredFields) {
            DatabaseField fieldAnnotation = field.getAnnotation(DatabaseField.class);
            if (fieldAnnotation != null) {
                DatabaseColumnInfo columnInfo = new DatabaseColumnInfo(fieldAnnotation);
                tableName2Field.put(columnInfo, field);
            }
        }

        return tableName2Field;
    }

    private void createTable() {
        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE " + mTableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT");

        for (DatabaseColumnInfo columnInfo : mColumnInfo2Field.keySet()) {
            String columnName = columnInfo.getColumnName();
            int columnType = columnInfo.getColumnType();
            String columnTypeStr = getColumnTypeStr(columnType);

            sqlBuilder.append(", ");

            sqlBuilder.append(columnName + " " + columnTypeStr + " ");
        }
        sqlBuilder.append(")");
        sqlBuilder.append(";");

        Log.d(TAG, "onCreate, Sql:" + sqlBuilder);
        db.execSQL(sqlBuilder.toString());
    }

    private String getColumnTypeStr(int columnType) {
        switch (columnType) {

            case DatabaseColumnInfo.ColumnTypeConstant.HASH_MAP:
                return "BLOB";
            case DatabaseColumnInfo.ColumnTypeConstant.LONG:
                return "INTEGER";
            default:
                return "TEXT";
        }
    }


    private static boolean isExistTable(String tableName, SQLiteDatabase db) {
        String table = "sqlite_master";
        String[] columns = {"*"};
        String selection = "type=? and name=?";
        String[] selectionArgs = {"table", tableName};
        Cursor cursor = db.query(table, columns, selection, selectionArgs, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        cursor.close();
        return count != 0;
    }

    /**
     * insert into if not exist in db
     * update if exist in db
     *
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     * the number of rows affected if update
     */
    public long save() {
        if (!isExistRow()) {
            return insert();
        } else {
            return update();
        }
    }

    private long update() {
        String table = mTableName;
        ContentValues values = getValues();
        String whereClause = "id = ?";
        String[] whereArgs = {Long.toString(mId)};
        int updateCount = db.update(table, values, whereClause, whereArgs);

        return updateCount;
    }

    private long insert() {
        String table = mTableName;
        String nullColumnHack = null;
        ContentValues values = getValues();

        long insertIndex = db.insert(table, nullColumnHack, values);

        return insertIndex;
    }

    private ContentValues getValues() {
        ContentValues values = new ContentValues(mColumnInfo2Field.size());
        Field field;
        for (DatabaseColumnInfo columnInfo : mColumnInfo2Field.keySet()) {
            field = mColumnInfo2Field.get(columnInfo);
            field.setAccessible(true);
            try {
                putValue(values, field, columnInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getValues: " + values);

        return values;
    }

    private void putValue(ContentValues values, Field field, DatabaseColumnInfo columnInfo) throws IllegalAccessException {
        int columnType = columnInfo.getColumnType();
        String columnName = columnInfo.getColumnName();
        switch (columnType) {
            case DatabaseColumnInfo.ColumnTypeConstant.HASH_MAP:
                HashMap<Integer, Long> hashMap = (HashMap<Integer, Long>) field.get(this);
                if (hashMap == null) {
                    hashMap = new HashMap<>();
                }
                byte[] bytes = SerializableUtil.serialize(hashMap);
                values.put(columnName, bytes);
                break;
            case DatabaseColumnInfo.ColumnTypeConstant.LONG:
                long longVal = field.getLong(this);
                values.put(columnName, longVal);
                break;
            default:
                String strVal = (String) field.get(this);
                if (strVal == null) {
                    strVal = "";
                }

                values.put(columnName, strVal);
        }
    }

    private boolean isExistRow() {
        if (mId == 0) {
            return false;
        }

        return true;
    }

    public static <T extends DatabaseEntry> List<T> queryAll(Context context, Class<T> clazz) {
        List<T> list = new ArrayList<>();

        SQLiteDatabase db = DatabaseManager.getInstance(context).getDb();
        String tableName = getTableName(clazz);

        if (!isExistTable(tableName, db)) {
            return list;
        }

        String[] columns = {"*"};
        Cursor cursor = db.query(tableName, columns, null, null, null, null, null);

        if (cursor.getCount() == 0){
            return null;
        }

        HashMap<DatabaseColumnInfo, Field> columnInfo2Field = getFieldsMap(clazz);
        HashMap<DatabaseColumnInfo, Integer> columnName2Index = new HashMap<>();

        int idIndex = cursor.getColumnIndex("id");
        columnName2Index.put(new DatabaseColumnInfo("id"), idIndex);

        for (DatabaseColumnInfo columnInfo : columnInfo2Field.keySet()) {
            String columnName = columnInfo.getColumnName();
            int index = cursor.getColumnIndex(columnName);
            columnName2Index.put(columnInfo, index);
        }


        cursor.moveToFirst();

        do {
            try {
                T o = clazz.getConstructor(Context.class).newInstance(context);
                for (DatabaseColumnInfo columnInfo : columnName2Index.keySet()) {
                    String columnName = columnInfo.getColumnName();
                    int columnType = columnInfo.getColumnType();
                    if (columnName.equals("id")) {
                        int index = columnName2Index.get(columnInfo);
                        long val = cursor.getLong(index);
                        o.setId(val);
                    } else {
                        Field field = columnInfo2Field.get(columnInfo);
                        int index = columnName2Index.get(columnInfo);
                        Object val = getObjectVal(cursor, index, columnType);

                        field.setAccessible(true);
                        field.set(o, val);
                    }
                }

                list.add(o);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (cursor.moveToNext());

        cursor.close();

        return list;
    }

    private static Object getObjectVal(Cursor cursor, int index, int columnType) {
        switch (columnType) {

            case DatabaseColumnInfo.ColumnTypeConstant.LONG:
                long longVal = cursor.getLong(index);
                return longVal;

            case DatabaseColumnInfo.ColumnTypeConstant.HASH_MAP:
                byte[] bytes = cursor.getBlob(index);
                HashMap<Integer, Long> hashMap = SerializableUtil.deserialize(bytes);

                return hashMap;
            default:
                String str = cursor.getString(index);

                return str;
        }
    }

    private static <T extends DatabaseEntry> String getTableName(Class<T> clazz) {


        String tableName = null;
        Annotation clazzAnnotation = clazz.getAnnotation(DatabaseTable.class);
        if (clazzAnnotation instanceof DatabaseTable) {
            DatabaseTable miguTable = (DatabaseTable) clazzAnnotation;
            tableName = miguTable.value();
        }

        return tableName;
    }

    public static <T extends DatabaseEntry> List<T> queryAllBySelection(Context context, Class<T> clazz, String selection, String[] selectionArgs) {
        List<T> list = new ArrayList<>();

        SQLiteDatabase db = DatabaseManager.getInstance(context).getDb();
        String tableName = getTableName(clazz);
        String[] columns = {"*"};

        if (!isExistTable(tableName, db)) {
            return list;
        }

        Cursor cursor = db.query(tableName, columns, selection, selectionArgs, null, null, null);

        if (cursor.getCount() == 0){
            return null;
        }

        HashMap<DatabaseColumnInfo, Field> columnInfo2Field = getFieldsMap(clazz);
        HashMap<DatabaseColumnInfo, Integer> columnName2Index = new HashMap<>();

        int idIndex = cursor.getColumnIndex("id");
        columnName2Index.put(new DatabaseColumnInfo("id"), idIndex);

        for (DatabaseColumnInfo columnInfo : columnInfo2Field.keySet()) {
            String columnName = columnInfo.getColumnName();
            int index = cursor.getColumnIndex(columnName);
            columnName2Index.put(columnInfo, index);
        }

        cursor.moveToFirst();

        do {
            try {
                T o = clazz.getConstructor(Context.class).newInstance(context);
                for (DatabaseColumnInfo columnInfo : columnName2Index.keySet()) {
                    String columnName = columnInfo.getColumnName();
                    int columnType = columnInfo.getColumnType();
                    if (columnName.equals("id")) {
                        int index = columnName2Index.get(columnInfo);
                        long val = cursor.getLong(index);
                        o.setId(val);
                    } else {
                        Field field = columnInfo2Field.get(columnInfo);
                        int index = columnName2Index.get(columnInfo);
                        Object val = getObjectVal(cursor, index, columnType);

                        field.setAccessible(true);
                        field.set(o, val);
                    }
                }

                list.add(o);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (cursor.moveToNext());

        cursor.close();

        return list;
    }

    public static <T extends DatabaseEntry> T queryBySelection(Context context, Class<T> clazz, String selection, String[] selectionArgs) {
        List<T> list = queryAllBySelection(context, clazz, selection, selectionArgs);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public long delete() {
        String table = mTableName;
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{Long.toString(mId)};
        long idex = db.delete(table, whereClause, whereArgs);

        return idex;
    }
}
