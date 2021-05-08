package com.example.runnerjourney;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "localDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE journey (" +
                "journeyID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "duration BIGINT NOT NULL," +
                "distance REAL NOT NULL," +
                "date DATETIME NOT NULL," +
                "name varchar(256) NOT NULL DEFAULT 'Journey'," +
                "rating INTEGER NOT NULL DEFAULT 1," +
                "comment varchar(256) NOT NULL DEFAULT 'None'," +
                "image varchar(256) DEFAULT NULL);");

        db.execSQL("CREATE TABLE location (" +
                " locationID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " journeyID INTEGER NOT NULL," +
                " altitude REAL NOT NULL," +
                " longitude REAL NOT NULL," +
                " latitude REAL NOT NULL," +
                " CONSTRAINT fk1 FOREIGN KEY (journeyID) REFERENCES journey (journeyID) ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
