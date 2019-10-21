package jp.pioneer.carsync.domain.model;

/**
 * オーディオ情報種別（DAB）.
 */
public class DabMediaInfoType {
    /** Service Component Label / No Service / No Signal状態. */
    public final static int SERVICE_COMPONENT_LABEL = 0x00;
    /** Dynamic Label. */
    public final static int DYNAMIC_LABEL = 0x01;
    /** PTY情報. */
    public final static int PTY_INFO = 0x02;
    /** SERVICE NUMBER. */
    public final static int SERVICE_NUMBER = 0x03;
}
