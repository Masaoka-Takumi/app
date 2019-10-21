package jp.pioneer.carsync.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;

/**
 * Created by NSW00_008316 on 2017/10/10.
 */

public class LiveSimulationItem implements Parcelable {
    private static final byte NOT_TYPE = -1;
    public SoundFieldControlSettingType type;
    public int resourceId;
    public int number;

    public LiveSimulationItem() {
        resourceId = 0;
        type = SoundFieldControlSettingType.OFF;
        number = 0;
    }

    public LiveSimulationItem(SoundFieldControlSettingType type,int resourceId, int number) {
        this.resourceId = resourceId;
        this.type = type;
        this.number = number;
    }

    private LiveSimulationItem(Parcel in) {
        if((byte)in.readInt()==NOT_TYPE){
            type = null;
        }else {
            type = SoundFieldControlSettingType.valueOf((byte) in.readInt());
        }
        resourceId = in.readInt();
        number = in.readInt();
    }

    public static final Parcelable.Creator<LiveSimulationItem> CREATOR =
            new Parcelable.Creator<LiveSimulationItem>() {
                @Override
                public LiveSimulationItem createFromParcel(Parcel in) {
                    return new LiveSimulationItem(in);
                }

                @Override
                public LiveSimulationItem[] newArray(int size) {
                    return new LiveSimulationItem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if(type ==null){
            out.writeInt(NOT_TYPE);
        }
        else{
            out.writeInt(type.code);
        }
        out.writeInt(resourceId);
        out.writeInt(number);
    }
}
