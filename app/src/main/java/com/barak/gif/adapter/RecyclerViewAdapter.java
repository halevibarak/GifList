package com.barak.gif.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.barak.gif.R;
import com.barak.gif.model.Gif;
import com.barak.gif.ui.ActionInterface;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak Halevi on 22/10/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Gif> articles = new ArrayList<>();
    private ActionInterface mLisenner;
    private long mLastClickTime;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    public RecyclerViewAdapter(ArrayList<Gif> items, ActionInterface listnner, RecyclerView recyclerView) {
        this.articles = items;
        this.mLisenner = listnner;
        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = articles.size();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if( totalItemCount <= (lastVisibleItem + visibleThreshold)){
                        if(onLoadMoreListener != null){
                            loading = true;
                             onLoadMoreListener.onLoadMore(totalItemCount);
                        }

                    }
                }
            });
        }

    }
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup view, int viewType) {
        switch (viewType) {
            default:
                View view1 = LayoutInflater.from(view.getContext()).inflate(R.layout.recycler_view_list_item, view, false);
                return new GifViewHolder(view1);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder view_holder, int position) {

        final Gif gif = articles.get(position);
        switch (view_holder.getItemViewType()) {
            case 0:
                GifViewHolder gifViewHolder = (GifViewHolder) view_holder;
                if (gif != null) {
                    gifViewHolder.gifView.setVisibility(View.VISIBLE);
                    Glide.with(gifViewHolder.gifView.getContext()).asGif().load(gif.getImages().getFixed_width_small().getUrl()).into(gifViewHolder.gifView);
                    gifViewHolder.itemView.setOnClickListener(v ->
                            mLisenner.goFullScreen(gif));
                    gifViewHolder.itemView.setOnLongClickListener(view -> {
                        mLisenner.goDownload(gif);
                        return true;
                    });
                }
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        return 0;    //meir 0 / local - 1 / rest 2
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {


        public GifViewHolder(View itemView_) {
            super(itemView_);
            gifView = itemView_.findViewById(R.id.imgplay);
            itemView = itemView_;
        }

        View itemView;
        ImageView gifView;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int totalItemCount);
    }
    public void setLoad(){
        loading = false;
    }

}
