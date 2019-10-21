package jp.pioneer.carsync.presentation.view;

public interface YouTubeLinkWebViewView {

    /**
     * WebViewにURLをセット
     * @param url セットするURL文字列
     */
    void loadUrl(String url);

    /**
     * WebViewを前の画面に戻す
     */
    void goBack();

    /**
     * WebViewが前の画面に戻れるかどうか
     */
    boolean canGoBack();

    /**
     * YouTubeLink走行規制画面の表示
     */
    void setVisibleYouTubeLinkRegulation();

    /**
     * YouTubeLink走行規制画面を消す
     */
    void setGoneYouTubeLinkRegulation();

    /**
     * キーボードを閉じる(YouTubeLinkContainerのメソッド利用)
     */
    void closeKeyBoard();

    /**
     * 動画のフルスクリーンを閉じる
     */
    void closeFullScreen();

    /**
     * ラストソースに復帰する(間接的に画面を閉じる)
     */
    void callbackCloseByChangeLastSource();

    /**
     * 画面を閉じる
     */
    void callbackCloseResetLastSource();
}
