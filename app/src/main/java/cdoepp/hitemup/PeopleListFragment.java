package cdoepp.hitemup;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cdoepp.hitemup.Database.AppDatabase;
import cdoepp.hitemup.Database.DatabaseResponse;
import cdoepp.hitemup.Database.Person;

import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PeopleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener,
        DatabaseResponse

        {

    private static final String TAG = "PeopleListFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Contacts.DISPLAY_NAME_PRIMARY
    };

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            Data._ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.PHOTO_THUMBNAIL_URI,
    };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " NOTNULL ";
    // Defines a variable for the search string
    private String mSearchString;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };



    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            R.id.name
    };
    // Define global mutable variables
    // Define a ListView object
    ListView mContactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    long mContactId;
    // The contact's LOOKUP_KEY
    String mContactKey;
    // A content URI for the selected contact
    Uri mContactUri;
    // An adapter that binds the result Cursor to the ListView
    private Cursor contactsCursor;
    //private ContactsCursorAdapter mCursorAdapter;
    private ArrayAdapter listAdapter;
    private List<Person> peopleList;
    private ProgressBar progressBar;


    public PeopleListFragment() {
    }

    public static PeopleListFragment newInstance(int columnCount) {
        PeopleListFragment fragment = new PeopleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person_list, container, false);

        mContactsList = view.findViewById(R.id.list);
        progressBar = view.findViewById(R.id.progress_bar);

        mContactsList.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //mCursorAdapter = new ContactsCursorAdapter(getActivity(), null);
        peopleList = new ArrayList<Person>();
        listAdapter = new PeopleListAdapter(getContext(), R.layout.item_person, peopleList);
        //listAdapter.notifyDataSetChanged();
        mContactsList.setAdapter(listAdapter);
        mContactsList.setOnItemClickListener(this);

        // Initializes the loader
        getLoaderManager().initLoader(0, null, this);
    }



    /*
    Receives the phone's contact data, then passes it on the to an asyntask to check
    it with our app's database and add any contacts we don't have yet
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d(TAG, "onLoadFinished");
        Log.d(TAG, "" + DatabaseUtils.dumpCursorToString(data));

        // Set cursor to use in the asynctask result; listview is still INVISIBLE
        contactsCursor = data;

        new FetchPeopleTask(this).execute("");

    }

    // Receives result list from Room database access in AsyncTask, checks with contacts from
    // loader, then shows list of contacts
    @Override
    public void processFinish(List<Person> people) {
        Log.d(TAG, "processFinish, " + people.toString());
        Log.d(TAG, "database people = " + people.toString() + ", size = " + people.size());

        if (contactsCursor == null || people == null) return;
        contactsCursor.moveToFirst();

        //Check people database with phone's contacts:
        for (int i = 0; i < contactsCursor.getCount(); i++) {

            long id = contactsCursor.getLong(0);

            //check if id in list. if not, aadd it:
            Person person = null;
            boolean found = false;
            for (int j = 0; j < people.size(); j++) {
                person = people.get(j);
                if (person.getId() == id) {
                    found = true;
                    break;
                }
            }
            if (found == false || person == null) {
                // Create new Person object:
                Cursor c = getActivity().getContentResolver().query(Data.CONTENT_URI,
                        new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                        Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                        new String[] {String.valueOf(id)}, null);

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
                String name = contactsCursor.getString(contactsCursor.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
                Log.d(TAG, "id = " + id + ", name = " + name + ", name = " + person.getName());
            }


            //String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
            contactsCursor.moveToNext();
        }
        //mCursorAdapter.swapCursor(contactsCursor, peopleMap);
        Log.d(TAG, "people list size = " + peopleList.size());
        //listAdapter.clear();
        //listAdapter.addAll(people);
        listAdapter.notifyDataSetChanged();
        mContactsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        //Makes search string into pattern and stores it in the selection array
        mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                null,
                null
        );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        // Delete the reference to the existing Cursor
        //mCursorAdapter.swapCursor(null);
        contactsCursor = null;
        peopleList.clear();
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {
        Log.d(TAG, "item clicked");

        Person person = (Person) parent.getAdapter().getItem(position);
        Log.d(TAG, "name = " + person.getName());
        mContactId = person.getId();

        //Log.d(TAG, "name = " + cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY)));
        //Log.d(TAG, "phone # = " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

        Cursor c = getActivity().getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                new String[] {String.valueOf(mContactId)}, null);

        c.moveToFirst();
        Log.d(TAG, "new cursor = " + DatabaseUtils.dumpCursorToString(c));
        String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
        Log.d(TAG, "phone num = " + phoneNumber);
        String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
        String photoString = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));


        Intent intent = new Intent(getActivity(), PersonDetailsActivity.class);
        intent.putExtra(MainActivity.CONTACT_ID, mContactId);
        intent.putExtra(MainActivity.PHONE_NUMBER, phoneNumber);
        intent.putExtra(MainActivity.NAME, name);
        intent.putExtra(MainActivity.PHOTO_URI, photoString);
        startActivity(intent);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onListFragmentInteraction(DummyItem item);
    }

    public HashMap<Long, Person> getMapFromList(List<Person> list) {
        HashMap<Long, Person> map = new HashMap<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Person person = list.get(i);
            map.put(person.getId(), person);
        }
        Log.d(TAG, "map size = " + map.size());


        return map;
    }

    ////////////////////////////////////////////////////////////////////////
    // Asynctasks:

    public class FetchPeopleTask extends AsyncTask<String, Void, List<Person>> {
        public DatabaseResponse delegate = null;

        public FetchPeopleTask(DatabaseResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected List<Person> doInBackground(String... params) {
            AppDatabase db = Room.databaseBuilder(getContext(),
                    AppDatabase.class, "database-name")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();
            List<Person> people = db.peopleDao().getAll();

            return people;
        }

        @Override
        protected void onPostExecute(List<Person> people) {
            delegate.processFinish(people);
            Log.d(TAG, "Executed");
        }
    }

    public class addContactTask extends AsyncTask<Person, Void, Void> {
        @Override
        protected Void doInBackground(Person... people) {
            AppDatabase db = Room.databaseBuilder(getContext(),
                    AppDatabase.class, "database-name")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();
            db.peopleDao().insertAll(people);
            Log.d(TAG, "insert person = " + people[0].getName());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "inserted contact to room database");
        }
    }

}
