package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;

/**
 * ジャンルカードAdapter
 */

public class GenreCardAdapter extends AbstractCardAdapter {

    private GenreAdapter mCursorAdapter;
    private int mCheckedPos = -1;
    private LongSparseArray<Boolean> mIds = new LongSparseArray<>();
    private boolean mIsSphCarDevice = false;

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    public void setCheckedPos(int checkedPos) {
        mCheckedPos = checkedPos;
        notifyDataSetChanged();
    }

    public int getCheckedPos() {
        return mCheckedPos;
    }

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public GenreCardAdapter(Context context, Cursor c) {
        super(context);
        mCursorAdapter = new GenreAdapter(mContext, c) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = mInflater.inflate(R.layout.element_list_item_cardview, parent, false);
                AbstractCardAdapter.ViewHolder holder = new AbstractCardAdapter.ViewHolder(view);
                view.setTag(holder);
                return view;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void bindView(View view, Context context, Cursor cursor) {
                AbstractCardAdapter.ViewHolder holder = (AbstractCardAdapter.ViewHolder) view.getTag();
                long id = AppMusicContract.Genre.getId(cursor);
                holder.title.setText(AppMusicContract.Genre.getName(cursor));
                if (!mIds.get(id,false)) {
                    Glide.with(context)
                            .load(AppMusicContract.Genre.getArtworkUri(cursor))
                            .error(R.drawable.p0070_noimage)
                            .bitmapTransform(new CropTransformation(context), new BlurTransformation(context, 30))
                            .listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                @SuppressWarnings("unchecked")
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    Glide.with(context)
                                            .load(R.drawable.p0070_noimage)
                                            .bitmapTransform(new CropTransformation(context), new BlurTransformation(context, 30))
                                            .into(holder.background);
                                    mIds.put(id, true);
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    // do something
                                    return false;
                                }
                            })
                            .into(holder.background);
                    //new ColorFilterTransformation(context,R.color.card_view_color_filter)
                }else{
                    Glide.with(context)
                            .load(R.drawable.p0070_noimage)
                            .bitmapTransform(new CropTransformation(context), new BlurTransformation(context, 30))
                            .into(holder.background);
                }
                if (cursor.getPosition() == mCheckedPos&&!mIsSphCarDevice) {
                    holder.itemSelect.setVisibility(View.VISIBLE);
                } else {
                    holder.itemSelect.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(AbstractCardAdapter.ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        holder.itemView.setOnClickListener(view -> {
            mCursorAdapter.getCursor().moveToPosition(holder.getAdapterPosition());
            onItemClick(mCursorAdapter.getCursor());
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return mCursorAdapter.getItemId(position);
    }

    /**
     * データ更新
     *
     * @param data Cursor
     * @param args Bundle
     */
    public void swapAdapter(Cursor data, Bundle args) {
        mCursorAdapter.swapCursor(data, args);
        notifyDataSetChanged();
    }

    public int getPositionForSection(int sectionIndex) {
        return mCursorAdapter.getPositionForSection(sectionIndex);
    }

    public int getSectionForPosition(int position) {
        return mCursorAdapter.getSectionForPosition(position);
    }

    public Object getItem(int position) {
        return mCursorAdapter.getItem(position);
    }

    public int getCount() {
        return mCursorAdapter.getCount();
    }

    public int getSectionCount() {
        return mCursorAdapter.getSectionCount();
    }

    public String getSectionString(int sectionIndex) {
        return (String) mCursorAdapter.getSections()[sectionIndex];
    }

}
