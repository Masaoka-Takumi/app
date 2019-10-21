package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * 検索用楽曲Adapter
 */

public class SearchSongAdapter extends AbstractCursorAdapter {
    private int mOrientation;
    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public SearchSongAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music, parent, false);
        ViewHolder holder = new ViewHolder(view);
        if(mOrientation == Configuration.ORIENTATION_LANDSCAPE){
            view.setPadding((int)mContext.getResources().getDimension(R.dimen.music_list_landscape_let_padding),0,0,0);
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.musicName.setText(AppMusicContract.Song.getTitle(cursor));
        holder.artistName.setText(AppMusicContract.Song.getArtist(cursor));
        Glide.with(context)
                .load(AppMusicContract.Song.getArtworkUri(cursor))
                .error(R.drawable.p0070_noimage)
                .into(holder.albumArt);
    }

    static class ViewHolder {
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView musicName;
        @BindView(R.id.subtitle_text) TextView artistName;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
