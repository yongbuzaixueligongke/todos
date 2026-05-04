package com.example.imageview;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TodoItem.class, Project.class, User.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TodoDao todoDao();

    public abstract ProjectDao projectDao();

    public abstract UserDao userDao();

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `passwordHash` TEXT, `passwordSalt` TEXT, `nickname` TEXT, `avatarUri` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)");
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
                            .addMigrations(MIGRATION_8_9)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
