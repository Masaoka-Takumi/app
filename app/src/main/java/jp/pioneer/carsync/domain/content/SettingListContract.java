package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SettingListType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リストコントラクト
 */
public class SettingListContract {

    /**
     * {@link QuerySettingListParams} のビルダー.
     */
    public static class QuerySettingListParamsBuilder {
        /**
         * デバイスリストを取得する {@link QuerySettingListParams} 生成.
         *
         * @return デバイスリストを取得するクエリーパラメータ
         */
        @NonNull
        public static QuerySettingListParams createDeviceList() {
            return new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    false,
                    false,
                    false
            );
        }

        /**
         * サーチリストを取得する {@link QuerySettingListParams} 生成.
         *
         * @return サーチリストを取得するクエリーパラメータ
         */
        @NonNull
        public static QuerySettingListParams createSearchList() {
            return new QuerySettingListParams(
                    SettingListType.SEARCH_LIST,
                    false,
                    false,
                    false
            );
        }

        /**
         * A2DPリストを取得する {@link QuerySettingListParams} 生成.
         *
         * @return A2DPリストを取得するクエリーパラメータ
         */
        @NonNull
        public static QuerySettingListParams createA2dpList() {
            return new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    true,
                    false,
                    false
            );
        }

        /**
         * 接続中デバイスを取得する {@link QuerySettingListParams} 生成.
         *
         * @return 接続中デバイスを取得するクエリーパラメータ
         */
        @NonNull
        public static QuerySettingListParams createAudioConnectedDevice() {
            return new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    false,
                    false,
                    true
            );
        }
    }

    /**
     * 設定リストの共通列名
     */
    public static class SettingListBaseColumn {
        /**
         * 行のユニークID.
         * <p>
         * <P>Type: long</P>
         */
        public static final String _ID = "_id";

        /**
         * BDアドレス
         */
        public static final String BD_ADDRESS = "bd_address";

        /**
         * デバイス名
         */
        public static final String DEVICE_NAME = "device_name";

        /**
         * Audio接続対応
         */
        public static final String AUDIO_SUPPORTED = "audio_supported";

        /**
         * Phone接続対応
         */
        public static final String PHONE_SUPPORTED = "phone_supported";

        /**
         * ID取得.
         *
         * @param cursor 設定リストのCursor
         * @return ID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
        }

        /**
         * BDアドレス取得.
         *
         * @param cursor 設定リストのCursor
         * @return BDアドレス
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getBdAddress(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(BD_ADDRESS));
        }

        /**
         * デバイス名取得.
         *
         * @param cursor 設定リストのCursor
         * @return デバイス名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getDeviceName(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(DEVICE_NAME));
        }

        /**
         * Audio接続対応有無取得.
         *
         * @param cursor 設定リストのCursor
         * @return Audio接続対応しているかどうか。 {@code true}:Audio接続に対応している。 {@code false}:Audioに対応していない。
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isAudioSupported(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(AUDIO_SUPPORTED)) == 1;
        }

        /**
         * Phone接続対応有無取得.
         *
         * @param cursor 設定リストのCursor
         * @return Phone接続対応しているかどうか。 {@code true}:Phone接続に対応している。 {@code false}:Phoneに対応していない。
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isPhoneSupported(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(PHONE_SUPPORTED)) == 1;
        }
    }

    /**
     * デバイスリスト
     */
    public static class DeviceList extends SettingListBaseColumn {

        /**
         * Phone接続状態.
         */
        public enum PhoneConnectStatus{
            STATUS_DEFAULT(0),
            STATUS_CONNECTING(1),
            STATUS_CONNECTED(2),
            STATUS_CONNECT_FAILED(3),
            STATUS_DISCONNECTING(4),
            STATUS_DISCONNECTED(5),
            STATUS_DISCONNECT_FAILED(6);

            /** 定義値 */
            public final int code;

            /**
             * コンストラクタ
             *
             * @param code 定義値
             */
            PhoneConnectStatus(int code){
                this.code = code;
            }

            /**
             * 定義値から取得.
             *
             * @param code 定義値
             * @return 定義値に該当するAudioConnectStatus
             * @throws IllegalArgumentException 定義値に該当するものがない
             */
            public static PhoneConnectStatus valueOf(int code) {
                for (PhoneConnectStatus value : values()) {
                    if (value.code == code) {
                        return value;
                    }
                }

                throw new IllegalArgumentException("invalid code: " + code);
            }

            public boolean isConnecting(){
                return this == STATUS_CONNECTING;
            }

            public boolean isDisconnecting(){
                return this == STATUS_DISCONNECTING;
            }
        }

        /**
         * Audio接続状態.
         */
        public enum AudioConnectStatus{
            STATUS_DEFAULT(0),
            STATUS_CONNECTING(1),
            STATUS_CONNECTED(2),
            STATUS_CONNECT_FAILED(3);

            /** 定義値 */
            public final int code;

            /**
             * コンストラクタ
             *
             * @param code 定義値
             */
            AudioConnectStatus(int code){
                this.code = code;
            }

            /**
             * 定義値から取得.
             *
             * @param code 定義値
             * @return 定義値に該当するAudioConnectStatus
             * @throws IllegalArgumentException 定義値に該当するものがない
             */
            public static AudioConnectStatus valueOf(int code) {
                for (AudioConnectStatus value : values()) {
                    if (value.code == code) {
                        return value;
                    }
                }

                throw new IllegalArgumentException("invalid code: " + code);
            }

            public boolean isConnecting(){
                return this == STATUS_CONNECTING;
            }
        }

        /**
         * ペアリング解除状態.
         */
        public enum DeleteStatus{
            STATUS_DEFAULT(0),
            STATUS_DELETING(7),
            STATUS_DELETED(8),
            STATUS_DELETE_FAILED(9);

            /** 定義値 */
            public final int code;

            /**
             * コンストラクタ
             *
             * @param code 定義値
             */
            DeleteStatus(int code){
                this.code = code;
            }

            /**
             * 定義値から取得.
             *
             * @param code 定義値
             * @return 定義値に該当するAudioConnectStatus
             * @throws IllegalArgumentException 定義値に該当するものがない
             */
            public static DeleteStatus valueOf(int code) {
                for (DeleteStatus value : values()) {
                    if (value.code == code) {
                        return value;
                    }
                }

                throw new IllegalArgumentException("invalid code: " + code);
            }

            public boolean isDeleting(){
                return this == STATUS_DELETING;
            }
        }

        /**
         * Audio接続中
         */
        public static final String AUDIO_CONNECTED = "audio_connected";

        /**
         * Phone1接続中
         */
        public static final String PHONE_1_CONNECTED = "phone_1_connected";

        /**
         * Phone2接続中
         */
        public static final String PHONE_2_CONNECTED = "phone_2_connected";

        /**
         * ラストオーディオデバイス状態
         */
        public static final String LAST_AUDIO_DEVICE = "last_audio_device";

        /**
         * 車載器と連携中
         */
        public static final String SESSION_CONNECTED = "session_connected";

        /**
         * Audio接続状態
         */
        public static final String AUDIO_CONNECT_STATUS = "audio_connect_status";

        /**
         * Phone接続状態
         */
        public static final String PHONE_CONNECT_STATUS = "phone_connect_status";

        /**
         * 削除状態
         */
        public static final String DELETE_STATUS = "delete_status";

        /**
         * Audio接続中判定取得.
         *
         * @param cursor 設定リストのCursor
         * @return Audio接続中かどうか。 {@code true}:Audio接続中 {@code false}:Audio未接続
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isAudioConnected(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(AUDIO_CONNECTED)) == 1;
        }

        /**
         * Phone1接続中判定.
         *
         * @param cursor 設定リストのCursor
         * @return Phone1接続中かどうか。 {@code true}:Phone1接続中 {@code false}:Phone1未接続
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isPhone1Connected(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(PHONE_1_CONNECTED)) == 1;
        }

        /**
         * Phone2接続中判定.
         *
         * @param cursor 設定リストのCursor
         * @return Phone2接続中かどうか。 {@code true}:Phone2接続中 {@code false}:Phone2未接続
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isPhone2Connected(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(PHONE_2_CONNECTED)) == 1;
        }

        /**
         * ラストオーディオデバイス状態取得.
         *
         * @param cursor 設定リストのCursor
         * @return ラストオーディオデバイス状態 {@code true}:ラストオーディオデバイスである。 {@code false}:ラストオーディオデバイスではない。
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isLastAudioDevice(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(LAST_AUDIO_DEVICE)) == 1;
        }

        /**
         * 車載機連携中判定取得.
         *
         * @param cursor 設定リストのCursor
         * @return 車載機連携中判定 {@code true}:車載機連携中である。 {@code false}:車載機連携中ではない。
         * @throws NullPointerException {@code cursor}がnull
         */
        public static boolean isSessionConnected(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(SESSION_CONNECTED)) == 1;
        }

        /**
         * Audio接続状態取得.
         *
         * @param cursor 設定リストのCursor
         * @return Audio接続状態
         * @throws NullPointerException {@code cursor}がnull
         */
        public static AudioConnectStatus getAudioConnectStatus(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return AudioConnectStatus.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(AUDIO_CONNECT_STATUS)));
        }

        /**
         * Phone接続状態取得.
         *
         * @param cursor 設定リストのCursor
         * @return Phone接続状態
         * @throws NullPointerException {@code cursor}がnull
         */
        public static PhoneConnectStatus getPhoneConnectStatus(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return PhoneConnectStatus.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(PHONE_CONNECT_STATUS)));
        }

        /**
         * 削除状態取得.
         *
         * @param cursor 設定リストのCursor
         * @return 削除状態
         * @throws NullPointerException {@code cursor}がnull
         */
        public static DeleteStatus getDeleteStatus(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return DeleteStatus.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DELETE_STATUS)));
        }
    }

    /**
     * サーチリスト
     */
    public static class SearchList extends SettingListBaseColumn {

    }
}
