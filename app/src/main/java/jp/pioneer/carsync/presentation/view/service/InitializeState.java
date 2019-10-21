package jp.pioneer.carsync.presentation.view.service;

/**
 * Created by NSW00_008316 on 2017/04/17.
 */

public enum InitializeState {
    YET,            // 未生成
    INITIALIZING,   // 生成中
    COMPLETE,       // 成功
    ERROR,;         // 失敗
}
