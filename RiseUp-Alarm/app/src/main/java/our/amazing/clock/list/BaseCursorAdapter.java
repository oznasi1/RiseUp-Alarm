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

package our.amazing.clock.list;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import our.amazing.clock.data.BaseItemCursor;
import our.amazing.clock.data.ObjectWithId;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public abstract class BaseCursorAdapter<
        T extends ObjectWithId,
        VH extends BaseViewHolder<T>,
        C extends BaseItemCursor<T>>
    extends RecyclerView.Adapter<VH> {

    private static final String TAG = "BaseCursorAdapter";

    private final OnListItemInteractionListener<T> mListener;
    private C mCursor;

    protected abstract VH onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<T> listener, int viewType);

    public BaseCursorAdapter(OnListItemInteractionListener<T> listener) {
        mListener = listener;
        // Excerpt from docs of notifyDataSetChanged():
        // "RecyclerView will attempt to synthesize [artificially create?]
        // visible structural change events [when items are inserted, removed or
        // moved] for adapters that report that they have stable IDs when
        // [notifyDataSetChanged()] is used. This can help for the purposes of
        // animation and visual object persistence [?] but individual item views
        // will still need to be rebound and relaid out."
        setHasStableIds(true);
    }

    /**
     * not final to allow subclasses to use the viewType if needed
     */
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(parent, mListener, viewType);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            Log.e(TAG, "Failed to bind item at position " + position);
            return;
        }
        holder.onBind(mCursor.getItem());
    }

    @Override
    public final int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public final long getItemId(int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return super.getItemId(position); // -1
        }
        return mCursor.getId();
    }

    public final void swapCursor(C cursor) {
        if (mCursor == cursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
