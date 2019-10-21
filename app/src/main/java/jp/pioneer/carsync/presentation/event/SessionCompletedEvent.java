package jp.pioneer.carsync.presentation.event;

/**
 * 車載機との連携開始の完了のイベント
 *
 * 車載機のClassId、仕向け情報がPreferenceに保存された後に発生するイベント
 * YouTubeLinkが車載機のClassId、仕向けに依存するために追加
 * ResourcefulPresenter#onCrpSessionStartedEventの最後にこのイベントを投げている
 */
public class SessionCompletedEvent {
}
