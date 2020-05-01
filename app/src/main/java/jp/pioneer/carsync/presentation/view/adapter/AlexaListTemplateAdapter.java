package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

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
    private float mLeftTextFieldLength;
    private int mOrientation;

    public AlexaListTemplateAdapter(Context context, String type) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mTemplateType = type;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    @Override
    public void addAll(@NonNull Collection<? extends AlexaIfDirectiveItem.ListItem> collection) {
        super.addAll(collection);
        mLeftTextFieldLength = 0;
        ViewGroup parent = new LinearLayout(mContext);
        View view = mInflater.inflate(R.layout.element_item_list_template, parent, false);
        TextView textview = view.findViewById(R.id.text_left);
        for (AlexaIfDirectiveItem.ListItem item : collection) {
            String leftTextField = item.getLeftTextField();
            textview.setText(leftTextField);
            mLeftTextFieldLength = Math.max(calculateTextLen(textview), mLeftTextFieldLength);
        }
    }

    private float calculateTextLen(TextView view) {
        TextPaint tp = view.getPaint();
        String strTxt = view.getText().toString();
        float mt = tp.measureText(strTxt);
        return mt;
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
                ViewGroup.LayoutParams lp = holder.leftTextField.getLayoutParams();
                lp.width = (int) Math.ceil(mLeftTextFieldLength);
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.alexa_display_card_main_title_margin_left_portrait), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
                } else {
                    mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.alexa_display_card_list_template_item_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
                }
                holder.leftTextField.setLayoutParams(mlp);
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
                ViewGroup.LayoutParams lp1 = searchViewHolder.listItem.getLayoutParams();
                ViewGroup.MarginLayoutParams mlp1 = (ViewGroup.MarginLayoutParams) lp1;
                ViewGroup.LayoutParams lp2 = searchViewHolder.separator_bottom.getLayoutParams();
                ViewGroup.MarginLayoutParams mlp2 = (ViewGroup.MarginLayoutParams) lp2;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mlp1.setMargins((int) mContext.getResources().getDimension(R.dimen.alexa_display_card_main_title_margin_left_portrait), mlp1.topMargin, mlp1.rightMargin, mlp1.bottomMargin);
                    mlp2.setMargins((int) mContext.getResources().getDimension(R.dimen.alexa_display_card_local_search_template_separator_left_margin_landscape), mlp2.topMargin, mlp2.rightMargin, mlp2.bottomMargin);

                } else {
                    mlp1.setMargins((int) mContext.getResources().getDimension(R.dimen.alexa_display_card_list_template_item_margin), mlp1.topMargin, mlp1.rightMargin, mlp1.bottomMargin);
                }
                searchViewHolder.listItem.setLayoutParams(mlp1);
                searchViewHolder.separator_bottom.setLayoutParams(mlp2);
                if (item != null) {
                    String number = String.valueOf(position + 1) + ".";
                    searchViewHolder.leftTextField.setText(number);
                    searchViewHolder.distanceTextField.setText(item.getLeftTextField());
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

        ListViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class LocalSearchViewHolder {
        @BindView(R.id.list_item)
        LinearLayout listItem;
        @BindView(R.id.text_left)
        TextView leftTextField;
        @BindView(R.id.right_primary_text)
        TextView rightPrimaryTextField;
        @BindView(R.id.distance_text)
        TextView distanceTextField;
        @BindView(R.id.right_secondary_text)
        TextView rightSecondaryTextField;
        @BindView(R.id.separator_bottom)
        View separator_bottom;

        LocalSearchViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}