package jp.pioneer.carsync.presentation.model;

import android.os.Bundle;
import android.util.SparseBooleanArray;

import icepick.Bundler;

/**
 * Created by BP06565 on 2017/02/16.
 */
public class SparseBooleanArrayPropertyBundler implements Bundler<Property<SparseBooleanArray>> {
    @Override
    public void put(String key, Property<SparseBooleanArray> property, Bundle bundle) {
        SparseBooleanArray array = property.peekValue();
        int[] keys = new int[array.size()];
        boolean[] values = new boolean[array.size()];
        for (int i = 0; i < array.size(); i++) {
            keys[i] = array.keyAt(i);
            values[i] = array.valueAt(i);
        }
        bundle.putIntArray(key + "#keys", keys);
        bundle.putBooleanArray(key + "#values", values);
    }

    @Override
    public Property<SparseBooleanArray> get(String key, Bundle bundle) {
        SparseBooleanArray array = new SparseBooleanArray();
        int[] keys = bundle.getIntArray(key + "#keys");
        boolean[] values = bundle.getBooleanArray(key + "#values");
        for (int i = 0; i < keys.length; i++) {
            array.put(keys[i], values[i]);
        }
        return new Property<>(array, true);
    }
}
