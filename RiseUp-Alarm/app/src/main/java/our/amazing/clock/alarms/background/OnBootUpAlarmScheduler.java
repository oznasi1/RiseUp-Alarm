/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package our.amazing.clock.alarms.background;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.data.AlarmCursor;
import our.amazing.clock.alarms.data.AlarmsTableManager;
import our.amazing.clock.alarms.misc.AlarmController;
import our.amazing.clock.ringtone.playback.AlarmRingtoneService;
import our.amazing.clock.util.TimeFormatUtils;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_DISMISS;
import static our.amazing.clock.util.ParcelableUtil.getRingingObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class OnBootUpAlarmScheduler extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "our.amazing.clock.action.FOO";
    private static final String ACTION_BAZ = "our.amazing.clock.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "our.amazing.clock.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "our.amazing.clock.extra.PARAM2";

    public OnBootUpAlarmScheduler() {
        super("OnBootUpAlarmScheduler");
            //startForeground(R.id.ringtone_service_notification, getForegroundNotification());

    }

    private  Notification getForegroundNotification() {
         String ACTION_SNOOZE = "our.amazing.clock.ringtone.action.SNOOZE";
         String ACTION_DISMISS = "our.amazing.clock.ringtone.action.DISMISS";

        String title = getRingingObject().label().isEmpty()
                ? getString(R.string.alarm)
                : getRingingObject().label();
        return new NotificationCompat.Builder(this)
                // Required contents
                .setSmallIcon(R.drawable.ic_alarm_24dp)
                .setContentTitle(title)
                .setContentText(TimeFormatUtils.formatTime(this, System.currentTimeMillis()))
                .addAction(R.drawable.ic_snooze_24dp,
                        getString(R.string.snooze),
                        getPendingIntent(ACTION_SNOOZE, getRingingObject().getIntId()))
                .addAction(R.drawable.ic_dismiss_alarm_24dp,
                        getString(R.string.dismiss),
                        getPendingIntent(ACTION_DISMISS, getRingingObject().getIntId()))
                .build();
    }

    protected final PendingIntent getPendingIntent(@NonNull String action, int requestCode) {
        Intent intent = new Intent(this, getClass())
                .setAction(action);
        return PendingIntent.getService(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, OnBootUpAlarmScheduler.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        //context.startService(intent);
        //context.startForegroundService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, OnBootUpAlarmScheduler.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        //context.startService(intent);
        //context.startForegroundService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AlarmController controller = new AlarmController(this, null);
            // IntentService works in a background thread, so this won't hold us up.
            AlarmCursor cursor = new AlarmsTableManager(this).queryEnabledAlarms();
            while (cursor.moveToNext()) {
                Alarm alarm = cursor.getItem();
                if (!alarm.isEnabled()) {
                    throw new IllegalStateException(
                            "queryEnabledAlarms() returned alarm(s) that aren't enabled");
                }
                controller.scheduleAlarm(alarm, false);
            }
            cursor.close();

/*            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
*/
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
