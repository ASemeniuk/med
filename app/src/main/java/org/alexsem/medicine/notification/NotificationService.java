package org.alexsem.medicine.notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import org.alexsem.medicine.R;
import org.alexsem.medicine.activity.MainActivity;
import org.alexsem.medicine.transfer.MedicineProvider;

import java.util.Calendar;
import java.util.Date;

/**
 * Service used to define which medicine items are out of date
 * @author Semeniuk A.D.
 */
public class NotificationService extends Service {

    private static final int[] NOTIFICATION_ID = {1337, 1338};
    private static final String[] NOTIFICATION_TITLE = {"", ""};
    private static final String[] NOTIFICATION_PREF = {"notification_near", "notification_exp"};

    private final int INTERVAL_REGULAR = 24 * 60 * 60 * 1000;
    private final int INTERVAL_FIRST_RUN = 10 * 1000;

    private AlarmManager mAlarmManager;
    private NotificationTask mLastCheckTask = null;

    @Override
    public void onCreate() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        NOTIFICATION_TITLE[0] = getString(R.string.notification_near);
        NOTIFICATION_TITLE[1] = getString(R.string.notification_expired);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intentToFire = new Intent(AlarmReceiver.ACTION_CHECK_UPDATES_ALARM);
        if (PendingIntent.getBroadcast(this, 0, intentToFire, PendingIntent.FLAG_NO_CREATE) == null) { //Alarm not set yet
            launchAlarm(INTERVAL_FIRST_RUN); //Set alarm to be launched
        } else { //Alarm triggered
            if (intent.getExtras() != null) {
                if (intent.getExtras().containsKey("noupdate")) { //No need to restart timer
                    //Do nothing
                }
            } else { //Not a "nocheck" intent
                performCheck();
                launchAlarm(INTERVAL_REGULAR); //Set regular alarm
            }
        }
        return Service.START_NOT_STICKY;
    }

    /**
     * Launches the check updates alarm
     * @param rate Time interval on which to fire the Service
     */
    private void launchAlarm(int rate) {
        Intent intentToFire = new Intent(AlarmReceiver.ACTION_CHECK_UPDATES_ALARM);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
        if (rate > 0) { //Need to check for updates
            long timeToRefresh = SystemClock.elapsedRealtime() + rate;
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, timeToRefresh, rate, alarmIntent);
        } else { //No need to
            mAlarmManager.cancel(alarmIntent);
            alarmIntent.cancel();
        }
    }

    /**
     * Launches the background task to check for updates
     */
    private void performCheck() {
        if (mLastCheckTask == null || mLastCheckTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            mLastCheckTask = new NotificationTask();
            mLastCheckTask.execute();
        }
    }

//=========================== ASYNCHRONOUS TASKS ===========================

    /**
     * Class which is used for Internet synchronization
     * @author Semeniuk A.D.
     */
    private class NotificationTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            String lastNear = prefs.getString(NOTIFICATION_PREF[0], "");
            String lastExp = prefs.getString(NOTIFICATION_PREF[1], "");
            if (date == 1 || date == 2) {
                Uri uri = MedicineProvider.Medicine.CONTENT_URI;
                String[] projection = {
                        MedicineProvider.Medicine.ID,
                        MedicineProvider.Medicine.NAME,
                        MedicineProvider.Medicine.EXPIRATION,
                        MedicineProvider.MedicineType.TYPE,
                        MedicineProvider.MedicineType.MEASURABLE
                };
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                try {
                    if (cursor.moveToFirst()) {
                        StringBuilder sExp = new StringBuilder();
                        StringBuilder sNear = new StringBuilder();
                        String now = MedicineProvider.formatExpireDate(new Date());
                        do {
                            String cur = cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.EXPIRATION));
                            if (date == 2 && cur.equals(now) && cur.compareTo(lastNear) > 0) { //Near list
                                if (sNear.length() > 0) {
                                    sNear.append('\n');
                                }
                                sNear.append(cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.NAME)))
                                        .append(" (").append(cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.TYPE))).append(");");
                            }
                            if (date == 1 && now.compareTo(cur) > 0 && cur.compareTo(lastExp) > 0) { //Expired list
                                if (sExp.length() > 0) {
                                    sExp.append('\n');
                                }
                                sExp.append(cursor.getString(cursor.getColumnIndex(MedicineProvider.Medicine.NAME)))
                                        .append(" (").append(cursor.getString(cursor.getColumnIndex(MedicineProvider.MedicineType.TYPE))).append(");");
                            }
                        } while (cursor.moveToNext());
                        return new String[]{sNear.toString(), sExp.toString()};
                    }
                } finally {
                    cursor.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) { //Success
                final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                SharedPreferences.Editor edit = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                String now = MedicineProvider.formatExpireDate(new Date());
                for (int i = 0; i < 2; i++) {
                    if (result[i].length() > 0) {
                        Intent intent = new Intent(NotificationService.this, MainActivity.class);
                        intent.putExtra(MainActivity.PARAM_SHOW_OUTDATED, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(NOTIFICATION_TITLE[i])
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(result[i]))
                                .setContentText(result[i])
                                .setAutoCancel(true)
                                .setContentIntent(contentIntent);
                        manager.notify(NotificationService.NOTIFICATION_ID[i], builder.build());
                        edit.putString(NOTIFICATION_PREF[i], now);
                    }
                }
                edit.commit();
                stopSelf();
            } else { //Error happened
                launchAlarm(INTERVAL_FIRST_RUN);
            }
        }
    }

}
