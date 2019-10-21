package jp.pioneer.mbg.alexa.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import jp.pioneer.mbg.alexa.AmazonAlexaManager;

/*
* Networkの状態を監視するレシーバーー
*
* */
public class ConnectionReceiver extends BroadcastReceiver {
    private AmazonAlexaManager.IAlexaCallback mAlexaCallback ;
    private Observer mObserver;
    public ConnectionReceiver(Observer  observer) {
        mObserver = observer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            // 切断時の処理

            mObserver.onNetworDisConnect();
            return;
        }

        if (networkInfo.isConnected()) {
            // 接続時の処理
            if (mAlexaCallback != null) {
                mAlexaCallback.onNetworkConnect();
            }
            mObserver.onNetworkConnect();
        }
    }

    public interface Observer {
        void onNetworkConnect();
        void onNetworDisConnect();
    }

}