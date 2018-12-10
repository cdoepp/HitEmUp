package cdoepp.hitemup;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cdoepp.hitemup.Database.Person;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cdoepp on 8/22/18.
 */

public class PeopleListAdapter extends ArrayAdapter<Person> {

    private static final String TAG = "PeopleListAdapter";
    private final List<Person> mItems;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public PeopleListAdapter(Context context, int resourceId, List<Person> items) {
        super(context, resourceId, items);

        mContext = context;
        mItems = items;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_person, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.photo = convertView.findViewById(R.id.photo);
            holder.level = convertView.findViewById(R.id.level);
            holder.details = convertView.findViewById(R.id.details);
            holder.alertIcon = convertView.findViewById(R.id.alert_icon);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        Person person = getItem(position);
        if (person != null) {
            // This is where you set up the views.
            // This is just an example of what you could do.
            holder.name.setText(person.getName());
            String photoString = person.getPhoto();
            if (photoString != null) {
                Uri photoUri = Uri.parse(photoString);
                holder.photo.setImageURI(photoUri);
            } else {
                holder.photo.setImageDrawable(getContext().getDrawable(R.drawable.ic_person_black_24dp));
            }
            holder.level.setText("" + person.getLevel());

            List<Message> messages = person.getMessages();
            if (messages != null && messages.size() > 0) {
                Message lastMessage = person.getMessages().get(0);
                long currentTimestamp = new Date().getTime();
                long days = TimeUnit.MILLISECONDS.toDays(currentTimestamp - lastMessage.getTimestamp());
                holder.details.setText(getLastContactTime(lastMessage.getTimestamp()));

                if (person.getLevel() > 0 && days > MainActivity.CONTACT_PRIORITY_MAP.get(person.getLevel())) {
                    holder.details.setTextColor(ContextCompat.getColor(mContext, R.color.red_alert));
                    holder.alertIcon.setVisibility(View.VISIBLE);

                } else {
                    holder.details.setTextColor(ContextCompat.getColor(mContext, R.color.default_text));
                    holder.alertIcon.setVisibility(View.GONE);
                }

            } else if (messages != null && messages.size() == 0) {
                holder.details.setText("No contact records");
                holder.details.setTextColor(ContextCompat.getColor(mContext, R.color.default_text));
                holder.alertIcon.setVisibility(View.GONE);
            }

        }

        return convertView;
    }

    private String getLastContactTime(long lastContact) {
        long currentTimestamp = new Date().getTime();
        long time = TimeUnit.MILLISECONDS.toDays(currentTimestamp - lastContact);
        String units = "day";
        if (time == 0) {
            time = TimeUnit.MILLISECONDS.toHours(currentTimestamp - lastContact);
            units = "hour";
        }
        if (time == 0) {
            time = TimeUnit.MILLISECONDS.toMinutes(currentTimestamp - lastContact);
            units = "minute";
        }
        if (time == 0) {
            time = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - lastContact);
            units = "second";
        }

        if (time > 1)
            units = units + "s";

        return "Last contacted " + time + " " + units + " ago";
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Person getItem(int position) {
        return mItems.get(position);
    }

    public class ViewHolder {
        TextView name;
        CircleImageView photo;
        TextView level;
        TextView details;
        ImageView alertIcon;
    }
}