package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-komiya on 2016/10/06.
 * アクセストークンなどのトークン管理をするクラス.
 */
public class TokenManager {
    private static final String TAG = TokenManager.class.getSimpleName();
    private static final boolean DBG = true;

    /** アクセストークン. */
    private static String sToken = null;

    public static void setToken(String token){
        sToken = token;
    }

    public static String getToken(){
        return sToken;
    }

}
