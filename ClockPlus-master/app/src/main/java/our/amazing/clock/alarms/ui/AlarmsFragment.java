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

package our.amazing.clock.alarms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.data.AlarmCursor;
import our.amazing.clock.alarms.data.AlarmsListCursorLoader;
import our.amazing.clock.alarms.data.AsyncAlarmsTableUpdateHandler;
import our.amazing.clock.alarms.misc.AlarmController;
import our.amazing.clock.dialogs.TimePickerDialogController;
import our.amazing.clock.list.RecyclerViewFragment;
import our.amazing.clock.util.DelayedSnackbarHandler;

import our.amazing.clock.util.FragmentTagUtils;

import static our.amazing.clock.util.FragmentTagUtils.makeTag;

public class AlarmsFragment extends RecyclerViewFragment<Alarm, BaseAlarmViewHolder, AlarmCursor,
        AlarmsCursorAdapter> implements BottomSheetTimePickerDialog.OnTimeSetListener {
    private static final String TAG = "AlarmsFragment";
    private static final String KEY_EXPANDED_POSITION = "expanded_position";
    public static final String EXTRA_SCROLL_TO_ALARM_ID = "our.amazing.clock.alarms.extra.SCROLL_TO_ALARM_ID";

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;
    private View mSnackbarAnchor;
    private TimePickerDialogController mTimePickerDialogController;

    private int mExpandedPosition = RecyclerView.NO_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the value of the last expanded position here.
            // We cannot tell the adapter to expand this item until onLoadFinished()
            // is called.
            mExpandedPosition = savedInstanceState.getInt(KEY_EXPANDED_POSITION, RecyclerView.NO_POSITION);
        }
        mTimePickerDialogController = new TimePickerDialogController(
                getFragmentManager(), getActivity(), this);
        mTimePickerDialogController.tryRestoreCallback(makeTimePickerDialogTag());

//        long scrollToStableId = getActivity().getIntent().getLongExtra(EXTRA_SCROLL_TO_ALARM_ID, -1);
//        if (scrollToStableId != -1) {
//            setScrollToStableId(scrollToStableId);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSnackbarAnchor = getActivity().findViewById(R.id.main_content);
        mAlarmController = new AlarmController(getActivity(), mSnackbarAnchor);
        mAsyncUpdateHandler = new AsyncAlarmsTableUpdateHandler(getActivity(),
                mSnackbarAnchor, this, mAlarmController);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        // Show the pending Snackbar, if any, that was prepared for us
        // by another app component.
        DelayedSnackbarHandler.makeAndShow(mSnackbarAnchor);
    }

    @Override
    public Loader<AlarmCursor> onCreateLoader(int id, Bundle args) {
        return new AlarmsListCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AlarmCursor> loader, AlarmCursor data) {
        super.onLoadFinished(loader, data);
        Log.d(TAG, "onLoadFinished()");
        // TODO: If this was a content change due to an update, verify that
        // we scroll to the updated alarm if its sort order changes.

        // Does nothing If there is no expanded position.
        getAdapter().expand(mExpandedPosition);
        // We shouldn't continue to keep a reference to this, so clear it.
        mExpandedPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public void onFabClick() {
        mTimePickerDialogController.show(0, 0, makeTimePickerDialogTag());
    }

    @Override
    protected AlarmsCursorAdapter onCreateAdapter() {
        return new AlarmsCursorAdapter(this, mAlarmController);
    }

    @Override
    protected int emptyMessage() {
        return R.string.empty_alarms_container;
    }

    @Override
    protected int emptyIcon() {
        return R.drawable.ic_alarm_96dp;
    }

    @Override
    public void onListItemClick(Alarm item, int position) {
        boolean expanded = getAdapter().expand(position);
        if (!expanded) {
            getAdapter().collapse(position);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: Just like with TimersCursorAdapter, we could pass in the mAsyncUpdateHandler
    // to the AlarmsCursorAdapter and call these on the save and delete button click bindings.

    @Override
    public void onListItemDeleted(final Alarm item) {
        // The corresponding VH will be automatically removed from view following
        // the requery, so we don't have to do anything to it.
        mAsyncUpdateHandler.asyncDelete(item);
    }

    @Override
    public void onListItemUpdate(Alarm item, int position) {
        // Once we update the relevant row in the db table, the VH will still
        // be in view. While the requery will probably update the values displayed
        // by the VH, the VH remains in its expanded state from before we were
        // called. Tell the adapter reset its expanded position.
        mAsyncUpdateHandler.asyncUpdate(item.getId(), item);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onScrolledToStableId(long id, int position) {
        boolean expanded = getAdapter().expand(position);
        if (!expanded) {
            // Otherwise, it was due to an item update. The VH is expanded
            // at this point, so reset it.
            getAdapter().collapse(position);
        }
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        // When we request the Builder, default values are provided for us,
        // which is why we don't have to set the ringtone, label, etc.
        Alarm alarm = Alarm.builder()
                .hour(hourOfDay)
                .minutes(minute)
                .build();
        alarm.setEnabled(true);
        mAsyncUpdateHandler.asyncInsert(alarm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
         * From Fragment#onSaveInstanceState():
         *   - This is called "at any time before onDestroy()".
         *   - "This corresponds to Activity.onSaveInstanceState(Bundle) and most of the discussion
         *     there applies here as well".
         * From Activity#onSaveInstanceState():
         *   - "If called, this method will occur before {@link #onStop}
         *     [which follows onPause() in the lifecycle].  There are
         *     no guarantees about whether it will occur before or after {@link #onPause}."
         *
         * isResumed() is true "for the duration of onResume() and onPause()".
         * From the results of a few trials, this never seemed to call through, so i'm assuming
         * isResumed() returned false every time.
         */
        if (/*isResumed() && */getAdapter() != null) {
            // Normally when we scroll far enough away from this Fragment, *its view* will be
            // destroyed, i.e. the maximum point in its lifecycle is onDestroyView(). However,
            // if the configuration changes, onDestroy() is called through, and then this Fragment
            // and all of its members will be destroyed. This is not
            // a problem if the page in which the configuration changed is this page, because
            // the Fragment will be recreated from onCreate() to onResume(), and any
            // member initialization between those points occurs as usual.
            //
            // However, when the page in which the configuration changed
            // is far enough away from this Fragment, there IS a problem. The Fragment
            // *at that page* is recreated, but this Fragment will NOT be; the ViewPager's
            // adapter will not reinstantiate this Fragment because it exceeds the
            // offscreen page limit relative to the initial page in the new configuration.
            //
            // As such, we should only save state if this Fragment's members (i.e. its RecyclerView.Adapter)
            // are not destroyed
            // because that indicates the Fragment is both registered in the adapter AND is within the offscreen
            // page limit, so its members have been initialized (recall that a Fragment in a ViewPager
            // does not actually need to be visible to the user for onCreateView() to onResume() to
            // be called through).
            outState.putInt(KEY_EXPANDED_POSITION, getAdapter().getExpandedPosition());
        }
    }

    private static String makeTimePickerDialogTag() {
        return FragmentTagUtils.makeTag(AlarmsFragment.class, R.id.fab);
    }
}
