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

package our.amazing.clock.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import our.amazing.clock.util.LocalBroadcastHelper;

/**
 * Created by Phillip Hsu on 6/28/2016.
 *
 * Efficiently loads and holds a Cursor.
 */
public abstract class SQLiteCursorLoader<
        T extends ObjectWithId,
        C extends BaseItemCursor<T>>
    extends AsyncTaskLoader<C> {

    private static final String TAG = "SQLiteCursorLoader";

    private C mCursor;
    private OnContentChangeReceiver mOnContentChangeReceiver;

    public SQLiteCursorLoader(Context context) {
        super(context);
    }

    protected abstract C loadCursor();

    /**
     * @return the Intent action that will be registered to this Loader
     * for receiving broadcasts about underlying data changes to our
     * designated database table
     */
    protected abstract String getOnContentChangeAction();

    /* Runs on a worker thread */
    @Override
    public C loadInBackground() {
        C cursor = loadCursor();
        if (cursor != null) {
            // Ensure that the content window is filled
            // Ensure that the data is available in memory once it is
            // passed to the main thread
            cursor.getCount();
        }
        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(C cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        // Close the old cursor because it is no longer needed.
        // Because an existing cursor may be cached and redelivered, it is important
        // to make sure that the old cursor and the new cursor are not the
        // same before the old cursor is closed.
        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    // Refer to the docs if you wish to understand the rest of the API as used below.

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }

        if (mOnContentChangeReceiver == null) {
            mOnContentChangeReceiver = new OnContentChangeReceiver();
            LocalBroadcastHelper.registerReceiver(getContext(),
                    mOnContentChangeReceiver, getOnContentChangeAction());
        }

        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(C cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;

        if (mOnContentChangeReceiver != null) {
            LocalBroadcastHelper.unregisterReceiver(getContext(),
                    mOnContentChangeReceiver);
            mOnContentChangeReceiver = null;
        }
    }

    private final class OnContentChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received content change event");
            onContentChanged();
        }
    }
}
