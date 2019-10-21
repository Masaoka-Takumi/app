package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
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
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_906320 on 2017/07/25.
 */

public class SmallCarTaSettingAdapter extends ArrayAdapter<SmallCarTaSettingType> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<SmallCarTaSettingType> mTypeArray = new ArrayList<>();
    private int mSelectedIndex = 0;
    private boolean mIsEnabled = true;
    private int mColor = 0;

    public SmallCarTaSettingAdapter(@NonNull Context context, @NonNull ArrayList<SmallCarTaSettingType> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
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
    public  @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        TodorokiSettingAdapter.ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_todoroki_setting, parent, false);
            vh = new TodorokiSettingAdapter.ViewHolder(view);
            view.setTag(vh);
        } else {
            vh = (TodorokiSettingAdapter.ViewHolder) view.getTag();
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
