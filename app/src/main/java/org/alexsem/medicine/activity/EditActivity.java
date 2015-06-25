package org.alexsem.medicine.activity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.alexsem.medicine.R;
import org.alexsem.medicine.adapter.MedicineTypeAdapter;
import org.alexsem.medicine.transfer.MedicineProvider;

import java.text.ParseException;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_MEDICINE_ID = "medicineId";

    private EditText mName;
    private Spinner mType;
    private EditText mAmount;
    private TextView mUnit;
    private EditText mExpiration;
    private EditText mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);

        mName = (EditText) findViewById(R.id.edit_name);
        mType = (Spinner) findViewById(R.id.edit_type);
        mAmount = (EditText) findViewById(R.id.edit_amount);
        mUnit = (TextView) findViewById(R.id.edit_unit);
        mExpiration = (EditText) findViewById(R.id.edit_expiration);
        mDescription = (EditText) findViewById(R.id.edit_description);


        Uri uri = MedicineProvider.MedicineType.CONTENT_URI;
        String[] projection = {
                MedicineProvider.MedicineType.ID,
                MedicineProvider.MedicineType.TYPE,
                MedicineProvider.MedicineType.UNIT,
                MedicineProvider.MedicineType.MEASURABLE
        };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        mType.setAdapter(new MedicineTypeAdapter(this, cursor));
        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mType.getAdapter().getItem(position);
                mUnit.setText(cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.UNIT)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mType.setSelection(0);

        if (savedInstanceState != null) {
            mName.setText(savedInstanceState.getString("name"));
//            mType.setSelection(savedInstanceState.getLong("typeId")); //TODO id
            mAmount.setText(savedInstanceState.getString("amount"));
            mUnit.setText(savedInstanceState.getString("unit"));
            mExpiration.setText(savedInstanceState.getString("expiration"));
            mDescription.setText(savedInstanceState.getString("description"));
        } else if (getIntent().hasExtra(EXTRA_MEDICINE_ID)) {
            getSupportLoaderManager().initLoader(0, getIntent().getExtras(), mLoaderCallbacks);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("name", mName.getText().toString());
        outState.putLong("typeId", mType.getSelectedItemId());
        outState.putString("amount", mAmount.getText().toString());
        outState.putString("unit", mUnit.getText().toString());
        outState.putString("expiration", mExpiration.getText().toString());
        outState.putString("description", mDescription.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //----------------------------------------------------------------------------------------------

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = Uri.withAppendedPath(MedicineProvider.Medicine.CONTENT_URI, String.valueOf(args.getLong(EXTRA_MEDICINE_ID)));
            String[] projection = {
                    MedicineProvider.Medicine.NAME,
                    MedicineProvider.Medicine.TYPE_ID,
                    MedicineProvider.Medicine.AMOUNT,
                    MedicineProvider.Medicine.EXPIRATION,
                    MedicineProvider.Medicine.DESCRIPTION
            };
            return new CursorLoader(EditActivity.this, uri, projection, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mName.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.NAME)));
//            mType.setSelection(data.getString(data.getColumnIndex(MedicineProvider.Medicine.NAME)));
            mAmount.setText(String.valueOf(data.getInt(data.getColumnIndex(MedicineProvider.Medicine.AMOUNT))));
//            mUnit.setText(/**/);
            try {
                mExpiration.setText(String.format("%1$tb %1$tY",
                        MedicineProvider.parseExpireDate(data.getString(data.getColumnIndex(MedicineProvider.Medicine.EXPIRATION)))));
            } catch (ParseException e) {
                mExpiration.setText(""); //TODO check dialog appear
            }
            mDescription.setText(data.getString(data.getColumnIndex(MedicineProvider.Medicine.DESCRIPTION)));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };
}
