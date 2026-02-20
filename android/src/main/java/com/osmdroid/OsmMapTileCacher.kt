package com.osmdroid;

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import java.io.File
import java.util.Calendar

class OsmMapTileCacher(private val context: Context) {

    private fun getDatabase(): SQLiteDatabase {
        val dbPath = "${context.filesDir.path}/osmdroid/tiles/cache.db"
        val dbFile = File(dbPath)

        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }

        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)

        db.beginTransaction()
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)")
            db.execSQL("INSERT OR IGNORE INTO android_metadata (locale) VALUES ('en_US')")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS tiles (
                    key INTEGER PRIMARY KEY,
                    provider TEXT,
                    tile BLOB,
                    expires INTEGER,
                    UNIQUE(key, provider)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS expires_index ON tiles (expires)")

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return db
    }

    init {
        getDatabase()
    }

    fun insertTile(z: Int, x: Int, y: Int, provider: String, tileFile: File) {
        val db = getDatabase()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.YEAR, 10)
        }
        val expiresTimestamp = calendar.timeInMillis

        val tileData = tileFile.readBytes()
        val key = getIndex(x.toLong(), y.toLong(), z.toLong())
        
        db.execSQL("INSERT OR REPLACE INTO tiles (key, provider, tile, expires) VALUES (?, ?, ?, ?)", arrayOf(key, provider, tileData, expiresTimestamp))
    }

    fun cacheTilesFromDirectory(directoryPath: String, showProgress: Boolean = false) {
        val sourceDir = File(directoryPath)
        val totalFiles = countFiles(sourceDir)

        if (totalFiles == 0) {
            if (showProgress) {
                Toast.makeText(context, "No tiles found in directory.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (showProgress) {
            Toast.makeText(context, "Caching map tiles in progress", Toast.LENGTH_SHORT).show()
        }

        var processedFiles = 0
        var lastPercentage = 0
        sourceDir.walk().forEach { file ->
            if (file.isFile) {
                val parts = file.relativeTo(File(directoryPath)).invariantSeparatorsPath.split("/")
                if (parts.size == 3) {
                    val z = parts[0].toIntOrNull() ?: return@forEach
                    val x = parts[1].toIntOrNull() ?: return@forEach

                    val fileName = parts[2]
                    val extensionIndex = fileName.lastIndexOf('.')
                    val yValue = if (extensionIndex > 0) {
                        fileName.substring(0, extensionIndex)
                    } else {
                        fileName
                    }
                    val y = yValue.toIntOrNull() ?: return@forEach

                    insertTile(z, x, y, DEFAULT_PROVIDER, file)
                    processedFiles++

                    if (showProgress) {
                        val currentPercentage = (processedFiles * 100 / totalFiles)
                        if (currentPercentage >= lastPercentage + 10 && currentPercentage % 10 == 0) {
                            lastPercentage = currentPercentage
                            showProgressToast(currentPercentage)
                        }
                    }
                }
            }
        }

        if (showProgress) {
            Toast.makeText(context, "Operation completed successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun countFiles(directory: File): Int = directory.walk().count { it.isFile }

    private fun showProgressToast(percentage: Int) {
        val runnable = Runnable {
            Toast.makeText(context, "Map tiles caching progress $percentage%", Toast.LENGTH_SHORT).show()
        }
        if (context is android.app.Activity) {
            context.runOnUiThread(runnable)
        } else {
            runnable.run()
        }
    }

    companion object {
        private const val DEFAULT_PROVIDER = "CustomTiles"

        fun getIndex(pX: Long, pY: Long, pZ: Long): Long {
            return ((pZ shl pZ.toInt()) + pX shl pZ.toInt()) + pY
        }
    }
}
