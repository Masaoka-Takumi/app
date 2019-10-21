package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.VoiceCommand;

/**
 * 検索パラメータ
 */

public class SearchContentParams {

    private static final String ARG_SEARCH_CONTENT = "search_content";
    private static final String ARG_SEARCH_WORDS = "search_words";

    public VoiceCommand voiceCommand;
    public String[] searchWords;

    /**
     * バンドル化
     *
     * @param voiceCommand コマンド
     * @param searchWords  検索ワード群
     * @return Bundle
     */
    @NonNull
    public static Bundle toBundle(VoiceCommand voiceCommand, String[] searchWords) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_SEARCH_CONTENT, voiceCommand);
        bundle.putStringArray(ARG_SEARCH_WORDS, searchWords);
        return bundle;
    }

    /**
     * 復元
     *
     * @param bundle Bundle
     * @return SearchContentParams
     */
    public static SearchContentParams from(@NonNull Bundle bundle) {
        SearchContentParams params = new SearchContentParams();
        params.voiceCommand = (VoiceCommand) bundle.getSerializable(ARG_SEARCH_CONTENT);
        params.searchWords = bundle.getStringArray(ARG_SEARCH_WORDS);
        return params;
    }
}
