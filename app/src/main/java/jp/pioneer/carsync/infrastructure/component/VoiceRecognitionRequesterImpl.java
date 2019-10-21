package jp.pioneer.carsync.infrastructure.component;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.VoiceRecognitionRequester;
import jp.pioneer.carsync.domain.model.VoiceRecognitionRequestType;
import jp.pioneer.carsync.domain.model.VoiceRecognitionResponseType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.entity.VoiceRecognitionResponse;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * VoiceRecognitionRequesterの実装.
 */
public class VoiceRecognitionRequesterImpl implements
        VoiceRecognitionRequester, RequestTask.Callback<VoiceRecognitionResponse> {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject Handler mHandler;
    @Inject EventBus mEventBus;
    private WeakReference<Callback> mStartCallback;
    private WeakReference<Callback> mFinishCallback;
    private VoiceRecognitionRequestType mRequestType;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;

    /**
     * コンストラクタ.
     */
    @Inject
    public VoiceRecognitionRequesterImpl() {
    }

    /**
     * 初期化
     */
    public void initialize() {
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void startRequest(@NonNull Callback callback) {
        Timber.d("startRequest");
        checkNotNull(callback);

        if (mStartCallback != null && mStartCallback.get() != null) {
            Timber.e("startRequest() multiple access.");
            return;
        }

        mStartCallback = new WeakReference<>(callback);
        mRequestType = VoiceRecognitionRequestType.START;
        callbackSuccess();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void finishRequest(@NonNull Callback callback) {
        Timber.d("finishRequest");
        checkNotNull(callback);

        if (mFinishCallback != null && mFinishCallback.get() != null) {
            Timber.e("finishRequest() multiple access.");
            return;
        }

        mFinishCallback = new WeakReference<>(callback);
        mRequestType = VoiceRecognitionRequestType.FINISH;
        callbackSuccess();
    }

    private void request(){
        try {
            if (requestVoiceRecognition(mRequestType)) {
                callbackSuccess();
            } else {
                callbackError();
            }
        } catch (InterruptedException e) {
            Timber.d("request() Interrupted.");
            callbackError();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(VoiceRecognitionResponse result) {
        mIsFailed = !(Objects.equal(result.type, getResponseType()) &&
                Objects.equal(result.result, ResponseCode.OK));
        mCountDownLatch.countDown();
    }

    private VoiceRecognitionResponseType getResponseType(){
        if(mRequestType == VoiceRecognitionRequestType.START){
            return VoiceRecognitionResponseType.START;
        } else {
            return VoiceRecognitionResponseType.FINISH;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError() {
        mIsFailed = true;
        mCountDownLatch.countDown();
    }

    /**
     * セッション停止イベントハンドラ
     * <p>
     * セッション停止した場合はCountDownを強制的に終了し、
     * 失敗したこととする。
     *
     * @param ev セッションストップイベント
     */
    @Subscribe
    public void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        synchronized (this) {
            if (mStartCallback != null || mFinishCallback != null) {
                mIsFailed = true;
                mCountDownLatch.countDown();
            }
        }
    }

    private boolean requestVoiceRecognition(VoiceRecognitionRequestType type) throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createVoiceRecognitionCommand(type);

        mCountDownLatch = new CountDownLatch(1);
        if (mCarDeviceConnection.sendRequestPacket(packet, this) == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private synchronized void callbackSuccess() {
        Timber.d("success");
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }
        callback.onSuccess();
    }

    private synchronized void callbackError() {
        Timber.d("error");
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }
        callback.onError();
    }

    @Nullable
    private Callback getCallback() {
        Callback callback;
        if(mRequestType == VoiceRecognitionRequestType.START) {
            if (mStartCallback == null) {
                Timber.w("getCallback() mStartCallback has been cleared.");
                return null;
            }

            callback = mStartCallback.get();
            mStartCallback = null;
            if (callback == null) {
                Timber.w("getCallback() start callback has been cleared.");
                return null;
            }
        } else {
            if (mFinishCallback == null) {
                Timber.w("getCallback() mFinishCallback has been cleared.");
                return null;
            }

            callback = mFinishCallback.get();
            mFinishCallback = null;
            if (callback == null) {
                Timber.w("getCallback() finish callback has been cleared.");
                return null;
            }
        }
        return callback;
    }
}
