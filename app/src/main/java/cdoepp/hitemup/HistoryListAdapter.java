package cdoepp.hitemup;

import android.content.Context;
import android.net.Uri;
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
import java.util.List;

import cdoepp.hitemup.Database.Person;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cdoepp on 8/22/18.
 */

public class HistoryListAdapter extends ArrayAdapter<Message> {

    private static final String TAG = "PeopleListAdapter";
    private final List<Message> messages;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public HistoryListAdapter(Context context, int resourceId, List<Message> items) {
        super(context, resourceId, items);

        messages = new ArrayList<Message>();
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // get last 3 messages:
        for (int i = 0; i < 3; i++) {
            if (items != null && i < items.size())
                messages.add(items.get(i));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.message = convertView.findViewById(R.id.message);
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            holder.icon = convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        Message message = getItem(position);
        if (message != null) {
            if (message.getType() == Message.TYPE_RECEIVED) {
                holder.message.setText(message.getText());
            } else {
                holder.message.setText("You: " + message.getText());
            }
            holder.timestamp.setText(message.getDate().toString());
        }

        return convertView;
    }




    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    public class ViewHolder {
        ImageView icon;
        TextView message;
        TextView timestamp;
    }
}