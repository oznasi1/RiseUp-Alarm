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

package our.amazing.clock.ringtone.playback;

import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.misc.AlarmController;
import our.amazing.clock.alarms.misc.AlarmPreferences;

import our.amazing.clock.util.TimeFormatUtils;

import static our.amazing.clock.util.TimeFormatUtils.formatTime;

public class AlarmRingtoneService extends RingtoneService<Alarm> {
    private static final String TAG = "AlarmRingtoneService";
    /* TOneverDO: not private */
    private static final String ACTION_SNOOZE = "our.amazing.clock.ringtone.action.SNOOZE";
    private static final String ACTION_DISMISS = "our.amazing.clock.ringtone.action.DISMISS";

    private AlarmController mAlarmController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We can have this before super because this will only call through
        // WHILE this Service has already been alive.
        if (intent.getAction() != null) {
            if (ACTION_SNOOZE.equals(intent.getAction())) {
                mAlarmController.snoozeAlarm(getRingingObject());
            } else if (ACTION_DISMISS.equals(intent.getAction())) {
                mAlarmController.cancelAlarm(getRingingObject(), false, true); // TODO do we really need to cancel the intent and alarm?
            } else {
                throw new UnsupportedOperationException();
            }
            // ==========================================================================
            stopSelf(startId);
            finishActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmController = new AlarmController(this, null);
    }

    @Override
    protected void onAutoSilenced() {
        // TODO do we really need to cancel the alarm and intent?
        mAlarmController.cancelAlarm(getRingingObject(), false, true);
    }

    @Override
    public  Uri getRingtoneUri() {
        String ringtone = getRingingObject().ringtone();
        // can't be null...
        if (ringtone.isEmpty()) {
            return Settings.System.DEFAULT_ALARM_ALERT_URI;
        }
        return Uri.parse(ringtone);
    }

    @Override
    protected Notification getForegroundNotification() {
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

    @Override
    protected boolean doesVibrate() {
        return getRingingObject().vibrates();
    }

    @Override
    protected int minutesToAutoSilence() {
        return AlarmPreferences.minutesToSilenceAfter(this);
    }

    @Override
    protected Parcelable.Creator<Alarm> getParcelableCreator() {
        return Alarm.CREATOR;
    }
}
