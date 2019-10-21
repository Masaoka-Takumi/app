package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;

/**
 * Listening Position設定の為のAdapter
 * Created by tsuyosh on 2016/02/24.
 */
public class ListeningPositionAdapter extends ArrayAdapter<ListeningPositionSetting> {
    public ListeningPositionAdapter(Context context) {
        super(context, 0, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.element_listening_position_adapter, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ListeningPositionSetting item = getItem(position);
        ViewHolder vh = (ViewHolder) convertView.getTag();
        int text;
        switch (item) {
            case OFF:
                text = R.string.com_001;
                break;
            case FRONT_LEFT:
                text = R.string.val_058;
                break;
            case FRONT_RIGHT:
                text = R.string.val_059;
                break;
            case FRONT:
                text = R.string.val_057;
                break;
            case ALL:
                text = R.string.set_011;
                break;
            default:
                text = R.string.unknown;
                break;
        }
        //vh.text1.setText(text);
        vh.text2.setText(text);
        return convertView;
    }

    public static class ViewHolder {
        @BindView(android.R.id.text1)
        CheckedTextView text1;

        @BindView(android.R.id.text2)
        TextView text2;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
