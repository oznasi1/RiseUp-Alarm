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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import our.amazing.clock.util.LocalBroadcastHelper;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public abstract class DatabaseTableManager<T extends ObjectWithId> {
    // TODO: Consider implementing BaseColumns for your table schemas.
    // This column should be present in all table schemas, and the value is simple enough
    // we can reproduce it here instead of relying on our subclasses to retrieve it from
    // their designated table schema.
    private static final String COLUMN_ID = "_id";

    private final SQLiteOpenHelper mDbHelper;
    private final Context mAppContext;

    public DatabaseTableManager(Context context) {
        // Internally uses the app context
        mDbHelper = ClockAppDatabaseHelper.getInstance(context);
        mAppContext = context.getApplicationContext();
    }

    /**
     * @return the table managed by this helper
     */
    protected abstract String getTableName();

    /**
     * @return the ContentValues representing the item's properties.
     * You do not need to put a mapping for {@link #COLUMN_ID}, since
     * the database manages ids for us (if you created your table
     * with {@link #COLUMN_ID} as an {@code INTEGER PRIMARY KEY}).
     */
    protected abstract ContentValues toContentValues(T item);

    /**
     * @return the Intent action that will be used to send broadcasts
     * to our designated {@link SQLiteCursorLoader} whenever an
     * underlying change to our data is detected. The Loader should
     * receive the broadcast and reload its data.
     */
    protected abstract String getOnContentChangeAction();

    /**
     * @return optional String specifying the sort order
     * to use when querying the database. The default
     * implementation returns null, which may return
     * queries unordered.
     */
    protected String getQuerySortOrder() {
        return null;
    }

    public long insertItem(T item) {
        long id = mDbHelper.getWritableDatabase().insert(
                getTableName(), null, toContentValues(item));
        item.setId(id);
        notifyContentChanged();
        return id;
    }

    public int updateItem(long id, T newItem) {
        newItem.setId(id);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(getTableName(),
                toContentValues(newItem),
                COLUMN_ID + " = " + id,
                null);
//        if (rowsUpdated == 0) {
//            throw new IllegalStateException("wtf?");
//        }
        notifyContentChanged();
        return rowsUpdated;
    }

    public int deleteItem(T item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(getTableName(),
                COLUMN_ID + " = " + item.getId(),
                null);
        notifyContentChanged();
        return rowsDeleted;
    }

    public Cursor queryItem(long id) {
        Cursor c = queryItems(COLUMN_ID + " = " + id, "1");
        // Since the query returns at most one row, move the cursor to that row.
        // Most callers of this method will not know they have to move the cursor.
        // How come we don't need to do this for queries that can potentially return
        // multiple rows? Because those returned cursors will almost always be
        // displayed by a BaseCursorAdapter, which moves cursors to the appropriate
        // positions as it binds VHs.
        c.moveToFirst();
        return c;
    }

    public Cursor queryItems() {
        // Select all rows and columns
        return queryItems(null, null);
    }

    protected Cursor queryItems(String where, String limit) {
        return mDbHelper.getReadableDatabase().query(getTableName(),
                null, // All columns
                where, // Selection, i.e. where COLUMN_* = [value we're looking for]
                null, // selection args, none b/c id already specified in selection
                null, // group by
                null, // having
                getQuerySortOrder(), // order/sort by
                limit); // limit
    }

    /**
     * Deletes all rows in this table.
     */
    public final void clear() {
        mDbHelper.getWritableDatabase().delete(getTableName(), null/*all rows*/, null);
        notifyContentChanged();
    }

    private void notifyContentChanged() {
        LocalBroadcastHelper.sendBroadcast(mAppContext, getOnContentChangeAction());
    }
}
