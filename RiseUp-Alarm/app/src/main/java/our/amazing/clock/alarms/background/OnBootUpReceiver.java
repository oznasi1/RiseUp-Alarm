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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.data.AlarmCursor;
import our.amazing.clock.alarms.data.AlarmsTableManager;
import our.amazing.clock.alarms.misc.AlarmController;

import static com.crashlytics.android.beta.Beta.TAG;

public class OnBootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Note that this will be called when the device boots up, not when the app first launches.
        // We may have a lot of alarms to reschedule, so do this in the background using an IntentService.
        //context.startService(new Intent(context, OnBootUpAlarmScheduler.class));

        AlarmController controller = new AlarmController(context.getApplicationContext(), null);
        // IntentService works in a background thread, so this won't hold us up.
        AlarmCursor cursor = new AlarmsTableManager(context.getApplicationContext()).queryEnabledAlarms();
        while (cursor.moveToNext()) {
            Alarm alarm = cursor.getItem();
            if (!alarm.isEnabled()) {
                throw new IllegalStateException(
                        "queryEnabledAlarms() returned alarm(s) that aren't enabled");
            }
            Log.i(TAG, "onHandleIntent: >> controller.scheduleAlarm " + alarm.hour());
            controller.scheduleAlarm(alarm, false);
        }
        cursor.close();

        Log.i("OnBootUpReceiver", "Completed " + SystemClock.elapsedRealtime());

    }


    private void doWork(Context context) {
        AlarmController controller = new AlarmController(context, null);
        // IntentService works in a background thread, so this won't hold us up.
        AlarmCursor cursor = new AlarmsTableManager(context).queryEnabledAlarms();
        while (cursor.moveToNext()) {
            Alarm alarm = cursor.getItem();
            if (!alarm.isEnabled()) {
                throw new IllegalStateException(
                        "queryEnabledAlarms() returned alarm(s) that aren't enabled");
            }
            Log.i(TAG, "onHandleIntent: >> controller.scheduleAlarm " + alarm.hour());
            controller.scheduleAlarm(alarm, false);
        }
        cursor.close();

        Log.i("OnBootUpAlarmScheduler", "Completed service @ " + SystemClock.elapsedRealtime());

    }
}
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, OnBootUpAlarmScheduler.class));
//            //context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
//        } else {
//            context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
//        }

//        Log.i(TAG, "onReceive: OnBootUpReceiver <<");
//
//            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//
//                OnBootUpAlarmScheduler.enqueueWork(context, new Intent());
//
//                //OnBootUpAlarmScheduler.enqueueWork(context,OnBootUpAlarmScheduler.class,OnBootUpAlarmScheduler.JOB_ID,intent);
//                //OnBootUpAlarmScheduler.schedule(context);
//            }
//        }



