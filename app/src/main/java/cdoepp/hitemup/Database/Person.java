package cdoepp.hitemup.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by cdoepp on 7/29/18.
 */

@Entity(tableName = "People")
public class Person implements Serializable {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "level")
    private int level;

    @ColumnInfo(name = "photo")
    private String photo;

    public Person(long id, String name, String phoneNumber, String photo, int level) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.level = level;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) { this.id = id; }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
