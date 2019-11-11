package com.smallcake.bluetooth.debug;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainPageAbout extends Fragment {
    private View rootView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = CreatePageView(inflater);
        }

        return rootView;
    }

    private View CreatePageView(@NonNull LayoutInflater inflater){
        View PageView = inflater.inflate(R.layout.fragment_main_page_about,null);
        return PageView;
    }

    @Override
    public void onDestroyView() {
        rootView = null;
        super.onDestroyView();
    }
}
