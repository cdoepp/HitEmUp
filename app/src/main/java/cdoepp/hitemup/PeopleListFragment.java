package cdoepp.hitemup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cdoepp.hitemup.dummy.DummyContent;
import cdoepp.hitemup.dummy.DummyContent.DummyItem;
import cdoepp.hitemup.Person;

import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.util.List;


public class PeopleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private static final String TAG = "PeopleList";
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
            "_id",
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.PHOTO_THUMBNAIL_URI
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
    private CursorAdapter mCursorAdapter;




    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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

        mContactsList = (ListView) view.findViewById(R.id.list);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCursorAdapter = new ContactsCursorAdapter(getActivity(), null);
        mContactsList.setAdapter(mCursorAdapter);
        mContactsList.setOnItemClickListener(this);

        // Initializes the loader
        getLoaderManager().initLoader(0, null, this);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        Log.d(TAG, "" + DatabaseUtils.dumpCursorToString(data));
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {
        Log.d(TAG, "item clicked");

        // Get the Cursor
        Cursor cursor = ((CursorAdapter) parent.getAdapter()).getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        mContactKey = cursor.getString(LOOKUP_KEY_INDEX);
        // Create the contact's content Uri
        mContactUri = Contacts.getLookupUri(mContactId, mContactKey);

        //You can use mContactUri as the content URI for retrieving the details for a contact.

        Intent intent = new Intent(getActivity(), PersonDetailsActivity.class);
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
        void onListFragmentInteraction(DummyItem item);
    }
}
