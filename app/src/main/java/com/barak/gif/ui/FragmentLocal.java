package com.barak.gif.ui;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barak.gif.R;
import com.barak.gif.adapter.RecyclerViewAdapter;
import com.barak.gif.app.AppUtility;
import com.barak.gif.model.Gif;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;


public class FragmentLocal extends Fragment implements ActionInterface {
    public static final String TAG = "FragmentLocal";
    private static final String LOGTAG = "FragmentLocal";
    private static final String FRAGTYPE = "FRAGTYPE";
    RecyclerView recyclerView;

    private ArrayList<Gif> mGif = new ArrayList<>();
    ;
    private RecyclerViewAdapter adapter;

    private View errorTextView;
    private long mTimeStamp;


    public static FragmentLocal newInstance() {
        return new FragmentLocal();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment, container, false);
        setRetainInstance(true);

        return rootView;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorTextView = view.findViewById(R.id.text_e);
        view.findViewById(R.id.edit_text).setVisibility(View.GONE);
        SwipeRefreshLayout mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setEnabled(false);
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new RecyclerViewAdapter(mGif, this, recyclerView);
        String[] files = AppUtility.getMainExternalFolder().list();
        if (files != null && files.length > 0) {
            for (String str : files) {
                mGif.add(new Gif(AppUtility.getMainExternalFolder().getAbsolutePath()+"/"+str));
            }
        }




        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }



    @Override
    public void goDownload(@NotNull Gif gif) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.delete))
                .setPositiveButton(getString(R.string.submit_delete), (dialogInterface, d) -> {
                    File fileName = new File( gif.getUrl());
                    if (fileName.exists()) {
                        fileName.delete();
                        mGif.remove(gif);
                        Snackbar.make(errorTextView, "קובץ נמחק", Snackbar.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getString(R.string.submit_share), (dialogInterface, d) -> {
                    AppUtility.shareDownloadedGif(getContext(),new File( gif.getUrl()));

                });

        alert.show();
    }




    @Override
    public void onDestroyView() {
        recyclerView.setAdapter(null);
        recyclerView = null;
        errorTextView = null;
        super.onDestroyView();
    }

    @Override
    public void goFullScreen(@NotNull Gif gif) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sample, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

}