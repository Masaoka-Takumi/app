package jp.pioneer.mbg.alexa.util;

/**
 * Created by esft-komiya on 2016/10/18.
 */
public class LogUtil {
    private static final boolean DEBUG = true;
    private static final boolean VERBOSE = true;
    private static final boolean INFO = true;
    private static final boolean WARN = true;
    private static final boolean ERROR = true;

    public static void v(String tag, String msg){
        if(VERBOSE)android.util.Log.v(tag, msg);
    }
    public static void d(String tag, String msg){
        if(DEBUG)android.util.Log.d(tag, msg);
    }
    public static void i(String tag, String msg){
        if(INFO)android.util.Log.i(tag, msg);
    }
    public static void w(String tag, String msg){
        if(WARN)android.util.Log.w(tag, msg);
    }
    public static void e(String tag, String msg){
        if(ERROR)android.util.Log.e(tag, msg);
    }
}
