package com.github.perlundq.yajsync.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.perlundq.yajsync.session.SessionConfig;
import com.github.perlundq.yajsync.session.Statistics;
import com.github.perlundq.yajsync.util.Environment;
import com.github.perlundq.yajsync.util.ProcessRunner;
import com.github.perlundq.yajsync.util.ProcessRunner.Result;

public class RsyncClient implements SyncClient {

	private final String whichCommand = "which";
	private final String rsyncCommand = "rsync";
	private String rsyncBinary;

	private Statistics statistics;

	public RsyncClient() {
		if (!Environment.IS_POSIX_FS) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName()+" test cannot be executed on this system");
		}

		try {
			Result whichRsync = ProcessRunner.exec(Arrays.asList(whichCommand, rsyncCommand));
			if (whichRsync.EXITCODE==0) {
				rsyncBinary = whichRsync.STDOUT.trim();
			} else {
				throw new UnsupportedOperationException("no rsync binary found on this system");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int start(String[] args) {

		this.statistics = new Statistics();

		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(rsyncBinary, "--protocol="+SessionConfig.VERSION.major(), "--stats"));
		// command.addAll(Arrays.asList(rsyncBinary, "--protocol="+SessionConfig.VERSION.major()));
		command.addAll(Arrays.asList(args));

		try {
			Result rsyncResult = ProcessRunner.exec(command);
			if (rsyncResult.EXITCODE==0) {
				this.statistics.setFileListBuildTime(parseStatTime("File list generation time: ", rsyncResult.STDOUT));
				this.statistics.setFileListTransferTime(parseStatTime("File list transfer time: ", rsyncResult.STDOUT));
				this.statistics.setNumFiles(parseStatInt("Number of files: ", rsyncResult.STDOUT));
				this.statistics.setNumTransferredFiles(parseStatInt("Number of regular files transferred: ", rsyncResult.STDOUT));
				this.statistics.setTotalFileListSize(parseStatInt("File list size: ", rsyncResult.STDOUT));		// File list size:
				this.statistics.setTotalFileSize(parseStatInt("Total file size: ", rsyncResult.STDOUT));				// Total file size:
				this.statistics.setTotalLiteralSize(parseStatInt("Literal data: ", rsyncResult.STDOUT));			// Literal data:
				this.statistics.setTotalMatchedSize(parseStatInt("Matched data: ", rsyncResult.STDOUT));			// Matched data:
				this.statistics.setTotalRead(parseStatInt("Total bytes received: ", rsyncResult.STDOUT));						// Total bytes received:
				this.statistics.setTotalTransferredSize(parseStatInt("Total transferred file size: ", rsyncResult.STDOUT));	// Total transferred file size:
				this.statistics.setTotalWritten(parseStatInt("Total bytes sent: ", rsyncResult.STDOUT));					// Total bytes sent:
			}

			return rsyncResult.EXITCODE;

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	Number of files: 1 (reg: 1)
	Number of created files: 1 (reg: 1)
	Number of regular files transferred: 1
	Total file size: 512 bytes
	Total transferred file size: 512 bytes
	Literal data: 512 bytes
	Matched data: 0 bytes
	File list size: 0
	File list generation time: 0.001 seconds
	File list transfer time: 0.000 seconds
	Total bytes sent: 619
	Total bytes received: 38
	 */

	private int parseStatInt(String prefix, String s) {
		String value = parseStatString(prefix, s);
		if (value==null) {
			return -1;
		}
		return Integer.parseInt(value.replaceAll("[\\D]", ""));
	}

	private long parseStatTime(String prefix, String s) {
		String value = parseStatString(prefix, s);
		if (value==null) {
			return -1;
		}
		return (long) (Double.parseDouble(value)*1000d);
	}

	private String parseStatString(String prefix, String s) {
		int beginIndex = s.indexOf(prefix);
		if (beginIndex>0) {
			int endIndex = s.indexOf("\n", beginIndex+prefix.length());
			if (endIndex<0) {
				endIndex = s.length()-1;
			}
			int spaceIndex = s.indexOf(" ", beginIndex+prefix.length());
			if (spaceIndex>0 && spaceIndex<endIndex) {
				endIndex = spaceIndex;
			}
			return s.substring(beginIndex+prefix.length(), endIndex);
		}

		return null;
	}

	@Override
	public Statistics statistics() {
		return statistics;
	}

}
