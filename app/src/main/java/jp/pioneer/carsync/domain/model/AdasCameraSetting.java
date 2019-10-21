package jp.pioneer.carsync.domain.model;

/**
 * ADASカメラ設定.
 */
public class AdasCameraSetting {

    /** 取り付けたカメラの高さ[mm]. */
    public int cameraHeight;

    /** 取り付けたカメラから車体先端の距離[mm]. */
    public int frontNoseDistance;

    /** 自車の幅[mm]. */
    public int vehicleWidth;

    /**
     * コンストラクタ.
     */
    public AdasCameraSetting(){
        reset();
    }

    /**
     * リセット.
     */
    public void reset(){
        cameraHeight = 1000;
        frontNoseDistance = 100;
        vehicleWidth = 1500;
    }
}
