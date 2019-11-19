package jp.pioneer.carsync.domain.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Date;
import java.util.Locale;

import jp.pioneer.carsync.application.content.ProviderContract.*;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerSeekStep;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * チューナーコントラクト
 */
public class TunerContract {

    /**
     * お気に入りコントラクト
     */
    public static class FavoriteContract {

        /**
         * {@link QueryParams} のビルダー.
         */
        public static class QueryParamsBuilder {

            /**
             * ラジオのお気に入り情報を取得する {@link QueryParams} 生成.
             *
             * @return ラジオのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createRadio() {
                return new QueryParams(
                    Favorite.CONTENT_URI,
                    Radio.PROJECTION,
                    "(" + Favorite.SOURCE_ID + " = ? "
                            + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NULL)",
                    new String[]{String.valueOf(MediaSourceType.RADIO.code)},
                    Radio.SORT_ORDER,
                    null
                );
            }
            /**
             * ラジオのユーザーPreset情報を取得する {@link QueryParams} 生成.
             *
             * @return ラジオのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createRadioPreset(RadioBandType radioBandType) {
                return new QueryParams(
                        Favorite.CONTENT_URI,
                        Radio.PROJECTION,
                        "(" + Favorite.SOURCE_ID + " = ? "
                                + " AND " + Favorite.TUNER_PARAM3 + " = ?"
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{String.valueOf(MediaSourceType.RADIO.code),String.valueOf(radioBandType==RadioBandType.LW?RadioBandType.MW.code:radioBandType.code)},
                        Radio.SORT_ORDER,
                        null
                );
            }
            /**
             * ラジオの全バンドユーザーPreset情報を取得する {@link QueryParams} 生成.
             *
             * @return ラジオのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createRadioPreset() {
                return new QueryParams(
                        Favorite.CONTENT_URI,
                        Radio.PROJECTION,
                        "(" + Favorite.SOURCE_ID + " = ? "
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{String.valueOf(MediaSourceType.RADIO.code)},
                        Radio.SORT_ORDER,
                        null
                );
            }
            /**
             * DABのお気に入り情報を取得する {@link QueryParams} 生成.
             *
             * @return DABのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createDab() {
                return new QueryParams(
                    Favorite.CONTENT_URI,
                    Dab.PROJECTION,
                    "(" + Favorite.SOURCE_ID + " = ? "
                            + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NULL)",
                    new String[]{String.valueOf(MediaSourceType.DAB.code)},
                    Dab.SORT_ORDER,
                    null
                );
            }
            /**
             * DABのユーザーPreset情報を取得する {@link QueryParams} 生成.
             *
             * @return DABのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createDabPreset(DabBandType dabBandType) {
                return new QueryParams(
                        Favorite.CONTENT_URI,
                        Radio.PROJECTION,
                        "(" + Favorite.SOURCE_ID + " = ? "
                                + " AND " + Favorite.TUNER_BAND + " = ?"
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{String.valueOf(MediaSourceType.DAB.code),String.valueOf(dabBandType.code)},
                        Radio.SORT_ORDER,
                        null
                );
            }
            /**
             * Sirius XMのお気に入り情報を取得する {@link QueryParams} 生成.
             *
             * @return Sirius XMのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createSiriusXm() {
                return new QueryParams(
                    Favorite.CONTENT_URI,
                    SiriusXm.PROJECTION,
                    "(" + Favorite.SOURCE_ID + " = ?)",
                    new String[]{String.valueOf(MediaSourceType.SIRIUS_XM.code)},
                    SiriusXm.SORT_ORDER,
                    null
                );
            }

            /**
             * HD Radioのお気に入り情報を取得する {@link QueryParams} 生成.
             *
             * @return HD Radioのお気に入り情報を取得するクエリーパラメータ
             */
            @NonNull
            public static QueryParams createHdRadio() {
                return new QueryParams(
                    Favorite.CONTENT_URI,
                    HdRadio.PROJECTION,
                    "(" + Favorite.SOURCE_ID + " = ?)",
                    new String[]{String.valueOf(MediaSourceType.HD_RADIO.code)},
                    HdRadio.SORT_ORDER,
                    null
                );
            }
        }

        /**
         * {@link UpdateParams} のビルダー.
         */
        public static class UpdateParamsBuilder {

            /**
             * ラジオ情報のお気に入り情報を登録・更新する {@link UpdateParams} 生成.
             * <p>
             * RadioInfoのBandTypeがFMの場合はTunerSeekStepは必要ないが、
             * 引数としては必須として、本メソッド内で使用するか判定する。
             *
             * @param info     ラジオ情報
             * @param seekStep TunerSeekStep
             * @param context  コンテキスト
             * @param infoText  psInfo/JP放送局
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException     {@code info}がnull
             * @throws NullPointerException     {@code seekStep}がnull
             * @throws NullPointerException     {@code context}がnull
             * @throws IllegalArgumentException {@code info.frequencyUnit}がnull
             * @throws IllegalArgumentException {@code info.currentFrequency}が0以下の値
             */
            @NonNull
            public static UpdateParams createRadio(@NonNull RadioInfo info, @NonNull TunerSeekStep seekStep, @NonNull Context context,@NonNull String infoText) {
                checkNotNull(info);
                checkNotNull(seekStep);
                checkNotNull(context);
                if (info.frequencyUnit == null) {
                    throw new IllegalArgumentException("invalid frequencyUnit");
                }
                if (info.currentFrequency == 0) {
                    throw new IllegalArgumentException("invalid currentFrequency:" + info.currentFrequency);
                }

                float value = info.currentFrequency / ((float) info.frequencyUnit.divide);
                String format = "%." + info.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, context.getString(info.frequencyUnit.label));
                String description = String.format(Locale.ENGLISH, "%s %s", context.getString(info.band.label), frequency);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.RADIO.code);
                values.put(Favorite.NAME, infoText);
                values.put(Favorite.DESCRIPTION, description);
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.currentFrequency);
                values.put(Favorite.TUNER_FREQUENCY_INDEX, info.index);
                values.put(Favorite.TUNER_BAND, info.getBand().code);
                values.put(Favorite.TUNER_PARAM1, info.pi);
                if (info.band.isAMVariant()) {
                    values.put(Favorite.TUNER_PARAM2, seekStep.code);
                }
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY1 + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NULL"
                    ;
                String[] selectionArgs = new String[]{
                    String.valueOf(MediaSourceType.RADIO.code),
                    String.valueOf(info.currentFrequency)
                };

                return new UpdateParams(
                    Favorite.CONTENT_URI,
                    values,
                    selection,
                    selectionArgs
                );
            }
            /**
             * ラジオのユーザー登録PCH情報を登録・更新する {@link UpdateParams} 生成.
             * <p>
             * RadioInfoのBandTypeがFMの場合はTunerSeekStepは必要ないが、
             * 引数としては必須として、本メソッド内で使用するか判定する。
             *
             * @param info     ラジオ情報
             * @param seekStep TunerSeekStep
             * @param context  コンテキスト
             * @param infoText  psInfo/JP放送局
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException     {@code info}がnull
             * @throws NullPointerException     {@code seekStep}がnull
             * @throws NullPointerException     {@code context}がnull
             * @throws IllegalArgumentException {@code info.frequencyUnit}がnull
             * @throws IllegalArgumentException {@code info.currentFrequency}が0以下の値
             */
            @NonNull
            public static UpdateParams createRadioPreset(@NonNull RadioInfo info, @NonNull int presetNumber,@NonNull TunerSeekStep seekStep, @NonNull Context context,@NonNull String infoText) {
                checkNotNull(info);
                checkNotNull(seekStep);
                checkNotNull(context);
                if (info.frequencyUnit == null) {
                    throw new IllegalArgumentException("invalid frequencyUnit");
                }
                if (info.currentFrequency == 0) {
                    throw new IllegalArgumentException("invalid currentFrequency:" + info.currentFrequency);
                }

                float value = info.currentFrequency / ((float) info.frequencyUnit.divide);
                String format = "%." + info.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, context.getString(info.frequencyUnit.label));
                String description = String.format(Locale.ENGLISH, "%s %s", context.getString(info.band.label), frequency);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.RADIO.code);
                values.put(Favorite.NAME, infoText);
                values.put(Favorite.DESCRIPTION, description);
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.currentFrequency);
                values.put(Favorite.TUNER_CHANNEL_KEY2, presetNumber);
                values.put(Favorite.TUNER_FREQUENCY_INDEX, info.index);
                values.put(Favorite.TUNER_BAND, info.getBand().code);//選局用Band
                values.put(Favorite.TUNER_PARAM1, info.pi);
                if (info.band.isAMVariant()) {
                    values.put(Favorite.TUNER_PARAM2, seekStep.code);
                }
                values.put(Favorite.TUNER_PARAM3, info.getBand()==RadioBandType.LW?RadioBandType.MW.code:info.getBand().code);//登録用Band
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                        + " AND " + Favorite.TUNER_PARAM3 + " = ?"
                        + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " = ?"
                        ;
                String[] selectionArgs = new String[]{
                        String.valueOf(MediaSourceType.RADIO.code),
                        String.valueOf(info.getBand()==RadioBandType.LW?RadioBandType.MW.code:info.getBand().code),
                        String.valueOf(presetNumber)
                };

                return new UpdateParams(
                        Favorite.CONTENT_URI,
                        values,
                        selection,
                        selectionArgs
                );
            }
            /**
             * DAB情報のお気に入り情報を登録・更新する {@link UpdateParams} 生成.
             *
             * @param info     DAB情報
             * @param context  コンテキスト
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException     {@code info}がnull
             * @throws NullPointerException     {@code context}がnull
             * @throws IllegalArgumentException {@code info.frequencyUnit}がnull
             * @throws IllegalArgumentException {@code info.currentFrequency}が0以下の値
             */
            @NonNull
            public static UpdateParams createDab(@NonNull DabInfo info, @NonNull Context context) {
                checkNotNull(info);
                checkNotNull(context);
                if (info.frequencyUnit == null) {
                    throw new IllegalArgumentException("invalid frequencyUnit");
                }
                if (info.currentFrequency == 0) {
                    throw new IllegalArgumentException("invalid currentFrequency:" + info.currentFrequency);
                }

                float value = info.currentFrequency / ((float) info.frequencyUnit.divide);
                String format = "%." + info.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, context.getString(info.frequencyUnit.label));
                String description = String.format(Locale.ENGLISH, "%s %s", context.getString(info.band.label), frequency);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.DAB.code);
                values.put(Favorite.NAME, TextUtils.isEmpty(info.serviceComponentLabel) ? "" : info.serviceComponentLabel);
                values.put(Favorite.DESCRIPTION, description);
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.currentFrequency);
                values.put(Favorite.TUNER_FREQUENCY_INDEX, info.index);
                values.put(Favorite.TUNER_BAND, info.getBand().code);
                values.put(Favorite.TUNER_PARAM1, info.eid);
                values.put(Favorite.TUNER_PARAM2, info.sid);
                values.put(Favorite.TUNER_PARAM3, info.scids);
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY1 + " = ?"
                    + " AND " + Favorite.TUNER_PARAM1 + " = ?"
                    + " AND " + Favorite.TUNER_PARAM2 + " = ?"
                    + " AND " + Favorite.TUNER_PARAM3 + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NULL"
                    ;
                String[] selectionArgs = new String[]{
                    String.valueOf(MediaSourceType.DAB.code),
                    String.valueOf(info.currentFrequency),
                    String.valueOf(info.eid),
                    String.valueOf(info.sid),
                    String.valueOf(info.scids),
                };

                return new UpdateParams(
                    Favorite.CONTENT_URI,
                    values,
                    selection,
                    selectionArgs
                );
            }
            /**
             * DAB情報のユーザー登録PCH情報を登録・更新する {@link UpdateParams} 生成.
             *
             * @param info     DAB情報
             * @param context  コンテキスト
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException     {@code info}がnull
             * @throws NullPointerException     {@code context}がnull
             * @throws IllegalArgumentException {@code info.frequencyUnit}がnull
             * @throws IllegalArgumentException {@code info.currentFrequency}が0以下の値
             */
            @NonNull
            public static UpdateParams createDabPreset(@NonNull DabInfo info, int presetNumber, @NonNull Context context) {
                checkNotNull(info);
                checkNotNull(context);
                if (info.frequencyUnit == null) {
                    throw new IllegalArgumentException("invalid frequencyUnit");
                }
                if (info.currentFrequency == 0) {
                    throw new IllegalArgumentException("invalid currentFrequency:" + info.currentFrequency);
                }

                float value = info.currentFrequency / ((float) info.frequencyUnit.divide);
                String format = "%." + info.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, context.getString(info.frequencyUnit.label));
                String description = String.format(Locale.ENGLISH, "%s %s", context.getString(info.band.label), frequency);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.DAB.code);
                values.put(Favorite.NAME, TextUtils.isEmpty(info.serviceComponentLabel) ? "" : info.serviceComponentLabel);
                values.put(Favorite.DESCRIPTION, description);
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.currentFrequency);
                values.put(Favorite.TUNER_CHANNEL_KEY2, presetNumber);
                values.put(Favorite.TUNER_FREQUENCY_INDEX, info.index);
                values.put(Favorite.TUNER_BAND, info.getBand().code);
                values.put(Favorite.TUNER_PARAM1, info.eid);
                values.put(Favorite.TUNER_PARAM2, info.sid);
                values.put(Favorite.TUNER_PARAM3, info.scids);
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                        + " AND " + Favorite.TUNER_BAND + " = ?"
                        + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " = ?"
                        ;
                String[] selectionArgs = new String[]{
                        String.valueOf(MediaSourceType.DAB.code),
                        String.valueOf(info.getBand().code),
                        String.valueOf(presetNumber)
                };

                return new UpdateParams(
                        Favorite.CONTENT_URI,
                        values,
                        selection,
                        selectionArgs
                );
            }
            /**
             * Sirius XM情報のお気に入り情報を登録・更新する {@link UpdateParams} 生成.
             *
             * @param info    SxmMediaInfo
             * @param context コンテキスト
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException {@code info}がnull
             * @throws NullPointerException {@code context}がnull
             */
            @NonNull
            public static UpdateParams createSiriusXm(@NonNull SxmMediaInfo info, @NonNull Context context) {
                checkNotNull(info);
                checkNotNull(info);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.SIRIUS_XM.code);
                values.put(Favorite.NAME, info.channelAndChannelNameOrAdvisoryMessage);
                values.put(Favorite.DESCRIPTION, String.format(Locale.ENGLISH, "%s %s", context.getString(info.getBand().getLabel()), info.currentChannelNumber));
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.sid);
                values.put(Favorite.TUNER_BAND, info.getBand().code);
                values.put(Favorite.TUNER_PARAM1, info.currentChannelNumber);
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY1 + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NULL"
                    ;
                String[] selectionArgs = new String[]{
                    String.valueOf(MediaSourceType.SIRIUS_XM.code),
                    String.valueOf(info.sid)
                };

                return new UpdateParams(
                    Favorite.CONTENT_URI,
                    values,
                    selection,
                    selectionArgs
                );
            }

            /**
             * HD Radio情報のお気に入り情報を登録・更新する {@link UpdateParams} 生成.
             *
             * @param info     HD Radio情報
             * @param context  コンテキスト
             * @return 引数の情報からお気に入り情報に登録又は更新する更新パラメータ
             * @throws NullPointerException     {@code info}がnull
             * @throws NullPointerException     {@code context}がnull
             * @throws IllegalArgumentException {@code info.frequencyUnit}がnull
             * @throws IllegalArgumentException {@code info.currentFrequency}が0以下の値
             */
            @NonNull
            public static UpdateParams createHdRadio(@NonNull HdRadioInfo info, @NonNull Context context) {
                checkNotNull(info);
                checkNotNull(context);
                if (info.frequencyUnit == null) {
                    throw new IllegalArgumentException("invalid frequencyUnit");
                }
                if (info.currentFrequency == 0) {
                    throw new IllegalArgumentException("invalid currentFrequency:" + info.currentFrequency);
                }

                float value = info.currentFrequency / ((float) info.frequencyUnit.divide);
                String format = "%." + info.frequencyUnit.fraction + "f%s";
                String frequency = String.format(Locale.ENGLISH, format, value, context.getString(info.frequencyUnit.label));
                String description = String.format(Locale.ENGLISH, "%s %s", context.getString(info.band.label), frequency);

                ContentValues values = new ContentValues();
                values.put(Favorite.SOURCE_ID, MediaSourceType.HD_RADIO.code);
                values.put(Favorite.NAME, TextUtils.isEmpty(info.stationInfo) ? "" : info.stationInfo);
                values.put(Favorite.DESCRIPTION, description);
                values.put(Favorite.TUNER_CHANNEL_KEY1, info.currentFrequency);
                values.put(Favorite.TUNER_CHANNEL_KEY2, info.multicastChannelNumber);
                values.put(Favorite.TUNER_FREQUENCY_INDEX, info.index);
                values.put(Favorite.TUNER_BAND, info.getBand().code);
                values.put(Favorite.CREATE_DATE, System.currentTimeMillis());

                String selection = Favorite.SOURCE_ID + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY1 + " = ?"
                    + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " = ?"
                    ;
                String[] selectionArgs = new String[]{
                    String.valueOf(MediaSourceType.HD_RADIO.code),
                    String.valueOf(info.currentFrequency),
                    String.valueOf(info.multicastChannelNumber)
                };

                return new UpdateParams(
                    Favorite.CONTENT_URI,
                    values,
                    selection,
                    selectionArgs
                );
            }
        }

        /**
         * {@link DeleteParams} のビルダー.
         */
        public static class DeleteParamsBuilder {

            /**
             * 該当のIDのお気に入り情報を削除する {@link DeleteParams} 生成.
             *
             * @param id 削除対象のID
             * @return 該当のIDのお気に入り情報を削除する削除パラメータ
             * @throws IllegalArgumentException {@code id}が0以下
             */
            @NonNull
            public static DeleteParams createParams(@IntRange(from = 1) long id) {
                checkArgument(id >= 1);

                return new DeleteParams(
                    Favorite.CONTENT_URI,
                    "(" + Favorite._ID + " = ?)",
                    new String[]{String.valueOf(id)}
                );
            }
            /**
             * 該当バンドのP.CH登録データを全て削除する。 {@link DeleteParams} 生成.
             *
             * @param bandType バンド
             * @return 該当バンドのP.CH登録データを削除する削除パラメータ
             * @throws NullPointerException {@code cursor}がnull
             */
            @NonNull
            public static DeleteParams createParamsPreset(@NonNull RadioBandType bandType) {
                checkNotNull(bandType);

                return new DeleteParams(
                        Favorite.CONTENT_URI,
                        "(" + Favorite.SOURCE_ID + " = ?"
                                + " AND " + Favorite.TUNER_PARAM3 + " = ?"
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{
                                String.valueOf(MediaSourceType.RADIO.code),
                                String.valueOf(bandType==RadioBandType.LW?RadioBandType.MW.code:bandType.code),
                        }
                );
            }
            /**
             * AMのP.CH登録データを全て削除する。 {@link DeleteParams} 生成.
             *
             * @return 該当バンドのP.CH登録データを削除する削除パラメータ
             */
            @NonNull
            public static DeleteParams createParamsPresetAm() {
                return new DeleteParams(
                        Favorite.CONTENT_URI,
                        "(" + Favorite.SOURCE_ID + " = ?"
                                + " AND " + Favorite.TUNER_PARAM3 + " = ?"
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{
                                String.valueOf(MediaSourceType.RADIO.code),
                                String.valueOf(RadioBandType.AM.code),
                        }
                );
            }

            /**
             * DABの該当バンドのP.CH登録データを全て削除する。 {@link DeleteParams} 生成.
             *
             * @param bandType バンド
             * @return 該当バンドのP.CH登録データを削除する削除パラメータ
             * @throws NullPointerException {@code cursor}がnull
             */
            @NonNull
            public static DeleteParams createParamsDabPreset(@NonNull DabBandType bandType) {
                checkNotNull(bandType);

                return new DeleteParams(
                        Favorite.CONTENT_URI,
                        "(" + Favorite.SOURCE_ID + " = ?"
                                + " AND " + Favorite.TUNER_BAND + " = ?"
                                + " AND " + Favorite.TUNER_CHANNEL_KEY2 + " IS NOT NULL)",
                        new String[]{
                                String.valueOf(MediaSourceType.DAB.code),
                                String.valueOf(bandType.code),
                        }
                );
            }
        }

        /**
         * Radio.
         */
        public static class Radio {
            static final String[] PROJECTION = new String[]{
                Favorite._ID,
                Favorite.NAME,
                Favorite.DESCRIPTION,
                Favorite.TUNER_CHANNEL_KEY1,
                Favorite.TUNER_CHANNEL_KEY2,
                Favorite.TUNER_FREQUENCY_INDEX,
                Favorite.TUNER_BAND,
                Favorite.TUNER_PARAM1,
                Favorite.TUNER_PARAM2,
                Favorite.TUNER_PARAM3,
                Favorite.CREATE_DATE
            };

            static final String SORT_ORDER = new SortOrder(Favorite.CREATE_DATE, SortOrder.Order.ASC).toQuery();

            /**
             * ラジオ情報 {@code cursor} からIDを取得する.
             *
             * @param cursor ラジオ情報
             * @return お気に入りID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getId(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite._ID));
            }

            /**
             * ラジオ情報 {@code cursor} から名前を取得する.
             *
             * @param cursor ラジオ情報
             * @return 名前
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getName(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.NAME));
            }

            /**
             * ラジオ情報 {@code cursor} から説明を取得する.
             *
             * @param cursor ラジオ情報
             * @return 説明
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getDescription(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.DESCRIPTION));
            }

            /**
             * ラジオ情報 {@code cursor} から周波数を取得する.
             *
             * @param cursor ラジオ情報
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getFrequency(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY1));
            }
            /**
             * ラジオ情報 {@code cursor} からPchNumberを取得する.
             *
             * @param cursor ラジオ情報
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getPresetNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY2));
            }

            /**
             * ラジオ情報 {@code cursor} からIndexを取得する.
             *
             * @param cursor ラジオ情報
             * @return Index
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getIndex(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_FREQUENCY_INDEX));
            }

            /**
             * ラジオ情報 {@code cursor} からバンド種別を取得する.
             *
             * @param cursor ラジオ情報
             * @return バンド種別 {@link RadioBandType}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static RadioBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer tunerBandCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_BAND));
                return RadioBandType.valueOf(tunerBandCode.byteValue());
            }

            /**
             * ラジオ情報 {@code cursor} からPIを取得する.
             *
             * @param cursor ラジオ情報
             * @return PI
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getPi(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM1));
            }

            /**
             * ラジオ情報 {@code cursor} からTunerSeekStepを取得する.
             *
             * @param cursor ラジオ情報
             * @return {@link TunerSeekStep}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static TunerSeekStep getTunerSeekStep(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer seekStepCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM2));
                return TunerSeekStep.valueOf(seekStepCode.byteValue());
            }

            /**
             * ラジオ情報 {@code cursor} からバンド種別（Preset登録用）を取得する.
             *
             * @param cursor ラジオ情報
             * @return バンド種別 {@link RadioBandType}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static RadioBandType getBandTypePreset(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer tunerBandCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM3));
                return RadioBandType.valueOf(tunerBandCode.byteValue());
            }

            /**
             * ラジオ情報 {@code cursor} から登録日を取得する.
             *
             * @param cursor ラジオ情報
             * @return 登録日
             * @throws NullPointerException {@code cursor}がnull
             */
            public static Date getCreateDate(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.CREATE_DATE)));
            }
        }

        /**
         * DAB.
         */
        public static class Dab {
            static final String[] PROJECTION = new String[]{
                Favorite._ID,
                Favorite.NAME,
                Favorite.DESCRIPTION,
                Favorite.TUNER_CHANNEL_KEY1,
                Favorite.TUNER_CHANNEL_KEY2,
                Favorite.TUNER_FREQUENCY_INDEX,
                Favorite.TUNER_BAND,
                Favorite.TUNER_PARAM1,
                Favorite.TUNER_PARAM2,
                Favorite.TUNER_PARAM3,
                Favorite.CREATE_DATE
            };

            static final String SORT_ORDER = new SortOrder(Favorite.CREATE_DATE, SortOrder.Order.ASC).toQuery();

            /**
             * DAB情報 {@code cursor} からIDを取得する.
             *
             * @param cursor DAB情報
             * @return お気に入りID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getId(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite._ID));
            }

            /**
             * DAB情報 {@code cursor} から名前を取得する.
             *
             * @param cursor DAB情報
             * @return 名前
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getName(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.NAME));
            }

            /**
             * DAB情報 {@code cursor} から説明を取得する.
             *
             * @param cursor DAB情報
             * @return 説明
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getDescription(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.DESCRIPTION));
            }

            /**
             * DAB情報 {@code cursor} から周波数を取得する.
             *
             * @param cursor DAB情報
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getFrequency(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY1));
            }

            /**
             * DAB情報 {@code cursor} からPchNumberを取得する.
             *
             * @param cursor DAB情報
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getPresetNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY2));
            }

            /**
             * DAB情報 {@code cursor} からIndexを取得する.
             *
             * @param cursor DAB情報
             * @return Index
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getIndex(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_FREQUENCY_INDEX));
            }

            /**
             * DAB情報 {@code cursor} からバンド種別を取得する.
             *
             * @param cursor DAB情報
             * @return バンド種別 {@link RadioBandType}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static DabBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer tunerBandCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_BAND));
                return DabBandType.valueOf(tunerBandCode.byteValue());
            }

            /**
             * DAB情報 {@code cursor} からEIDを取得する.
             *
             * @param cursor DAB情報
             * @return EID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getEid(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM1));
            }

            /**
             * DAB情報 {@code cursor} からSIDを取得する.
             *
             * @param cursor DAB情報
             * @return SID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getSid(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM2));
            }

            /**
             * DAB情報 {@code cursor} からSCIdSを取得する.
             *
             * @param cursor DAB情報
             * @return SCIdS
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getScids(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM3));
            }

            /**
             * DAB情報 {@code cursor} から登録日を取得する.
             *
             * @param cursor DAB情報
             * @return 登録日
             * @throws NullPointerException {@code cursor}がnull
             */
            public static Date getCreateDate(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.CREATE_DATE)));
            }
        }

        /**
         * SiriusXm.
         */
        public static class SiriusXm {
            static final String[] PROJECTION = new String[]{
                Favorite._ID,
                Favorite.NAME,
                Favorite.DESCRIPTION,
                Favorite.TUNER_CHANNEL_KEY1,
                Favorite.TUNER_BAND,
                Favorite.TUNER_PARAM1,
                Favorite.CREATE_DATE
            };

            static final String SORT_ORDER = new SortOrder(Favorite.CREATE_DATE, SortOrder.Order.ASC).toQuery();

            /**
             * SiriusXm情報 {@code cursor} からIDを取得する.
             *
             * @param cursor SiriusXm情報
             * @return お気に入りID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getId(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite._ID));
            }

            /**
             * SiriusXm情報 {@code cursor} から名前を取得する.
             *
             * @param cursor SiriusXm情報
             * @return 名前
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getName(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.NAME));
            }

            /**
             * SiriusXm情報 {@code cursor} から説明を取得する.
             *
             * @param cursor SiriusXm情報
             * @return 説明
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getDescription(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.DESCRIPTION));
            }

            /**
             * SiriusXm情報 {@code cursor} からSIDを取得する.
             *
             * @param cursor SiriusXm情報
             * @return SID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getSid(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY1));
            }

            /**
             * SiriusXm情報 {@code cursor} からバンド種別を取得する.
             *
             * @param cursor SiriusXm情報
             * @return バンド種別 {@link SxmBandType}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static SxmBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer tunerBandCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_BAND));
                return SxmBandType.valueOf(tunerBandCode.byteValue());
            }

            /**
             * SiriusXm情報 {@code cursor} からチャンネルナンバーを取得する.
             *
             * @param cursor SiriusXm情報
             * @return チャンネルナンバー
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getChannelNo(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_PARAM1));
            }

            /**
             * SiriusXm情報 {@code cursor} から登録日を取得する.
             *
             * @param cursor SiriusXm情報
             * @return 登録日
             * @throws NullPointerException {@code cursor}がnull
             */
            public static Date getCreateDate(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.CREATE_DATE)));
            }
        }

        /**
         * HD Radio.
         */
        public static class HdRadio {
            static final String[] PROJECTION = new String[]{
                Favorite._ID,
                Favorite.NAME,
                Favorite.DESCRIPTION,
                Favorite.TUNER_CHANNEL_KEY1,
                Favorite.TUNER_CHANNEL_KEY2,
                Favorite.TUNER_FREQUENCY_INDEX,
                Favorite.TUNER_BAND,
                Favorite.CREATE_DATE
            };

            static final String SORT_ORDER = new SortOrder(Favorite.CREATE_DATE, SortOrder.Order.ASC).toQuery();

            /**
             * HD Radio情報 {@code cursor} からIDを取得する.
             *
             * @param cursor HD Radio情報
             * @return お気に入りID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getId(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite._ID));
            }

            /**
             * HD Radio情報 {@code cursor} から名前を取得する.
             *
             * @param cursor HD Radio情報
             * @return 名前
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getName(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.NAME));
            }

            /**
             * HD Radio情報 {@code cursor} から説明を取得する.
             *
             * @param cursor HD Radio情報
             * @return 説明
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getDescription(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(Favorite.DESCRIPTION));
            }

            /**
             * HD Radio情報 {@code cursor} から周波数を取得する.
             *
             * @param cursor HD Radio情報
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getFrequency(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY1));
            }

            /**
             * HD Radio情報 {@code cursor} からマルチキャストCH番号を取得する.
             *
             * @param cursor HD Radio情報
             * @return マルチキャストCH番号
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getMulticastChNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_CHANNEL_KEY2));
            }

            /**
             * HD Radio情報 {@code cursor} からIndexを取得する.
             *
             * @param cursor HD Radio情報
             * @return Index
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getIndex(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_FREQUENCY_INDEX));
            }

            /**
             * HD Radio情報 {@code cursor} からバンド種別を取得する.
             *
             * @param cursor HD Radio情報
             * @return バンド種別 {@link RadioBandType}
             * @throws NullPointerException {@code cursor}がnull
             */
            public static HdRadioBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                Integer tunerBandCode = cursor.getInt(cursor.getColumnIndexOrThrow(Favorite.TUNER_BAND));
                return HdRadioBandType.valueOf(tunerBandCode.byteValue());
            }

            /**
             * HD Radio情報 {@code cursor} から登録日を取得する.
             *
             * @param cursor HD Radio情報
             * @return 登録日
             * @throws NullPointerException {@code cursor}がnull
             */
            public static Date getCreateDate(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Favorite.CREATE_DATE)));
            }
        }
    }

    /**
     * リスト項目コントラクト
     */
    public static class ListItemContract {
        /**
         * リスト項目共通列.
         */
        public static class ListItemBaseColumns {
            /**
             * 行のユニークID.
             * <p>
             * <P>Type: long</P>
             */
            public static final String _ID = "_id";

            /**
             * リストインデックス.
             * <p>
             * <P>Type: int</P>
             */
            public static final String LIST_INDEX = "list_index";

            /**
             * 文字列.
             * <p>
             * <pre>
             * Radio/HD Radio:PS/Call sign
             * DAB:service name
             * SiriusXM:channel name
             * </pre>
             * <p>
             * <P>Type: String</P>
             */
            public static final String TEXT = "text";

            /**
             * ID取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return ID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getId(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            }

            /**
             * リストインデックス取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return リストインデックス
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getListIndex(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(LIST_INDEX));
            }

            /**
             * 文字列取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return 文字列（{@link #TEXT}）
             * @throws NullPointerException {@code cursor}がnull
             */
            public static String getText(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getString(cursor.getColumnIndexOrThrow(TEXT));
            }
        }

        /**
         * Radio/HD Radio 兼用
         * <p>
         * 以下の実装事情で、[Ver2.5] HD Radio 増設時に、この型を加えた。
         * <ul>
         * <li> 両者は必要項目(列)がまったく同じ、違いはBandTypeの型のみ とう関係性。
         * <li> なので、重複コードは避けたい。
         * <li> けれど、保守上は 型系列が縦割り個別 であったほうが、わかりよい。
         * </ul>
         * <li>
         */
        public static class XXRadio extends ListItemBaseColumns {
            /**
             * P.CH番号.
             * <p>
             * <P>Type: int</P>
             */
            public static final String PCH_NUMBER = "pch_number";

            /**
             * 周波数.
             * <p>
             * <P>Type: long</P>
             */
            public static final String FREQUENCY = "frequency";

            /**
             * 周波数単位.
             * <p>
             * <P>Type: String（{@link TunerFrequencyUnit#name()}）</P>
             */
            public static final String FREQUENCY_UNIT = "frequency_unit";

            /**
             * バンド種別.
             * <p>
             * <P>Type: String（{@link RadioBandType#name()}）</P>
             */
            public static final String BAND_TYPE = "band_type";

            /**
             * P.CH番号取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return P.CH番号
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getPchNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(PCH_NUMBER));
            }

            /**
             * 周波数取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return 周波数
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getFrequency(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(FREQUENCY));
            }

            /**
             * 周波数単位取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return 周波数単位
             * @throws NullPointerException {@code cursor}がnull
             */
            public static TunerFrequencyUnit getFrequencyUnit(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return TunerFrequencyUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(FREQUENCY_UNIT)));
            }
        }

        /**
         * ラジオ.
         */
        public static class Radio extends XXRadio {
            /**
             * バンド種別取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return バンド種別
             * @throws NullPointerException {@code cursor}がnull
             */
            public static RadioBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return RadioBandType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(BAND_TYPE)));
            }
        }

        /**
         * HD Radio.
         */
        public static class HdRadio extends XXRadio {
            /**
             * バンド種別取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return バンド種別
             * @throws NullPointerException {@code cursor}がnull
             */
            public static HdRadioBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return HdRadioBandType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(BAND_TYPE)));
            }
        }

        /**
         * DAB.
         */
        public static class Dab extends ListItemBaseColumns {
            /**
             * 周波数インデックス.
             * <p>
             * <P>Type: int</P>
             */
            public static final String INDEX = "index";

            /**
             * EID.
             * <p>
             * <P>Type: int</P>
             */
            public static final String EID = "eid";

            /**
             * SID.
             * <p>
             * <P>Type: long</P>
             */
            public static final String SID = "sid";

            /**
             * SCIdS.
             * <p>
             * <P>Type: int</P>
             */
            public static final String SCIDS = "scids";

            /**
             * 周波数インデックス取得.
             *
             * @param cursor Cursor
             * @return 周波数インデックス
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getIndex(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(INDEX));
            }

            /**
             * EID取得.
             *
             * @param cursor Cursor
             * @return EID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getEid(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(EID));
            }

            /**
             * SID取得.
             *
             * @param cursor Cursor
             * @return SID
             * @throws NullPointerException {@code cursor}がnull
             */
            public static long getSid(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getLong(cursor.getColumnIndexOrThrow(SID));
            }

            /**
             * SCIdS取得.
             *
             * @param cursor Cursor
             * @return SCIdS
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getScids(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(SCIDS));
            }
        }

        /**
         * SiriusXM.
         */
        public static class SiriusXm extends ListItemBaseColumns {
            /**
             * P.CH番号.
             * <p>
             * <P>Type: int</P>
             */
            public static final String PCH_NUMBER = "pch_number";

            /**
             * CH番号.
             * <p>
             * <P>Type: int</P>
             */
            public static final String CH_NUMBER = "ch_number";

            /**
             * バンド種別.
             * <p>
             * <P>Type: String（{@link SxmBandType#name()}）</P>
             */
            public static final String BAND_TYPE = "band_type";

            /**
             * P.CH番号取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return P.CH番号
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getPchNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(PCH_NUMBER));
            }

            /**
             * CH番号取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return CH番号
             * @throws NullPointerException {@code cursor}がnull
             */
            public static int getChNumber(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return cursor.getInt(cursor.getColumnIndexOrThrow(CH_NUMBER));
            }

            /**
             * バンド種別取得.
             *
             * @param cursor プリセットチャンネルのCursor
             * @return バンド種別
             * @throws NullPointerException {@code cursor}がnull
             */
            public static SxmBandType getBandType(@NonNull Cursor cursor) {
                checkNotNull(cursor);

                return SxmBandType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(BAND_TYPE)));
            }
        }
    }
}
