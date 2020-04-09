package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import me.grantland.widget.AutofitTextView;
import timber.log.Timber;

public class AlexaListTemplateAdapter extends ArrayAdapter<AlexaIfDirectiveItem.ListItem> {
    private LayoutInflater mInflater;
    private Context mContext;
    private String mTemplateType;

    public AlexaListTemplateAdapter(Context context, String type) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mTemplateType = type;
    }

    @Nonnull
    @Override
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        Timber.i("getView position=" + position);

        AlexaIfDirectiveItem.ListItem item = getItem(position);
        switch (mTemplateType) {
            case "ListTemplate1":
                ListViewHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.element_item_list_template, parent, false);
                    holder = new ListViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ListViewHolder) convertView.getTag();
                }
                if (item != null) {
                    holder.leftTextField.setText(item.getLeftTextField());
                    holder.rightTextField.setText(item.getRightTextField());
                }
                break;
            case "LocalSearchListTemplate1":
                LocalSearchViewHolder searchViewHolder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.element_item_local_search_list_template, parent, false);
                    searchViewHolder = new LocalSearchViewHolder(convertView);
                    convertView.setTag(searchViewHolder);
                } else {
                    searchViewHolder = (LocalSearchViewHolder) convertView.getTag();
                }
                if (item != null) {
                    searchViewHolder.leftTextField.setText(item.getLeftTextField());
                    searchViewHolder.rightPrimaryTextField.setText(item.getRightPrimaryTextField());
                    searchViewHolder.rightSecondaryTextField.setText(item.getRightSecondaryTextField());
                }
                break;
        }

        return convertView;
    }

    static class ListViewHolder {
        @BindView(R.id.text_left)
        TextView leftTextField;
        @BindView(R.id.text_right)
        TextView rightTextField;
        @BindView(R.id.separator_bottom)
        View separator_bottom;

        ListViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class LocalSearchViewHolder {
        @BindView(R.id.text_left)
        TextView leftTextField;
        @BindView(R.id.right_primary_text)
        TextView rightPrimaryTextField;
        @BindView(R.id.right_secondary_text)
        TextView rightSecondaryTextField;
        @BindView(R.id.separator_bottom)
        View separator_bottom;

        LocalSearchViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}