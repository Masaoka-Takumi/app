package jp.pioneer.carsync.domain.event;

/**
 * 車載機ステータス変更イベント.
 * <p>
 * 車載機ステータス:
 *  <pre>{@code
 *      CarDeviceStatus status = statusHolder.getCarDeviceStatus();
 *  }</pre>
 *
 *  ソース種別が変わった場合、本イベントに加えて{@link MediaSourceTypeChangeEvent}も発生する。
 *  リバース線状態が変わった場合、本イベントに加えて{@link ParkingSensorDisplayStatusChangeEvent}も発生する。
 */
public class CarDeviceStatusChangeEvent {
}
