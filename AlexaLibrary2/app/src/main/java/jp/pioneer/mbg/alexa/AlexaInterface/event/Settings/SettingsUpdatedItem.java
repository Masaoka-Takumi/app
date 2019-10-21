package jp.pioneer.mbg.alexa.AlexaInterface.event.Settings;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSettingsItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SettingsUpdatedItem extends AlexaIfSettingsItem {
    private List<Setting> settings;

    public SettingsUpdatedItem(List<Setting> settings){
        super(Constant.EVENT_SETTINGS_UPDATED);
        this.settings = settings;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(UUID.randomUUID().toString());
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        this.payLoad.setSetting(settings);
    }
}