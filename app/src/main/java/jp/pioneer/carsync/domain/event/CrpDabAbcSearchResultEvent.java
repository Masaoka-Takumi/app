package jp.pioneer.carsync.domain.event;

public class CrpDabAbcSearchResultEvent {
    public final boolean result;

    public CrpDabAbcSearchResultEvent(boolean result) {
        this.result = result;
    }
}
