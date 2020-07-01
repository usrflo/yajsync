/*
 * Copyright (C) 2014 Per Lundqvist
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
package com.github.perlundq.yajsync.filelist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.perlundq.yajsync.util.FileOps;

public class FileInfoTest {

    RsyncFileAttributes _fileAttrs =
        new RsyncFileAttributes(FileOps.S_IFREG | 0644, 0, 0, User.whoami(), Group.whoami());
    RsyncFileAttributes _dirAttrs =
        new RsyncFileAttributes(FileOps.S_IFDIR | 0755, 0, 0, User.whoami(), Group.whoami());
    Path _dotDirPath = Paths.get("./");
    Path dotDirAbsPath = Paths.get("/path/to/module/root/");
    FileInfo _dotDir = new FileInfo(dotDirAbsPath,
                                    _dotDirPath,
                                    _dotDirPath.toString().getBytes(),
                                    _dirAttrs);

    @Test
    public void testSortDotDirWithDotDirEqual()
    {
        assertTrue(_dotDir.equals(_dotDir));
        assertTrue(_dotDir.compareTo(_dotDir) == 0);
    }

    @Test
    public void testSortDotDirBeforeNonDotDir()
    {
        Path p = Paths.get(".a");
        FileInfo f = new FileInfo(_dotDirPath.resolve(p), p,
                                  p.toString().getBytes(), _fileAttrs);
        assertFalse(_dotDir.equals(f));
        assertTrue(_dotDir.compareTo(f) < 0);
    }

    @Test
    public void testSortDotDirBeforeNonDotDir2()
    {
        Path p = Paths.get("...");
        FileInfo f = new FileInfo(_dotDirPath.resolve(p), p,
                                  p.toString().getBytes(), _fileAttrs);
        assertFalse(_dotDir.equals(f));
        assertTrue(_dotDir.compareTo(f) < 0);
    }

    @Test
    public void testSortDotDirBeforeNonDotDir3()
    {
        Path p = Paths.get("...");
        FileInfo f = new FileInfo(_dotDirPath.resolve(p), p,
                                  p.toString().getBytes(), _dirAttrs);
        assertFalse(_dotDir.equals(f));
        assertTrue(_dotDir.compareTo(f) < 0);
    }

    // test empty throws illegalargumentexception
    // test dot is always a directory

    /*
     * ./
     * ...
     * ..../
     * .a
     * a.
     * a..
     * ..a
     * .a.
     *
     * a.
     * a../
     * .a/
     * .a/b
     * b/
     * b/.
     * c
     * cc
     * c.c
     */

    @Test
    public void testSortDotDirBeforeNonDotDir4()
    {
        Path p = Paths.get("a.");
        FileInfo f = new FileInfo(_dotDirPath.resolve(p), p,
                                  "a./".getBytes(), _dirAttrs);
        assertFalse(_dotDir.equals(f));
        assertTrue(_dotDir.compareTo(f) < 0);
    }
}