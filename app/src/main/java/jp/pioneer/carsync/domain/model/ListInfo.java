package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * リスト情報.
 */
@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
public class ListInfo extends SerialVersion {
    /** 現在のフォーカス位置（1オリジン）. リスト情報がない場合は 0xFFFF */
    public int focusListIndex;
    /** 現在のABCサーチのインデックス文字. */
    public String abcSearchWord;
    /** トランザクション情報. */
    public final TransactionInfo transactionInfo = new TransactionInfo();
    /** ABCサーチ結果情報. */
    public boolean  abcSearchResult;

    /**
     * コンストラクタ.
     */
    public ListInfo() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        focusListIndex = 0;
        transactionInfo.reset();
        abcSearchResult = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("focusListIndex", focusListIndex)
                .add("transactionInfo", transactionInfo)
                .add("abcSearchResult", abcSearchResult)
                .toString();
    }

    /**
     * リスト項目（共通）.
     */
    public static class ListItem {
        /** リストインデックス（1オリジン）. */
        public int listIndex;
        /**
         * 文字列.
         * <p>
         * <pre>
         * Radio/HD Radio:PS/Call sign
         * DAB:service name
         * SiriusXM:channel name
         * USB:folder name / file name
         * </pre>
         */
        public String text;

        /**
         * P.CH番号取得.
         * <p>
         * DABは無い。（-1となる）
         *
         * @return P.CH番号
         */
        public int getPchNumber() {
            return -1;
        }

        /**
         * 周波数取得.
         * <p>
         * DABとSiriusXMは無い。（-1となる）
         *
         * @return 周波数
         */
        public long getFrequency() {
            return -1;
        }

        /**
         * 周波数単位.
         * <p>
         * DABとSiriusXMは無い。（nullとなる）
         *
         * @return 周波数単位
         */
        public TunerFrequencyUnit getFrequencyUnit() {
            return null;
        }

        /**
         * CHANNEL番号取得.
         * <p>
         * SiriusXMのみある。（SiriusXM以外は-1となる）
         *
         * @return CHANNEL番号
         */
        public int getChannelNumber() {
            return -1;
        }

        /**
         * Band取得.
         * <p>
         * DABは無い。（nullとなる）
         *
         * @return Band
         */
        public BandType getBand() {
            return null;
        }

        public UsbInfoType getUsbInfoType(){
            return null;
        }
    }

    /**
     * Radio/HD Radioのリスト項目.
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static class RadioListItem extends ListItem {
        /** P.CH番号. */
        public int pchNumber;
        /** 周波数. */
        public long frequency;
        /** 周波数単位. */
        public TunerFrequencyUnit frequencyUnit;
        /** Band. */
        public BandType band;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("listIndex", listIndex)
                    .add("text", text)
                    .add("pchNumber", pchNumber)
                    .add("frequency", frequency)
                    .add("frequencyUnit", frequencyUnit)
                    .add("band", band)
                    .toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getPchNumber() {
            return pchNumber;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getFrequency() {
            return frequency;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TunerFrequencyUnit getFrequencyUnit() {
            return frequencyUnit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BandType getBand() {
            return band;
        }
    }

    /**
     * DABのリスト項目.
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static class DabListItem extends ListItem {
        /** 周波数のINDEX. */
        public int index;
        /** EID. */
        public int eid;
        /** SID. */
        public long sid;
        /** SCIdS. */
        public int scids;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("listIndex", listIndex)
                    .add("text", text)
                    .add("index", index)
                    .add("eid", eid)
                    .add("sid", sid)
                    .add("scids", scids)
                    .toString();
        }
    }

    /**
     * SiriusXMのリスト項目.
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static class SxmListItem extends ListItem {
        /** P.CH番号. */
        public int pchNumber;
        /** CHANNEL番号. */
        public int channelNumber;
        /** Band. */
        public BandType band;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("listIndex", listIndex)
                    .add("text", text)
                    .add("pchNumber", pchNumber)
                    .add("channelNumber", channelNumber)
                    .add("band", band)
                    .toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getPchNumber() {
            return pchNumber;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BandType getBand() {
            return band;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getChannelNumber() {
            return channelNumber;
        }
    }

    /**
     * USBのリスト項目.
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static class UsbListItem extends ListItem {
        /** リストの情報種別. */
        public UsbInfoType type;

        @Override
        public UsbInfoType getUsbInfoType() {
            return type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("listIndex", listIndex)
                    .add("text", text)
                    .add("type", type)
                    .toString();
        }
    }

    /**
     * トランザクション情報.
     */
    public static class TransactionInfo {
        /** トランザクションID. */
        public int id;
        /** ソース種別. */
        public MediaSourceType sourceType;
        /** リスト種別. */
        public ListType listType;
        /** リストインデックス. */
        public int listIndex;
        /** 件数. */
        public int total;
        /** 一括要求可能件数. リスト情報が存在しない場合は 0xFFFF */
        public int limit;
        /** フォーカス位置. リスト情報が存在しない場合は 0xFFFF */
        public int focusListIndex;
        /** リスト項目. */
        public SparseArrayCompat<ListItem> items;
        /** 階層名. 対応していない場合はNULL */
        public String hierarchyName;

        /**
         * リセット.
         */
        public void reset() {
            id = 0;
            sourceType = null;
            listType = null;
            listIndex = 1;
            total = 0;
            limit = 0;
            focusListIndex = 1;
            items = null;
            hierarchyName = "";
        }

        /**
         * 初期情報設定.
         *
         * @param sourceType ソース種別
         * @param listType リスト種別
         * @param total 件数
         * @param limit 一括要求可能件数
         * @param focusListIndex フォーカス位置
         * @param hierarchyName 階層名
         * @throws NullPointerException {@code sourceType}、{@code listType}、{@code hierarchyName}がnull
         */
        public void setInitialInfo(@NonNull MediaSourceType sourceType, @NonNull ListType listType, int total, int limit, int focusListIndex, @NonNull String hierarchyName) {
            this.sourceType = checkNotNull(sourceType);
            this.listType = checkNotNull(listType);
            this.total = total;
            this.limit = limit;
            this.focusListIndex = focusListIndex;
            this.hierarchyName = checkNotNull(hierarchyName);

            this.listIndex = 1 - limit; // next()で足し算して1になります
            this.items = new SparseArrayCompat<>(total);
        }

        /**
         * さらに項目があるか否か取得.
         * <p>
         * {@code true}となった場合、項目を件数分取得し終えていない。
         *
         * @return {@code true}:項目がある。{@code false}:それ以外。（項目を全て取得した）
         */
        public boolean hasNext() {
            if (total == 0 || listIndex + limit > total) {
                return false;
            } else {
                id++;
                listIndex += limit;

                return true;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("id", id)
                    .add("sourceType", sourceType)
                    .add("listType", listType)
                    .add("total", total)
                    .add("limit", limit)
                    .add("listIndex", listIndex)
                    .add("hierarchyName", hierarchyName)
                    .toString();
        }
    }
}
