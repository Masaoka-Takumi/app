package jp.pioneer.carsync.domain.util;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * Created by tsuyosh on 2015/08/03.
 */
public class PresetChannelDictionary {
    private int mLifeTime;
    private Map<Long, Integer> mPresetChannelMap = new HashMap<>();

    private long mLastCommandApplyTime;
    private MediaSourceType mCurrentSourceType;
    private int mCurrentBandCode;
    private int mCurrentPresetChannelNumber;

    public PresetChannelDictionary() {
        this(3000);
    }

    public PresetChannelDictionary(int lifeTime) {
        mLifeTime = lifeTime;
        reset();
        //makeDummyMap();
    }

    /**
     * 現在のソース、バンド、Presetチャンネル番号をセットします。Presetコマンドを送信する際に呼ばれます
     * @param source
     * @param bandCode
     * @param presetChannelNumber
     */
    public void applyPresetCommand(MediaSourceType source, int bandCode, int presetChannelNumber) {
        mCurrentSourceType = source;
        mCurrentBandCode = bandCode;
        mCurrentPresetChannelNumber = presetChannelNumber;
        mLastCommandApplyTime = SystemClock.elapsedRealtime();
    }

    public void reset() {
        mCurrentSourceType = null;
        mCurrentBandCode = -1;
        mCurrentPresetChannelNumber = -1;
        mLastCommandApplyTime = -1;
    }

    private void makeDummyMap(){
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 1000L), 1);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 200L), 2);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 300L), 3);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 1000L), 4);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 2000L), 5);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 3000L), 6);
    }

    /**
     * Radio, DAB, HD Radio用. 条件を満たせば指定したソース、バンド、周波数とプリセットチャンネル番号を紐付けます
     * @param source
     * @param bandCode
     * @param frequency
     */
    public void applyFrequency(@NonNull MediaSourceType source, int bandCode, long frequency) {
        Timber.d("source=%s, bandCode=%X, frequency=%s", source, bandCode, frequency);
        Timber.d(
                "mCurrentSourceType=%s, mCurrentBandCode=%X, mCurrentPresetChannelNumber=%s, mLastCommandApplyTime=%s",
                mCurrentSourceType, mCurrentBandCode, mCurrentPresetChannelNumber, mLastCommandApplyTime
        );

        if (!checkCurrentStatus(source, bandCode)) {
            reset();
            return;
        }

        long key = createKey(source, bandCode, frequency);
        Timber.d("key=%X", key);
        mPresetChannelMap.put(key, mCurrentPresetChannelNumber);
        reset();
    }

    /**
     * SXM用. 条件を満たせば指定したソース、バンド、チャンネル番号とプリセットチャンネル番号を紐付けます
     * @param source
     * @param bandCode
     * @param channelNumber
     */
    public void applyChannel(@NonNull MediaSourceType source, int bandCode, int channelNumber) {
        Timber.d("source=%s, bandCode=%X, channelNumber=%s", source, bandCode, channelNumber);
        Timber.d(
                "mCurrentSourceType=%s, mCurrentBandCode=%X, mCurrentPresetChannelNumber=%s, mLastCommandApplyTime=%s",
                mCurrentSourceType, mCurrentBandCode, mCurrentPresetChannelNumber, mLastCommandApplyTime
        );

        if (!checkCurrentStatus(source, bandCode)) {
            reset();
            return;
        }

        long key = createKey(source, bandCode, channelNumber);
        Timber.d("key=%X", key);
        mPresetChannelMap.put(key, mCurrentPresetChannelNumber);
        reset();
    }

    /**
     * プリセットチャンネル番号辞書に反映して問題無ければtrueを返します
     * @param source
     * @param bandCode
     * @return
     */
    private boolean checkCurrentStatus(@NonNull MediaSourceType source, int bandCode) {
        boolean valid = true;
        if (mCurrentSourceType == null || mCurrentBandCode == -1 || mCurrentPresetChannelNumber == -1) {
            // applyPresetCommandを呼んでない or resetされた場合
            Timber.d("null");
            valid = false;
        } else if (mCurrentSourceType != source || mCurrentBandCode != bandCode) {
            // applyPresetCommandで指定したソース、バンドと食い違う
            Timber.d("not equal");
            valid = false;
        } else if (mLastCommandApplyTime < SystemClock.elapsedRealtime() - mLifeTime) {
            // applyPresetCommandで指定してから時間が経ちすぎ
            Timber.d("expired");
            valid = false;
        }
        return valid;
    }

    /**
     * Radio, DAB, HD Radio用. 指定したソース、バンド、周波数にひも付けされているプリセットチャンネル番号を探して返します
     * @param source
     * @param bandCode
     * @param frequency
     * @return プリセットチャンネル番号または見つからない場合は-1を返します
     */
    public int findPresetChannelNumber(@NonNull MediaSourceType source, int bandCode, long frequency) {
        long key = createKey(source, bandCode, frequency);
        return findPresetChannelNumber(key);
    }

    private long createKey(@NonNull MediaSourceType source, int bandCode, long frequency) {
        long key = frequency;
        key |= (long)(bandCode & 0xFF) << 32;
        key |= (long)(source.code & 0xFF) << 40;
        return key;
    }

    /**
     * SXM用. 指定したソース、バンド、チャンネル番号にひも付けされているプリセットチャンネル番号を探して返します
     * @param source
     * @param bandCode
     * @param channelNumber
     * @return プリセットチャンネル番号または見つからない場合は-1を返します
     */
    public int findPresetChannelNumber(@NonNull MediaSourceType source, int bandCode, int channelNumber) {
        long key = createKey(source, bandCode, channelNumber);
        return findPresetChannelNumber(key);
    }

    private long createKey(@NonNull MediaSourceType source, int bandCode, int channelNumber) {
        long key = (long) channelNumber;
        key |= (long)(bandCode & 0xFF) << 32;
        key |= (long)(source.code & 0xFF) << 40;
        return key;
    }

    private int findPresetChannelNumber(long key) {
        int number = mPresetChannelMap.containsKey(key) ? mPresetChannelMap.get(key) : -1;
        Timber.d("key=%X, number=%s", key, number);
        return number;
    }
}
