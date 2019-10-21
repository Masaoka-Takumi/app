package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_008316 on 2017/06/07.
 */

public class BtDeviceAdapter extends AbstractCursorAdapter {
    private int mColorRes;
    private boolean mIsDeleteMode = false;
    private boolean isMusicDisp = false;
    private boolean isPhoneDisp = false;
    private boolean mIsEnabled = true;
    private Timer mMusicTimer = new Timer(true);
    private Timer mPhoneTimer = new Timer(true);
    public BtDeviceAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColorRes = outValue.resourceId;
    }

    public void setDeleteMode(boolean deleteMode) {
        mIsDeleteMode = deleteMode;
        notifyDataSetChanged();
    }

    public boolean isDeleteMode() {
        return mIsDeleteMode;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        mMusicTimer.cancel();
        mPhoneTimer.cancel();
        super.notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_bt_device, parent, false);
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
        if(mIsDeleteMode){
            holder.phone.setVisibility(View.GONE);
            holder.music.setVisibility(View.GONE);
            if(SettingListContract.DeviceList.isSessionConnected(cursor)) {
                holder.delete.setVisibility(View.GONE);
            } else {
                holder.delete.setVisibility(View.VISIBLE);
            }
            holder.delete.setImageResource(R.drawable.p1103_setting_del);
        }else {
            holder.phone.setVisibility(View.VISIBLE);
            holder.music.setVisibility(View.VISIBLE);
            holder.music.setEnabled(false);
            holder.delete.setVisibility(View.GONE);
            if (SettingListContract.DeviceList.isAudioConnected(cursor)) {
                holder.music.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1100_setting_bta, mColorRes));
            } else if (SettingListContract.DeviceList.isAudioSupported(cursor)) {
                holder.music.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1100_setting_bta, R.color.drawable_white_color));
            } else {
                holder.music.setVisibility(View.INVISIBLE);
            }

            if(SettingListContract.DeviceList.getPhoneConnectStatus(cursor).isConnecting()
                    ||SettingListContract.DeviceList.getPhoneConnectStatus(cursor).isDisconnecting()) {
                if(SettingListContract.DeviceList.getPhoneConnectStatus(cursor).isConnecting()) {
                    holder.bdAddress.setText(R.string.set_262);
                }
				//点滅表示
                mPhoneTimer = new Timer(true);
                final android.os.Handler handler = new android.os.Handler();
                mPhoneTimer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                handler.post( new Runnable(){
                                    public void run(){
                                        isPhoneDisp = !isPhoneDisp;

                                        //Viewの表示を切り替える
                                        if (isPhoneDisp) {
                                            holder.phone.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1101_setting_handsfree, mColorRes));
                                        } else {
                                            holder.phone.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1101_setting_handsfree, R.color.drawable_white_color));
                                        }
                                    }
                                });
                            }
                        }
                        , 0, 500
                );
            } else if (SettingListContract.DeviceList.isPhone1Connected(cursor)) {
                holder.phone.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1102_setting_handsfree1, mColorRes));
            } else if (SettingListContract.DeviceList.isPhone2Connected(cursor)) {
                holder.phone.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1102_setting_handsfree2, mColorRes));
            } else if (SettingListContract.DeviceList.isPhoneSupported(cursor)) {
                holder.phone.setImageDrawable(ImageViewUtil.setTintColor(mContext, R.drawable.p1101_setting_handsfree, R.color.drawable_white_color));
            } else {
                holder.phone.setVisibility(View.INVISIBLE);
            }
        }
        holder.deviceName.setEnabled(mIsEnabled);
        holder.bdAddress.setEnabled(mIsEnabled);
        holder.position = cursor.getPosition();
    }

    protected void onClickPhoneButton(Cursor cursor){

    }
    protected void onClickDeleteButton(Cursor cursor){

    }

    class ViewHolder {
        int position = -1;
        @BindView(R.id.phone_view) ImageView phone;
        @BindView(R.id.music_view) ImageView music;
        @BindView(R.id.delete_view) ImageView delete;
        @BindView(R.id.name_text) TextView deviceName;
        @BindView(R.id.bd_address_text) TextView bdAddress;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.phone_view)
        public void onClickPhone(View view) {
            onClickPhoneButton((Cursor) getItem(position));
        }

        @OnClick(R.id.delete_view)
        public void onClickDelete(View view) {
            onClickDeleteButton((Cursor) getItem(position));
        }
    }
}
