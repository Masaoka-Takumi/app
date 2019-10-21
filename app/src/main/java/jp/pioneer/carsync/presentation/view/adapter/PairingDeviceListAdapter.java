package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.PairingDeviceInfo;

/**
 * ペアリングデバイスリストのAdapter
 */

public class PairingDeviceListAdapter extends ArrayAdapter<PairingDeviceInfo> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<PairingDeviceInfo> mTypeArray = new ArrayList<>();

    public PairingDeviceListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public PairingDeviceListAdapter(@NonNull Context context,  @NonNull ArrayList<PairingDeviceInfo> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
    }

    public void setTypeArray(ArrayList<PairingDeviceInfo> typeArray) {
        mTypeArray = typeArray;
        //notifyDataSetChanged();
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
        PairingDeviceListAdapter.ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_pairing_device, parent, false);
            vh = new PairingDeviceListAdapter.ViewHolder(view);
            view.setTag(vh);
        } else {
            vh = (PairingDeviceListAdapter.ViewHolder) view.getTag();
        }
        vh.mBdAddress.setText(mTypeArray.get(position).bdAddress);
        vh.mBtLinkKey.setText(mTypeArray.get(position).btLinkKey);

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.bd_address) TextView mBdAddress;
        @BindView(R.id.bt_link_key) TextView mBtLinkKey;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

}
