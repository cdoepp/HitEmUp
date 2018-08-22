package cdoepp.hitemup.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by cdoepp on 7/29/18.
 */

@Dao
public interface PersonDao {
    @Query("SELECT * FROM Person")
    List<Person> getAll();

    @Query("SELECT * FROM Person WHERE id LIKE :id")
    Person getById(long id);

    @Query("SELECT * FROM Person WHERE name LIKE :name")
    Person findByName(String name);

    @Query("SELECT COUNT(*) from Person")
    int countPeople();

    @Insert
    void insertAll(Person person);

    @Delete
    void delete(Person person);

}