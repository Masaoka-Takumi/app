package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;

/**
 * Created by BP06566 on 2017/03/10.
 */

public class AbstractCardAdapter extends RecyclerView.Adapter<AbstractCardAdapter.ViewHolder> {

    protected Context mContext;

    public AbstractCardAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void onItemClick(Cursor cursor) {
    }

    public void onPlayClick(int position) {
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.background_view) public ImageView background;
        @BindView(R.id.title_text) public TextView title;
        @BindView(R.id.item_select) RelativeLayout itemSelect;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.play_button)
        public void onClick(View v) {
            onPlayClick(getAdapterPosition());
        }
    }
}
