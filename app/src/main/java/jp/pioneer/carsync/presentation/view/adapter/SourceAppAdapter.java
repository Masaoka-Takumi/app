package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;

/**
 * Created by NSW00_007906 on 2017/10/23.
 */

public class SourceAppAdapter extends ArrayAdapter<ApplicationInfo> {

    private LayoutInflater mInflater;
    private Context mContext;
    private int mOrientation;
    private SparseBooleanArray mCheckedPositions;

    public SourceAppAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    public void setCheckedPositions(SparseBooleanArray checkedPositions) {
        mCheckedPositions = checkedPositions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SourceAppAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.element_item_select_multichoice, parent, false);
            holder = new SourceAppAdapter.ViewHolder(convertView);
            if(mOrientation == Configuration.ORIENTATION_LANDSCAPE){
                holder.checkedTextView.setPadding((int)mContext.getResources().getDimension(R.dimen.source_app_setting_landscape_left_padding),0,(int)mContext.getResources().getDimension(R.dimen.source_app_setting_right_padding),0);
            }
            convertView.setTag(holder);
        } else {
            holder = (SourceAppAdapter.ViewHolder) convertView.getTag();
        }

        ApplicationInfo app = getItem(position);
        if (app != null) {
            PackageManager pm = getContext().getPackageManager();
            holder.checkedTextView.setText(app.loadLabel(pm));
            Drawable icon = app.loadIcon(pm);
            float iconSize = mContext.getResources().getDimension(R.dimen.source_app_icon_size);
            icon.setBounds(0, 0, (int)iconSize, (int)iconSize);
            holder.checkedTextView.setCompoundDrawables(icon, null, null, null);
        }
        holder.checkBox.setChecked(mCheckedPositions.get(position));
        return convertView;
    }

    static class ViewHolder {
        @BindView(android.R.id.text1) TextView checkedTextView;
        @BindView(R.id.checkBox1) CheckBox checkBox;
        ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
