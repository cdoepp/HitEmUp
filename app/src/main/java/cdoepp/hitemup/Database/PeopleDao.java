package cdoepp.hitemup.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.HashMap;
import java.util.List;

/**
 * Created by cdoepp on 7/29/18.
 */

@Dao
public interface PeopleDao {
    @Query("SELECT * FROM People")
    List<Person> getAll();

    @Query("SELECT * FROM People WHERE id LIKE :id")
    Person getById(long id);

    @Query("SELECT * FROM People WHERE name LIKE :name")
    Person findByName(String name);

    @Query("SELECT COUNT(*) from People")
    int countPeople();

    @Insert
    void insertAll(Person... people);

    @Delete
    void delete(Person person);

}