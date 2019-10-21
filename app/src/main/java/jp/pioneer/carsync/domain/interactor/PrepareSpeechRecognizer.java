package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BluetoothHeadsetProvider;
import jp.pioneer.carsync.domain.component.VoiceRecognitionRequester;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 音声認識準備.
 * <p>
 * 音声の認識をするデバイスの準備をする。
 * 音声認識実施後は{@link FinishBluetoothHeadset#execute(FinishCallback)} を必ず実行する.
 */
public class PrepareSpeechRecognizer implements VoiceRecognitionRequester.Callback, BluetoothHeadsetProvider.Callback {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject BluetoothHeadsetProvider mBluetoothHeadsetProvider;
    @Inject VoiceRecognitionRequester mRequester;
    private PrepareCallback mCallback;

    /**
     * コンストラクタ.
     */
    @Inject
    public PrepareSpeechRecognizer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess() {
        mBluetoothHeadsetProvider.prepareBluetoothHeadset(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError() {
        mCallback.onError();
    }

    @Override
    public void onComplete(BluetoothHeadsetProvider.HeadsetRequestType type, BluetoothHeadsetProvider.Result result) {
        if (type == BluetoothHeadsetProvider.HeadsetRequestType.CONNECT &&
                result == BluetoothHeadsetProvider.Result.COMPLETE) {
            mCallback.onComplete(Device.HEADSET, new FinishBluetoothHeadset());
        } else {
            mCallback.onComplete(Device.PHONE, new FinishBluetoothHeadset());
        }
    }

    /**
     * 音声認識終了用クラス.
     */
    public class FinishBluetoothHeadset implements VoiceRecognitionRequester.Callback, BluetoothHeadsetProvider.Callback {
        private FinishCallback mCallback;

        /**
         * 実行.
         */
        public void execute(FinishCallback callback, boolean isRinging) {
            mCallback = callback;
            mHandler.post(() -> mBluetoothHeadsetProvider.finishBluetoothHeadset(this, isRinging));
        }

        @Override
        public void onComplete(BluetoothHeadsetProvider.HeadsetRequestType type, BluetoothHeadsetProvider.Result result) {
            mRequester.finishRequest(this);
        }

        @Override
        public void onSuccess() {
            mCallback.onComplete();
        }

        @Override
        public void onError() {
            mCallback.onComplete();
        }
    }

    /**
     * 実行.
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback} がnull
     */
    public void execute(@NonNull PrepareCallback callback) {
        mCallback = checkNotNull(callback);

        mHandler.post(() -> mRequester.startRequest(this));
    }

    /**
     * 音声を取得するデバイス
     */
    public enum Device {
        HEADSET,
        PHONE,
    }

    /**
     * コールバック.
     */
    public interface PrepareCallback {
        /**
         * 完了.
         * <p>
         * 音声認識準備が完了した場合に呼ばれる
         *
         * @param device                 音声取得デバイス
         * @param finishBluetoothHeadset 音声認識終了用インスタンス
         */
        @UiThread
        void onComplete(Device device, FinishBluetoothHeadset finishBluetoothHeadset);

        /**
         * エラー.
         */
        @UiThread
        void onError();
    }

    /**
     * コールバック.
     */
    public interface FinishCallback{
        /**
         * 完了.
         */
        @UiThread
        void onComplete();
    }
}
