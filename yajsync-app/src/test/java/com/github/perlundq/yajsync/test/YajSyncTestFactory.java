/*
 * Copyright (C) 2016 Florian Sager
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
package com.github.perlundq.yajsync.test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import com.github.perlundq.yajsync.ui.SyncClient;
import com.github.perlundq.yajsync.ui.YajSyncClient;

public class YajSyncTestFactory implements ClientTestFactory {

    @Override
    public SyncClient newClient() {
        return new YajSyncClient().setStandardOut(_nullOut)
                .setStandardErr(_nullOut);
    }

    @Override
    public ReturnStatus fileCopy(ExecutorService _service, Path src, Path dst, String... args) {
        return fileCopy(true, _service, src, dst, args);
    }

    @Override
    public ReturnStatus fileCopy(boolean startServer, ExecutorService _service,
                                 Path src, Path dst, String... args) {
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

    private final PrintStream _nullOut =
            new PrintStream(new OutputStream() {
                @Override
                public void write(int b) { /* nop */}
            }
    );
}
