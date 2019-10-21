package jp.pioneer.carsync.domain.model;

/**
 * 各ソースの表示情報.
 */
public class CarDeviceMediaInfoHolder {
    /** ラジオ情報. */
    public RadioInfo radioInfo = new RadioInfo();
    /** DAB情報. */
    public DabInfo dabInfo = new DabInfo();
    /** HD Radio情報. */
    public HdRadioInfo hdRadioInfo = new HdRadioInfo();
    /** CD情報. */
    public CdInfo cdInfo = new CdInfo();
    /** USB情報. */
    public UsbMediaInfo usbMediaInfo = new UsbMediaInfo();
    /** Pandora情報. */
    public PandoraMediaInfo pandoraMediaInfo = new PandoraMediaInfo();
    /** AppMusic情報. */
    public AndroidMusicMediaInfo androidMusicMediaInfo = new AndroidMusicMediaInfo();
    /** Spotify情報. */
    public SpotifyMediaInfo spotifyMediaInfo = new SpotifyMediaInfo();
    /** SiriusXM情報. */
    public SxmMediaInfo sxmMediaInfo = new SxmMediaInfo();
    /** BT Audio情報. */
    public BtAudioInfo btAudioInfo = new BtAudioInfo();

    /**
     * コンストラクタ.
     */
    public CarDeviceMediaInfoHolder() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        radioInfo.reset();
        dabInfo.reset();
        hdRadioInfo.reset();
        cdInfo.reset();
        usbMediaInfo.reset();
        pandoraMediaInfo.reset();
        spotifyMediaInfo.reset();
        sxmMediaInfo.reset();
		btAudioInfo.reset();
    }
}
