package jp.pioneer.carsync.infrastructure.crp.handler;

import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * データ応答パケットハンドラ.
 * <p>
 * 何らかのデータが返されるパケットの基底クラス。
 * ハンドラで処理を行った結果を{@link StatusHolder}に設定するため、{@link #getResult()}は
 * 単に{@link Boolean#TRUE}か{@link Boolean#FALSE}が設定されるだけである。
 * 通常、{@link Boolean#FALSE}は応答パケットが不正の場合であり、応答に含まれる結果が失敗を
 * 示すものではない。（製品版で発生することはないはずである）
 */
public abstract class DataResponsePacketHandler extends ResponsePacketHandler<Boolean> {
}
