package jp.pioneer.carsync.infrastructure.content;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.google.common.primitives.Ints;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.pioneer.carsync.domain.content.ContactsCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;

/**
 * ContactsCursorLoaderの実装
 */
public class ContactsCursorLoaderImpl extends ContactsCursorLoader {
    private String mIndexColumn;
    private Bundle mExtras = Bundle.EMPTY;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param params クエリーパラメータ
     */
    public ContactsCursorLoaderImpl(Context context, QueryParams params) {
        super(context, params.uri, params.projection, params.selection, params.selectionArgs, params.sortOrder);
        mIndexColumn = params.indexColumn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor loadInBackground() {
        Cursor c = super.loadInBackground();
        //mExtras = c.getExtras();
        Bundle bundle = new Bundle();
        if (mIndexColumn == null || c == null || c.getCount() == 0) {
            bundle.putStringArray(SECTION_STRINGS, null);
            bundle.putStringArray(SECTION_INDEXES, null);
        } else {
            List<String> strings = new ArrayList<>();
            List<Integer> indexes = new ArrayList<>();
            int index = 0;
            String lastAlphabet = "";
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);

            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndex(mIndexColumn));
                String bucket = "";
                if (!title.isEmpty()) {
                    bucket = title.substring(0, 1);
                }

                char alphabet = 'A';
                boolean isNotAlphabet = true;
                for (int i = 0; i <= ('Z' - 'A'); i++, alphabet++) {
                    String stringAlphabet = String.valueOf(alphabet);
                    if(collator.compare(bucket, stringAlphabet) == 0){
                        if(!Objects.equals(lastAlphabet, stringAlphabet)) {
                            strings.add(stringAlphabet);
                            indexes.add(index);
                            lastAlphabet = stringAlphabet;
                        }
                        isNotAlphabet = false;
                        break;
                    }
                }

                if(isNotAlphabet && !Objects.equals(lastAlphabet, "#")){
                    strings.add("#");
                    indexes.add(index);
                    lastAlphabet = "#";
                }

                ++index;
            }

            bundle.putStringArray(SECTION_STRINGS, strings.toArray(new String[0]));
            bundle.putIntArray(SECTION_INDEXES, Ints.toArray(indexes));
        }

        mExtras = bundle;
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getExtras() {
        return mExtras;
    }
}
