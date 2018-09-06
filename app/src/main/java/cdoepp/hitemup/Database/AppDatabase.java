package cdoepp.hitemup.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

/**
 * Created by cdoepp on 7/29/18.
 */

@Database(entities = {Person.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PeopleDao peopleDao();


    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE People "
                    + " ADD COLUMN photo TEXT");
        }
    };
    /*
    Get an instance of this with:
    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").build();
     */
}