package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.wasabeef.glide.transformations.MaskTransformation;

/**
 * 検索用連絡先Adapter
 */

public class SearchContactAdapter extends SimpleCursorTreeAdapter {

    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public SearchContactAdapter(Context context) {
        super(context, null, android.R.layout.simple_expandable_list_item_1,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                new int[]{android.R.id.text1},
                android.R.layout.simple_expandable_list_item_1,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                new int[]{android.R.id.text1});
    }

    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.element_list_item_contact_number_search, parent, false);
        view.setTag(new ChildViewHolder(view));
        return view;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ChildViewHolder holder = (ChildViewHolder) view.getTag();
        holder.number.setText(jp.pioneer.carsync.domain.content.ContactsContract.Phone.getNumber(cursor));
        switch (jp.pioneer.carsync.domain.content.ContactsContract.Phone.getNumberType(cursor)) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                holder.type.setImageResource(R.drawable.p0052_home);
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                holder.type.setImageResource(R.drawable.p0051_mobile);
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                holder.type.setImageResource(R.drawable.p0053_business);
                break;
            default:
                holder.type.setImageResource(R.drawable.p0050_phone);
                break;
        }
    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.element_list_item_contact_name_search, parent, false);
        view.setTag(new GroupViewHolder(view));
        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        GroupViewHolder holder = (GroupViewHolder) view.getTag();
        holder.mark.setVisibility(View.GONE);
        holder.separator.setVisibility(View.VISIBLE);
        if (cursor.isFirst()) {
            holder.separator.setVisibility(View.GONE);
        }
        holder.separatorBottom.setVisibility(View.GONE);
        if (cursor.isLast()) {
            holder.separatorBottom.setVisibility(View.VISIBLE);
        }
        Glide.with(context)
                .load(jp.pioneer.carsync.domain.content.ContactsContract.Contact.getPhotoUri(cursor))
                .error(R.drawable.p0071_nocontact)
                .bitmapTransform(new MaskTransformation(context,R.drawable.p0071_nocontact))
                .into(holder.icon);
        holder.name.setText(jp.pioneer.carsync.domain.content.ContactsContract.Contact.getDisplayName(cursor));
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }

    static class GroupViewHolder {
        @BindView(R.id.contact_icon) ImageView icon;
        @BindView(R.id.name_text) TextView name;
        @BindView(R.id.separator) View separator;
        @BindView(R.id.check_icon) ImageView mark;
        @BindView(R.id.separator_bottom) View separatorBottom;
        public GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ChildViewHolder {
        @BindView(R.id.number_type) ImageView type;
        @BindView(R.id.number_text) TextView number;

        public ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
