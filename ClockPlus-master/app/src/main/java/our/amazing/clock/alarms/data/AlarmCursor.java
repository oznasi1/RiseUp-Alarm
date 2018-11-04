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

package our.amazing.clock.alarms.data;

import android.database.Cursor;

import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.data.BaseItemCursor;

import static our.amazing.clock.alarms.misc.DaysOfWeek.FRIDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.MONDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.SATURDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.SUNDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.THURSDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.TUESDAY;
import static our.amazing.clock.alarms.misc.DaysOfWeek.WEDNESDAY;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
// An alternative method to creating an Alarm from a cursor is to
// make an Alarm constructor that takes an Cursor param. However,
// this method has the advantage of keeping the contents of
// the Alarm class as pure Java, which can facilitate unit testing
// because it has no dependence on Cursor, which is part of the Android SDK.
public class AlarmCursor extends BaseItemCursor<Alarm> {
    private static final String TAG = "AlarmCursor";

    public AlarmCursor(Cursor c) {
        super(c);
    }

    /**
     * @return an Alarm instance configured for the current row,
     * or null if the current row is invalid
     */
    @Override
    public Alarm getItem() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        Alarm alarm = Alarm.builder()
                .hour(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_HOUR)))
                .minutes(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_MINUTES)))
                .vibrates(isTrue(AlarmsTable.COLUMN_VIBRATES))
                .ringtone(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_RINGTONE)))
                .label(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_LABEL)))
                .build();
        alarm.setId(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_ID)));
        alarm.setEnabled(isTrue(AlarmsTable.COLUMN_ENABLED));
        alarm.setSnoozing(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS)));
        alarm.setRecurring(SUNDAY, isTrue(AlarmsTable.COLUMN_SUNDAY));
        alarm.setRecurring(MONDAY, isTrue(AlarmsTable.COLUMN_MONDAY));
        alarm.setRecurring(TUESDAY, isTrue(AlarmsTable.COLUMN_TUESDAY));
        alarm.setRecurring(WEDNESDAY, isTrue(AlarmsTable.COLUMN_WEDNESDAY));
        alarm.setRecurring(THURSDAY, isTrue(AlarmsTable.COLUMN_THURSDAY));
        alarm.setRecurring(FRIDAY, isTrue(AlarmsTable.COLUMN_FRIDAY));
        alarm.setRecurring(SATURDAY, isTrue(AlarmsTable.COLUMN_SATURDAY));
        alarm.ignoreUpcomingRingTime(isTrue(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME));
        return alarm;
    }
}
