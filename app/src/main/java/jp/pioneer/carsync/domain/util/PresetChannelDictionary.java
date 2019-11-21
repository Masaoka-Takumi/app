package jp.pioneer.carsync.domain.util;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * Created by tsuyosh on 2015/08/03.
 */
public class PresetChannelDictionary {
    private int mLifeTime;
    private Map<PresetKey, Integer> mPresetChannelMap = new HashMap<>();
    private Map<PresetKey, Integer> mPresetChannelMapSph = new HashMap<>();
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
        //makeDummyPreset();
        initSphPresetList();
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

    private void makeDummyPreset(){
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 1000L,1,2,3), 1);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 200L,1,3,3), 2);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 300L,1,2,3), 3);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 200L,1,2,3), 4);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 2000L,1,2,3), 5);
        mPresetChannelMap.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 3000L,1,2,3), 6);
    }

    private void initSphPresetList(){
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 1000L,1,2,3), 1);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 200L,1,3,3), 2);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 300L,1,2,3), 3);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 1000L,1,2,3), 4);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 200L,1,2,3), 5);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND1.code, 3000L,1,2,3), 6);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 1000L,1,2,3), 1);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 200L,1,2,3), 2);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 300L,1,2,3), 3);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 1000L,1,2,3), 4);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 2000L,1,2,3), 5);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND2.code, 3000L,1,2,3), 6);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 1000L,1,2,3), 1);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 200L,1,2,3), 2);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 300L,1,2,3), 3);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 1000L,1,2,3), 4);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 2000L,1,2,3), 5);
        mPresetChannelMapSph.put(createKey(MediaSourceType.DAB, DabBandType.BAND3.code, 3000L,1,2,3), 6);
    }

    /**
     * Radio, HD Radio用. 条件を満たせば指定したソース、バンド、周波数とプリセットチャンネル番号を紐付けます
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

        PresetKey key = createKey(source, bandCode, frequency);
        deleteOldKey(key);
        mPresetChannelMap.put(key, mCurrentPresetChannelNumber);
        reset();
    }

    /**
     * DAB用. 条件を満たせば指定したソース、バンド、周波数、eid、sid、scidsとプリセットチャンネル番号を紐付けます
     * @param source
     * @param bandCode
     * @param frequency
     * @param eid
     * @param sid
     * @param scids
     */
    public void applyFrequencyDab(@NonNull MediaSourceType source, int bandCode, long frequency,int eid,long sid,int scids){
        Timber.d("applyFrequencyDab:source=%s, bandCode=%X, frequency=%s", source, bandCode, frequency);
        Timber.d(
                "applyFrequencyDab:mCurrentSourceType=%s, mCurrentBandCode=%X, mCurrentPresetChannelNumber=%s, mLastCommandApplyTime=%s",
                mCurrentSourceType, mCurrentBandCode, mCurrentPresetChannelNumber, mLastCommandApplyTime
        );

        if (!checkCurrentStatus(source, bandCode)) {
            reset();
            return;
        }

        PresetKey key = createKey(source, bandCode, frequency, eid, sid, scids);
        deleteOldKey(key);
        mPresetChannelMap.put(key,mCurrentPresetChannelNumber);
        //Timber.d("mPresetChannelMap:"+mPresetChannelMap.toString());
        reset();
    }

    /**
     * 過去に登録した該当のPCHの同Bandの異なるKeyはDictionaryから削除
     */
    private void deleteOldKey(PresetKey key){
        //Timber.d("deleteOldKey:containPCHNumber="+mCurrentPresetChannelNumber+" is "+mPresetChannelDabMap.containsValue(mCurrentPresetChannelNumber) );
        for(java.util.Iterator<Map.Entry<PresetKey, Integer>> i = mPresetChannelMap.entrySet().iterator();i.hasNext();) {
            Map.Entry<PresetKey, Integer> entry = i.next();
            if(entry.getValue()==mCurrentPresetChannelNumber) {
                if(entry.getKey().source==key.source&&entry.getKey().band==key.band){
                    //Timber.d("deleteOldKey:key.frequency="+entry.getKey().frequency+" Number="+mCurrentPresetChannelNumber);
                        Timber.d("deleteOldKey:key is not equal");
                    	i.remove();
                }
            }
        }
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

        PresetKey key = createKey(source, bandCode, channelNumber);
        deleteOldKey(key);
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
     * Radio, HD Radio用. 指定したソース、バンド、周波数にひも付けされているプリセットチャンネル番号を探して返します
     * @param source
     * @param bandCode
     * @param frequency
     * @return プリセットチャンネル番号または見つからない場合は-1を返します
     */
    public int findPresetChannelNumber(@NonNull MediaSourceType source, int bandCode, long frequency) {
        PresetKey key = createKey(source, bandCode, frequency);
        return findPresetChannelNumber(key);
    }

    private PresetKey createKey(@NonNull MediaSourceType source, int bandCode, long frequency) {
        return new PresetKey(source.code,bandCode,frequency);
    }

    /**
     * SXM用. 指定したソース、バンド、チャンネル番号にひも付けされているプリセットチャンネル番号を探して返します
     * @param source
     * @param bandCode
     * @param channelNumber
     * @return プリセットチャンネル番号または見つからない場合は-1を返します
     */
    public int findPresetChannelNumber(@NonNull MediaSourceType source, int bandCode, int channelNumber) {
        PresetKey key = createKey(source, bandCode, channelNumber);
        return findPresetChannelNumber(key);
    }

    private PresetKey createKey(@NonNull MediaSourceType source, int bandCode, int channelNumber) {
        return new PresetKey(source.code,bandCode,channelNumber);
    }

    /**
     * DAB用. 指定したソース、バンド、周波数、eid、sid、scidsにひも付けされているプリセットチャンネル番号を探して返します
     * @param source
     * @param bandCode
     * @param frequency
     * @param eid
     * @param sid
     * @param scids
     * @return プリセットチャンネル番号または見つからない場合は-1を返します
     */
    public int findPresetChannelNumberDab(@NonNull MediaSourceType source, int bandCode, long frequency,int eid,long sid,int scids) {
        PresetKey key = createKey(source, bandCode, frequency,eid,sid,scids);
        return findPresetChannelNumber(key);
    }

    private PresetKey createKey(@NonNull MediaSourceType source, int bandCode, long frequency,int eid,long sid,int scids) {
        return new PresetKey(source.code,bandCode,frequency,eid,sid,scids);
    }

    private int findPresetChannelNumber(PresetKey key) {
        int number = -1;
        Integer value;
        value = mPresetChannelMap.containsKey(key) ? mPresetChannelMap.get(key) : -1;
        if (value != null) {
            number = value;
        }
        Timber.d("findPresetChannelNumber:key.frequency=%d, key.eid=%d, key.sid=%d, key.scids=%d, number=%s", key.frequency, key.eid, key.sid, key.scids, number);
        return number;
    }

    public int findPresetChannelNumberDabSph(@NonNull MediaSourceType source, int bandCode, long frequency,int eid,long sid,int scids) {
        PresetKey key = createKey(source, bandCode, frequency,eid,sid,scids);
        return findPresetChannelNumberSph(key);
    }

    private int findPresetChannelNumberSph(PresetKey key) {
        int number = -1;
        Integer value;
        value = mPresetChannelMapSph.containsKey(key) ? mPresetChannelMapSph.get(key) : -1;
        if (value != null) {
            number = value;
        }
        Timber.d("findPresetChannelNumberSph:key.frequency=%d, key.eid=%d, key.sid=%d, key.scids=%d, number=%s", key.frequency, key.eid, key.sid, key.scids, number);
        return number;
    }

    static private class PresetKey{
        int source;
        int band;
        long frequency;
        /** EID. */
        public int eid;
        /** SID. */
        public long sid;
        /** SCIdS. */
        public int scids;
        int channelNumber;

        //Radio, HD Radio用
        PresetKey(int source,int band, long frequency){
            this.source = source;
            this.band = band;
            this.frequency = frequency;
            this.eid = 0;
            this.sid = 0;
            this.scids = 0;
            this.channelNumber = 0;
        }
        //SXM用
        PresetKey(int source,int band, int channelNumber){
            this.source = source;
            this.band = band;
            this.frequency = 0;
            this.eid = 0;
            this.sid = 0;
            this.scids = 0;
            this.channelNumber = channelNumber;
        }
        //DAB用
        PresetKey(int source,int band, long frequency,int eid,long sid,int scids){
            this.source = source;
            this.band = band;
            this.frequency = frequency;
            this.eid = eid;
            this.sid = sid;
            this.scids = scids;
            this.channelNumber = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PresetKey)) return false;
            PresetKey presetKey = (PresetKey) o;
            return source == presetKey.source &&
                    band == presetKey.band &&
                    frequency == presetKey.frequency &&
                    eid == presetKey.eid &&
                    sid == presetKey.sid &&
                    scids == presetKey.scids &&
                    channelNumber == presetKey.channelNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, band, frequency, eid, sid, scids, channelNumber);
        }
    }
}
