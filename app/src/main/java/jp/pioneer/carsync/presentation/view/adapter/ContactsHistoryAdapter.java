package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;


/**
 * 電話帳 発着信履歴リストのアダプター
 */

public class ContactsHistoryAdapter extends ArrayAdapter<ContactsHistoryItem> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<ContactsHistoryItem> mList = new ArrayList<>();
    public ContactsHistoryAdapter(Context context, @NonNull ArrayList<ContactsHistoryItem> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mList = objects;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public  @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ContactsHistoryAdapter.ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_contact_history, parent, false);
            vh = new ContactsHistoryAdapter.ViewHolder(view);
            view.setTag(vh);
        } else {
            vh = (ContactsHistoryAdapter.ViewHolder) view.getTag();
        }
        ContactsHistoryItem item = mList.get(position);
        switch (item.callType){
            case CallLog.Calls.INCOMING_TYPE:
                vh.type.setImageResource(R.drawable.p0055_callreceived);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                vh.type.setImageResource(R.drawable.p0054_callmade);
                break;
            case CallLog.Calls.MISSED_TYPE:
                vh.type.setImageResource(R.drawable.p0056_callmissed);
                break;
            default:
                vh.type.setImageResource(R.drawable.p0050_phone);
                break;
        }
        String strCount = (item.count>1) ? " ("+ item.count +")":"";
        String name = item.displayName + strCount;
        vh.name.setText(name);
        Date date = item.date;
        String strDate = (String) android.text.format.DateFormat.format("yyyy/MM/dd", date);
        String nowDate = (String) android.text.format.DateFormat.format("yyyy/MM/dd", Calendar.getInstance());
        String date_text;
        DateFormat formatDate = android.text.format.DateFormat.getDateFormat(mContext);
        DateFormat formatTime = android.text.format.DateFormat.getTimeFormat(mContext);

        //履歴が本日の日付であればToday表示
        if (strDate.equals(nowDate)) {
            date_text = String.format("Today %s", formatTime.format(date));
        } else {
            date_text = String.format("%s %s", formatDate.format(date), formatTime.format(date));
        }
        vh.date.setText(date_text);
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.name_text) TextView name;
        @BindView(R.id.date_text) TextView date;
        @BindView(R.id.history_type) ImageView type;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
