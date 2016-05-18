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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import com.github.perlundq.yajsync.session.Module;
import com.github.perlundq.yajsync.session.Modules;
import com.github.perlundq.yajsync.ui.NativeRsyncClient;
import com.github.perlundq.yajsync.ui.SyncClient;
import com.github.perlundq.yajsync.ui.YajSyncServer;

public class NativeRsyncTestFactory implements ClientTestFactory {

    Path _modulePath;
    String _moduleName;

    @Override
    public SyncClient newClient() {
        return new NativeRsyncClient();
    }

    @Override
    public ReturnStatus fileCopy(ExecutorService _service, Path src, Path dst, String... args) {
        return fileCopy(true, _service, src, dst, args);
    }

    @Override
    public ReturnStatus fileCopy(boolean startServer, ExecutorService _service,
                                 Path src, Path dst, String... args) {

        _moduleName = "testmodule";
        String dstPath;

        if (Files.isDirectory(dst)) {

            _modulePath = dst;
            dstPath = "/";

        } else {

            _modulePath = dst.getParent();
            dstPath = "/" + dst.getFileName();
        }

        if (startServer) {

            final CountDownLatch isListeningLatch = new CountDownLatch(1);

            Callable<Integer> serverTask = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    try {
                        Module m = new SimpleModule(
                                NativeRsyncTestFactory.this._moduleName,
                                NativeRsyncTestFactory.this._modulePath, "a test module",
                                true, true);
                        int rc = newServer(new TestModules(m))
                                .setIsListeningLatch(isListeningLatch)
                                .start(new String[] { "--port=14415" });
                        return rc;
                    } catch (Exception e) {
                        throw e;
                    }
                }
            };

            _service.submit(serverTask);
            try {
                isListeningLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        SyncClient client = newClient();
        String[] nargs = new String[args.length + 3];
        int i = 0;
        for (String arg : args) {
            nargs[i++] = arg;
        }
        nargs[i++] = "--port=14415";
        nargs[i++] = src.toString();
        nargs[i++] = "localhost::" + _moduleName + dstPath;
        int rc = client.start(nargs);

        return new ReturnStatus(rc, client.statistics());
    }

    private YajSyncServer newServer(Modules modules)
    {
        YajSyncServer server = new YajSyncServer().setStandardOut(_nullOut).
                                                   setStandardErr(_nullOut);
        server.setModuleProvider(new TestModuleProvider(modules));
        return server;
    }

    private final PrintStream _nullOut =
            new PrintStream(new OutputStream() {
                @Override
                public void write(int b) { /* nop */}
            }
    );
}
