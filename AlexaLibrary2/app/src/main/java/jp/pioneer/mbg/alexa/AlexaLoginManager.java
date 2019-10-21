package jp.pioneer.mbg.alexa;

/**
 * Created by esft-sakamori on 2018/06/01.
 */

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.BuildConfig;
import android.support.v4.app.SupportActivity;
import android.text.TextUtils;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.manager.AlexaEventManager;
import jp.pioneer.mbg.alexa.manager.TokenManager;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Login With Amazonライブラリを使用したログイン機能を実装する
 */
public class AlexaLoginManager {
    private static final String TAG = AlexaLoginManager.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * Alexaアプリ プロダクトID
     */
    //    private String PRODUCT_ID = "esoft_alexa_test_2";           // イ．ソフト作成アカウント
    private String PRODUCT_ID_DEBUG = "pioneer_smart_sync_debug";     // パイオニア様作成アカウント（debug版）
    private String PRODUCT_ID_RELEASE = "pioneer_smart_sync_Android";     // パイオニア様作成アカウント（release版）
    private String PRODUCT_ID = BuildConfig.DEBUG?PRODUCT_ID_DEBUG:PRODUCT_ID_RELEASE;     // パイオニア様作成アカウント（release版）
    /**
     * Alexaアプリ デバイスシリアルナンバー
     * AlexaAppにて、端末の判別に使用する。
     */
    private String PRODUCT_DSN;

    /**
     * マネージャインスタンス
     */
    private static AlexaLoginManager mManager = null;

    /**
     * コンテキスト
     */
    private SupportActivity mActivity = null;

    /**
     * Androidのライフサイクルに対応しながら、対話型のユーザーエクスペリエンスをサポートするコンテキスト。
     */
    private RequestContext requestContext = null;

    /**
     * コールバック
     */
    private IAlexaLoginCallback mAlexaLoginCallback = null;

    /**
     * ログイン中フラグ
     */
    private boolean isSignedIn = false;

    /**
     * ハンドラー
     */
    private Handler mHandler = null;

    /**
     * ログインの結果をコールバックするインタフェース
     */
    public interface IAlexaLoginCallback {
        /**
         * ログイン成功
         */
        public void onLoginSuccess(String accessToken);

        /**
         * ログイン失敗
         */
        public void onLoginFailed();

        /**
         * エラー
         */
        public void onLoginError();

        /**
         * 自動ログイン成功
         */
        public void onAutoLoginSuccess(String accessToken);

        /**
         * 自動ログイン失敗
         */
        public void onAutoLoginFailed();

        /**
         * 自動ログインエラー
         */
        public void onAutoLoginError();

        /**
         * ログアウト
         */
        public void onAlexaLogoutSuccess();

        /**
         * ログアウト失敗
         */
        public void onAlexaLogoutFailed();

        /**
         * トークン再取得
         * @param accessToken
         */
        public void onRefreshToken(String accessToken);

        /**
         * トークン再取得失敗
         */
        public void onRefreshTokenError();

        /**
         * 機能API送信成功
         */
        public void onCapabilitiesSendSuccess();
    }

    /**
     * コンストラクタ
     */
    private AlexaLoginManager() {
        if (DBG) android.util.Log.d(TAG, "AlexaLoginManager()");
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 生成済みマネージャインスタンスを取得
     * @return
     */
    public static AlexaLoginManager getInstance() {
        if (DBG) android.util.Log.d(TAG, "getInstance()");
        if (mManager == null) {
            mManager = new AlexaLoginManager();
        }
        return mManager;
    }

    public static void resetManager() {
        if (mManager != null) {
            // シリアルナンバーを削除
            mManager.PRODUCT_DSN = null;

            // RequestContextを破棄
            mManager.requestContext = null;
            // ログイン中フラグを初期化
            mManager.isSignedIn = false;
            // コールバックインスタンスを削除
            mManager.mAlexaLoginCallback = null;
            // Activityを削除
            mManager.mActivity = null;
        }
    }

    /**
     * ActivityがonResume()した時に実行
     */
    public void onActivityResume() {
        if (DBG) android.util.Log.d(TAG, "onActivityResume()");
        if (requestContext != null) {
            requestContext.onResume();
        }
    }

    /**
     * コールバックインスタンスの登録
     * @param callback
     */
    public void setAlexaLoginCallback(IAlexaLoginCallback callback) {
        if (DBG) android.util.Log.d(TAG, "setAlexaLoginCallback(), callback = " + callback);
        this.mAlexaLoginCallback = callback;
    }

    /**
     * RequestContext設定
     */
    public void init(final SupportActivity activity) {
        if (DBG) android.util.Log.d(TAG, "init(), activity = " + activity);
        mActivity = activity;
        // デバイスシリアルナンバー取得
        PRODUCT_DSN = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        requestContext = RequestContext.create(activity);       // Activity、又は、Fragmentが必要
        // 認証状態の通知を受けるリスナーを登録
        requestContext.registerListener(new AuthorizeListener() {
            /**
             * 認証成功
             * @param result
             */
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (DBG) android.util.Log.d(TAG, "AuthorizeListener#onSuccess()");

                // トークンを再取得
                Scope[] scopes = { ScopeFactory.scopeNamed("alexa:all") };
                AuthorizationManager.getToken(mActivity, scopes, new Listener<AuthorizeResult, AuthError>() {

                    /**
                     * トークン再取得に成功
                     * @param result
                     */
                    @Override
                    public void onSuccess(AuthorizeResult result) {
                        if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onSuccess() - AuthorizeListener");
                        // アクセストークンを取得.
                        final String accessToken = result.getAccessToken();
                        if (!TextUtils.isEmpty(accessToken)) {
                            // トークン有り
                            if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): accessToken:"+accessToken);
                            isSignedIn = true;
                            if (mAlexaLoginCallback != null) {
                                mAlexaLoginCallback.onLoginSuccess(accessToken);
                            }
                        } else {
                            // トークン無し
                            if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): Token is null!");
                            isSignedIn = false;
                            if (mAlexaLoginCallback != null) {
                                mAlexaLoginCallback.onLoginFailed();
                            }
                        }
                    }

                    /**
                     * トークン再取得に失敗
                     * @param ae
                     */
                    @Override
                    public void onError(AuthError ae) {
                        if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onError() - AuthorizeListener, ae = " + ae);
                        isSignedIn = false;
                        if (mAlexaLoginCallback != null) {
                            mAlexaLoginCallback.onLoginError();
                        }
                    }
                });
            }

            /**
             * 認証失敗
             * @param ae
             */
            @Override
            public void onError(AuthError ae) {
                if (DBG) android.util.Log.d(TAG, "AuthorizeListener#onError(), ae = " + ae);
                isSignedIn = false;
                if (mAlexaLoginCallback != null) {
                    mAlexaLoginCallback.onLoginError();
                }
            }

            /**
             * 認証キャンセル
             * @param cancellation
             */
            @Override
            public void onCancel(AuthCancellation cancellation) {
                if (DBG) android.util.Log.d(TAG, "AuthorizeListener#onCancel()");
                isSignedIn = false;
                if (mAlexaLoginCallback != null) {
                    mAlexaLoginCallback.onLoginError();
                }
            }

        });
    }

    /**
     * ログイン処理。-> ブラウザ遷移
     */
    public void doLogin() {
        if (DBG) android.util.Log.d(TAG, "doLogin()");
        // Login With Amazonホームページを参考.（http://login.amazon.com/android）
        final JSONObject scopeData = new JSONObject();
        final JSONObject productInstanceAttributes = new JSONObject();
        try {
            // デバイスシリアルナンバー
            productInstanceAttributes.put("deviceSerialNumber", PRODUCT_DSN);
            scopeData.put("productInstanceAttributes", productInstanceAttributes);
            // プロダクトID
            scopeData.put("productID", PRODUCT_ID);
            // TODO:デバイス名などを入れる箇所があっても良いと思うが公式サイトからは見当らない

            // 認証開始（ブラウザに遷移）
            AuthorizationManager.authorize(new AuthorizeRequest.Builder(requestContext)
                    .addScope(ScopeFactory.scopeNamed("alexa:all", scopeData))
                    .forGrantType(AuthorizeRequest.GrantType.ACCESS_TOKEN)
                    .shouldReturnUserData(false)
                    .build());
            // 認証の結果は、RequestContextに登録したAuthorizeListenerにコールバックされる

        } catch (JSONException e) {
            // handle exception here
            e.printStackTrace();
        }
    }

    /**
     * 自動ログイン（前回ログインを再利用）
     */
    public void doAutoLogin() {
        if (DBG) android.util.Log.d(TAG, "doAutoLogin()");
        final JSONObject scopeData = new JSONObject();
        final JSONObject productInstanceAttributes = new JSONObject();
        try {
            // デバイスシリアルナンバー
            productInstanceAttributes.put("deviceSerialNumber", PRODUCT_DSN);
            scopeData.put("productInstanceAttributes", productInstanceAttributes);
            // プロダクトID
            scopeData.put("productID", PRODUCT_ID);
            // TODO:デバイス名などを入れる箇所があっても良いと思うが公式サイトからは見当らない
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 認証状態が残っている場合、トークンを取得
        Scope[] scopes = { ScopeFactory.scopeNamed("alexa:all", scopeData) };
        AuthorizationManager.getToken(mActivity, scopes, new Listener<AuthorizeResult, AuthError>() {

            /**
             * 判定に成功
             * @param result
             */
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onSuccess() - AutoLogin");
                final String accessToken = result.getAccessToken();
                if (accessToken != null) {
                    // トークン有り
                    if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): accessToken = " + accessToken);
                    isSignedIn = true;
                    if (mAlexaLoginCallback != null && mHandler != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAlexaLoginCallback.onAutoLoginSuccess(accessToken);
                            }
                        });
                    }
                } else {
                    // トークン無し
                    if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): Token is null!");
                    isSignedIn = false;
                    if (mAlexaLoginCallback != null && mHandler != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAlexaLoginCallback.onAutoLoginFailed();
                            }
                        });
                    }
                }
            }

            /**
             * 判定失敗
             * @param ae
             */
            @Override
            public void onError(AuthError ae) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onError() - AutoLogin, ae = " + ae);
                isSignedIn = false;
                if (mAlexaLoginCallback != null && mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAlexaLoginCallback.onAutoLoginError();
                        }
                    });
                }
            }
        });

    }

    /**
     * ログアウト
     */
    public void logout() {
        if (DBG) android.util.Log.d(TAG, "logout()");
        // ログアウト実行
        AuthorizationManager.signOut(mActivity, new Listener<Void, AuthError>() {

            /**
             * ログアウト成功
             * @param data
             */
            @Override
            public void onSuccess(Void data) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onSuccess() - logout");
                isSignedIn = false;
                if (mAlexaLoginCallback != null) {
                    mAlexaLoginCallback.onAlexaLogoutSuccess();
                }
                //BaseActivity.set(PreferenceConst.LOGIN, false) ;
            }

            /**
             * ログアウト失敗
             * @param ae
             */
            @Override
            public void onError(AuthError ae) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onError() - logout, ae = " + ae);
                isSignedIn = false;
                if (mAlexaLoginCallback != null) {
                    mAlexaLoginCallback.onAlexaLogoutFailed();
                }
            }
        });
    }

    /**
     * トークン再取得
     */
    public void refreshToken() {
        if (DBG) android.util.Log.d(TAG, "refreshToken()");
        final JSONObject scopeData = new JSONObject();
        final JSONObject productInstanceAttributes = new JSONObject();
        try {
            // デバイスシリアルナンバー
            productInstanceAttributes.put("deviceSerialNumber", PRODUCT_DSN);
            scopeData.put("productInstanceAttributes", productInstanceAttributes);
            // プロダクトID
            scopeData.put("productID", PRODUCT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 認証状態が残っている場合、トークンを取得
        Scope[] scopes = {ScopeFactory.scopeNamed("alexa:all", scopeData)};
        AuthorizationManager.getToken(mActivity, scopes, new Listener<AuthorizeResult, AuthError>() {

            /**
             * 判定に成功
             * @param result
             */
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onSuccess() - refreshToken");
                // アクセストークンを取得.
                final String accessToken = result.getAccessToken();
                if (!TextUtils.isEmpty(accessToken)) {
                    if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): accessToken = " + accessToken);
                    if (mAlexaLoginCallback != null) {
                        mAlexaLoginCallback.onRefreshToken(accessToken);
                    }
                } else {
                    if (DBG) android.util.Log.d(TAG, " - AuthorizationManager.getToken(): Token is null!");
                    if (mAlexaLoginCallback != null) {
                        mAlexaLoginCallback.onRefreshTokenError();
                    }
                }
            }

            /**
             * 判定失敗
             * @param ae
             */
            @Override
            public void onError(AuthError ae) {
                if (DBG) android.util.Log.d(TAG, "AuthorizationManager.getToken()#onError() - refreshToken, ae = " + ae);
                if (mAlexaLoginCallback != null) {
                    mAlexaLoginCallback.onRefreshTokenError();
                }
            }
        });
    }

    public boolean isSignedIn() {
        return this.isSignedIn;
    }

    private int mRetryCount = 0;

    /*
        * Capabilities API送信
        *
        * */
    public void sendCapabilities() {
        if (DBG) android.util.Log.i(TAG, "sendCapabilities()");
        AlexaEventManager.sendCapabilities(mActivity, TokenManager.getToken(), new AlexaEventManager.AlexaCallback() {
            @Override
            public void onResponse(Call call, int httpCode) {
                if (httpCode == 204) {
                    // 成功
                    mAlexaLoginCallback.onCapabilitiesSendSuccess();
                    if (DBG) android.util.Log.d(TAG, "Capabilities  onResponse(), Success");
                } else {
                    // 失敗
                    if (DBG) android.util.Log.w(TAG, "Capabilities onResponse(), Error");
                    if (DBG) android.util.Log.w(TAG, "http code is " + httpCode);
                    if (httpCode == 500) {
                        double time = Math.pow(2d, mRetryCount);
                        if (DBG) android.util.Log.w(TAG, "Capabilities RetryCount = " + mRetryCount
                                + " time = " + time);
                        if (mRetryCount >= 7) {
                            mRetryCount = 0;
                        } else {
                            mRetryCount++;
                        }
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendCapabilities();
                            }
                        }, (long) (time * 1000));
                    } else if (httpCode == 400) {
                        if (DBG) android.util.Log.w(TAG, "Capabilities onResponse() error");
/*                        try {
                            String str = response.body().string();
                            if (DBG) android.util.Log.w(TAG, "Capabilities onResponse() error =" + str);
                            JSONObject json = new JSONObject(str);
                            String error = json.getString("error");
                            if (DBG) android.util.Log.w(TAG, "Capabilities onResponse() error =" + error);
                        } catch (org.json.JSONException | IOException e) {
                            if (DBG) android.util.Log.e(TAG, e.getMessage());
                        }*/
                    }

                }
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "Capabilities onParsedResponse(), itemList = " + itemList);
            }

            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "Capabilities onExecute()");
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "Capabilities onFailure(), e = " + e);
                Request request = call.request();
                HttpUrl url = null;
                RequestBody body = null;
                if (request != null) {
                    url = request.url();
                    body = request.body();
                }
                final StringBuffer buffer = new StringBuffer();
                buffer.append("url = ").append(url);
                buffer.append(", ");
                buffer.append("body = ").append(body);
                android.util.Log.w(TAG, " - onFailure(), " + buffer.toString());
            }
        });
    }
}
