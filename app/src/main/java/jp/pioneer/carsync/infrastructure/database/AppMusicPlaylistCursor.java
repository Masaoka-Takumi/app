package jp.pioneer.carsync.infrastructure.database;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * PMGPlayerのPlaylistCursor.
 */
public class AppMusicPlaylistCursor implements Cursor {
    private Cursor mCursor;
    private int mPosition;
    private ShuffleMode mShuffleMode;
    private SmartPhoneRepeatMode mRepeatMode;
    private List<Integer> mOriginalPositions;

    private Action mAction;
    private int mCurrentIndex = -1;
    public enum Action{
        NEXT,
        PREVIOUS,
        COMPLETE,
        PREPARE,
        SELECT
    }

    /**
     * コンストラクタ
     *
     * @param cursor 楽曲一覧
     * @param repeatMode リピートモード
     * @param shuffleMode シャッフルモード
     * @throws NullPointerException {@code cursor}がnull
     * @throws NullPointerException {@code repeatMode}がnull
     * @throws NullPointerException {@code shuffleMode}がnull
     */
    public AppMusicPlaylistCursor(@NonNull Cursor cursor, @NonNull SmartPhoneRepeatMode repeatMode, @NonNull ShuffleMode shuffleMode) {
        mCursor = checkNotNull(cursor);
        mPosition = -1;
        setRepeatMode(checkNotNull(repeatMode));
        setShuffleMode(checkNotNull(shuffleMode));
    }

    /**
     * シャッフルモード設定.
     *
     * @param shuffleMode 設定するシャッフルモード
     * @return 本オブジェクト
     * @throws NullPointerException {@code shuffleMode}がnull
     */
    public AppMusicPlaylistCursor setShuffleMode(@NonNull ShuffleMode shuffleMode) {
        checkNotNull(shuffleMode);

        if (mShuffleMode != shuffleMode) {
            mShuffleMode = shuffleMode;
            if (shuffleMode == ShuffleMode.OFF) {
                mOriginalPositions = null;
                mPosition = -1;
            } else {
                mOriginalPositions = new ArrayList<>();
                int pos = mCursor.getPosition();
                int count = getCount();
                for (int i = 0; i < count; i++) {
                    if (i != pos) {
                        mOriginalPositions.add(i);
                    }
                }

                Collections.shuffle(mOriginalPositions);
                if (pos != -1) {
                    mOriginalPositions.add(0, pos);
                    SmartPhoneRepeatMode repeatMode = mRepeatMode;
                    mRepeatMode = SmartPhoneRepeatMode.OFF;
                    moveToFirst();
                    mRepeatMode = repeatMode;
                }
            }
        }

        return this;
    }

    /**
     * リピートモード設定.
     *
     * @param repeatMode 設定するリピートモード
     * @return 本オブジェクト
     * @throws NullPointerException {@code repeatMode}がnull
     */
    public AppMusicPlaylistCursor setRepeatMode(@NonNull SmartPhoneRepeatMode repeatMode) {
        checkNotNull(repeatMode);

        mRepeatMode = repeatMode;
        return this;
    }

    /**
     * 次の楽曲へ移動.
     *
     * @return 次の楽曲へ移動できたか否か。 {@code true}:移動に成功 {@code false}:移動に失敗
     */
    public boolean skipNext() {
        SmartPhoneRepeatMode repeatMode = mRepeatMode;
        try {
            mRepeatMode = SmartPhoneRepeatMode.ALL;
            return moveToNext();
        } finally {
            mRepeatMode = repeatMode;
        }
    }

    /**
     * 前の曲へ移動.
     *
     * @return 前の曲へ移動できたか否か。 {@code true}:移動に成功 {@code false}:移動に失敗
     */
    public boolean skipPrevious() {
        SmartPhoneRepeatMode repeatMode = mRepeatMode;
        try {
            mRepeatMode = SmartPhoneRepeatMode.ALL;
            return moveToPrevious();
        } finally {
            mRepeatMode = repeatMode;
        }
    }

    /**
     * 検索準備.
     *
     * @param action 検索時の動作
     */
    public void prepareSearch(Action action){
        mAction = action;
        mCurrentIndex = -1;
    }

    /**
     * 再検索
     *
     * @return 検索に成功したか否か
     */
    public boolean reSearch(){
        if(mCurrentIndex == -1){
            mCurrentIndex = getPosition();
        } else if(mCurrentIndex == getPosition()) {
            return false;
        }
        if(mAction == Action.PREVIOUS){
            return skipPrevious();
        } else {
            return skipNext();
        }
    }

    /**
     * 指定したトラック番号へ移動.
     *
     * @param trackNo トラック番号
     */
    public boolean selectTrack(int trackNo){
        return moveToPositionForRepeatModeOff(trackNo);
    }

    /**
     * シャッフル状態取得.
     *
     * @return シャッフル状態
     */
    public ShuffleMode getShuffleMode(){
        return mShuffleMode;
    }

    /**
     * リピート状態取得.
     *
     * @return リピート状態
     */
    public SmartPhoneRepeatMode getSmartPhoneRepeatMode(){
        return mRepeatMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition() {
        if (mShuffleMode == ShuffleMode.ON) {
            return mPosition;
        } else {
            return mCursor.getPosition();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(int offset) {
        return moveToPosition(getPosition() + offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToPosition(int position) {
        switch (mRepeatMode) {
            case OFF:
                return moveToPositionForRepeatModeOff(position);
            case ONE:
                return moveToPositionForRepeatModeOne(position);
            case ALL:
                return moveToPositionForRepeatModeAll(position);
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNext() {
        return moveToPosition(getPosition() + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToPrevious() {
        return moveToPosition(getPosition() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirst() {
        return (getPosition() == 0) && (getCount() > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLast() {
        int count = getCount();
        return (getPosition() == (count - 1)) && (count > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeFirst() {
        return (getCount() == 0) ? true : (getPosition() == -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterLast() {
        int count = getCount();
        return (count == 0) ? true : (getPosition() == count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndex(String columnName) {
        return mCursor.getColumnIndex(columnName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return mCursor.getColumnIndexOrThrow(columnName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int columnIndex) {
        return mCursor.getColumnName(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return mCursor.getColumnCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBlob(int columnIndex) {
        return mCursor.getBlob(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(int columnIndex) {
        return mCursor.getString(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        mCursor.copyStringToBuffer(columnIndex, buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(int columnIndex) {
        return mCursor.getShort(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(int columnIndex) {
        return mCursor.getInt(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(int columnIndex) {
        return mCursor.getLong(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat(int columnIndex) {
        return mCursor.getFloat(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble(int columnIndex) {
        return mCursor.getDouble(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType(int columnIndex) {
        return mCursor.getType(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNull(int columnIndex) {
        return mCursor.isNull(columnIndex);
    }

    @Override
    @Deprecated
    public void deactivate() {
    }

    @Override
    @Deprecated
    public boolean requery() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        mCursor.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mCursor.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContentObserver(ContentObserver observer) {
        mCursor.registerContentObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        mCursor.unregisterContentObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mCursor.registerDataSetObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mCursor.unregisterDataSetObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        mCursor.setNotificationUri(cr, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri getNotificationUri() {
        return mCursor.getNotificationUri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWantsAllOnMoveCalls() {
        return mCursor.getWantsAllOnMoveCalls();
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(23)
    @Override
    public void setExtras(Bundle extras) {
        mCursor.setExtras(extras);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getExtras() {
        return mCursor.getExtras();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle respond(Bundle extras) {
        return mCursor.respond(extras);
    }

    private boolean moveToPositionForRepeatModeOne(int position) {
        if (isBeforeFirst() || isAfterLast()) {
            SmartPhoneRepeatMode repeatMode = mRepeatMode;
            try {
                mRepeatMode = SmartPhoneRepeatMode.OFF;
                return moveToPosition(position);
            } finally {
                mRepeatMode = repeatMode;
            }
        } else {
            return (getCount() > 0);
        }
    }

    private boolean moveToPositionForRepeatModeAll(int position) {
        int count = getCount();
        if (count == 0) {
            return false;
        }

        SmartPhoneRepeatMode repeatMode = mRepeatMode;
        try {
            mRepeatMode = SmartPhoneRepeatMode.OFF;
            if (moveToPosition(position)) {
                return true;
            } else if (isAfterLast()) {
                return moveToFirst();
            } else if (isBeforeFirst()) {
                return moveToLast();
            } else {
                return false;
            }
        } finally {
            mRepeatMode = repeatMode;
        }
    }

    private boolean moveToPositionForRepeatModeOff(int position) {
        int count = getCount();
        if (mShuffleMode == ShuffleMode.ON) {
            if (position >= count) {
                mPosition = count;
                return false;
            }

            if (position < 0) {
                mPosition = -1;
                return false;
            }

            int originalPos = mOriginalPositions.get(position);
            if (mCursor.moveToPosition(originalPos)) {
                mPosition = position;
                return true;
            } else {
                mPosition = -1;
                return false;
            }
        } else {
            return mCursor.moveToPosition(position);
        }
    }
}
