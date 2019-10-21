package jp.pioneer.mbg.logmanager;

import java.util.EnumMap;
import java.util.Map;

import jp.pioneer.mobile.logger.api.Logger;
import jp.pioneer.mobile.logger.PLogger;


public class TagManager {

    // 各TAGに対応するLoggerを格納
    private Map<TAGS, Logger> mLoggerBuilderMap;

    /**
     *  初期化
     *    各TAGに対応するLoggerインスタンスを生成する
     */
    public void initialize () {
        mLoggerBuilderMap = new EnumMap<>(TAGS.class);

        for ( TAGS enumTag : TAGS.values() ) {
            Logger logger = new PLogger().tag(enumTag.name());
            mLoggerBuilderMap.put( enumTag, logger );
        }
    }


    /**
     *  指定TAGのLoggerを返す
     *
     */
    public Logger getLogger(TAGS e) {
            return mLoggerBuilderMap.get(e);
    }


    //////////////////////////////////////////////////////
    // Singleton
    //////////////////////////////////////////////////////

    private static volatile TagManager singleton;

    public static TagManager getInstance() {
        if (singleton == null) {
            synchronized (TagManager.class) {
                if (singleton == null) {
                    singleton = new TagManager();
                }
            }
        }
        return singleton;
    }

    private TagManager() {

    }


}
