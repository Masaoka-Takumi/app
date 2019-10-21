package jp.pioneer.mbg.alexa;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * GeolocationAPIで推奨される平滑法
 */
public class KalmanFilterManager {

    private double mSigmaW = 5.0;       // （推定値側のウェイト）
    private double mSigmaV = 50.0;     // 観測残差の共分散（センサー側のウェイト）

    private double xPre = 0.0;          // 今の時刻の予測推定値
    private double pPre = 0.0;          // 誤差の共分散行列（推定値の精度）

    public KalmanFilterManager() {
        init();
    }
    public KalmanFilterManager(double sigmaW, double sigmaV) {
        mSigmaW = sigmaW;
        mSigmaV = sigmaV;
        init();
    }

    private void init() {
        this.xPre = 0.0;
        this.pPre = 0.0;
    }

    /**
     *  カルマンフィルタにて推定値を算出
     * 【参考】https://qiita.com/torumitsutake/items/18d5a0fc1e4af55916fe
     * @param now
     */
    public double filtered(double now){
        ArrayList<Double> result = new ArrayList<Double>();

        double xForecast = xPre;                                        // 今の時刻の予測推定値（-> 計算用に代入）
        double pForecast = pPre + mSigmaW;                             // （-> 前回値の誤差にカルマンゲインを加える）
        double KGain = pForecast / (pForecast + mSigmaV);              // カルマンゲイン（前回推定値と計測値のどちらを優先するか->値が高いと計測値を優先）
        double xFiltered  = (xForecast + KGain * (now - xForecast));    // 更新された状態の推定値
        double pFiletered = (1 - KGain) * pForecast;                    // 更新された誤差の共分散

        xPre = xFiltered;
        pPre = pFiletered;

        return xFiltered;
    }
}
