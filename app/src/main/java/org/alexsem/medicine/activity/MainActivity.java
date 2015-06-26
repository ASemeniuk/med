package org.alexsem.medicine.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.alexsem.medicine.R;
import org.alexsem.medicine.adapter.MedicineAdapter;
import org.alexsem.medicine.transfer.MedicineProvider;

public class MainActivity extends AppCompatActivity {

    private MedicineAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAdapter = new MedicineAdapter(this, null);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setEmptyView(findViewById(android.R.id.empty));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.EXTRA_MEDICINE_ID, id);
                startActivity(intent);
            }
        });
        list.setAdapter(mAdapter);

        findViewById(R.id.main_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });

        getSupportLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }


    //----------------------------------------------------------------------------------------------

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
            return new CursorLoader(MainActivity.this, MedicineProvider.Medicine.CONTENT_URI, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };
}
