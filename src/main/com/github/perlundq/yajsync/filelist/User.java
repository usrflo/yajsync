package com.github.perlundq.yajsync.filelist;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

import com.github.perlundq.yajsync.util.Environment;

public final class User extends AbstractPrincipal
{
	private static final User ROOT = new User("root", 0);
    private static final User NOBODY = new User("nobody", ID_NOBODY);
    private static final User JVM_USER = new User(Environment.getUserName(),
                                                  Environment.getUserId());

    public User(String name, int uid)
    {
    	super(name, uid);
    }

    public static User whoami()
    {
        return JVM_USER;
    }

    public static User root()
    {
        return ROOT;
    }

    public static User nobody()
    {
        return NOBODY;
    }

    public UserPrincipal userPrincipal() throws IOException
    {
    	UserPrincipalLookupService lookupService =
            FileSystems.getDefault().getUserPrincipalLookupService();
        return lookupService.lookupPrincipalByName(_name);
    }
}
