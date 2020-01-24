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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.model.YouTubeLinkSearchItem;
import timber.log.Timber;

/**
 * YouTube Link検索対象切り替え画面のArrayAdapter
 */
public class YouTubeLinkSearchItemAdapter extends ArrayAdapter<YouTubeLinkSearchItem> {

    private LayoutInflater mInflater;
    private Context mContext;
    private SparseBooleanArray mCheckedPositions;

    public YouTubeLinkSearchItemAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
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
            convertView.setTag(holder);
        } else {
            holder = (SourceAppAdapter.ViewHolder) convertView.getTag();
        }
        YouTubeLinkSearchItem item = getItem(position);
        if (item != null) {
            holder.checkedTextView.setText(item.textResource);
        }
        holder.checkBox.setChecked(mCheckedPositions.get(position));
        return convertView;
    }

    static class ViewHolder {
        @BindView(android.R.id.text1)
        TextView checkedTextView;
        @BindView(R.id.checkBox1)
        CheckBox checkBox;

        ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

}