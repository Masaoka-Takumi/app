package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.BtAudioMediaInfoType;
import jp.pioneer.carsync.domain.model.CdInfo;
import jp.pioneer.carsync.domain.model.CdMediaInfoType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.DabMediaInfoType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.HdRadioMediaInfoType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.PandoraMediaInfoType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.RadioMediaInfoType;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfoType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.SxmMediaInfoType;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.domain.model.UsbMediaInfoType;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * オーディオ情報通知パケットハンドラ.
 */
public class DeviceAudioInfoNotificationPacketHandler extends AbstractPacketHandler {
    private static final int MIN_DATA_LENGTH = 4;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceAudioInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            // D1:ソース情報
            MediaSourceType sourceType = MediaSourceType.valueOf(data[1]);
            MediaSourceType currentSourceType = mStatusHolder.getCarDeviceStatus().sourceType;
            if (sourceType != currentSourceType) {
                // 現在のソースと一致しないので無視する
                Timber.w("doHandle() Unexpected MediaSourceType. (Expected, Actual) = %s , %s", currentSourceType, sourceType);
                return null;
            }
            // D2:情報種別
            int infoType = ubyteToInt(data[2]);
            // D3:文字コード
            // D4-DN:文字列
            String text = TextBytesUtil.extractText(data, 3);
            switch (sourceType) {
                case RADIO:
                    processRadio(infoType, text);
                    break;
                case DAB:
                    processDab(infoType, text);
                    break;
                case SIRIUS_XM:
                    processSiriusXm(infoType, text);
                    break;
                case HD_RADIO:
                    processHdRadio(infoType, text);
                    break;
                case CD:
                    processCd(infoType, text);
                    break;
                case USB:
                    processUsb(infoType, text);
                    break;
                case BT_AUDIO:
                    processBtAudio(infoType, text);
                    break;
                case PANDORA:
                    processPandora(infoType, text);
                    break;
                case SPOTIFY:
                    processSpotify(infoType, text);
                    break;
                default:
                    // noting to do
            }

            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }

    private void processRadio(int infoType, String text) {
        RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
        switch(infoType) {
            case RadioMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case RadioMediaInfoType.PS_INFO:
                info.psInfo = text;
                break;
            case RadioMediaInfoType.PTY_INFO:
                info.ptyInfo = text;
                break;
            case RadioMediaInfoType.SONG_TITLE:
                info.songTitle = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processRadio() RadioInfo = " + info);
    }

    private void processDab(int infoType, String text) {
        DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
        switch (infoType) {
            case DabMediaInfoType.SERVICE_COMPONENT_LABEL:
                info.serviceComponentLabel = text;
                break;
            case DabMediaInfoType.DYNAMIC_LABEL:
                info.dynamicLabel = text;
                break;
            case DabMediaInfoType.PTY_INFO:
                info.ptyInfo = text;
                break;
            case DabMediaInfoType.SERVICE_NUMBER:
                info.serviceNumber = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processDab() DabInfo = " + info);
    }

    private void processSiriusXm(int infoType, String text) {
        SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
        switch(infoType) {
            case SxmMediaInfoType.CHANNEL_AND_CHANNEL_NAME_OR_ADVISORY_MESSAGE:
                info.channelAndChannelNameOrAdvisoryMessage = text;
                break;
            case SxmMediaInfoType.ARTIST_NAME_OR_CONTENT_INFO:
                info.artistNameOrContentInfo = text;
                break;
            case SxmMediaInfoType.SONG_TITLE:
                info.songTitle = text;
                break;
            case SxmMediaInfoType.CATEGORY_NAME:
                info.categoryName = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processSiriusXm() SxmMediaInfo = " + info);
    }

    private void processHdRadio(int infoType, String text) {
        HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
        switch(infoType) {
            case HdRadioMediaInfoType.STATION_INFO:
                info.stationInfo = text;
                break;
            case HdRadioMediaInfoType.SONG_TITLE:
                info.songTitle = text;
                break;
            case HdRadioMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case HdRadioMediaInfoType.MULTICAST_PROGRAM_NUMBER:
                info.multicastProgramNumber = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processHdRadio() HdRadioInfo = " + info);
    }

    private void processCd(int infoType, String text) {
        CdInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().cdInfo;
        switch(infoType) {
            case CdMediaInfoType.TRACK_NUMBER:
                info.trackNumber = text;
                break;
            case CdMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case CdMediaInfoType.DISC_TITLE:
                info.discTitle = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processCd() CdInfo = " + info);
    }

    private void processUsb(int infoType, String text) {
        UsbMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().usbMediaInfo;
        switch(infoType) {
            case UsbMediaInfoType.SONG_TITLE:
                info.songTitle = text;
                break;
            case UsbMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case UsbMediaInfoType.ALBUM_NAME:
                info.albumName = text;
                break;
            case UsbMediaInfoType.GENRE:
                info.genre = text;
                break;
            case UsbMediaInfoType.TRACK_NUMBER:
                info.trackNumber = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processUsb() CdInfo = " + info);
    }

    private void processBtAudio(int infoType, String text) {
        BtAudioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().btAudioInfo;
        switch(infoType) {
            case BtAudioMediaInfoType.SONG_TITLE:
                info.songTitle = text;
                break;
            case BtAudioMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case BtAudioMediaInfoType.ALBUM_TITLE:
                info.albumName = text;
                break;
            case BtAudioMediaInfoType.DEVICE_NAME:
                info.deviceName = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }


        info.updateVersion();
        Timber.d("processBtAudio() BtAudioInfo = " + info);
    }

    private void processPandora(int infoType, String text) {
        PandoraMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().pandoraMediaInfo;
        switch(infoType) {
            case PandoraMediaInfoType.STATION_NAME:
                info.stationName = text;
                break;
            case PandoraMediaInfoType.SONG_TITLE:
                if (!TextUtils.equals(info.songTitle, text)) {
                    // 曲名が変わった場合リセット
                    info.thumbStatus = ThumbStatus.NONE;
                }
                info.songTitle = text;
                break;
            case PandoraMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case PandoraMediaInfoType.ALBUM_TITLE:
                info.albumName = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processPandora() PandoraMediaInfo = " + info);
    }

    private void processSpotify(int infoType, String text) {
        SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;
        switch(infoType) {
            case SpotifyMediaInfoType.TRACK_NAME_OR_SPOTIFY_MESSAGE:
                if (!TextUtils.equals(info.trackNameOrSpotifyError, text)) {
                    // 曲名が変わった場合リセット
                    info.thumbStatus = ThumbStatus.NONE;
                }
                info.trackNameOrSpotifyError = text;
                break;
            case SpotifyMediaInfoType.ARTIST_NAME:
                info.artistName = text;
                break;
            case SpotifyMediaInfoType.ALBUM_NAME:
                info.albumName = text;
                break;
            case SpotifyMediaInfoType.PLAYING_TRACK_SOURCE:
                info.playingTrackSource = text;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        info.updateVersion();
        Timber.d("processSpotify() SpotifyMediaInfo = " + info);
    }
}
