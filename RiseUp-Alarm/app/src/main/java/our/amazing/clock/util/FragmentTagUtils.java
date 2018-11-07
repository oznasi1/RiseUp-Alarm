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

package our.amazing.clock.util;

import android.support.annotation.IdRes;

/**
 * Created by Phillip Hsu on 9/7/2016.
 */
public final class FragmentTagUtils {

    /**
     * For general use.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId) {
        return cls.getName() + ":" + viewId;
    }

    /**
     * A version suitable for our ViewHolders.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId, long itemId) {
        return makeTag(cls, viewId) + ":" + itemId;
    }

    private FragmentTagUtils() {}
}
