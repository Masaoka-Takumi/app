package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;

/**
 * カーソルアダプターの抽象クラス
 */

public abstract class AbstractCursorAdapter extends CursorAdapter implements SectionIndexer {

    protected static final int SECTION_NUMBER = 0;

    protected Context mContext;
    protected LayoutInflater mInflater;

    @Nullable private String[] mSectionStrings;
    @Nullable private int[] mSectionIndexes;

    public AbstractCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public Cursor swapCursor(Cursor c, Bundle extras) {
        if (c == null || extras == null) {
            mSectionStrings = null;
            mSectionIndexes = null;
        } else {
            mSectionStrings = extras.getStringArray("section_strings");
            mSectionIndexes = extras.getIntArray("section_indexes");
        }

        return super.swapCursor(c);
    }

    public int getSectionCount(){
        return mSectionIndexes == null ? 0 : mSectionIndexes.length;
    }

    @Override
    public Object[] getSections() {
        return mSectionStrings == null ? null : mSectionStrings.clone();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(mSectionIndexes == null){
            return 0;
        }

        if (mSectionIndexes.length <= sectionIndex) {
            return 0;
        }

        return mSectionIndexes[sectionIndex];
    }

    @Override
    public int getSectionForPosition(int position) {
        int result = 0;
        if(mSectionIndexes != null) {
            for (int i = 0; i < mSectionIndexes.length; ++i) {
                if (mSectionIndexes[i] <= position) {
                    result = i;
                } else {
                    break;
                }
            }
        }

        return result;
    }
}
