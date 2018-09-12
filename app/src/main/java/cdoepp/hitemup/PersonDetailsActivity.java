package cdoepp.hitemup;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
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
import android.widget.NumberPicker;
import android.widget.QuickContactBadge;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

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

    private String name;
    private String phoneNumber;
    private Person person;

    private TextView userLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);


        /*
        Cursor c = this.getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                new String[] {String.valueOf(person.getId())}, null);
        c.moveToFirst();

        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
        Log.d(TAG, "name = " + name);
        String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
        Log.d(TAG, "phoneNum = " + phoneNumber);
        String photo = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));
        Log.d(TAG, "photo = " + photo);
        int level = -1;
        person = new Person(id, name, phoneNumber, photo, level);
        people.add(person);
        peopleList.add(person);
    } else {
        String name = cursor.getString(cursor.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
        String name = contactsCursor.getString(contactsCursor.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
        Log.d(TAG, "id = " + id + ", na
         */



        Intent intent = getIntent();
        person = (Person) intent.getSerializableExtra(MainActivity.PERSON);
        if (person == null) finish();
        String phoneNumber = person.getPhoneNumber();
        String name = person.getName();
        String photoString = person.getPhoto();

        contactBadge = findViewById(R.id.contact_badge);
        contactBadge.assignContactFromPhone(phoneNumber, true);
        contactBadge.setImageURI(null);
        if (photoString != null) {
            Uri photoUri = Uri.parse(photoString);
            contactBadge.setImageURI(photoUri);
        } else {
            contactBadge.setImageDrawable(getDrawable(R.drawable.ic_person_black_24dp));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        TextView tvName = findViewById(R.id.name);
        tvName.setText(name);
        TextView tvPhoneNumber = findViewById(R.id.phone_number);
        tvPhoneNumber.setText(phoneNumber);
        TextView tvDetails1 = findViewById(R.id.details_text1);
        tvDetails1.setText("Id = " + intent.getLongExtra(MainActivity.CONTACT_ID, 0));

        userLevel = findViewById(R.id.level);
        userLevel.setText("Priority level = " + person.getLevel());

        ImageButton editStatus = findViewById(R.id.edit_status);
        editStatus.setOnClickListener(this);


        // Initializes the loader framework
        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == findViewById(R.id.edit_status).getId()) Log.d(TAG, "EDIT STATUS");
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
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                Log.d(TAG, "onValueChange: ");
            }
        });
        d.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: " + numberPicker.getValue());
                person.setLevel(numberPicker.getValue());
                userLevel.setText("Priority level = " + numberPicker.getValue());
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
