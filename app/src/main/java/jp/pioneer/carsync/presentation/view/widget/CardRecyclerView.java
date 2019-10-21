package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import jp.pioneer.carsync.presentation.view.adapter.AbstractCardAdapter;

/**
 * Created by BP06566 on 2017/03/09.
 */

public class CardRecyclerView extends RecyclerView {
    public CardRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
                if (manager.getChildCount() == 0)
                    return;

                int first = manager.findFirstVisibleItemPosition();
                int last = manager.findLastVisibleItemPosition();
                int viewHeight = recyclerView.getHeight();
                float itemHeight = manager.findViewByPosition(first).getHeight();
                float imageRange = itemHeight - ((AbstractCardAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(first)).background.getHeight();
                float viewRange = viewHeight + itemHeight;

                for (int i = first; i <= last; ++i) {
                    float itemY = manager.findViewByPosition(i).getY();
                    AbstractCardAdapter.ViewHolder holder = (AbstractCardAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    holder.background.setScrollY((int) (imageRange * (itemY / viewRange - 0.5) * 5));
                }
            }
        });
    }
}
