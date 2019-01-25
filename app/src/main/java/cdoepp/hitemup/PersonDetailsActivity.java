package cdoepp.hitemup;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.QuickContactBadge;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cdoepp.hitemup.Database.AppDatabase;
import cdoepp.hitemup.Database.Person;


public class PersonDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String TAG = "PersonDetailsActivity";
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private long contactId;
    private QuickContactBadge contactBadge;

    private Person person;

    private TextView tvLevel;
    private TextView tvName;
    private ImageButton ibEditStatus;
    private TextView tvDetails1;
    private TextView tvPhoneNumber;
    private ListView historyListView;
    private HistoryListAdapter historyListAdapter;
    private LinearLayout history;
    private TextView emptyHistory;
    private List<Message> messagesHistoryList;
    private static String phoneNumber;

    private Comparator<Message> SORT_BY_DATE = new Comparator<Message>() {
        public int compare(Message m1, Message m2) {
            return Long.compare(m2.getTimestamp(), m1.getTimestamp());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        // Initializes the loader framework
        getSupportLoaderManager().initLoader(0, null, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        person = (Person) intent.getSerializableExtra(MainActivity.PERSON);
        if (person == null) finish();

        if (person.getMessages() != null)

        contactBadge = findViewById(R.id.contact_badge);
        tvName = findViewById(R.id.name);
        tvPhoneNumber = findViewById(R.id.phone_number);
        tvLevel = findViewById(R.id.level);
        ibEditStatus = findViewById(R.id.edit_status);
        ibEditStatus.setOnClickListener(this);

        tvDetails1 = findViewById(R.id.details_text1);
        tvDetails1.setText("Id = " + intent.getLongExtra(MainActivity.CONTACT_ID, 0));

        history = findViewById(R.id.history_list);
        emptyHistory = findViewById(R.id.empty_history);

        messagesHistoryList = new ArrayList<Message>();
        phoneNumber = person.getPhoneNumber();

        if (person.getMessages() != null)
            insertRecentMessages(person.getMessages());

    }

    public void insertRecentMessages(List<Message> messages) {
        TextView tvMessage;
        TextView tvTimestamp;
        ImageView iconSent;
        ImageView iconReceived;

        Log.d(TAG, "THIS PERSON's MESSAGES = " + person.getMessages().toString());

        if (messages.size() != 0)
            emptyHistory.setVisibility(View.GONE);

        //Sorting the list of messages:
        Collections.sort(messages, SORT_BY_DATE);

        int i = 0;
        while (i < 3 && i < messages.size()) {
            Message message = messages.get(i);
            View messageView = getLayoutInflater().inflate(R.layout.item_history, null);
            messageView.setTag(message.getId());
            messageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "MESSAGE VIEW CLICKED, id = " + v.getTag());
                    Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", (String) v.getTag());
                    startActivity(smsIntent);
                }
            });

            tvMessage = messageView.findViewById(R.id.message);
            tvTimestamp = messageView.findViewById(R.id.timestamp);
            iconSent = messageView.findViewById(R.id.icon_sent);
            iconReceived = messageView.findViewById(R.id.icon_received);

            if (message.getType() == Message.TYPE_RECEIVED) {
                iconSent.setVisibility(View.INVISIBLE);
                iconReceived.setVisibility(View.VISIBLE);
                tvMessage.setText(message.getText());
            } else {
                tvMessage.setText("You: " + message.getText());
            }

            tvTimestamp.setText(message.getDate().toString());
            history.addView(messageView);

            if (i < messages.size() - 1 && i < 2) {
                View dividerView = getLayoutInflater().inflate(R.layout.item_list_divider, null);
                history.addView(dividerView);
            }
            messagesHistoryList.add(message);
            i++;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check this user with the phone's contact info again in case changes were made using
        // the contact badge

        Cursor c = this.getContentResolver().query(Data.CONTENT_URI,
        new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
        Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
        new String[] {String.valueOf(person.getId())}, null);
        c.moveToFirst();

        person.setName(c.getString(c.getColumnIndex(Data.DISPLAY_NAME_PRIMARY)));
        person.setPhoneNumber(c.getString(c.getColumnIndex(Phone.NUMBER)));
        person.setPhoto(c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI)));

        getSupportActionBar().setTitle(person.getName());

        Log.d(TAG, "phone number = " + person.getPhoneNumber());
        contactBadge = findViewById(R.id.contact_badge);
        contactBadge.assignContactFromPhone(person.getPhoneNumber(), true);
        contactBadge.setImageURI(null);
        if (person.getPhoto() != null) {
        Uri photoUri = Uri.parse(person.getPhoto());
        contactBadge.setImageURI(photoUri);
        } else {
        contactBadge.setImageDrawable(getDrawable(R.drawable.ic_person_black_24dp));
        }

        tvName.setText(person.getName());
        tvPhoneNumber.setText(person.getPhoneNumber());
        tvLevel.setText("Priority level = " + person.getLevel());

        }

    @Override
    public void onClick(View v) {
        if (v.getId() == findViewById(R.id.edit_status).getId())
            Log.d(TAG, "EDIT STATUS");
        // Show numberpicker dialog:
        final  AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        //d.setTitle("Title");
        d.setMessage("Contact priority level");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.number_picker);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(0);
        numberPicker.setValue(person.getLevel());
        d.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: " + numberPicker.getValue());
                person.setLevel(numberPicker.getValue());
                tvLevel.setText("Priority level = " + numberPicker.getValue());
                new updateContactTask().execute(person);
            }
        });
        d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void onCallButtonClicked(View v) {
        Log.d(TAG, "CALL BUTTON CLICKED");
    }

    public void onMessageButtonClicked(View v) {
        Log.d(TAG, "TEXT BUTTON CLICKED");
        //Intent androidSix = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null));
        //startActivity(androidSix);
        //Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("mms:" + "15404461277;17032312766"));
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("mms:" + person.getPhoneNumber()));
        startActivity(smsIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Log.d(TAG, "onSupportNavigateUp");
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(MainActivity.PERSON, person);
        setResult(MainActivity.RESULT_OK, returnIntent);
        finish();
    }

    public class addContactTask extends AsyncTask<Person, Void, Void> {
        @Override
        protected Void doInBackground(Person... people) {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "database-name")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();
            db.peopleDao().insertAll(people);
            db.close();
            Log.d(TAG, "inserting person = " + people[0].getName());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "inserted contact to room database");
        }
    }

    public class updateContactTask extends AsyncTask<Person, Void, Void> {
        @Override
        protected Void doInBackground(Person... person) {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "database-name")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();
            db.peopleDao().update(person[0]);
            db.close();
            Log.d(TAG, "updating person = " + person[0].getName());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "updated contact");
        }
    }
}
