package cdoepp.hitemup.Database;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cdoepp.hitemup.Message;

/**
 * Created by cdoepp on 11/8/18.
 */

public class Converters {
    @TypeConverter
    public static ArrayList<Message> fromString(String value) {
        Log.d("CONVERTERS", "value = " + value);
        Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Message> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
