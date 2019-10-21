package jp.pioneer.mbg.alexa.AlexaInterface;

import java.util.List;

public class AlexaIfDirectiveItem extends AlexaIfItem {
    public AlexaIfDirectiveItem(){
        super();
    }

    public AlexaIfDirectiveItem(String namespace){
        super(namespace);
    }

    /** 各Interfaceのヘッダーアイテムを管理するHashMap. */
    public Header header;
    /** 各Interfaceのペイロードアイテムを管理するHashMap. */
    public PayLoad payLoad;

    public Header getHeader() {
        setHeader();
        return header;
    }

    public PayLoad getPayLoad() {
        setPayLoad();
        return payLoad;
    }

    protected void setHeader() {}
    protected void setPayLoad() {}

    public static class Header{
        public String namespace;
        public String name;
        public String messageId;
        public String dialogRequestId;

        public String getNamespace() {
            return namespace;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        public String getMessageId() {
            return messageId;
        }
        public String getDialogRequestId() {
            return dialogRequestId;
        }
        public void setDialogRequestId(String dialogRequestId) {
            this.dialogRequestId = dialogRequestId;
        }
    }

    public class PayLoad {
        public String url;
        public String endpoint;
        public String format;
        public String token;
        public String type;
        public String scheduledTime;
        public String playBehavior;
        public AudioItem audioItem;
        public Long volume;
        public Boolean mute;
        public Long timeoutInMilliseconds;
        public String description;
        public String code;
        public String initiator;
        public Asset asset;
        public List<Asset> assets;
        public List<String> assetPlayOrder;
        public String backgroundAlertAsset;
        public Long loopCount;
        public Long loopPauseInMilliseconds;
        public String clearBehavior;
        public Boolean persistVisualIndicator;
        public Boolean playAudioIndicator;
        public Title title;
        public ImageStructure skillIcon;
        public String textField;
        public ImageStructure image;
        public List<ListItem> listItems;
        public String currentWeather;
        public ImageStructure currentWeatherIcon;
        public Temperature highTemperature;
        public Temperature lowTemperature;
        public List<WeatherForecast> weatherForecast;
        public String audioItemId;
        public Content content;
        public List<Control> controls;
        public Double latitude;
        public Double longitude;
        public String destinationName;

        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }

        public void setFormat(String format) {
            this.format = format;
        }
        public String getFormat() {
            return format;
        }

        public void setToken(String token) {
            this.token = token;
        }
        public String getToken() {
            if(token == null){
                //sometimes we need to return the stream tokens, not the top level tokens
                if(audioItem != null && audioItem.getStream() != null){
                    return audioItem.getStream().getToken();
                }
            }
            return token;
        }

        public void setType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }

        public void setScheduledTime(String scheduledTime) {
            this.scheduledTime = scheduledTime;
        }
        public String getScheduledTime() {
            return scheduledTime;
        }

        public void setPlayBehavior(String playBehavior) {
            this.playBehavior = playBehavior;
        }
        public String getPlayBehavior() {
            return playBehavior;
        }

        public void setAudioItem(AudioItem audioItem) {
            this.audioItem = audioItem;
        }
        public AudioItem getAudioItem() {
            return audioItem;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }
        public Long getVolume() {
            return volume;
        }

        public void setMute(Boolean mute) {
            this.mute = mute;
        }
        public Boolean getMute(){
            return mute;
        }

        public void setTimeoutInMilliseconds(Long timeoutInMilliseconds) {
            this.timeoutInMilliseconds = timeoutInMilliseconds;
        }
        public Long getTimeoutInMilliseconds(){
            return timeoutInMilliseconds;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }

        public void setCode(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        public String getEndpoint() {
            return endpoint;
        }

        public void setInitiator(String initiator) {
            this.initiator = initiator;
        }
        public String getInitiator() {
            return initiator;
        }

        public void setAsset(Asset asset) {
            this.asset = asset;
        }
        public Asset getAsset() {
            return asset;
        }

        public void setAssets(List<Asset> assets) {
            this.assets = assets;
        }
        public List<Asset> getAssets() {
            return assets;
        }

        public void setAssetPlayOrder(List<String> assetPlayOrder) {
            this.assetPlayOrder = assetPlayOrder;
        }
        public List<String> getAssetPlayOrder() {
            return assetPlayOrder;
        }

        public void setBackgroundAlertAsset(String backgroundAlertAsset) {
            this.backgroundAlertAsset = backgroundAlertAsset;
        }
        public String getBackgroundAlertAsset() {
            return backgroundAlertAsset;
        }

        public void setLoopCount(Long loopCount) {
            this.loopCount = loopCount;
        }
        public Long getLoopCount() {
            return loopCount;
        }

        public void setLoopPauseInMilliseconds(Long loopPauseInMilliseconds) {
            this.loopPauseInMilliseconds = loopPauseInMilliseconds;
        }
        public Long getLoopPauseInMilliseconds() {
            return loopPauseInMilliseconds;
        }

        public void setClearBehavior(String clearBehavior) {
            this.clearBehavior = clearBehavior;
        }

        public String getClearBehavior() {
            return clearBehavior;
        }

        public void setPersistVisualIndicator(Boolean persistVisualIndicator) {
            this.persistVisualIndicator = persistVisualIndicator;
        }
        public Boolean getPersistVisualIndicator() {
            return persistVisualIndicator;
        }

        public void setPlayAudioIndicator(Boolean playAudioIndicator) {
            this.playAudioIndicator = playAudioIndicator;
        }

        public Boolean getPlayAudioIndicator() {
            return playAudioIndicator;
        }

        public void setTitle(Title title) {
            this.title = title;
        }

        public Title getTitle() {
            return title;
        }

        public void setSkillIcon(ImageStructure skillIcon) {
            this.skillIcon = skillIcon;
        }

        public ImageStructure getSkillIcon() {
            return skillIcon;
        }

        public void setTextField(String textField) {
            this.textField = textField;
        }

        public String getTextField() {
            return textField;
        }

        public void setImage(ImageStructure image) {
            this.image = image;
        }

        public ImageStructure getImage() {
            return image;
        }

        public void setListItems(List<ListItem> listItems) {
            this.listItems = listItems;
        }

        public List<ListItem> getListItems() {
            return listItems;
        }

        public void setCurrentWeather(String currentWeather) {
            this.currentWeather = currentWeather;
        }

        public String getCurrentWeather() {
            return currentWeather;
        }

        public void setCurrentWeatherIcon(ImageStructure currentWeatherIcon) {
            this.currentWeatherIcon = currentWeatherIcon;
        }

        public ImageStructure getCurrentWeatherIcon() {
            return currentWeatherIcon;
        }

        public void setHighTemperature(Temperature highTemperature) {
            this.highTemperature = highTemperature;
        }

        public Temperature getHighTemperature() {
            return highTemperature;
        }

        public void setLowTemperature(Temperature lowTemperature) {
            this.lowTemperature = lowTemperature;
        }

        public Temperature getLowTemperature() {
            return lowTemperature;
        }

        public void setWeatherForecast(List<WeatherForecast> weatherForecast) {
            this.weatherForecast = weatherForecast;
        }

        public List<WeatherForecast> getWeatherForecast() {
            return weatherForecast;
        }

        public void setAudioItemId(String audioItemId) {
            this.audioItemId = audioItemId;
        }

        public String getAudioItemId() {
            return audioItemId;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        public Content getContent() {
            return content;
        }

        public void setControls(List<Control> controls) {
            this.controls = controls;
        }

        public List<Control> getControls() {
            return controls;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLongitude() {
            return longitude;
        }
        public void setDestinationName(String destinationName) {
            this.destinationName = destinationName;
        }

        public String getDestinationName() {
            return destinationName;
        }

    }

    public static class AudioItem {
        public String audioItemId;
        public Stream stream;

        public void setAudioItemId(String audioItemId) {
            this.audioItemId = audioItemId;
        }
        public String getAudioItemId() {
            return audioItemId;
        }
        public void setStream(Stream stream) {
            this.stream = stream;
        }
        public Stream getStream() {
            return stream;
        }
    }

    public static class Stream {
        public String url;
        public String streamFormat;
        public Long offsetInMilliseconds;
        public String expiryTime;
        public ProgressReport progressReport;
        public String token;
        public String expectedPreviousToken;

        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }

        public void setStreamFormat(String streamFormat) {
            this.streamFormat = streamFormat;
        }
        public String getStreamFormat() {
            return streamFormat;
        }

        public void setOffsetInMilliseconds(Long offsetInMilliseconds) {
            this.offsetInMilliseconds = offsetInMilliseconds;
        }
        public Long getOffsetInMilliseconds() {
            return offsetInMilliseconds;
        }

        public void setExpiryTime(String expiryTime) {
            this.expiryTime = expiryTime;
        }
        public String getExpiryTime() {
            return expiryTime;
        }

        public void setProgressReport(ProgressReport progressReport) {
            this.progressReport = progressReport;
        }
        public ProgressReport getProgressReport() {
            return progressReport;
        }

        public void setToken(String token) {
            this.token = token;
        }
        public String getToken() {
            return token;
        }

        public void setExpectedPreviousToken(String expectedPreviousToken) {
            this.expectedPreviousToken = expectedPreviousToken;
        }
        public String getExpectedPreviousToken() {
            return expectedPreviousToken;
        }
    }

    public static class ProgressReport {
        public Long progressReportDelayInMilliseconds;
        public Long progressReportIntervalInMilliseconds;

        public void setProgressReportDelayInMilliseconds(Long progressReportDelayInMilliseconds) {
            this.progressReportDelayInMilliseconds = progressReportDelayInMilliseconds;
        }
        public Long getProgressReportDelayInMilliseconds() {
            return progressReportDelayInMilliseconds;
        }

        public void setProgressReportIntervalInMilliseconds(Long progressReportIntervalInMilliseconds) {
            this.progressReportIntervalInMilliseconds = progressReportIntervalInMilliseconds;
        }
        public Long getProgressReportIntervalInMilliseconds() {
            return progressReportIntervalInMilliseconds;
        }
    }

    public static class Asset {
        public String assetId;
        public String url;
        public byte[] cache;


        public void setAssetId(String assetId) {
            this.assetId = assetId;
        }
        public String getAssetId() {
            return assetId;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }

        public void setCache(byte[] cache) {
            this.cache = cache;
        }
        public byte[] getCache() {
            return cache;
        }
    }

    public static class Title {
        public String mainTitle;
        public String subTitle;

        public void setMainTitle(String mainTitle) {
            this.mainTitle = mainTitle;
        }
        public String getMainTitle() {
            return mainTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }
        public String getSubTitle() {
            return subTitle;
        }
    }

    public static class ListItem {
        public String leftTextField;
        public String rightTextField;

        public void setLeftTextField(String leftTextField) {
            this.leftTextField = leftTextField;
        }

        public String getLeftTextField() {
            return leftTextField;
        }

        public void setRightTextField(String rightTextField) {
            this.rightTextField = rightTextField;
        }

        public String getRightTextField() {
            return rightTextField;
        }
    }

    public static class Temperature {
        public String value;
        public ImageStructure arrow;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setArrow(ImageStructure arrow) {
            this.arrow = arrow;
        }

        public ImageStructure getArrow() {
            return arrow;
        }
    }

    public static class WeatherForecast {
        public ImageStructure image;
        public String day;
        public String date;
        public String highTemperature;
        public String lowTemperature;

        public void setImage(ImageStructure image) {
            this.image = image;
        }

        public ImageStructure getImage() {
            return image;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getDay() {
            return day;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }

        public void setHighTemperature(String highTemperature) {
            this.highTemperature = highTemperature;
        }

        public String getHighTemperature() {
            return highTemperature;
        }

        public void setLowTemperature(String lowTemperature) {
            this.lowTemperature = lowTemperature;
        }

        public String getLowTemperature() {
            return lowTemperature;
        }
    }

    public static class ImageStructure {
        public String contentDescription;
        public List<Source> sources;

        public void setContentDescription(String contentDescription) {
            this.contentDescription = contentDescription;
        }

        public String getContentDescription() {
            return contentDescription;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }

        public List<Source> getSources() {
            return sources;
        }
    }

    public static class Source {
        public String url;
        public String darkBackgroundUrl;
        public String size;
        public Long widthPixels;
        public Long heightPixels;

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setDarkBackgroundUrl(String darkBackgroundUrl) {
            this.darkBackgroundUrl = darkBackgroundUrl;
        }

        public String getDarkBackgroundUrl() {
            return darkBackgroundUrl;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getSize() {
            return size;
        }

        public void setWidthPixels(Long widthPixels) {
            this.widthPixels = widthPixels;
        }

        public Long getWidthPixels() {
            return widthPixels;
        }

        public void setHeightPixels(Long heightPixels) {
            this.heightPixels = heightPixels;
        }

        public Long getHeightPixels() {
            return heightPixels;
        }
    }

    public static class Content {
        public String title;
        public String titleSubtext1;
        public String titleSubtext2;
        public String header;
        public String headerSubtext1;
        public Long mediaLengthInMilliseconds;
        public ImageStructure art;
        public Provider provider;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitleSubtext1(String titleSubtext1) {
            this.titleSubtext1 = titleSubtext1;
        }

        public String getTitleSubtext1() {
            return titleSubtext1;
        }

        public void setTitleSubtext2(String titleSubtext2) {
            this.titleSubtext2 = titleSubtext2;
        }

        public String getTitleSubtext2() {
            return titleSubtext2;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        public void setHeaderSubtext1(String headerSubtext1) {
            this.headerSubtext1 = headerSubtext1;
        }

        public String getHeaderSubtext1() {
            return headerSubtext1;
        }

        public void setMediaLengthInMilliseconds(Long mediaLengthInMilliseconds) {
            this.mediaLengthInMilliseconds = mediaLengthInMilliseconds;
        }

        public Long getMediaLengthInMilliseconds() {
            return mediaLengthInMilliseconds;
        }

        public void setArt(ImageStructure art) {
            this.art = art;
        }

        public ImageStructure getArt() {
            return art;
        }

        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        public Provider getProvider() {
            return provider;
        }
    }

    public static class Provider {
        public String name;
        public ImageStructure logo;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setLogo(ImageStructure logo) {
            this.logo = logo;
        }

        public ImageStructure getLogo() {
            return logo;
        }
    }

    public static class Control {
        public String type;
        public String name;
        public Boolean enabled;
        public Boolean selected;

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public Boolean getSelected() {
            return selected;
        }
    }
}