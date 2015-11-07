package com.github.perlundq.yajsync.filelist;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

import com.github.perlundq.yajsync.util.Environment;

public final class Group extends AbstractPrincipal
{
	private static final Group ROOT = new Group("root", 0);
    private static final Group NOBODY = new Group("nobody", ID_NOBODY);
    private static final Group JVM_USER = new Group(Environment.getGroupName(),
                                                  Environment.getGroupId());

    public Group(String name, int gid)
    {
    	super(name, gid);
    }

    public static Group whoami()
    {
        return JVM_USER;
    }

    public static Group root()
    {
        return ROOT;
    }

    public static Group nobody()
    {
        return NOBODY;
    }

    public GroupPrincipal groupPrincipal() throws IOException
    {
        UserPrincipalLookupService lookupService =
        		FileSystems.getDefault().getUserPrincipalLookupService();
        return lookupService.lookupPrincipalByGroupName(_name);
    }
}
