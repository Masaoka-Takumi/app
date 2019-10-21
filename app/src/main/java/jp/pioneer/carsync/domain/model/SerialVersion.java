package jp.pioneer.carsync.domain.model;

/**
 * シリアルバージョン.
 * <p>
 * {@link StatusHolder}の更新チェックを簡易的に行えるようにするために、
 * オブジェクトにバージョンを付けて、その値を比較することで更新されたか
 * どうかを判断できるようにする。
 * バージョンの値に意味はない。
 */
public class SerialVersion {
    private long serialVersion;

    /**
     * シリアルバージョン更新.
     */
    public void updateVersion() {
        ++serialVersion;
    }

    /**
     * シリアルバージョン取得.
     *
     * @return シリアルバージョン
     */
    public long getSerialVersion() {
        return serialVersion;
    }
}
