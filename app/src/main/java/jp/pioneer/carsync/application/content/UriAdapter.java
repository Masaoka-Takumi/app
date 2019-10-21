package jp.pioneer.carsync.application.content;

import android.net.Uri;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Uri型アダプタ（Gson用）.
 * <p>
 * フィールドにUri型がある場合、Uri型アダプタを必要とする。
 * <pre>{@code
 *  Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriAdapter()).create();
 * }</pre>
 *
 */
public class UriAdapter extends TypeAdapter<Uri> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, Uri uri) throws IOException {
        out.value(uri.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri read(JsonReader in) throws IOException {
        return Uri.parse(in.nextString());
    }
}