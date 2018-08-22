package cdoepp.hitemup.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by cdoepp on 7/29/18.
 */

@Database(entities = {Person.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PeopleDao peopleDao();


    /*
    Get an instance of this with:
    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").build();
     */
}