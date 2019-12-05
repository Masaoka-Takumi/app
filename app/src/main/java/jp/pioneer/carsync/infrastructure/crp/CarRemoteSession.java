package jp.pioneer.carsync.infrastructure.crp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Inject;
import javax.inject.Provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForCarRemoteSession;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.domain.event.AppStartCommandEvent;
import jp.pioneer.carsync.domain.model.AbstractTunerInfo;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SessionErrorType;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneMediaInfoType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.util.PresetChannelDictionary;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSendTaskFinishedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionErrorEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.PacketHandlerFactory;
import jp.pioneer.carsync.infrastructure.crp.task.AudioSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.FunctionSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.IdleReceiveCommTimerTask;
import jp.pioneer.carsync.infrastructure.crp.task.IlluminationSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.InitialAuthTask;
import jp.pioneer.carsync.infrastructure.crp.task.InitialSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.NaviGuideVoiceSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.ParkingSensorSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.PeriodicCommTimerTask;
import jp.pioneer.carsync.infrastructure.crp.task.PhoneSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.PostTask;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTaskId;
import jp.pioneer.carsync.infrastructure.crp.task.SessionStartTask;
import jp.pioneer.carsync.infrastructure.crp.task.SoundFxSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SyncPacketSendListener;
import jp.pioneer.carsync.infrastructure.crp.task.SystemSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.TaskStatusMonitor;
import jp.pioneer.carsync.infrastructure.crp.transport.Transport;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.toHex;

/**
 * 車載機とのCarRemoteProtocolセッション.
 */
public class CarRemoteSession implements PacketSenderThread.OnPacketSendListener, PacketReaderThread.OnPacketReceivedListener, TaskStatusMonitor {
    @Inject EventBus mEventBus;
    @Inject @Nullable SessionLogger mLogger;
    @Inject SessionConfig mSessionConfig;
    @Inject PacketSenderThread mPacketSenderThread;
    @Inject PacketReaderThread mPacketReaderThread;
    @Inject @ForCarRemoteSession ExecutorService mTaskExecutor;
    @Inject Provider<PeriodicCommTimerTask> mPeriodicCommTimerTaskProvider;
    @Inject Provider<IdleReceiveCommTimerTask> mIdleReceiveCommTimerTaskProvider;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject PacketHandlerFactory mHandlerFactory;
    @Inject AppSharedPreference mPreference;
    private StatusTimerHandler mStatusTimerHandler;
    private Context mContext;
    private Transport mTransport;
    private StatusHolder mStatusHolder;
    private SendTask mRunningTask;
    private CarRemoteSessionComponent mSessionComponent;
    // 認証前にSMART_PHONE_APP_START_COMMANDが来るので止むを得ず初期データに設定
    private Set<IncomingPacketIdType> mPacketHandlerIdTypes =
            EnumSet.of(IncomingPacketIdType.SMART_PHONE_APP_START_COMMAND);

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param transport 車載機との通信路
     * @throws NullPointerException {@code context}、{@code transport}、{@code statusHolder}のいずれかがnull
     */
    public CarRemoteSession(@NonNull Context context, @NonNull Transport transport, @NonNull StatusHolder statusHolder) {
        mContext = checkNotNull(context);
        mTransport = checkNotNull(transport);
        mStatusHolder = checkNotNull(statusHolder);
        mSessionComponent = createCarRemoteSessionComponent();
        mSessionComponent.inject(this);
        mStatusTimerHandler = new StatusTimerHandler(mPeriodicCommTimerTaskProvider.get(), mIdleReceiveCommTimerTaskProvider.get());
    }

    /**
     * 開始.
     *
     * 車載機連携が抑制されている場合、セッションを開始しない。(この場合、SessionStatusはSessionStatus.PENDINGとなる。)
     * BT連携の場合、セッションを開始しなくても来るApp起動コマンドを受け取ってアプリを起動する必要がある関係で、
     * 送受信スレッドは動作させる。
     * USB連携はアプリから能動的にAOA(車載機)をチェック出来、指定のAOAが接続されたらActivityを起動する仕組みがあるため、
     * 本仕組みは不要である。そのため、車載機連携が抑制されている場合は利用者側で本メソッドを呼ばないようにすること。
     * 車載機連携抑制が解除され、DeviceConnectionSuppressEventが発行されたら{@link #startSessionIfNeeded()}を呼び出す
     * こと。
     * 車載機連携抑制が解除されたがDeviceConnectionSuppressEventが発行されない場合、自アプリの
     * App起動コマンドを受信するか、{@link #startSessionIfNeeded()}が呼ばれた場合にセッションを開始する。
     * 自アプリのApp起動コマンド受信検知は本クラスの責務、{@link #startSessionIfNeeded()}の呼び出しは
     * 利用者の責務となる。
     *
     * @throws IllegalStateException 接続中
     * @throws IOException I/Oエラー発生
     */
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    public synchronized void start() throws IOException {
        Timber.i("start()");

        if(!mPreference.isLogEnabled()){
            mLogger = null;
        }

        if (mLogger != null) {
            mLogger.starting();
        }

        // 状態クリア、接続中遷移
        mStatusHolder.reset();
        if (mStatusHolder.getAppStatus().deviceConnectionSuppress) {
            mStatusHolder.setSessionStatus(SessionStatus.PENDING);
        } else {
            mStatusHolder.setSessionStatus(SessionStatus.STARTING);
        }
        publishStatusUpdateEvent(null);  // 状態クリア & SessionStatusの通知
        // 接続
        mTransport.connect(mStatusHolder);
        publishStatusUpdateEvent(null);  // 接続情報の通知
        // 送受信スレッド開始
        mPacketSenderThread.setOnPacketSendListener(this);
        mPacketReaderThread.setOnPacketSendListener(this);
        mPacketSenderThread.start();
        mPacketReaderThread.start();
        if (mStatusHolder.getAppStatus().deviceConnectionSuppress) {
            mEventBus.register(this);
        } else {
            // 初期認証タスク、セッション開始タスク実行
            executeSendTask(createInitialAuthTask());
            executeSendTask(createSessionStartTask());
        }
    }

    /**
     * 終了.
     *
     * 車載機連携が抑制されている場合、セッションを開始していなかった場合も、
     * SessionStatusがSessionStatus.STOPPEDに変化する。
     * これを切断として扱うのは不適当であるので、PENDINGからSTOPPEDに変化した場合は
     * 切断として扱わないこと。
     */
    public synchronized void stop() {
        Timber.i("stop()");

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        if (mStatusHolder.getSessionStatus() == SessionStatus.STOPPED) {
            return;
        }

        // 開始していた場合のみセッション停止イベントを発行する
        boolean isPublishCrpSessionStoppedEvent = (mStatusHolder.getSessionStatus() == SessionStatus.STARTED);
        if (mStatusHolder.getSessionStatus() != SessionStatus.PENDING) {
            // 切断中遷移
            mStatusHolder.setSessionStatus(SessionStatus.STOPPING);
            publishStatusUpdateEvent(null);  // SessionStatusの通知
        }
        // いろいろ終了
        mStatusTimerHandler.removeAllMessages();
        mTaskExecutor.shutdownNow();
        mPacketReaderThread.quit();
        mPacketSenderThread.quit();
        // 切断
        mTransport.disconnect();
        // 状態クリア
        mStatusHolder.reset();
        if (isPublishCrpSessionStoppedEvent) {
            publishEvent(new CrpSessionStoppedEvent());
        }
        publishStatusUpdateEvent(null);  // 状態クリアの通知
        Optional.ofNullable(mLogger)
                .ifPresent(SessionLogger::stopped);
    }

    /**
     * パケット送信.
     * <p>
     * 通知か要求かを自動で判断する。
     * パケット送信にタスクを使用する。
     *
     * @param packet 送信パケット
     * @throws NullPointerException {@code packet}がnull
     */
    public void sendPacket(@NonNull OutgoingPacket packet) {
        if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return;
        }

        if (checkNotNull(packet).shouldWaitForResponse()) {
            executeSendTask(createRequestTask(packet, null));
        } else {
            executeSendTask(createPostTask(packet, null));
        }
    }

    /**
     * 要求パケット送信.
     * <p>
     * 応答があるパケットを送信する。
     * 結果の型は、応答パケットの受信パケットハンドラクラスの定義を参照。
     *
     * @param packet 送信パケット
     * @param callback コールバック
     * @param <T> 結果の型
     * @return タスクのFuture。実行出来ない状態の場合null。
     * @throws NullPointerException {@code packet}、または、{@code callback}がnull
     * @throws IllegalArgumentException {@code packet}が応答がないパケット
     */
    public <T> Future<?> sendRequestPacket(@NonNull OutgoingPacket packet, @NonNull RequestTask.Callback<T> callback) {
        checkArgument(checkNotNull(packet).shouldWaitForResponse());
        checkNotNull(callback);

        if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return null;
        }

        return executeSendTask(createRequestTask(packet, callback));
    }

    /**
     * パケット送信
     * <p>
     * パケットを送信キューに直接追加し送信する。（パケットが送信されるまで見届ける）
     * 定期通信や応答など直ぐに送信する必要がある場合に使用する。
     * タスクを使用しないので応答があるパケットは利用不可。
     *
     * @param packet 送信パケット
     * @return {@code true}:送信成功。{@code false}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     * @throws IllegalArgumentException {@code packet}が応答があるパケット
     */
    public boolean sendPacketDirect(@NonNull OutgoingPacket packet) {
        Timber.i("sendPacketDirect()");

        checkNotNull(packet);
        checkArgument(!packet.shouldWaitForResponse());

        if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return false;
        }

        SyncPacketSendListener listener = new SyncPacketSendListener();
        if (!mPacketSenderThread.send(packet, listener)) {
            return false;
        }

        try {
            if (!listener.isSent()) {
                Timber.e("sendPacketDirect() send failed.");
                return false;
            }

            return true;
        } catch (InterruptedException e) {
            Timber.w("sendPacketDirect() Interrupted.");
            return false;
        }
    }

    /**
     * 送信タスク実行.
     *
     * @param sendTask 送信タスク
     * @return タスクのFuture。実行出来ない状態の場合null。
     * @throws NullPointerException {@code sendTask}がnull
     */
    public Future<?> executeSendTask(@NonNull SendTask sendTask) {
        Timber.i("executeSendTask() task = " + sendTask);

        if (mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            return null;
        }

        try {
            return mTaskExecutor.submit(checkNotNull(sendTask));
        } catch (RejectedExecutionException e) {
            Timber.w("executeSendTask() Rejected.");
            return null;
        }
    }

    /**
     * 車載機との通信路取得.
     *
     * @return 車載機との通信路
     */
    @NonNull
    public Transport getTransport() {
        return mTransport;
    }

    /**
     * 車載機のステータスやアプリ内の再生楽曲情報取得.
     *
     * @return StatusHolder
     */
    @NonNull
    public StatusHolder getStatusHolder() {
        return mStatusHolder;
    }

    /**
     * 端末に保存されている情報取得.
     *
     * @return StatusHolder
     */
    @NonNull
    public AppSharedPreference getPreference() {
        return mPreference;
    }

    /**
     * CarRemoteSession用のコンポーネント取得.
     *
     * @return CarRemoteSessionComponent
     */
    @NonNull
    public CarRemoteSessionComponent getSessionComponent() {
        return mSessionComponent;
    }

    /**
     * 送信パケットビルダー取得.
     *
     * @return OutgoingPacketBuilder
     */
    @NonNull
    public OutgoingPacketBuilder getOutgoingPacketBuilder() {
        return mPacketBuilder;
    }

    /**
     * StatusHolder更新イベント発行.
     *
     * @param hint 更新の要因となった受信パケットIDタイプ
     */
    public void publishStatusUpdateEvent(@Nullable IncomingPacketIdType hint) {
        publishEvent(new CrpStatusUpdateEvent(hint));
    }

    /**
     * イベント発行.
     *
     * @param ev イベント
     * @throws NullPointerException {@code ev}がnull
     */
    public void publishEvent(@Nullable Object ev) {
        mEventBus.post(checkNotNull(ev));
    }

    /**
     * デバッグログ出力.
     *
     * @param format フォーマット
     * @param logText 内容
     */
    public void outputDebugLog(String format, Object[] logText){
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.log(SessionLogger.Type.DEBUG.name(), format, logText));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSending(@NonNull OutgoingPacket packet) {
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.sending("[seq:%d] %s (%s)",
                        packet.seqNumber, toHex(packet.toByteArray()), packet.packetIdType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSent(@NonNull OutgoingPacket packet) {
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.sent("[seq:%d]", packet.seqNumber));

        if (packet.packetIdType == OutgoingPacketIdType.SMART_PHONE_STATUS_NOTIFICATION) {
            // 定期通信
            startPeriodicCommTimerTask();
        }
        // DABのPreset登録
        updatePresetDictionaryIfNeeded(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketDropped(@NonNull OutgoingPacket packet) {
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.sendDropped("[seq:%d] %s (%s)",
                        packet.seqNumber, toHex(packet.toByteArray()), packet.packetIdType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketSendFailed(@NonNull OutgoingPacket packet, @NonNull Throwable t) {
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.sendError("[seq:%d] err = %s", packet.seqNumber, t));

        if(t.getMessage().contains("ENODEV")){
            mStatusHolder.getAppStatus().errorType = SessionErrorType.USB_NO_SUCH_DEVICE;
            mEventBus.post(new CrpSessionErrorEvent());
        }

        stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketReceived(@NonNull IncomingPacket packet) {
        if (mStatusHolder.getSessionStatus() != SessionStatus.PENDING) {
            startIdleReceiveCommTimerTask();
        }
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.read("%s (%s)",
                        toHex(packet.getRawPacket()), packet.getPacketIdType()));

        try {
            if (packet.getPacketIdType().isResponsePacket()) {
                if (mRunningTask != null) {
                    mRunningTask.handlePacket(packet);
                } else {
                    Timber.w("onPacketReceived() Unhandled response packet.");
                }
            } else {
                if (!mPacketHandlerIdTypes.contains(packet.getPacketIdType())) {
                    Timber.w("onPacketReceived() Unexpected packet. IdType = " + packet.getPacketIdType());
                } else {
                    mHandlerFactory.create(packet.getPacketIdType()).handle(packet);
                }
            }
        } catch (Exception e) {
            Timber.e("onPacketReceived() " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketDropped(@NonNull IncomingPacket packet) {
        Optional.ofNullable(mLogger)
                .ifPresent(logger -> logger.readDropped("%s (%s)",
                        toHex(packet.getRawPacket()), packet.getPacketIdType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketReadFailed(@NonNull Throwable t) {
        Timber.i("onPacketReadFailed() " + t.getMessage());

        stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskStarted(@NonNull SendTask sendTask) {
        Timber.i("onTaskStarted() task = " + sendTask);

        mRunningTask = sendTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskFinished(@NonNull SendTask sendTask) {
        Timber.i("onTaskFinished() task = " + sendTask);

        mRunningTask = null;
        SendTaskId taskId = sendTask.getSendTaskId();
        if (taskId == SendTaskId.INITIAL_AUTH) {
            // 応答パケット以外でハンドルするパケットIDタイプ群のセットアップ
            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            Stream.of(IncomingPacketIdType.values())
                    .filter(value -> !value.isResponsePacket() && value.supportVersion != null && value.supportVersion.isLessThanOrEqual(version))
                    .forEach(packetIdType -> mPacketHandlerIdTypes.add(packetIdType));
        } else if (sendTask.getSendTaskId() == SendTaskId.SESSION_START) {
            onSessionStarted();
        }

        publishEvent(new CrpSendTaskFinishedEvent(taskId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskFailed(@NonNull SendTask sendTask, @NonNull Throwable t) {
        Timber.i("onTaskFailed() task = " + sendTask);

        mRunningTask = null;
        if (t instanceof SendTimeoutException) {
            Optional.ofNullable(mLogger)
                    .ifPresent(logger -> logger.sendTimeout("[seq:%d]", ((SendTimeoutException) t).packet.seqNumber));
        } else {
            stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskCanceled(@NonNull SendTask sendTask) {
        Timber.i("onTaskCanceled() task = " + sendTask);

        mRunningTask = null;
    }

    /**
     * App起動コマンドイベント.
     *
     * @param event AppStartCommandEvent
     */
    @Subscribe
    public void onAppStartCommandEvent(AppStartCommandEvent event) {
        if (mContext.getPackageName().equals(event.packageName)) {
            this.startSessionIfNeeded();
        }
    }

    /**
     * 必要であればセッションを開始.
     *
     * 車載機連携が抑制されていてセッションを開始していなかった場合、抑制が解除されていればセッションを開始する。
     */
    public void startSessionIfNeeded() {
        if (!mStatusHolder.getAppStatus().deviceConnectionSuppress && mStatusHolder.getSessionStatus() == SessionStatus.PENDING) {
            mEventBus.unregister(this);
            mStatusHolder.setSessionStatus(SessionStatus.STARTING);
            publishStatusUpdateEvent(null);  //SessionStatusの通知
            // 初期認証タスク、セッション開始タスク実行
            executeSendTask(createInitialAuthTask());
            executeSendTask(createSessionStartTask());
        }
    }

    private void sessionStop(){
        Timber.i("sessionStop");
        stop();
    }

    /**
     * 車載機とのセッションが開始した.
     */
    private void onSessionStarted() {
        Optional.ofNullable(mLogger)
                .ifPresent(SessionLogger::started);

        // SmartPhoneステータス情報の通知
        // 本来は、初期通信中にステータスが変化したとき通知するものであるが、
        // 変化の判定が面倒なので常に送信する。
        startPeriodicCommTimerTaskImmediate();
        // ヘルスチェック開始
        startIdleReceiveCommTimerTask();
        // オーディオ情報通知
        notifyAudioInfo();
        // システム設定情報要求
        executeSendTask(createSystemSettingsRequestTask());
        // オーディオ設定情報要求
        executeSendTask(createAudioSettingsRequestTask());
        // イルミ設定情報要求
        executeSendTask(createIlluminationSettingsRequestTask());
        // Function設定情報要求
        executeSendTask(createFunctionSettingsRequestTask());
        // パーキングセンサー設定情報要求
        executeSendTask(createParkingSensorSettingsRequestTask());
        // ナビガイド音声設定情報要求
        executeSendTask(createNaviGuideVoiceSettingsRequestTask());
        // 初期設定情報要求
        executeSendTask(createInitialSettingsRequestTask());
        // Phone設定情報要求
        executeSendTask(createPhoneSettingsRequestTask());
        // Sound FX設定情報要求
        executeSendTask(createSoundFxSettingsRequestTask());

        // 接続済遷移
        mStatusHolder.setSessionStatus(SessionStatus.STARTED);
        publishEvent(new CrpSessionStartedEvent());
        publishStatusUpdateEvent(null);
    }

    private void startPeriodicCommTimerTaskImmediate() {
        mStatusTimerHandler.sendPeriodicCommDelayed(0);
    }

    private void startPeriodicCommTimerTask() {
        mStatusTimerHandler.sendPeriodicCommDelayed(mSessionConfig.getPeriodicCommInterval());
    }

    private void startIdleReceiveCommTimerTask() {
        mStatusTimerHandler.sendIdleReceiveCommDelayed(mSessionConfig.getIdleReceiveCommTimeout());
    }

    private void notifyAudioInfo() {
        if (mStatusHolder.getCarDeviceStatus().sourceType != MediaSourceType.APP_MUSIC) {
            return;
        }

        AndroidMusicMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo;
        if (info.mediaId == 0) {
            // 再生情報が無い
            Timber.d("notifyAudioInfo() no media info.");
            return;
        }

        CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
        Stream.of(SmartPhoneMediaInfoType.values())
                .forEach(type -> sendPacket(mPacketBuilder.createSmartPhoneAudioInfoNotification(info, type, carDeviceSpec)));
    }

    private ComponentFactory getComponentFactory() {
        return App.getApp(mContext).getComponentFactory();
    }

    private AppComponent getAppComponent() {
        return App.getApp(mContext).getAppComponent();
    }

    private CarRemoteSessionComponent createCarRemoteSessionComponent() {
        return getComponentFactory().createCarRemoteProtocolComponent(getAppComponent(), this);
    }

    private SendTask createInitialAuthTask() {
        InitialAuthTask task = new InitialAuthTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createSessionStartTask() {
        SessionStartTask task = new SessionStartTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createSystemSettingsRequestTask() {
        SystemSettingsRequestTask task = new SystemSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createAudioSettingsRequestTask() {
        AudioSettingsRequestTask task = new AudioSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createIlluminationSettingsRequestTask() {
        IlluminationSettingsRequestTask task = new IlluminationSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createFunctionSettingsRequestTask() {
        FunctionSettingsRequestTask task = new FunctionSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createParkingSensorSettingsRequestTask() {
        ParkingSensorSettingsRequestTask task = new ParkingSensorSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createNaviGuideVoiceSettingsRequestTask() {
        NaviGuideVoiceSettingsRequestTask task = new NaviGuideVoiceSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createInitialSettingsRequestTask() {
        InitialSettingsRequestTask task = new InitialSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createPhoneSettingsRequestTask() {
        PhoneSettingsRequestTask task = new PhoneSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private SendTask createSoundFxSettingsRequestTask() {
        SoundFxSettingsRequestTask task = new SoundFxSettingsRequestTask();
        return task.inject(mSessionComponent);
    }

    private <T> SendTask createRequestTask(OutgoingPacket outgoingPacket, RequestTask.Callback<T> callback) {
        RequestTask<T> task = new RequestTask<>(outgoingPacket, callback);
        return task.inject(mSessionComponent);
    }

    private SendTask createPostTask(OutgoingPacket outgoingPacket, PostTask.Callback callback) {
        PostTask task = new PostTask(outgoingPacket, callback);
        return task.inject(mSessionComponent);
    }

    private static class StatusTimerHandler extends Handler {
        private static final int MSG_SEND_STATUS = 1;
        private static final int MSG_CHECK_STATUS_RECEIVED = 2;

        private final Runnable mPeriodicCommTimerTask;
        private final Runnable mIdleReceiveCommTimerTask;

        private StatusTimerHandler(Runnable periodicCommTimerTask, Runnable idleReceiveCommTimerTask) {
            super(Looper.getMainLooper());
            mPeriodicCommTimerTask = periodicCommTimerTask;
            mIdleReceiveCommTimerTask = idleReceiveCommTimerTask;
        }

        public void sendPeriodicCommDelayed(long delay) {
            removeMessages(MSG_SEND_STATUS);
            sendEmptyMessageDelayed(MSG_SEND_STATUS, delay);
        }

        public void sendIdleReceiveCommDelayed(long delay) {
            removeMessages(MSG_CHECK_STATUS_RECEIVED);
            sendEmptyMessageDelayed(MSG_CHECK_STATUS_RECEIVED, delay);
        }

        public void removeAllMessages() {
            removeMessages(MSG_SEND_STATUS);
            removeMessages(MSG_CHECK_STATUS_RECEIVED);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_SEND_STATUS:
                        mPeriodicCommTimerTask.run();
                        break;
                    case MSG_CHECK_STATUS_RECEIVED:
                        mIdleReceiveCommTimerTask.run();
                        break;
                    default:
                        break;
                }
            } catch (Throwable t) {
                Timber.w(t, "A exception thrown in handling message");
            }
        }
    }

    /**
     * パケットがPresetコマンドならpresetDictionaryを更新する
     * @param packet 送信完了したパケット
     */
    private void updatePresetDictionaryIfNeeded(@NonNull OutgoingPacket packet) {
        if (packet.packetIdType != OutgoingPacketIdType.DEVICE_CONTROL_COMMAND) {
            return;
        }

        MediaSourceType source = mStatusHolder.getCarDeviceStatus().sourceType;
		//DABのみ登録
        if(source!=MediaSourceType.DAB)return;

        int number;
        CarDeviceControlCommand command = CarDeviceControlCommand.valueOf(packet.data[1]); // D1が車載機操作コマンド
        PresetChannelDictionary dictionary = mStatusHolder.getPresetChannelDictionary();
        switch (command) {
            case PRESET_KEY_1:
                number = 1;
                break;
            case PRESET_KEY_2:
                number = 2;
                break;
            case PRESET_KEY_3:
                number = 3;
                break;
            case PRESET_KEY_4:
                number = 4;
                break;
            case PRESET_KEY_5:
                number = 5;
                break;
            case PRESET_KEY_6:
                number = 6;
                break;
            case CROSS_UP:
            case CROSS_DOWN:
                // PRESET_KEY_*の直後にP.CH UP/DOWNをされると辞書に正しくない情報が登録されるので破棄
                dictionary.reset();
                return;
            default:
                // その他のコマンドの場合は何もする必要はなし
                return;
        }
        AbstractTunerInfo abstractTunerInfo = mStatusHolder.findTunerInfoByMediaSourceType(source);
        if (abstractTunerInfo == null||abstractTunerInfo.getBand()==null) {
            return;
        }
        int bandCode = abstractTunerInfo.getBand().getCode();

        dictionary.applyPresetCommand(source, bandCode, number);
    }
}