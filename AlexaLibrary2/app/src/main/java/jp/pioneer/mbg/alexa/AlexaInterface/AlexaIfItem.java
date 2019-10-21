package jp.pioneer.mbg.alexa.AlexaInterface;

public class AlexaIfItem {
    protected String namespace = null;
    protected String name = null;
    public String messageId;
    protected String dialogRequestId;

    public AlexaIfItem(){
    }

    public AlexaIfItem(String namespace){
        this.namespace = namespace;
    }

    public String getName() {
        return this.name;
    }
    public String getNamespace() {
        return this.namespace;
    }

    public final String getDialogRequestId(){
        return  this.dialogRequestId;
    }
}