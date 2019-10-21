package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.wasabeef.glide.transformations.MaskTransformation;
import timber.log.Timber;

/**
 * 電話帳 お気に入りリストのアダプター
 */

public class ContactsFavoriteAdapter extends SimpleCursorTreeAdapter implements SectionIndexer {
    @Nullable private String[] mSectionStrings;
    @Nullable private int[] mSectionIndexes;
    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public ContactsFavoriteAdapter(Context context) {
        super(context, null, android.R.layout.simple_expandable_list_item_1,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                new int[]{android.R.id.text1},
                android.R.layout.simple_expandable_list_item_1,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                new int[]{android.R.id.text1});
    }

    public void setGroupCursor(Cursor cursor, Bundle extras) {
        if (cursor == null || extras == null) {
            mSectionStrings = null;
            mSectionIndexes = null;
        } else {
            mSectionStrings = extras.getStringArray("section_strings");
            mSectionIndexes = extras.getIntArray("section_indexes");
        }
        super.setGroupCursor(cursor);
    }

    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.element_list_item_contact_number, parent, false);
        view.setTag(new ContactsFavoriteAdapter.ChildViewHolder(view));
        return view;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ContactsFavoriteAdapter.ChildViewHolder holder = (ContactsFavoriteAdapter.ChildViewHolder) view.getTag();
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
        holder.number.setText(jp.pioneer.carsync.domain.content.ContactsContract.Phone.getNumber(cursor));
    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.element_list_item_contact_name, parent, false);
        view.setTag(new ContactsFavoriteAdapter.GroupViewHolder(view));
        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        ContactsFavoriteAdapter.GroupViewHolder holder = (ContactsFavoriteAdapter.GroupViewHolder) view.getTag();
        holder.separator.setVisibility(View.VISIBLE);
        if (cursor.isFirst()) {
            holder.separator.setVisibility(View.GONE);
        }
        holder.separatorBottom.setVisibility(View.GONE);
        if (cursor.isLast()&&!isExpanded) {
            holder.separatorBottom.setVisibility(View.VISIBLE);
        }
        Glide.with(context)
                .load(jp.pioneer.carsync.domain.content.ContactsContract.Contact.getPhotoUri(cursor))
                .error(R.drawable.p0071_nocontact)
                .bitmapTransform(new MaskTransformation(context, R.drawable.p0071_nocontact))
                .into(holder.icon);
        holder.name.setText(jp.pioneer.carsync.domain.content.ContactsContract.Contact.getDisplayName(cursor));
        holder.star.setVisibility(View.GONE);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }

    static class GroupViewHolder {
        @BindView(R.id.contact_icon) ImageView icon;
        @BindView(R.id.name_text) TextView name;
        @BindView(R.id.check_icon) ImageView star;
        @BindView(R.id.separator) View separator;
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

    @Override
    public Object[] getSections() {
        return mSectionStrings == null ? null : mSectionStrings.clone();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(mSectionIndexes == null){
            return 0;
        }

        if (mSectionIndexes.length <= sectionIndex) {
            return 0;
        }
        //Timber.d("getPositionForSection(sectionIndex=" + sectionIndex + ",result=" + mSectionIndexes[sectionIndex]+")");
        return mSectionIndexes[sectionIndex];
    }

    @Override
    public int getSectionForPosition(int position) {
        int result = 0;
        if(mSectionIndexes != null) {
            for (int i = 0; i < mSectionIndexes.length; ++i) {
                if (mSectionIndexes[i] <= position) {
                    result = i;
                } else {
                    break;
                }
            }
        }

        //Timber.d("getSectionForPosition(position=" + position + ",result=" +result+")");
        return result;
    }
}
