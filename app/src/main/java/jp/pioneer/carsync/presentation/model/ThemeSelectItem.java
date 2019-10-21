package jp.pioneer.carsync.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import jp.pioneer.carsync.domain.model.ThemeType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by NSW00_007906 on 2018/01/23.
 */

public class ThemeSelectItem implements Parcelable {
    public ThemeType themeType;
    public int number;
    public ThemeSelectItem() {
        themeType = ThemeType.VIDEO_PATTERN1;
        number = 0;
    }

    public ThemeSelectItem(@NonNull ThemeType themeType, int number) {
        this.themeType = checkNotNull(themeType);
        this.number = number;
    }

    private ThemeSelectItem(Parcel in) {
        String type = in.readString();
        if(TextUtils.isEmpty(type)){
            themeType = ThemeType.VIDEO_PATTERN1;
        } else{
            themeType = ThemeType.valueOf(type);
        }
        number = in.readInt();
    }

    public static final Parcelable.Creator<ThemeSelectItem> CREATOR =
            new Parcelable.Creator<ThemeSelectItem>() {
                @Override
                public ThemeSelectItem createFromParcel(Parcel in) {
                    return new ThemeSelectItem(in);
                }

                @Override
                public ThemeSelectItem[] newArray(int size) {
                    return new ThemeSelectItem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(themeType.name());
        out.writeInt(number);
    }
}
