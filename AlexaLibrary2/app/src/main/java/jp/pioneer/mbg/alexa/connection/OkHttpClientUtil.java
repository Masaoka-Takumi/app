package jp.pioneer.mbg.alexa.connection;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class OkHttpClientUtil {

    /** 通常通信用設定. */
    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 10;
    private static final long DEFAULT_CONNECTION_POOL_TIMEOUT = 60 * 60 * 1000;

    /** AVS通信用設定. */
    private static final int AVS_MAX_IDLE_CONNECTIONS = 10;
    private static final long AVS_CONNECTION_POOL_TIMEOUT = 60 * 60 * 1000;

    /** AVS通信用のコネクション. */
    private static OkHttpClient mAvsConnectionOkHttpClient = null;

    /**
     * コンストラクタ.
     */
    private OkHttpClientUtil() {
    }

    /**
     * 通常の通信用のOKHTTPクライアント.
     *
     * 全ての通信でAVS用のクライアントを使用すると、
     * 上限に達してしまって通信が行えない現象が発生する為、
     * AVS以外の通信は個別でクライアントを生成する.
     *
     * @return OkHttpClient
     */
    public synchronized static OkHttpClient getNormalConnectionOkHttpClient() {
        return createHttpClient(DEFAULT_MAX_IDLE_CONNECTIONS, DEFAULT_CONNECTION_POOL_TIMEOUT);
    }

    /**
     * AVSとの通信を行うOKHTTPクライアント.
     *
     * DownChannel/Event送信/Directive受信には、同一のクライアントを使用する必要がある為。
     * AVS通信用のクライアントを生成する.
     *
     * @return OkHttpClient
     */
    public synchronized static OkHttpClient getAvsConnectionOkHttpClient(){
        if(mAvsConnectionOkHttpClient == null) {
            mAvsConnectionOkHttpClient = createHttpClient(AVS_MAX_IDLE_CONNECTIONS, AVS_CONNECTION_POOL_TIMEOUT);
        }
        return mAvsConnectionOkHttpClient;
    }

    /**
     * OkHttpクライアントを生成する.
     * @return OkHttpClient
     */
    private static OkHttpClient createHttpClient(int maxIdleConnections, long connectionPoolTimeout) {
        ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections,
                connectionPoolTimeout, TimeUnit.SECONDS);
        OkHttpClient.Builder client = new OkHttpClient.Builder() .connectTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .connectionPool(connectionPool);

        return client.build();
    }
}
