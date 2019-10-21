package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.VisualEffectItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_008316 on 2017/06/20.
 */

public class VisualEffectAdapter extends RecyclerView.Adapter<VisualEffectAdapter.ViewHolder> {

    private Context mContext;
    private List<VisualEffectItem> mItems;
    private OnRecyclerListener mListener;
    private int mSelectedIndex = 0;
    private int mColor = 0;

    public VisualEffectAdapter(Context context, List<VisualEffectItem> data, OnRecyclerListener listener) {
        mContext = context;
        mItems = data;
        mListener = listener;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.element_list_item_visual_effect, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.item.setImageResource(mItems.get(position).resourceId);
        if(position==mSelectedIndex){
            holder.icon_base.setAlpha(0.9f);
            holder.icon_select.setVisibility(View.VISIBLE);
            if(mColor!=0) {
                holder.item.setImageDrawable(ImageViewUtil.setTintColor(mContext, mItems.get(position).resourceId, mColor));
            }
        }else{
            holder.icon_base.setAlpha(0.4f);
            holder.icon_select.setVisibility(View.INVISIBLE);
            holder.item.setImageDrawable(ImageViewUtil.setTintColor(mContext, mItems.get(position).resourceId, R.color.drawable_white_color));
        }
        // クリック処理
        holder.item.setOnClickListener(v -> {
            mListener.onRecyclerClicked(v, position);
        });
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon_select) ImageView icon_select;
        @BindView(R.id.icon_base) View icon_base;
        @BindView(R.id.item_image) ImageView item;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnRecyclerListener {
        void onRecyclerClicked(View v, int position);
    }
}
