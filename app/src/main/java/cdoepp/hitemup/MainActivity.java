package cdoepp.hitemup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PeopleListFragment.OnListFragmentInteractionListener,
        PopupMenu.OnMenuItemClickListener {

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_CALL_LOG
    };

    public static final HashMap<Integer, Integer> CONTACT_PRIORITY_MAP = createMap();
    private static HashMap<Integer, Integer> createMap()
    {
        //int[] pmap = {0, 120, 90, 70, 60, 50, 40, 30, 21, 13};
        HashMap<Integer, Integer> priorityMap = new HashMap<Integer, Integer>();
        priorityMap.put(1, 120);
        priorityMap.put(2, 90);
        priorityMap.put(3, 70);
        priorityMap.put(4, 60);
        priorityMap.put(5, 50);
        priorityMap.put(6, 40);
        priorityMap.put(7, 30);
        priorityMap.put(8, 21);
        priorityMap.put(9, 14);
        priorityMap.put(10, 7);
        return priorityMap;
    }

    public static final String PEOPLE_LIST_FRAGMENT = "PeopleListFragment";
    public static final String PERSON = "person";
    public static final int EDIT = 1;
    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 2;
    public static final String CONTACT_URI = "CONTACT_URI";
    public static final String LOOKUP_KEY = "LOOKUP_KEY";
    public static final String CONTACT_ID = "CONTACT_ID";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String NAME = "NAME";
    public static final String PHOTO_URI = "PHOTO_URI";

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        showPeopleListFragment();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
    }

    public void showPeopleListFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, PeopleListFragment.newInstance(1), PEOPLE_LIST_FRAGMENT).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected, id = " + id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_sort) {
            View menuItemView = findViewById(R.id.toolbar); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, menuItemView, Gravity.RIGHT|Gravity.TOP, R.attr.actionOverflowMenuStyle, 0);
            //PopupMenu popupMenu = new PopupMenu(this, menuItemView);
            popupMenu.inflate(R.menu.sort_contacts);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when a sort popup menu item is clicked:
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onMenuItemClick, id = " + id);
        Fragment peopleListFragment = getSupportFragmentManager().findFragmentByTag(PEOPLE_LIST_FRAGMENT);
        if (peopleListFragment == null) return false;

        if (id == R.id.sort_name)
            ((PeopleListFragment) peopleListFragment).setSortMethod(PeopleListFragment.SORT_METHOD_NAME);
        else if (id == R.id.sort_level)
            ((PeopleListFragment) peopleListFragment).setSortMethod(PeopleListFragment.SORT_METHOD_LEVEL);

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
