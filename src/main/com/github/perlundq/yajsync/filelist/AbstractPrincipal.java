package com.github.perlundq.yajsync.filelist;

import java.util.Objects;

public abstract class AbstractPrincipal {

	public static final int ID_MAX = 65535;

    protected static final int ID_NOBODY = ID_MAX - 1;
    protected static final int MAX_NAME_LENGTH = 255;

    protected final String _name;
    protected final int _id;

    public AbstractPrincipal(String name, int id)
    {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException();
        }
        if (id < 0 || id > ID_MAX) {
            throw new IllegalArgumentException();
        }
        _name = name;
        _id = id;
    }

    @Override
    public String toString()
    {
        return String.format("%s (%s, %d)",
                             getClass().getSimpleName(), _name, _id);
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) {
            return true;
        } else if (other != null && getClass() == other.getClass()) {
        	AbstractPrincipal otherUser = (AbstractPrincipal) other;
            return _id == otherUser._id && _name.equals(otherUser._name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_name, _id);
    }

    public String name()
    {
        return _name;
    }

    public int id()
    {
        return _id;
    }
}
