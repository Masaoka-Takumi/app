package jp.pioneer.mbg.alexa.AlexaInterface.directive.Navigation;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfNavigationItem;
import jp.pioneer.mbg.alexa.util.Constant;

/**
 * Created by NSW00_007906 on 2018/12/11.
 */

public class SetDestinationItem extends AlexaIfNavigationItem {

    public Double latitude;
    public Double longitude;
    public String destinationName;
    public SetDestinationItem(){
        super(Constant.DIRECTIVE_SET_DESTINATION);
    }

    public SetDestinationItem(String messageId, Double latitude, Double longitude, String name){
        super(Constant.DIRECTIVE_SET_DESTINATION);
        this.messageId = messageId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.destinationName = name;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(messageId);
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        payLoad.setLatitude(latitude);
        payLoad.setLongitude(longitude);
        payLoad.setDestinationName(destinationName);
    }
}