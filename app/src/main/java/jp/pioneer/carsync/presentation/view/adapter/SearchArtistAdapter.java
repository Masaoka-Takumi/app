package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * 検索用アーティストAdapter
 */

public class SearchArtistAdapter extends AbstractCursorAdapter {

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public SearchArtistAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music_search, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.artistName.setText(AppMusicContract.Artist.getArtist(cursor));
        holder.id = AppMusicContract.Artist.getId(cursor);
        Glide.with(context)
                .load(AppMusicContract.Artist.getArtworkUri(cursor))
                .error(R.drawable.p0070_noimage)
                .into(holder.albumArt);
    }

    private String getTracks(Cursor cursor) {
        int count = AppMusicContract.Artist.getNumberOfAlbums(cursor);
        return String.format(mContext.getResources().getString(R.string.ply_001), count);
    }

    /**
     * ジャケット押下処理
     *
     * @param id アーティストID
     */
    protected void onJacketClick(long id) {

    }

    class ViewHolder {
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView artistName;
        long id;
        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.play_button)
        public void onClick(View v) {
            onJacketClick(id);
        }
    }
}
