package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Super轟Sound設定リストのAdapter
 */

public class TodorokiSettingAdapter extends ArrayAdapter<SuperTodorokiSetting> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<SuperTodorokiSetting> mTypeArray = new ArrayList<>();
    private int mSelectedIndex = 0;
    private int mColor = 0;
    private boolean mIsEnabled = true;
    private int mOrientation;
    public TodorokiSettingAdapter(@NonNull Context context, @NonNull ArrayList<SuperTodorokiSetting> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public int getCount() {
        return mTypeArray.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public  @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_todoroki_setting, parent, false);
            if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.super_todoroki_list_height_portrait)));
            }else{
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.super_todoroki_list_height_land)));
            }
            vh = new ViewHolder(view);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        vh.mEqTypeName.setText(mTypeArray.get(position).getLabel());
        if(position==mSelectedIndex){
            if(mColor!=0) {
                vh.mCheckBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0022_checkselect, mColor));
            }
            vh.mCheck.setVisibility(View.VISIBLE);
        }
        else{
            vh.mCheck.setVisibility(View.INVISIBLE);
        }
        vh.mEqTypeName.setEnabled(mIsEnabled);
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.textView) TextView mEqTypeName;
        @BindView(R.id.check) RelativeLayout mCheck;
        @BindView(R.id.check_back) ImageView mCheckBack;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

}

