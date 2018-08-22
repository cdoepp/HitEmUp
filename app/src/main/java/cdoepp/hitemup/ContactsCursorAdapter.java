package cdoepp.hitemup;

import android.arch.persistence.room.Room;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import cdoepp.hitemup.Database.AppDatabase;
import cdoepp.hitemup.Database.Person;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cdoepp on 7/17/18.
 */

public class ContactsCursorAdapter extends CursorAdapter {
    private static final String TAG = "ContactsCursorAdapter";
    private Context context;

    private HashMap<Long, Person> peopleMap;

    public ContactsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_person, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(TAG, "bindView");
        // Find fields to populate in inflated template
        TextView tvName = view.findViewById(R.id.name);
        CircleImageView ivPhoto = view.findViewById(R.id.photo);
        TextView tvLevel = view.findViewById(R.id.level);

        Long id = cursor.getLong(0);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME_PRIMARY));
        String photoString = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));
        int level = -1;

        ivPhoto.setImageURI(null);
        if (photoString != null) {
            Uri photoUri = Uri.parse(photoString);
            ivPhoto.setImageURI(photoUri);
        } else {
            ivPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_person_black_24dp));
        }
        if (peopleMap != null) {
            Person person = peopleMap.get(id);
            if (person != null) {
                level = person.getLevel();
                tvLevel.setText("" + level);
            }
        }
        if (level == -1) {
            tvLevel.setText("?");
        }

        //Log.d(TAG, "photo uri = " + photoUri);

        // Populate fields with extracted properties
        tvName.setText(name);
    }


    public Cursor swapCursor(Cursor newCursor, HashMap<Long, Person> peopleMap) {
        this.peopleMap = peopleMap;
        return super.swapCursor(newCursor);
    }

}