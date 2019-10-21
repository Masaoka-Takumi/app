package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
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
import jp.pioneer.carsync.presentation.util.TextViewUtil;

/**
 * NowPlayingListのAdapter
 */

public class NowPlayAdapter extends AbstractCursorAdapter {
    private long  mNowPlaySongId;
    private boolean  mIsPlaying;
    public NowPlayAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, false);
        mInflater = LayoutInflater.from(context);
    }

    public void setNowPlaySongId(long nowPlaySongId, boolean isPlaying) {
        mNowPlaySongId = nowPlaySongId;
        mIsPlaying = isPlaying;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music_nowplay, parent, false);
        NowPlayAdapter.ViewHolder holder = new NowPlayAdapter.ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        NowPlayAdapter.ViewHolder holder = (NowPlayAdapter.ViewHolder) view.getTag();
        if(AppMusicContract.Song.getId(cursor)==mNowPlaySongId){
            holder.nowPlayIcon.setVisibility(View.VISIBLE);
            // AnimationDrawableのXMLリソースを指定
            AnimationDrawable frameAnimation = (AnimationDrawable) holder.nowPlayIcon.getBackground();
            if(mIsPlaying) {
                // アニメーションの開始
                frameAnimation.start();
            }else{
                // アニメーションの停止
                frameAnimation.stop();
            }
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_selected));
        } else{
            holder.nowPlayIcon.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }
        holder.musicName.setText(AppMusicContract.Song.getTitle(cursor));
        TextViewUtil.setMarqueeTextIfChanged(holder.musicName, AppMusicContract.Song.getTitle(cursor));
        holder.artistName.setText(AppMusicContract.Song.getArtist(cursor));
        Glide.with(context)
                .load(AppMusicContract.Song.getArtworkUri(cursor))
                .error(R.drawable.p0070_noimage)
                .into(holder.albumArt);
    }

    static class ViewHolder {
        @BindView(R.id.nowplay_icon) ImageView nowPlayIcon;
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView musicName;
        @BindView(R.id.subtitle_text) TextView artistName;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
