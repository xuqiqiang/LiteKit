package com.google.android.material.appbar;

public class AppBarLayout {

    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    }

    public interface OnOffsetChangedListener {
        void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
    }
}