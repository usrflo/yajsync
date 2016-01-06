/*
 * Rsync filter rules
 *
 * Copyright (C) 2013, 2014 Per Lundqvist
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.perlundq.yajsync.util;

public class StatusResult<T>
{
    private final boolean _isOK;
    private final T _value;

    public StatusResult(boolean isOK, T value)
    {
        _isOK = isOK;
        _value = value;
    }

    public boolean isOK()
    {
        return _isOK;
    }

    public T value()
    {
        return _value;
    }
}