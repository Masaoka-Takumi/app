package jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfTemplateRuntimeItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.List;

public class RenderTemplateItem extends AlexaIfTemplateRuntimeItem {

//    public String messageId;
//    public String dialogRequestId;
    public String token;
    public String type;
    public Title title;
    public ImageStructure skillIcon;
    public String textField;
    public ImageStructure image;
    public List<ListItem> listItems;
    public String currentWeather;
    public String description;
    public ImageStructure currentWeatherIcon;
    public Temperature highTemperature;
    public Temperature lowTemperature;
    public List<WeatherForecast> weatherForecast;

    public RenderTemplateItem(){
        super(Constant.DIRECTIVE_RENDER_TEMPLATE);
    }

    public RenderTemplateItem(String messageId,String dialogRequestId, String token, String type, Title title, ImageStructure skillIcon, String textField){
        super(Constant.DIRECTIVE_RENDER_TEMPLATE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
        this.type = type;
        this.title = title;
        this.skillIcon = skillIcon;
        this.textField = textField;
    }

    public RenderTemplateItem(String messageId,String dialogRequestId, String token, String type, Title title, ImageStructure skillIcon, String textField, ImageStructure image){
        super(Constant.DIRECTIVE_RENDER_TEMPLATE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
        this.type = type;
        this.title = title;
        this.skillIcon = skillIcon;
        this.textField = textField;
        this.image = image;
    }

    public RenderTemplateItem(String messageId,String dialogRequestId, String token, String type, Title title, ImageStructure skillIcon, List<ListItem> listItems){
        super(Constant.DIRECTIVE_RENDER_TEMPLATE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
        this.type = type;
        this.title = title;
        this.skillIcon = skillIcon;
        this.listItems = listItems;
    }

    public RenderTemplateItem(String messageId,String dialogRequestId, String token, String type, Title title, ImageStructure skillIcon, String currentWeather, String description, ImageStructure currentWeatherIcon, Temperature highTemperature, Temperature lowTemperature, List<WeatherForecast> weatherForecast){
        super(Constant.DIRECTIVE_RENDER_TEMPLATE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
        this.type = type;
        this.title = title;
        this.skillIcon = skillIcon;
        this.currentWeather = currentWeather;
        this.description = description;
        this.currentWeatherIcon = currentWeatherIcon;
        this.highTemperature = highTemperature;
        this.lowTemperature = lowTemperature;
        this.weatherForecast = weatherForecast;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(messageId);
        this.header.setDialogRequestId(dialogRequestId);
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        payLoad.setToken(token);
        payLoad.setType(type);
        payLoad.setTitle(title);
        payLoad.setSkillIcon(skillIcon);
        payLoad.setTextField(textField);
        payLoad.setImage(image);
        payLoad.setListItems(listItems);
        payLoad.setCurrentWeather(currentWeather);
        payLoad.setDescription(description);
        payLoad.setCurrentWeatherIcon(currentWeatherIcon);
        payLoad.setHighTemperature(highTemperature);
        payLoad.setLowTemperature(lowTemperature);
        payLoad.setWeatherForecast(weatherForecast);
    }
}