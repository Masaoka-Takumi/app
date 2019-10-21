package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * アルバムアートリストAdapter
 */

public class AlbumArtAdapter extends AbstractCursorAdapter {
    private int mSelectedPosition = -1;
    private boolean mIsSphCarDevice = false;
    /**
     * コンストラクタ
     *
     * @param context
     * @param c
     */
    public AlbumArtAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = LayoutInflater.from(context);
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_album_art, parent, false);
        view.setTag(new AlbumArtAdapter.ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AlbumArtAdapter.ViewHolder holder = (AlbumArtAdapter.ViewHolder) view.getTag();
        if (holder.id != AppMusicContract.Album.getId(cursor)) {
            holder.id = AppMusicContract.Album.getId(cursor);
            Glide.with(context)
                    .load(AppMusicContract.Album.getArtworkUri(cursor))
                    .error(R.drawable.p0070_noimage)
                    .into(holder.albumArt);
        }
        if(mSelectedPosition==cursor.getPosition()&&!mIsSphCarDevice){
            holder.itemSelect.setVisibility(View.VISIBLE);
        }else{
            holder.itemSelect.setVisibility(View.INVISIBLE);
        }
    }

    static class ViewHolder {
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.item_select) RelativeLayout itemSelect;
        long id;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
