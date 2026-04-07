package com.example.imageview;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TodoItem.class, Project.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TodoDao todoDao();
    public abstract ProjectDao projectDao();

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE todo_items ADD COLUMN uuid TEXT");
            database.execSQL("ALTER TABLE todo_items ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE todo_items ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE todo_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE todo_items ADD COLUMN syncedAt INTEGER NOT NULL DEFAULT 0");
            
            database.execSQL("ALTER TABLE projects ADD COLUMN uuid TEXT");
            database.execSQL("ALTER TABLE projects ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE projects ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE projects ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE projects ADD COLUMN syncedAt INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "todo_database"
                    )
                    .addMigrations(MIGRATION_5_6)
                    .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
