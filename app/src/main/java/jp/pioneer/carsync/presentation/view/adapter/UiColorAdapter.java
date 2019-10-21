package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * UIカラーadapter
 */

public class UiColorAdapter extends RecyclerView.Adapter<UiColorAdapter.ViewHolder> {

    private Context mContext;
    private List<UiColor> mItems;
    private IllumiColorAdapter.OnRecyclerListener mListener;
    private int mPosition = -1;

    /**
     * コンストラクタ
     *
     * @param context  Context
     * @param data     設定カラーリスト
     * @param listener コールバック先
     */
    public UiColorAdapter(Context context, List<UiColor> data, IllumiColorAdapter.OnRecyclerListener listener) {
        mContext = context;
        mItems = data;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.element_list_item_ui_color, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UiColor color = mItems.get(position);
        //TODO 表示(色)
        //String name = mContext.getString(color.label).substring(0, 1).toUpperCase() + mContext.getString(color.label).substring(1).toLowerCase();
        String name = mContext.getString(color.label);
        holder.name.setText(name);
        holder.name.setTextColor(ContextCompat.getColor(mContext, color.getResource()));
        Drawable checkDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0137_check, color.getResource());
        Drawable checkDrwNewCopy = checkDr.getConstantState().newDrawable().mutate();
        holder.check.setImageDrawable(checkDrwNewCopy);
        Drawable frameLightDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0135_colorbtnselect_1nrm, color.getResource());
        Drawable frameLightDrwNewCopy = frameLightDr.getConstantState().newDrawable().mutate();
        holder.frameLight.setImageDrawable(frameLightDrwNewCopy);
        Drawable frameDr = ImageViewUtil.setTintColor(mContext, R.drawable.p0136_colobtnrlineselect_1nrm, color.getResource());
        Drawable frameDrwNewCopy = frameDr.getConstantState().newDrawable().mutate();
        holder.frame.setImageDrawable(frameDrwNewCopy);
        if (mPosition == position) {
            holder.check.setVisibility(View.VISIBLE);
            holder.name.setAlpha(1.0f);
            holder.frame.setAlpha(1.0f);
            holder.frameLight.setAlpha(1.0f);
            holder.imageLight.setAlpha(1.0f);
        } else {
            holder.check.setVisibility(View.INVISIBLE);
            holder.name.setAlpha(0.5f);
            holder.frame.setAlpha(0.5f);
            holder.frameLight.setAlpha(0.5f);
            holder.imageLight.setAlpha(0.5f);
        }

        // クリック処理
        holder.rootView.setOnClickListener(v -> mListener.onRecyclerClicked(v, position));
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
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
        mPosition = position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ui_item) RelativeLayout rootView;
        @BindView(R.id.check_icon) ImageView check;
        @BindView(R.id.color_text) TextView name;
        @BindView(R.id.image_frame) ImageView frame;
        @BindView(R.id.image_frame_light) ImageView frameLight;
        @BindView(R.id.image_light) ImageView imageLight;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnRecyclerListener {
        void onRecyclerClicked(View v, int position);
    }
}
