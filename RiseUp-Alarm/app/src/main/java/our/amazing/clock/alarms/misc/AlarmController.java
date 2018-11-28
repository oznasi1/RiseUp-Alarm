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

package our.amazing.clock.alarms.misc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import our.amazing.clock.MainActivity;
import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.background.PendingAlarmScheduler;
import our.amazing.clock.alarms.background.UpcomingAlarmReceiver;
import our.amazing.clock.alarms.data.AlarmsTableManager;
import our.amazing.clock.ringtone.PlayAlarmActivity;
import our.amazing.clock.ringtone.AlarmActivity;
import our.amazing.clock.ringtone.playback.AlarmRingtoneService;
import our.amazing.clock.util.ContentIntentUtils;
import our.amazing.clock.util.DelayedSnackbarHandler;
import our.amazing.clock.util.DurationUtils;
import our.amazing.clock.util.ParcelableUtil;

import our.amazing.clock.util.TimeFormatUtils;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.getActivity;
import static our.amazing.clock.util.TimeFormatUtils.formatTime;

/**
 * Created by Phillip Hsu on 7/10/2016.
 *
 * API to control alarm states and update the UI.
 * TODO: Rename to AlarmStateHandler? AlarmStateController?
 */
public final class AlarmController {
    private static final String TAG = "AlarmController";

    private final Context mAppContext;
    private final View mSnackbarAnchor;
    // TODO: Why aren't we using AsyncAlarmsTableUpdateHandler?
    private final AlarmsTableManager mTableManager;

    /**
     *
     * @param context the Context from which the application context will be requested
     * @param snackbarAnchor an optional anchor for a Snackbar to anchor to
     */
    public AlarmController(Context context, View snackbarAnchor) {
        mAppContext = context.getApplicationContext();
        mSnackbarAnchor = snackbarAnchor;
        mTableManager = new AlarmsTableManager(context);
    }

    /**
     * Schedules the alarm with the {@link AlarmManager}.
     * If {@code alarm.}{@link Alarm#isEnabled() isEnabled()}
     * returns false, this does nothing and returns immediately.
     * 
     * If there is already an alarm for this Intent scheduled (with the equality of two
     * intents being defined by filterEquals(Intent)), then it will be removed and replaced
     * by this one. For most of our uses, the relevant criteria for equality will be the
     * action, the data, and the class (component). Although not documented, the request code
     * of a PendingIntent is also considered to determine equality of two intents.
     */
    public void scheduleAlarm(Alarm alarm, boolean showSnackbar) {
        Log.i(TAG, "scheduleAlarm: " +Boolean.toString(alarm.isEnabled()) );
        if (!alarm.isEnabled()) {
            return;
        }
        // Does nothing if it's not posted. This is primarily here for when alarms
        // are updated, instead of newly created, so that we don't leave behind
        // stray upcoming alarm notifications. This occurs e.g. when a single-use
        // alarm is updated to recur on a weekday later than the current day.
        removeUpcomingAlarmNotification(alarm);


        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        final long ringAt = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
        final PendingIntent alarmIntent = alarmIntent(alarm, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent showIntent = ContentIntentUtils.create(mAppContext, MainActivity.PAGE_ALARMS, alarm.getId());

            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(ringAt, showIntent);

            am.setAlarmClock(info, alarmIntent);
        } else {
            // WAKEUP alarm types wake the CPU up, but NOT the screen;
            // you would handle that yourself by using a wakelock, etc..
            am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent);
            // Show alarm in the status bar
            Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
            alarmChanged.putExtra("alarmSet", true/*enabled*/);
            mAppContext.sendBroadcast(alarmChanged);
        }
//
//        final int hoursToNotifyInAdvance = AlarmPreferences.hoursBeforeUpcoming(mAppContext);
//        if (hoursToNotifyInAdvance > 0 || alarm.isSnoozed()) {
//            // If snoozed, upcoming note posted immediately.
//            long upcomingAt = ringAt - HOURS.toMillis(hoursToNotifyInAdvance);
//            // We use a WAKEUP alarm to send the upcoming alarm notification so it goes off even if the
//            // device is asleep. Otherwise, it will not go off until the device is turned back on.
//            am.set(AlarmManager.RTC_WAKEUP, upcomingAt, notifyUpcomingAlarmIntent(alarm, false));
//        }

        if (showSnackbar) {
            String message = mAppContext.getString(R.string.alarm_set_for,
                    DurationUtils.toString(mAppContext, alarm.ringsIn(), false/*abbreviate*/));
            //showSnackbar(message);
        }
    }

    /**
     * Cancel the alarm. This does NOT check if you previously scheduled the alarm.
     * @param rescheduleIfRecurring True if the alarm should be rescheduled after cancelling.
     *                              This param will only be considered if the alarm has recurrence
     *                              and is enabled.
     */
    public void cancelAlarm(Alarm alarm, boolean showSnackbar, boolean rescheduleIfRecurring) {

        Log.d(TAG, "Cancelling alarm " + alarm);
//        Intent i = new Intent(mAppContext,YouTubePlayer.class);
//        mAppContext.startActivity(i);
        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = alarmIntent(alarm, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Remove alarm in the status bar
                Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
                alarmChanged.putExtra("alarmSet", false/*enabled*/);
                mAppContext.sendBroadcast(alarmChanged);
            }
        }

//        pi = notifyUpcomingAlarmIntent(alarm, true);
//        if (pi != null) {
//            am.cancel(pi);
//            pi.cancel();
//        }

        // Does nothing if it's not posted.
        removeUpcomingAlarmNotification(alarm);

        final int hoursToNotifyInAdvance = AlarmPreferences.hoursBeforeUpcoming(mAppContext);
        // ------------------------------------------------------------------------------------
        // TOneverDO: Place block after making value changes to the alarm.
        if ((hoursToNotifyInAdvance > 0 && showSnackbar
                // TODO: Consider showing the snackbar for non-upcoming alarms too;
                // then, we can remove these checks.
                && alarm.ringsWithinHours(hoursToNotifyInAdvance)) || alarm.isSnoozed()) {
            long time = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
            String msg = mAppContext.getString(R.string.upcoming_alarm_dismissed,
                    TimeFormatUtils.formatTime(mAppContext, time));
            showSnackbar(msg);
        }
        // ------------------------------------------------------------------------------------

        if (alarm.isSnoozed()) {
            alarm.stopSnoozing();
        }

        if (!alarm.hasRecurrence()) {
            alarm.setEnabled(false);
        } else if (alarm.isEnabled() && rescheduleIfRecurring) {
            if (alarm.ringsWithinHours(hoursToNotifyInAdvance)) {
                // Still upcoming today, so wait until the normal ring time
                // passes before rescheduling the alarm.
                alarm.ignoreUpcomingRingTime(true); // Useful only for VH binding
                Intent intent = new Intent(mAppContext, PendingAlarmScheduler.class)
                        .putExtra(PendingAlarmScheduler.EXTRA_ALARM_ID, alarm.getId());
                pi = PendingIntent.getBroadcast(mAppContext, alarm.getIntId(),
                        intent, FLAG_CANCEL_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, alarm.ringsAt(), pi);
            } else {
                scheduleAlarm(alarm, false);
            }
        }

        save(alarm);

        // If service is not running, nothing happens
        mAppContext.stopService(new Intent(mAppContext, AlarmRingtoneService.class));
    }

    public void snoozeAlarm(Alarm alarm) {

        int minutesToSnooze = AlarmPreferences.snoozeDuration(mAppContext);
        alarm.snooze(minutesToSnooze);

        scheduleAlarm(alarm, false);
        String message = mAppContext.getString(R.string.title_snoozing_until,
                TimeFormatUtils.formatTime(mAppContext, alarm.snoozingUntil()));
        // Since snoozing is always done by an app component away from
        // the list screen, the Snackbar will never be shown. In fact, this
        // controller has a null mSnackbarAnchor if we're using it for snoozing
        // an alarm. We solve this by preparing the message, and waiting until
        // the list screen is resumed so that it can display the Snackbar for us.
        DelayedSnackbarHandler.prepareMessage(message);
        save(alarm);
    }

    public void removeUpcomingAlarmNotification(Alarm a) {
//        Intent intent = new Intent(mAppContext, UpcomingAlarmReceiver.class)
//                .setAction(UpcomingAlarmReceiver.ACTION_CANCEL_NOTIFICATION)
//                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, ParcelableUtil.marshall(a));
//        mAppContext.sendBroadcast(intent);
    }

    public void save(final Alarm alarm) {
        // TODO: Will using the Runnable like this cause a memory leak?
        mTableManager.updateItem(alarm.getId(), alarm);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mTableManager.updateItem(alarm.getId(), alarm);
//            }
//        }).start();
    }

    private PendingIntent alarmIntent(Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(mAppContext, PlayAlarmActivity.class)
                .putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, ParcelableUtil.marshall(alarm));

        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason. Thus, we don't checkNotNull().
        return getActivity(mAppContext, alarm.getIntId(), intent, flag);
    }

    private PendingIntent notifyUpcomingAlarmIntent(Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(mAppContext, UpcomingAlarmReceiver.class)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, ParcelableUtil.marshall(alarm));
        if (alarm.isSnoozed()) {
            intent.setAction(UpcomingAlarmReceiver.ACTION_SHOW_SNOOZING);
        }
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason. Thus, we don't checkNotNull().
        return PendingIntent.getBroadcast(mAppContext, alarm.getIntId(), intent, flag);
    }

    private void showSnackbar(final String message) {
        // Is the window containing this anchor currently focused?
//        Log.d(TAG, "Anchor has window focus? " + mSnackbarAnchor.hasWindowFocus());
        if (mSnackbarAnchor != null /*&& mSnackbarAnchor.hasWindowFocus()*/) {
            // Queue the message on the view's message loop, so the message
            // gets processed once the view gets attached to the window.
            // This executes on the UI thread, just like not queueing it will,
            // but the difference here is we wait for the view to be attached
            // to the window (if not already) before executing the runnable code.
            mSnackbarAnchor.post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mSnackbarAnchor, message, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }
}
