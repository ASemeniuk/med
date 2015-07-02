package org.alexsem.medicine.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	public static final String ACTION_CHECK_UPDATES_ALARM = "org.alexsem.medicine.ACTION_ALARM";

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, NotificationService.class));
	}

}
