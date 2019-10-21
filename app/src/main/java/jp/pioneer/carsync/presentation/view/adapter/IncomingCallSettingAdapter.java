package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;

/**
 * Created by NSW00_008320 on 2017/12/14.
 */

public class IncomingCallSettingAdapter extends ArrayAdapter<String> {
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private Context mContext = null;
    private LayoutInflater mLayoutInflater;
    private int mSelectedIndex = -1;

    public IncomingCallSettingAdapter(@NonNull Context context, @NonNull ArrayList<String> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        notifyDataSetChanged();
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
    public
    @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.element_setting_phone_incoming_call_list_item, parent, false);
            vh = new ViewHolder(convertView);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.mTitle.setText(mTypeArray.get(position));

        if (position == mSelectedIndex) {
            vh.mCheck.setVisibility(View.VISIBLE);
        } else {
            vh.mCheck.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.textView) TextView mTitle;
        @BindView(R.id.check) ImageView mCheck;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}

