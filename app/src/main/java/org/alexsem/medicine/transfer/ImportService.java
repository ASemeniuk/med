package org.alexsem.medicine.transfer;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.alexsem.medicine.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Service for performing data imports
 */
public class ImportService extends IntentService {

    private Handler mHandler;

    public ImportService() {
        super("ImportService");
        mHandler = new Handler();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InputStream stream = getContentResolver().openInputStream(intent.getData());
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                ImportExportManager.importData(ImportService.this, builder.toString());
            } finally {
                reader.close();
            }
            mHandler.post(successRunnable);
        } catch (Exception ex) {
            mHandler.post(errorRunnable);
        }
    }


    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ImportService.this, R.string.import_success, Toast.LENGTH_LONG).show();
        }
    };

    private Runnable errorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(ImportService.this, R.string.import_error, Toast.LENGTH_LONG).show();
        }
    };
}
