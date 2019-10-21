package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.SettingListContract;

/**
 * Created by NSW00_008320 on 2017/07/26.
 */

public class BtDeviceSearchAdapter extends AbstractCursorAdapter {
    private boolean mIsEnabled = true;
    public BtDeviceSearchAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_bt_device_search, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String deviceName = SettingListContract.DeviceList.getDeviceName(cursor);
        deviceName = TextUtils.isEmpty(deviceName) ? ""
                : deviceName;
        holder.deviceName.setText(deviceName);
        holder.bdAddress.setText(SettingListContract.DeviceList.getBdAddress(cursor));
        holder.position = cursor.getPosition();
        holder.deviceName.setEnabled(mIsEnabled);
        holder.bdAddress.setEnabled(mIsEnabled);
    }

    static class ViewHolder {
        int position = -1;
        @BindView(R.id.name_text) TextView deviceName;
        @BindView(R.id.bd_address_text) TextView bdAddress;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}