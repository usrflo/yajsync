package com.github.perlundq.yajsync.test;

import java.nio.file.Path;

import com.github.perlundq.yajsync.ui.SyncClient;
import com.github.perlundq.yajsync.ui.YajSyncClient;

public class YajSyncSystemTest extends SystemTest {

	@Override
	protected SyncClient newClient()
	{
	        return new YajSyncClient().
	            setStandardOut(_nullOut).
	            setStandardErr(_nullOut);
	}

	@Override
	protected ReturnStatus fileCopy(Path src, Path dst, String ... args) {
		return fileCopy(true, src, dst, args);
	}

	@Override
	protected ReturnStatus fileCopy(boolean startServer, Path src, Path dst, String ... args) {
        SyncClient client = newClient();
        String[] nargs = new String[args.length + 2];
        int i = 0;
        for (String arg : args) {
            nargs[i++] = arg;
        }
        nargs[i++] = src.toString();
        nargs[i++] = dst.toString();
        int rc = client.start(nargs);
        return new ReturnStatus(rc, client.statistics());
    }
}
