package com.osmdroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class OsmMapTileCacher {

    private Context context;

    public OsmMapTileCacher(Context context) {
        this.context = context;
        getDatabase(); 
    }

    private SQLiteDatabase getDatabase() {
        String dbPath = context.getFilesDir().getPath() + "/osmdroid/tiles/cache.db";
        File dbFile = new File(dbPath);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "DB Path: " + dbPath, Toast.LENGTH_LONG).show();
            }
        });

        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)");
            db.execSQL("INSERT OR IGNORE INTO android_metadata (locale) VALUES ('en_US')");
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS tiles (" +
                "key INTEGER PRIMARY KEY," +
                "provider TEXT," +
                "tile BLOB," +
                "expires INTEGER," +
                "UNIQUE(key, provider))"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS expires_index ON tiles (expires)");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return db;
    }

    public void insertTile(int z, int x, int y, String provider, File tileFile) {
        SQLiteDatabase db = getDatabase();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        long expiresTimestamp = calendar.getTimeInMillis();

        byte[] tileData = new byte[(int) tileFile.length()];
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(tileFile);
            fis.read(tileData);
            fis.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        long key = getIndex(x, y, z);

        db.execSQL("INSERT OR REPLACE INTO tiles (key, provider, tile, expires) VALUES (?, ?, ?, ?)",
            new Object[]{key, provider, tileData, expiresTimestamp});
    }

    public void cacheTilesFromDirectory(String directoryPath) {
        File sourceDir = new File(directoryPath);
        if (sourceDir.isDirectory()) {
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String[] parts = file.getName().split("_");
                    if (parts.length == 3) {
                        int z = Integer.parseInt(parts[0]);
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2].replace(".png", ""));
                        insertTile(z, x, y, "OsmMapTileSource", file);
                    }
                }
            }
        }
    }

    public static long getIndex(long x, long y, long z) {
        return (z << z) + (x << z) + y;
    }
}
