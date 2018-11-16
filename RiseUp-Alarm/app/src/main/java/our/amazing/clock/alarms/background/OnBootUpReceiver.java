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
import android.util.Log;

import static com.crashlytics.android.beta.Beta.TAG;

public class OnBootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Note that this will be called when the device boots up, not when the app first launches.
        // We may have a lot of alarms to reschedule, so do this in the background using an IntentService.
        //context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
//        Log.i(TAG, "onReceive: OnBootUpReceiver >>");
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            context.startForegroundService(new Intent(context, OnBootUpAlarmScheduler.class));
//        }
//        else{
//            context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
//        }
//        Log.i(TAG, "onReceive: OnBootUpReceiver <<");

        context.startService(new Intent(context, OnBootUpAlarmScheduler.class));


    }
}
