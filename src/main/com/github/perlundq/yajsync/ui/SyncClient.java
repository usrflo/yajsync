package com.github.perlundq.yajsync.ui;

import com.github.perlundq.yajsync.session.Statistics;

public interface SyncClient {

	public int start(String[] args);

	public Statistics statistics();
}
