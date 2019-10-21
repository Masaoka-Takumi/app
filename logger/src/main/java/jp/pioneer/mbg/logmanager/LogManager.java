package jp.pioneer.mbg.logmanager;

import android.content.Context;


import jp.pioneer.mobile.logger.PLogger;


public class LogManager {

    public void setup (Context applicationContext) {
        TagManager.getInstance().initialize();

        PLogger.factoryDefault(applicationContext);
    }


    //////////////////////////////////////////////////////
    // Singleton
    //////////////////////////////////////////////////////

    private static volatile LogManager singleton;

    public static LogManager getInstance() {
        if (singleton == null) {
            synchronized (LogManager.class) {
                if (singleton == null) {
                    singleton = new LogManager();
                }
            }
        }
        return singleton;
    }

    private LogManager() {

    }

}
