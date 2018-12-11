package cdoepp.hitemup;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.PhoneNumberUtils;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class PeopleListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, DatabaseResponse {

    private static final String TAG = "PeopleListFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String SAVE_SORT_METHOD = "saved_sort_method";
    public static final int SORT_METHOD_NAME = 0;
    public static final int SORT_METHOD_LEVEL = 1;
    public static final int SORT_METHOD_LAST_CONTACT = 2;

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

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " NOTNULL ";
    // Defines a variable for the search string
    private String mSearchString;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };


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

    private long mLastClickTime = 0;

    //Sorting the list of contacts:
    private Comparator<Person> SORT_BY_NAME = new Comparator<Person>() {
        public int compare(Person p1, Person p2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
            return res;
        }
    };

    private Comparator<Person> SORT_BY_LEVEL = new Comparator<Person>() {
        public int compare(Person p1, Person p2) {
            if (p1.getLevel() < p2.getLevel())
                return 1;
            else if (p1.getLevel() > p2.getLevel())
                return -1;
            else    // sort by name if levels are equal:
                return String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
        }
    };

    private Comparator<Person> SORT_BY_LAST_CONTACT = new Comparator<Person>() {
        public int compare(Person p1, Person p2) {
            List<Message> m1 = p1.getMessages();
            List<Message> m2 = p2.getMessages();
            if ((m1 == null || m1.size() == 0) && (m2 == null || m2.size() == 0)) {
                return String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
            } else if ((m1 == null || m1.size() == 0) && (m2 != null && m2.size() > 0)) {
                return 1;
            } else if ((m1 != null && m1.size() > 0) && (m2 == null || m2.size() == 0)) {
                return -1;
            } else {
                long currentTimestamp = new Date().getTime();
                long d1 = currentTimestamp - m1.get(0).getTimestamp();
                long d2 = currentTimestamp - m2.get(0).getTimestamp();
                if (d1 < d2)
                    return -1;
                else if (d1 > d2)
                    return 1;
                else
                    return String.CASE_INSENSITIVE_ORDER.compare(p1.getName(), p2.getName());
            }
        }
    };


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
        Log.d(TAG, "onActivityCreated");

        peopleList = new ArrayList<Person>();
        listAdapter = new PeopleListAdapter(getContext(), R.layout.item_person, peopleList);
        mContactsList.setAdapter(listAdapter);
        mContactsList.setOnItemClickListener(this);

        // Initializes the loader
        getLoaderManager().initLoader(0, null, this);
    }

    /*
    Receives the phone's contact data, then passes it on the to an asynctask to check
    it with our app's database and add any contacts we don't have yet
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d(TAG, "onLoadFinished");
        Log.d(TAG, "" + DatabaseUtils.dumpCursorToString(data));

        // Set cursor to use in the asynctask result; listview is still INVISIBLE

        data.moveToFirst();
        contactsCursor = data;

        new FetchPeopleTask(this, data).execute("");

    }


    // Receives result list from Room database access in AsyncTask, checks with contacts from
    // loader, then shows list of contacts
    @Override
    public void processFinish(List<Person> people) {
        Log.d(TAG, "processFinish, " + people.toString() + ", size = " + people.size());

        if (contactsCursor == null || people == null) return;
        contactsCursor.moveToFirst();

        peopleList.clear();
        for (Person person : people) peopleList.add(person);

        //Check people database with phone's contacts:
        for (int i = 0; i < contactsCursor.getCount(); i++) {

            long id = contactsCursor.getLong(0);

            //check if id in list. if not, add it:
            Person person = null;
            boolean found = false;
            for (int j = 0; j < peopleList.size(); j++) {
                person = peopleList.get(j);
                if (person.getId() == id) {
                    found = true;
                    break;
                }
            }
            // Create new Person object and add to our database:
            Cursor c = getActivity().getContentResolver().query(Data.CONTENT_URI,
                    new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                    Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                    new String[] {String.valueOf(id)}, null);

            c.moveToFirst();
            String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
            String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
            String photo = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));
            if (found == false || person == null) {
                int level = 0;
                person = new Person(id, name, phoneNumber, photo, level);
                peopleList.add(person);
                new addContactTask().execute(person);

            } else {
                // Already in database, update parameters:
                int level = person.getLevel();
                person.setName(name);
                person.setPhoneNumber(phoneNumber);
                person.setPhoto(photo);
            }
            contactsCursor.moveToNext();
        }
        Log.d(TAG, "people list size = " + peopleList.size());
        sortListView();



        //List<Message> messageList = getAllSMSAndMMS();

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
        // Prevents multiple clicks, using threshold of 1 second:
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return;
        mLastClickTime = SystemClock.elapsedRealtime();

        Person person = (Person) parent.getAdapter().getItem(position);
        Log.d(TAG, "item clicked, name = " + person.getName() + ", id = " + person.getId());

        Intent intent = new Intent(getActivity(), PersonDetailsActivity.class);
        intent.putExtra(MainActivity.PERSON, person);
        startActivityForResult(intent, MainActivity.EDIT);
    }

    // Receives edited person from details activity and updates our list with changes made
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.EDIT) {
            if (resultCode == MainActivity.RESULT_OK) {
                Person person = (Person) data.getSerializableExtra(MainActivity.PERSON);
                Log.d(TAG, "onActivityResult, name = " + person.getName() + ", level = " + person.getLevel());
                // TODO: replace person object with correct one based on id
                for (int i = 0; i < peopleList.size(); i++) {
                    Person p = peopleList.get(i);
                    if (p.getId() == person.getId()) {
                        p.setName(person.getName());
                        p.setLevel(person.getLevel());
                        p.setPhoneNumber(person.getPhoneNumber());
                        p.setPhoto(person.getPhoto());
                    }
                }
                listAdapter.notifyDataSetChanged();
                //sortListView();
            }
        }
    }

    // Called by main activity when the user changes the sort method using the toolbar button
    public void setSortMethod(int sortMethod) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SAVE_SORT_METHOD, sortMethod);
        editor.apply();
        sortListView();
    }

    public void sortListView() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int sortMethod = preferences.getInt(SAVE_SORT_METHOD, 0);
        if (sortMethod == SORT_METHOD_NAME)
            Collections.sort(peopleList, SORT_BY_NAME);
        else if (sortMethod == SORT_METHOD_LEVEL)
            Collections.sort(peopleList, SORT_BY_LEVEL);
        else if (sortMethod == SORT_METHOD_LAST_CONTACT)
            Collections.sort(peopleList, SORT_BY_LAST_CONTACT);
        listAdapter = new PeopleListAdapter(getContext(), R.layout.item_person, peopleList);
        mContactsList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
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

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
    }



    ////////////////////////////////////////////////////////////////////////
    // Asynctasks:

    public class FetchPeopleTask extends AsyncTask<String, Void, List<Person>> {
        public DatabaseResponse delegate = null;
        public Cursor contactsCursor = null;

        public FetchPeopleTask(DatabaseResponse delegate, Cursor data){
            this.delegate = delegate;
            contactsCursor = data;
        }

        @Override
        protected List<Person> doInBackground(String... params) {
            AppDatabase db = Room.databaseBuilder(getContext(),
                    AppDatabase.class, "database-name")
                    .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                    .build();
            //db.peopleDao().deleteById(200);
            List<Person> people = db.peopleDao().getAll();

            if (contactsCursor == null || people == null) return null;
            contactsCursor.moveToFirst();

            peopleList.clear();
            // Populate list of people and remove invalid ones:
            for (Person person : people) {
                // Check if each person is a real contact: (sometimes phone automatically adds bad contacts)
                boolean valid = false;
                if (contactsCursor.moveToFirst()) {
                    do {
                        long id = contactsCursor.getLong(0);
                        if (id == person.getId())
                            valid = true;
                    } while (contactsCursor.moveToNext() && !valid);
                }
                if (valid)
                    peopleList.add(person);
                else // remove from database:
                    db.peopleDao().delete(person);

            }
            contactsCursor.moveToFirst();

            //Check people database with phone's contacts:
            for (int i = 0; i < contactsCursor.getCount(); i++) {
                long id = contactsCursor.getLong(0);

                //check if id in list. if not, add it:
                Person person = null;
                boolean found = false;
                for (int j = 0; j < peopleList.size(); j++) {
                    person = peopleList.get(j);
                    if (person.getId() == id) {
                        found = true;
                        break;
                    }
                }
                // Create new Person object and add to our database:
                Cursor c = getActivity().getContentResolver().query(Data.CONTENT_URI,
                        new String[] {Data._ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_URI, Data.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                        Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                        new String[] {String.valueOf(id)}, null);

                c.moveToFirst();
                String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
                String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                String photo = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));
                if (found == false || person == null) {
                    int level = 0;
                    person = new Person(id, name, phoneNumber, photo, level);
                    peopleList.add(person);
                    new addContactTask().execute(person);

                } else {
                    // Already in database, update parameters:
                    Log.d(TAG, "Already in database");
                    int level = person.getLevel();
                    person.setName(name);
                    person.setPhoneNumber(phoneNumber);
                    person.setPhoto(photo);
                }
                contactsCursor.moveToNext();
            }
            Log.d(TAG, "people list size = " + peopleList.size());


            // GET ALL TEXT MESSAGES:
            //List<Message> messageList = getAllSMSAndMMS();
            Log.d(TAG, "MMSSMS URI = " + MmsSms.CONTENT_FILTER_BYPHONE_URI);
            Log.d(TAG, "MMSSMS URI = " + MmsSms.CONTENT_CONVERSATIONS_URI);
            List<Message> messageList = new ArrayList<Message>();
            Cursor c = getActivity().getContentResolver().query(
                    Uri.parse("content://mms-sms/complete-conversations/"),
                    new String[]{ MmsSms.TYPE_DISCRIMINATOR_COLUMN, MmsSms._ID }, // Select body text
                    null, //Sms.Inbox.PERSON + " = 6",
                    null,
                    //Sms.Inbox.DATE); // Default sort order
                    null, null);

            Message msg = null;
            if (c.moveToFirst()) {
                do {
                    msg = null;
                    Log.d(TAG, "type = " + c.getString(c.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN))
                            + ", id = " + c.getString(c.getColumnIndex(MmsSms._ID)));
                    String id = c.getString(c.getColumnIndex(MmsSms._ID));
                    String type = c.getString(c.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN));
                    //Log.d(TAG, "date = " + c.getString(c.getColumnIndex(Mms.DATE)));
                    if (type.equals("sms")) {
                        msg = getSmsDetails(id);
                        if (msg != null) messageList.add(msg);
                    } else if (type.equals("mms")) {
                        msg = getMmsDetails(id);
                        if (msg != null) messageList.add(msg);
                    }
                } while (c.moveToNext());
            } else {
                throw new RuntimeException("You have no SMS in Inbox");
            }
            c.close();

            //Sort messages by date, newest first:
            Comparator<Message> SORT_BY_DATE = new Comparator<Message>() {
                public int compare(Message m1, Message m2) {
                    return Long.compare(m2.getTimestamp(), m1.getTimestamp());
                }
            };
            Collections.sort(messageList, SORT_BY_DATE);


            for (int i = 0; i < peopleList.size(); i++) {
                Person person = peopleList.get(i);
                String contactAddress = person.getPhoneNumber();
                ArrayList<Message> contactMessages = new ArrayList<Message>();

                Iterator<Message> it = messageList.iterator();
                while (it.hasNext()) {
                    Message message = it.next();
                    if (PhoneNumberUtils.compare(contactAddress, message.getAddress())) {
                        contactMessages.add(message);
                        it.remove();
                    }
                }

                /*
                // OR: (maybe slower??)
                for (Message message : messageList) {
                    if (PhoneNumberUtils.compare(contactAddress, message.getAddress())) {
                        contactMessages.add(message);
                    }
                }
                */

                person.setMessages(contactMessages);

            }



            return people;
        }

        public Message getSmsDetails(String id) {
            String selection = Sms.Inbox._ID + " = " +id;
            Cursor cursor = getActivity().getContentResolver().query(Sms.CONTENT_URI, null, selection, null, null);
            cursor.moveToFirst();
            String address = cursor.getString(cursor.getColumnIndex(Sms.Inbox.ADDRESS));
            int type = cursor.getInt(cursor.getColumnIndex(Sms.Inbox.TYPE)); // 1 = received, 2 = sent
            long timestamp = cursor.getLong(cursor.getColumnIndex(Sms.DATE));
            Date date = new Date(timestamp);
            String text = cursor.getString(cursor.getColumnIndex(Sms.Inbox.BODY));

            if (cursor != null) cursor.close();
            Log.d(TAG, "     address = " + address + ", type = " + type + ", date = " + date.toString() + ", text = " + text);
            // Checks if message was an sms group message sent to multiple people. We already have
            // an individual message for each recipient, so this one can be ignored
            if (address.split(" ").length > 1) {    // SMS gr
                Log.d(TAG, "SMS GROUP MESSAGE, MULTIPLE!!, address = " + address.split(" ")[0] + ", " + address.split(" ")[1]);
                return null;
            }
            return new Message(id, type, address, date, text);

        }

        public Message getMmsDetails(String id) {
            Date date = getMmsDate(id);
            String selection = Mms.Part.MSG_ID + "=" + id;
            Uri uri = Uri.parse("content://mms/part");
            Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
            Message message = null;
            if (cursor.moveToFirst()) {
                do {
                    String partId = cursor.getString(cursor.getColumnIndex(Mms._ID));
                    String type = cursor.getString(cursor.getColumnIndex(Mms.Part.CONTENT_TYPE));
                    if (type.equals("text/plain")) {
                        String text = cursor.getString(cursor.getColumnIndex(Mms.Part.TEXT));
                        String charset = cursor.getString(cursor.getColumnIndex(Mms.Part.CHARSET));

                        String selectionAdd = new String(Mms.Addr.MSG_ID + "=" + id);
                        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);

                        //TODO:
                        final String MY_PHONE_NUMBER = "5404553006";
                        int type2 = -1;
                        String address = null;

                        Uri uriAddress = Uri.parse(uriStr);
                        Cursor cAdd = getActivity().getContentResolver().query(uriAddress, null,
                                selectionAdd, null, null);
                        if (cAdd.moveToFirst()) {
                            address = cAdd.getString(cAdd.getColumnIndex(Mms.Addr.ADDRESS));
                            if (PhoneNumberUtils.compare(MY_PHONE_NUMBER, address)) //if first one, it's our own message
                                type2 = Message.TYPE_SENT;
                            else
                                type2 = Message.TYPE_RECEIVED;
                        }
                        if (cAdd != null)
                            cAdd.close();

                        Log.d(TAG, "     partid = " + partId + ", type = " + type + ", date = " + date.toString() + ", type2 = " + type2
                                + ", charset = " + charset + ", text = " + text + ", address = " + address);

                        message = new Message(id, type2, address, date, text);
                    }
                } while(cursor.moveToNext());
            }
            if (cursor != null) cursor.close();

            return message;
        }

        public Date getMmsDate(String id) {
            String selection = Mms._ID + "=" + id;
            Uri uri = Uri.parse("content://mms/");
            Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);

            Date date = null;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long timestamp = cursor.getLong(2);
                    date = new Date(timestamp);
                    if (date.getYear() < 100) //before year 2000, need to correct timestamp
                        timestamp = timestamp * 1000;
                    date.setTime(timestamp);
                } while(cursor.moveToNext());
            }
            if (cursor != null) cursor.close();

            return date;
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
