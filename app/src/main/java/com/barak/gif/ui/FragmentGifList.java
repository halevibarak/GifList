package com.barak.gif.ui;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.barak.gif.R;
import com.barak.gif.adapter.RecyclerViewAdapter;
import com.barak.gif.app.App;
import com.barak.gif.model.Gif;
import com.barak.gif.model.MyViewModelFactory;
import com.barak.gif.network.ConnectivityHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.barak.gif.ui.GifActivity.LINK;


public class FragmentGifList extends Fragment implements ActionInterface, RecyclerViewAdapter.OnLoadMoreListener {

    RecyclerView recyclerView;

    private ArrayList<Gif> mArticles = new ArrayList<>();

    private RecyclerViewAdapter adapter;

    private OnCompleteListener mListener;
    private GifModel articleModel;
    private View errorTextView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private int mPage = 0;
    private EditText mEditSearch;
    private Disposable _disposable;

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    public static FragmentGifList newInstance() {
        return new FragmentGifList();
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
        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mEditSearch = view.findViewById(R.id.edit_text);
        _disposable =
                RxTextView.textChangeEvents(mEditSearch)
                        .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(_getSearchObserver());

        mySwipeRefreshLayout.setEnabled(false);

        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(mArticles, this, recyclerView);
        adapter.setOnLoadMoreListener(this);
        recyclerView.setAdapter(adapter);
    }

    private DisposableObserver<TextViewTextChangeEvent> _getSearchObserver() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                if (mEditSearch.length() == 0) {
                    mArticles.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    if (!ConnectivityHelper.isConnectedToNetwork(App.getInstance().getApplicationContext())) {
                        Snackbar.make(recyclerView, getString(R.string.no_record), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    mArticles.clear();
                    mySwipeRefreshLayout.setRefreshing(true);
                    mPage = 0;
                    if (articleModel == null) {
                        modelConfig(mEditSearch.getText().toString() + "&offset=" + mPage++);
                    } else {
                        articleModel.refreshData(mEditSearch.getText().toString() + "&offset=" + mPage++);
                    }
                }
            }
        };
    }


    private void modelConfig(String pharam) {
        {
            mySwipeRefreshLayout.setRefreshing(true);
            articleModel = ViewModelProviders.of(this, new MyViewModelFactory(App.getInstance(), pharam)).get(GifModel.class);
            articleModel.getArticleList().observe(this, articles -> {
                mySwipeRefreshLayout.setRefreshing(false);
                if (articles == null || articles.size() == 0) {
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                errorTextView.setVisibility(View.GONE);
                if (mArticles.size() == 0) {
                    mArticles.addAll(articles);
                } else {
                    for (Gif ne : articles) {
                        if (!mArticles.contains(ne))
                            mArticles.add(ne);
                    }
                }
                adapter.setLoad();
                adapter.notifyDataSetChanged();
            });
        }
    }


    @Override
    public void goDownload(Gif gif) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.download_title)).setMessage(getString(R.string.download_text))
                .setNeutralButton(getString(R.string.submit), (dialogInterface, d) ->
                        mListener.download(gif))
                .setOnCancelListener(dialogInterface -> {
                });
        alert.show();
    }

    @Override
    public void goFullScreen(Gif gif) {
        Intent intent = new Intent(getActivity(), GifActivity.class);
        Bundle b = new Bundle();
        b.putString(LINK, gif.getImages().getOriginal().getUrl());
        intent.putExtras(b);
        getActivity().startActivityForResult(intent, 1);
        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onLoadMore(int totalItemCount) {
        articleModel.refreshData(mEditSearch.getText().toString() + "&offset=" + mPage++);
    }


    public interface OnCompleteListener {
        void download(Gif article);
    }


    @Override
    public void onDestroyView() {
        recyclerView.setAdapter(null);
        recyclerView = null;
        articleModel = null;
        mySwipeRefreshLayout = null;
        errorTextView = null;
        mEditSearch = null;
        _disposable.dispose();
        super.onDestroyView();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            FragmentLocal myFragment = (FragmentLocal) getActivity().getSupportFragmentManager().findFragmentByTag("FragmentLocal");
            if (myFragment != null && myFragment.isVisible()) {
                return true;
            }
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, FragmentLocal.newInstance(), "FragmentLocal");
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mEditSearch.requestFocus();
    }
}