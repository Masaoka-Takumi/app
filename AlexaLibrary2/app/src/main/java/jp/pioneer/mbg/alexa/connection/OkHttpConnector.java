package jp.pioneer.mbg.alexa.connection;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class OkHttpConnector {
    public synchronized static byte[] getBody(String url) throws IOException {
        byte[] result = null;
        Response response = null;
        ResponseBody body = null;
        BufferedSource bufferedSource = null;

                Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = null;
        try {
            OkHttpClient client = OkHttpClientUtil.getNormalConnectionOkHttpClient();
            call = client.newCall(request);

            response = call.execute();

            if (response != null && response.isSuccessful()) {
                body = response.body();

                if (body != null) {
                    bufferedSource = body.source();
                    result = bufferedSource.readByteArray();

                    bufferedSource.close();
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (bufferedSource != null) {
                bufferedSource.close();
            }
            if (body != null) {
                body.close();
            }
            if (call != null) {
                call.cancel();
            }
        }
        return result;
    }
}
