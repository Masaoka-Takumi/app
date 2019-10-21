package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.UsbListContract;
import jp.pioneer.carsync.domain.model.UsbInfoType;

/**
 * Created by NSW00_007906 on 2017/12/25.
 */

public class UsbListAdapter extends AbstractCursorAdapter{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mOrientation;
    private boolean mIsSphCarDevice = false;
    public UsbListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.element_list_item_usb, parent, false);
        ViewHolder holder = new ViewHolder(view);
        if(mOrientation == Configuration.ORIENTATION_LANDSCAPE){
            view.setPadding((int)mContext.getResources().getDimension(R.dimen.music_list_landscape_let_padding),0,0,0);
        }
        if(mIsSphCarDevice) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.list_item_background_selector_music_no_focus));
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.id = UsbListContract.getId(cursor);
        holder.index = UsbListContract.getListIndex(cursor);
        if(UsbListContract.getDataEnabled(cursor)) {
            UsbInfoType infoType = UsbListContract.getInfoType(cursor);
            switch(infoType){
                case FILE:
                    holder.iconView.setImageResource(R.drawable.p0971_icon_usbfile);
                    break;
                case FOLDER_MUSIC_EXIST:
                case FOLDER_MUSIC_NOT_EXIST:
                    holder.iconView.setImageResource(R.drawable.p0970_icon_usbfolder);
                    break;
                default:
                    break;
            }
            holder.titleText.setText(UsbListContract.getText(cursor));
        }else{
            holder.iconView.setImageBitmap(null);
            holder.titleText.setText("");
            onWantedItem(UsbListContract.getListIndex(cursor));
        }
    }

    public void onWantedItem(int index){
    }

    static class ViewHolder {
        @BindView(R.id.icon_view) ImageView iconView;
        @BindView(R.id.title_text) TextView titleText;
        long id;
        int index;
        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
