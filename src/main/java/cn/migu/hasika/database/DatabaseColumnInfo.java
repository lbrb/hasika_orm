package cn.migu.hasika.database;

import cn.migu.hasika.database.annotation.DatabaseField;

/**
 * Created by hasika on 2018/3/15.
 */

public final class DatabaseColumnInfo {
    private String mColumnName;
    private int mColumnType;

    public DatabaseColumnInfo(String columnName){
        mColumnName = columnName;
        mColumnType = ColumnTypeConstant.TEXT;
    }

    public DatabaseColumnInfo(String columnName, int columnType){
        mColumnName = columnName;
        mColumnType = columnType;
    }

    public DatabaseColumnInfo(DatabaseField fieldAnnotation){
        mColumnName = fieldAnnotation.value();
        mColumnType = fieldAnnotation.type();
    }

    public String getColumnName(){
        return mColumnName;
    }

    public int getColumnType(){
        return mColumnType;
    }

    public final class ColumnTypeConstant{
        public static final int TEXT = 0;
        public static final int LONG = 1;
        public static final int HASH_MAP = 2;
    }
}
