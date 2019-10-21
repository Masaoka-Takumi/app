package jp.pioneer.carsync.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

import jp.pioneer.carsync.domain.model.MediaSourceType;

/**
 * SourceSelectItem
 */

public class SourceSelectItem implements Parcelable {
    private static final byte NOT_SOURCE = -1;
    public MediaSourceType sourceType;
    public int sourceTypeIcon;
    public int sourceTypeName;
    public String appPackageName;
    public String appLabelName;

    public SourceSelectItem() {
        this.sourceType = null;
        this.sourceTypeName = 0;
        this.sourceTypeIcon = 0;
        this.appPackageName = null;
        this.appLabelName = null;
    }

    public SourceSelectItem(MediaSourceType sourceType, int sourceTypeName, int sourceTypeIcon) {
        this.sourceType = sourceType;
        this.sourceTypeName = sourceTypeName;
        this.sourceTypeIcon = sourceTypeIcon;
        this.appPackageName = null;
        this.appLabelName = null;
    }

    public SourceSelectItem(String appPackageName, String appLabelName) {
        this.sourceType = null;
        this.sourceTypeName = 0;
        this.sourceTypeIcon = 0;
        this.appPackageName = appPackageName;
        this.appLabelName = appLabelName;
    }

    private SourceSelectItem(Parcel in) {
        if((byte)in.readInt()==NOT_SOURCE){
            sourceType = null;
        }else {
            sourceType = MediaSourceType.valueOf((byte) in.readInt());
        }
        sourceTypeName = in.readInt();
        sourceTypeIcon = in.readInt();
        appPackageName = in.readString();
        appLabelName = in.readString();
    }


    public static final Parcelable.Creator<SourceSelectItem> CREATOR =
            new Parcelable.Creator<SourceSelectItem>() {
                @Override
                public SourceSelectItem createFromParcel(Parcel in) {
                    return new SourceSelectItem(in);
                }

                @Override
                public SourceSelectItem[] newArray(int size) {
                    return new SourceSelectItem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if(sourceType ==null){
            out.writeInt(NOT_SOURCE);
        }
        else{
            out.writeInt(sourceType.code);
        }
        out.writeInt(sourceTypeName);
        out.writeInt(sourceTypeIcon);
        out.writeString(appPackageName);
        out.writeString(appLabelName);
    }

}
