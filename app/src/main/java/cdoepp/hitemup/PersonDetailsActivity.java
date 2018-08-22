package cdoepp.hitemup;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.QuickContactBadge;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;


public class PersonDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "PersonDetailsActivity";
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private Uri contactUri;
    private String lookupKey;
    private long contactId;
    private QuickContactBadge contactBadge;

    private String name;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        Intent intent = getIntent();
        contactUri = (Uri) intent.getParcelableExtra(MainActivity.CONTACT_URI);
        lookupKey = intent.getStringExtra(MainActivity.LOOKUP_KEY);
        String phoneNumber = intent.getStringExtra(MainActivity.PHONE_NUMBER);
        String name = intent.getStringExtra(MainActivity.NAME);
        String photoString = intent.getStringExtra(MainActivity.PHOTO_URI);

        Log.d(TAG, "uri = " + contactUri.toString());
        Log.d(TAG, "lookup key = " + lookupKey);

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


        // Initializes the loader framework
        getSupportLoaderManager().initLoader(0, null, this);

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
}
