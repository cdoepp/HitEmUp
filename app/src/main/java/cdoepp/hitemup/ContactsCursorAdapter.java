package cdoepp.hitemup;

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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cdoepp on 7/17/18.
 */

public class ContactsCursorAdapter extends CursorAdapter {
    private static final String TAG = "ContactsCursorAdapter";
    private Context context;

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

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME_PRIMARY));
        String photoString = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));

        ivPhoto.setImageURI(null);
        if (photoString != null) {
            Uri photoUri = Uri.parse(photoString);
            ivPhoto.setImageURI(photoUri);
        } else {
            ivPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_person_black_24dp));
        }

        //Log.d(TAG, "photo uri = " + photoUri);

        // Populate fields with extracted properties
        tvName.setText(name);
    }

    private InputStream openPhoto(Uri photoUri, Context context) {
        //Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI);
        //Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

}