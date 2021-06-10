package com.xuqiqiang.uikit.view.xrecyclerview;

interface BaseLoadingFooter {

    int STATE_LOADING = 0;
    int STATE_COMPLETE = 1;
    int STATE_NOMORE = 2;

    void setState(int state);

    int getState();

    void destroy();
}