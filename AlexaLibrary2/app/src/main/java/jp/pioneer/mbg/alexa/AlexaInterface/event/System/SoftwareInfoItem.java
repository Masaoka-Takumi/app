package jp.pioneer.mbg.alexa.AlexaInterface.event.System;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

/**
 * Created by esft-hatori on 2018/02/01.
 */

public class SoftwareInfoItem extends AlexaIfSystemItem {
    private String firmwareVersion;

    public SoftwareInfoItem(String firmwareVersion) {
        super(Constant.EVENT_SOFTWARE_INFO);
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new AlexaIfEventItem.Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(UUID.randomUUID().toString());
    }

    @Override
    protected void setPayLoad() {
        super.setPayLoad();
        this.payLoad = new AlexaIfEventItem.PayLoad();
        this.payLoad.setFirmwareVersion(firmwareVersion);
    }

}