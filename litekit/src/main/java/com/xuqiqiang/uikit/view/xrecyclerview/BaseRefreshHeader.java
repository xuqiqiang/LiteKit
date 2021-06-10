package com.xuqiqiang.uikit.view.xrecyclerview;

public interface BaseRefreshHeader {

	int STATE_NORMAL = 0;
	int STATE_RELEASE_TO_REFRESH = 1;
	int STATE_REFRESHING = 2;
	int STATE_DONE = 3;
	int STATE_ERROR = 4;

	void setState(int state);

	int getState();

	int getVisibleHeight();

	void onMove(float delta);

	boolean releaseAction();

	void refreshComplete();

	void destroy();
}