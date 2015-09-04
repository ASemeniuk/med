package org.alexsem.medicine.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.alexsem.medicine.R;
import org.alexsem.medicine.adapter.MedicineAdapter;
import org.alexsem.medicine.notification.NotificationService;
import org.alexsem.medicine.transfer.MedicineProvider;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_EDIT = 293;

    private final int LOADER_MEDICINE = 0;
    private final int LOADER_GROUP = 1;

    private Button mGroupRemove;
    private SearchView mSearchView;
    private MedicineAdapter mAdapter;
    private long mSelectedGroupId = -1;
    private String mSearchString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAdapter = new MedicineAdapter(this, null);
        SwipeMenuListView list = (SwipeMenuListView) findViewById(android.R.id.list);
        list.setEmptyView(findViewById(android.R.id.empty));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.EXTRA_MEDICINE_ID, id);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });
        list.setMenuCreator(mSwipeMenuCreator);
        list.setOnMenuItemClickListener(mSwipeMenuClickListener);
        list.setAdapter(mAdapter);
        registerForContextMenu(list);

        mGroupRemove = (Button) findViewById(R.id.main_group_remove);
        mGroupRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.withAppendedPath(MedicineProvider.MedicineGroup.CONTENT_URI, String.valueOf(mSelectedGroupId));
                getContentResolver().delete(uri, null, null);
                getSupportLoaderManager().restartLoader(LOADER_GROUP, null, mGroupLoaderCallbacks);
            }
        });

        findViewById(R.id.main_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.EXTRA_GROUP_ID, mSelectedGroupId);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });

        Intent updateIntent = new Intent(this, NotificationService.class);
        updateIntent.putExtra("noupdate", true);
        startService(updateIntent);

        if (savedInstanceState != null) {
            mSelectedGroupId = savedInstanceState.getLong("selectedGroupId");
            mSearchString = savedInstanceState.getString("searchString");
        }

        getSupportLoaderManager().initLoader(LOADER_GROUP, null, mGroupLoaderCallbacks);
        getSupportLoaderManager().initLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mSearchString = "";
                mSearchView.setQuery(mSearchString, false);
                getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mSearchString = null;
                getSupportLoaderManager().restartLoader(LOADER_GROUP, null, mGroupLoaderCallbacks);
                return false;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchString = newText;
                getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
                return false;
            }
        });
        mSearchView.setIconified(mSearchString == null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mSearchView.setQuery("", false);
                mSearchView.setIconified(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case android.R.id.list:
                menu.add(Menu.NONE, 0, 0, R.string.action_delete);
                break;
            default:
                super.onCreateContextMenu(menu, v, menuInfo);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: //Delete
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Uri uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_URI, String.valueOf(info.id));
                getContentResolver().delete(uri, null, null);
                getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_EDIT:
                switch (resultCode) {
                    case EditActivity.RESULT_ITEM_CHANGED:
                        getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
                        break;
                    case EditActivity.RESULT_GROUP_ADDED:
                        getSupportLoaderManager().restartLoader(LOADER_GROUP, null, mGroupLoaderCallbacks);
                        break;
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("selectedGroupId", mSelectedGroupId);
        outState.putString("searchString", mSearchString);
        super.onSaveInstanceState(outState);
    }

    //----------------------------------------------------------------------------------------------

    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(MainActivity.this);
            deleteItem.setBackground(getResources().getDrawable(R.drawable.selector_accent));
            deleteItem.setWidth(getResources().getDimensionPixelSize(R.dimen.list_action_width));
            deleteItem.setIcon(R.drawable.ic_delete);
            menu.addMenuItem(deleteItem);
        }
    };

    private SwipeMenuListView.OnMenuItemClickListener mSwipeMenuClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0: //Delete
                    Uri uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_URI, String.valueOf(mAdapter.getItemId(position)));
                    getContentResolver().delete(uri, null, null);
                    getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
                    break;
            }
            return false;
        }
    };

    private ActionBar.TabListener mGroupTabListener = new ActionBar.TabListener() {
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mSelectedGroupId = (Long) tab.getTag();
            getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    };

    //----------------------------------------------------------------------------------------------

    private LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            Uri uri = MedicineProvider.MedicineGroup.CONTENT_URI;
            String[] projection = {
                    MedicineProvider.MedicineGroup.ID,
                    MedicineProvider.Medicine.NAME
            };
            return new CursorLoader(MainActivity.this, uri, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            ActionBar actionBar = getSupportActionBar();
            if (cursor.moveToFirst()) {
                if (cursor.getCount() == 1 || mSearchString != null) {
                    long id = cursor.getLong(cursor.getColumnIndex(MedicineProvider.Medicine.ID));
                    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mSelectedGroupId = id;
                    getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
                } else {
                    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                    actionBar.removeAllTabs();
                    long selectedGroupId = mSelectedGroupId;
                    do {
                        long id = cursor.getLong(cursor.getColumnIndex(MedicineProvider.Medicine.ID));
                        ActionBar.Tab tab = actionBar.newTab().setTag(id).setText(cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.NAME))).setTabListener(mGroupTabListener);
                        actionBar.addTab(tab);
                        if (id == selectedGroupId) {
                            tab.select();
                        }
                    } while (cursor.moveToNext());
                }
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mSelectedGroupId = -1;
                getSupportLoaderManager().restartLoader(LOADER_MEDICINE, null, mMedicineLoaderCallbacks);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> mMedicineLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            Uri uri;
            if (mSearchString != null) { //Search
                uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_SEARCH_URI, mSearchString);
            } else { //Group
                uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_GROUP_URI, String.valueOf(mSelectedGroupId));
            }
            String[] projection = {
                    MedicineProvider.Medicine.ID,
                    MedicineProvider.Medicine.NAME,
                    MedicineProvider.Medicine.DESCRIPTION,
                    MedicineProvider.Medicine.AMOUNT,
                    MedicineProvider.Medicine.EXPIRATION,
                    MedicineProvider.MedicineType.TYPE,
                    MedicineProvider.MedicineType.UNIT,
                    MedicineProvider.MedicineType.MEASURABLE
            };
            return new CursorLoader(MainActivity.this, uri, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);
            mGroupRemove.setVisibility(mSearchString == null && cursor.getCount() <= 0 && getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };
}
