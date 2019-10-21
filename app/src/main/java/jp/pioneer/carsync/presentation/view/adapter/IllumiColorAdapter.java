package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * イルミネーションカラーadapter
 */

public class IllumiColorAdapter extends RecyclerView.Adapter<IllumiColorAdapter.ViewHolder> {

    private Context mContext;
    private List<Integer> mItems;
    private IllumiColorAdapter.OnRecyclerListener mListener;
    private int mSelectPosition = -1;

    /**
     * コンストラクタ
     *
     * @param context  Context
     * @param data     設定カラー
     * @param listener コールバック先
     */
    public IllumiColorAdapter(Context context, List<Integer> data, IllumiColorAdapter.OnRecyclerListener listener) {
        mContext = context;
        mItems = data;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.element_list_item_illumi_color, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == mItems.size()){
            holder.frame.setVisibility(View.GONE);
            holder.frameLight.setVisibility(View.GONE);
            holder.select.setVisibility(View.GONE);
            holder.selectLight.setVisibility(View.GONE);
            holder.frameScan.setVisibility(View.VISIBLE);
            if (mSelectPosition == position) {
                holder.selectScan.setVisibility(View.VISIBLE);
            }else{
                holder.selectScan.setVisibility(View.GONE);
            }
        }else {
            int color = mItems.get(position);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            holder.frame.setVisibility(View.VISIBLE);
            holder.frameLight.setVisibility(View.VISIBLE);
            holder.frameScan.setVisibility(View.GONE);
            holder.selectScan.setVisibility(View.GONE);
            // 古いAPIだとColorFilterの色が上書きされてしまうため深コピー
            Drawable frameDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0033_illcolorselectbtn_1nrm, r, g, b);
            Drawable frameDrwNewCopy = frameDr.getConstantState().newDrawable().mutate();
            Drawable frameLightDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0034_illcolorselectbtn_1nrm, r, g, b);
            Drawable frameLightDrwNewCopy = frameLightDr.getConstantState().newDrawable().mutate();
            Drawable selectDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0036_illcolorbtn_1nrm, r, g, b);
            Drawable selectDrwNewCopy = selectDr.getConstantState().newDrawable().mutate();
            holder.frame.setImageDrawable(frameDrwNewCopy);
            holder.frameLight.setImageDrawable(frameLightDrwNewCopy);
            holder.selectLight.setImageDrawable(selectDrwNewCopy);
            if (mSelectPosition == position) {
                holder.select.setVisibility(View.VISIBLE);
                holder.selectLight.setVisibility(View.VISIBLE);
            } else {
                holder.select.setVisibility(View.GONE);
                holder.selectLight.setVisibility(View.GONE);
            }
        }
        // クリック処理
        holder.rootView.setOnClickListener(v -> mListener.onRecyclerClicked(v, position));
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size() + 1;
        } else {
            return 0;
        }
    }

    /**
     * 選択位置の設定
     *
     * @param position 選択位置
     */
    public void setPosition(int position) {
        mSelectPosition = position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.illumi_item) RelativeLayout rootView;
        @BindView(R.id.illumi_frame) ImageView frame;
        @BindView(R.id.illumi_frame_light) ImageView frameLight;
        @BindView(R.id.illumi_select) ImageView select;
        @BindView(R.id.illumi_select_light) ImageView selectLight;
        @BindView(R.id.illumi_frame_scan) ImageView frameScan;
        @BindView(R.id.illumi_select_scan) ImageView selectScan;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * クリックリスナー
     */
    public interface OnRecyclerListener {
        /**
         * アイテムクリックイベント
         *
         * @param v        View
         * @param position 選択位置
         */
        void onRecyclerClicked(View v, int position);
    }
}
