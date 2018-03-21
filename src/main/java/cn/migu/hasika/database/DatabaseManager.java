package cn.migu.hasika.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * user can not use this class, please user custom-class extends {@link DatabaseEntry}
 * Author: hasika
 * Time: 2018/3/21
 * Any questions can send email to lbhasika@gmail.com
 */

public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseHelper dbHelper;

    public static synchronized DatabaseManager getInstance(Context context){
        if (instance == null){
            String dbPath = "migu_download_sqlite.db";
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){

                String sdDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator;
                File dir = new File(sdDirPath);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                dbPath = sdDirPath+File.separator+"cmgame"+ File.separator+"plugin"+File.separator+dbPath;
            }

            instance = new DatabaseManager(context, dbPath);

        }

        return instance;
    }

    public SQLiteDatabase getDb(){
        return dbHelper.getWritableDatabase();
    }

    private DatabaseManager(Context context, String dbPath){
        dbHelper = new DatabaseHelper(context, dbPath);
    }
}
