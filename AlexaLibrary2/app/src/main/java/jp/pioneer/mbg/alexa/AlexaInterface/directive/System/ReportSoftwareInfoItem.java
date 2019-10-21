package jp.pioneer.mbg.alexa.AlexaInterface.directive.System;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;



public class ReportSoftwareInfoItem extends  AlexaIfSystemItem {

//    public String messageId;

    public ReportSoftwareInfoItem(){
        super(Constant.DIRECTIVE_REPORT_SOFTWARE_INFO);
    }

    public ReportSoftwareInfoItem(String messageId){
        super(Constant.DIRECTIVE_REPORT_SOFTWARE_INFO);
        this.messageId = messageId;
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
    }
}