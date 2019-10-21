package jp.pioneer.carsync.presentation.model;

import android.os.Bundle;

import icepick.Bundler;

/**
 * Created by NSW00_008316 on 2017/06/27.
 */

public class IntegerPropertyBundler implements Bundler<Property<Integer>> {
    @Override
    public void put(String key, Property<Integer> property, Bundle bundle) {
        if (property.peekValue() != null) {
            bundle.putInt(key, property.peekValue());
        }
    }

    @Override
    public Property<Integer> get(String key, Bundle bundle) {
        Property<Integer> property = new Property<>();
        if (bundle.containsKey(key)) {
            property.setValue(bundle.getInt(key));
        }
        return property;
    }
}
