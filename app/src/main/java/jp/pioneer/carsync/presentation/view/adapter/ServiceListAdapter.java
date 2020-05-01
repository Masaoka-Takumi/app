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
import jp.pioneer.carsync.domain.content.TunerContract;

public class ServiceListAdapter extends AbstractCursorAdapter{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mOrientation;
    private boolean mIsSphCarDevice = false;
    public ServiceListAdapter(Context context, Cursor c, boolean autoRequery) {
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
        if(mIsSphCarDevice) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.list_item_background_selector_music_no_focus));
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.iconView.setVisibility(View.GONE);
        holder.id =  TunerContract.ListItemContract.Dab.getId(cursor);
        holder.index = TunerContract.ListItemContract.Dab.getListIndex(cursor);
        holder.titleText.setText(TunerContract.ListItemContract.Dab.getText(cursor));
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
