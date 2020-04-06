package jp.pioneer.mbg.alexa.response;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.DeleteAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.SetAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.ClearQueueItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.StopItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Navigation.SetDestinationItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.ClearIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.SetIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.AdjustVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetMuteItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker.SetVolumeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.ExpectSpeechItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer.StopCaptureItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.ReportSoftwareInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.ResetUserInactivityItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.System.SetEndpointItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.logmanager.TAGS;
import jp.pioneer.mbg.logmanager.TagManager;
import jp.pioneer.mobile.logger.api.Logger;

/**
 * Created by esft-sakamori on 2017/08/10.
 */

/**
 * ディレクティブのJsonをパースする
 */
public class DirectiveParser {
    private static final Logger loggerAVS = TagManager.getInstance().getLogger(TAGS.AVS_Directive);

    /**
     * ディレクティブの解析（外部ライブラリ未使用）
     * @param json
     * @return
     */
    public static AlexaIfDirectiveItem parse(String json) throws org.json.JSONException {
        AlexaIfDirectiveItem result = null;

        if (TextUtils.isEmpty(json) == false) {
            JSONObject header = null;
            JSONObject payload = null;

            loggerAVS.json(json);

            {
                JSONObject jsonObject = new JSONObject(json);
                // headerとpayloadを取得
                if (jsonObject.has(Constant.JSON_PARAM_DIRECTIVE)) {
                    JSONObject directive = jsonObject.getJSONObject(Constant.JSON_PARAM_DIRECTIVE);
                    if (directive.has(Constant.JSON_EVENT_HEADER)) {
                        header = directive.getJSONObject(Constant.JSON_EVENT_HEADER);
                    }
                    if (directive.has(Constant.JSON_EVENT_PAYLOAD)) {
                        payload = directive.getJSONObject(Constant.JSON_EVENT_PAYLOAD);
                    }
                }
            }
            String headerName = null;
            if (header != null && header.has(Constant.JSON_HEADER_MESSAGE_ID)) {
                headerName = header.getString(Constant.JSON_HEADER_NAME);
            }
            String messageId = null;
            if (header != null && header.has(Constant.JSON_HEADER_MESSAGE_ID)) {
                messageId = header.getString(Constant.JSON_HEADER_MESSAGE_ID);
            }
            String dialogRequestId = null;
            if (header != null && header.has(Constant.JSON_HEADER_DIALOG_ID)) {
                dialogRequestId = header.getString(Constant.JSON_HEADER_DIALOG_ID);
            }
            String token = null;
            if (payload != null && payload.has(Constant.JSON_PAYLOAD_TOKEN)) {
                token = payload.getString(Constant.JSON_PAYLOAD_TOKEN);
            }
            if (headerName != null) {
                switch (headerName) {
                    case Constant.DIRECTIVE_DELETE_ALERT: {
                        result = new DeleteAlertItem(messageId, dialogRequestId, token);
                        break;
                    }
                    case Constant.DIRECTIVE_SET_ALERT: {
                        String type = null;
                        if (payload != null && payload.has("type")) {
                            type = payload.getString("type");
                        }
                        String scheduledTime = null;
                        if (payload != null && payload.has("scheduledTime")) {
                            scheduledTime = payload.getString("scheduledTime");
                        }
                        ArrayList<AlexaIfDirectiveItem.Asset> assets = new ArrayList<>();
                        if (payload != null && payload.has("assets")) {
                            JSONArray array = payload.getJSONArray("assets");
                            for (int index = 0; index < array.length(); index++) {
                                JSONObject object = array.getJSONObject(index);
                                String assetId = null;
                                if (object.has("assetId")) {
                                    assetId = object.getString("assetId");
                                }
                                String url = null;
                                if (object.has("url")) {
                                    url = object.getString("url");
                                }

                                AlexaIfDirectiveItem.Asset asset = new AlexaIfDirectiveItem.Asset();
                                asset.setAssetId(assetId);
                                asset.setUrl(url);

                                assets.add(asset);
                            }
                        }
                        ArrayList<String> assetPlayOrder = new ArrayList<>();
                        if (payload != null && payload.has("assetPlayOrder")) {
                            JSONArray array = payload.getJSONArray("assetPlayOrder");
                            for (int index = 0; index < array.length(); index++) {
                                String order = array.getString(index);
                                assetPlayOrder.add(order);
                            }
                        }
                        String backgroundAlertAsset = null;
                        if (payload.has("backgroundAlertAsset")) {
                            backgroundAlertAsset = payload.getString("backgroundAlertAsset");
                        }
                        Long loopCount = null;
                        if (payload.has("loopCount")) {
                            loopCount = payload.getLong("loopCount");
                        }
                        Long loopPauseInMilliseconds = null;
                        if (payload.has("loopPauseInMilliSeconds")) {
                            loopPauseInMilliseconds = payload.getLong("loopPauseInMilliSeconds");
                        }

                        result = new SetAlertItem(messageId, dialogRequestId, token, type, scheduledTime, assets, assetPlayOrder, backgroundAlertAsset, loopCount, loopPauseInMilliseconds);
                        break;
                    }
                    case Constant.DIRECTIVE_CLEAR_QUEUE: {
                        String clearBehavior = null;
                        if (payload != null && payload.has(Constant.JSON_PAYLOAD_CLEAR_BEHAVIOR)) {
                            clearBehavior = payload.getString(Constant.JSON_PAYLOAD_CLEAR_BEHAVIOR);
                        }
                        result = new ClearQueueItem(messageId, dialogRequestId, clearBehavior);
                        break;
                    }
                    case Constant.DIRECTIVE_PLAY: {
                        String playBehavior = null;
                        if (payload != null && payload.has(Constant.JSON_PAYLOAD_PLAY_BEHAVIOR)) {
                            playBehavior = payload.getString(Constant.JSON_PAYLOAD_PLAY_BEHAVIOR);
                        }
                        AlexaIfDirectiveItem.AudioItem audioItem = new AlexaIfDirectiveItem.AudioItem();
                        if (payload != null && payload.has("audioItem")) {
                            JSONObject audioItemObject = payload.getJSONObject("audioItem");
                            String audioItemId = null;
                            if (audioItemObject.has("audioItemId")) {
                                audioItemId = audioItemObject.getString("audioItemId");
                            }
                            AlexaIfDirectiveItem.Stream stream = new AlexaIfDirectiveItem.Stream();
                            if (audioItemObject.has("stream")) {
                                JSONObject streamObject = audioItemObject.getJSONObject("stream");
                                String url = null;
                                if (streamObject.has("url")) {
                                    url = streamObject.getString("url");
                                }
                                String streamFormat = null;
                                if (streamObject.has("streamFormat")) {
                                    streamFormat = streamObject.getString("streamFormat");
                                }
                                Long offsetInMilliseconds = null;
                                if (streamObject.has("offsetInMilliseconds")) {
                                    offsetInMilliseconds = streamObject.getLong("offsetInMilliseconds");
                                }
                                String expiryTime = null;
                                if (streamObject.has("expiryTime")) {
                                    expiryTime = streamObject.getString("expiryTime");
                                }
                                AlexaIfDirectiveItem.ProgressReport progressReport = new AlexaIfDirectiveItem.ProgressReport();
                                if (streamObject.has("progressReport")) {
                                    JSONObject progressReportObject = streamObject.getJSONObject("progressReport");
                                    Long progressReportDelayInMilliseconds = null;
                                    if (progressReportObject.has("progressReportDelayInMilliseconds")) {
                                        progressReportDelayInMilliseconds = progressReportObject.getLong("progressReportDelayInMilliseconds");
                                    }
                                    Long progressReportIntervalInMilliseconds = null;
                                    if (progressReportObject.has("progressReportIntervalInMilliseconds")) {
                                        progressReportIntervalInMilliseconds = progressReportObject.getLong("progressReportIntervalInMilliseconds");
                                    }
                                    progressReport.setProgressReportDelayInMilliseconds(progressReportDelayInMilliseconds);
                                    progressReport.setProgressReportIntervalInMilliseconds(progressReportIntervalInMilliseconds);
                                }
                                String token2 = null;
                                if (streamObject.has("token")) {
                                    token2 = streamObject.getString("token");
                                }
                                String expectedPreviousToken = null;
                                if (streamObject.has("expectedPreviousToken")) {
                                    expectedPreviousToken = streamObject.getString("expectedPreviousToken");
                                }

                                stream.setUrl(url);
                                stream.setStreamFormat(streamFormat);
                                stream.setOffsetInMilliseconds(offsetInMilliseconds);
                                stream.setExpiryTime(expiryTime);
                                stream.setProgressReport(progressReport);
                                stream.setToken(token2);
                                stream.setExpectedPreviousToken(expectedPreviousToken);
                            }
                            audioItem.setAudioItemId(audioItemId);
                            audioItem.setStream(stream);
                        }

                        result = new PlayItem(messageId, dialogRequestId, playBehavior, audioItem);
                        break;
                    }
                    case Constant.DIRECTIVE_STOP: {
                        result = new StopItem(messageId, dialogRequestId);
                        break;
                    }
                    case Constant.DIRECTIVE_CLEAR_INDICATOR: {
                        result = new ClearIndicatorItem(messageId);
                        break;
                    }
                    case Constant.DIRECTIVE_SET_INDICATOR: {
                        Boolean persistVisualIndicator = null;
                        if (payload != null && payload.has("persistVisualIndicator")) {
                            persistVisualIndicator = payload.getBoolean("persistVisualIndicator");
                        }
                        Boolean playAudioIndicator = null;
                        if (payload != null && payload.has("playAudioIndicator")) {
                            playAudioIndicator = payload.getBoolean("playAudioIndicator");
                        }
                        AlexaIfDirectiveItem.Asset asset = new AlexaIfDirectiveItem.Asset();
                        if (payload != null && payload.has("asset")) {
                            JSONObject assetObject = payload.getJSONObject("asset");
                            String assetId = null;
                            if (assetObject.has("assetId")) {
                                assetId = assetObject.getString("assetId");
                            }
                            String url = null;
                            if (assetObject.has("url")) {
                                url = assetObject.getString("url");
                            }
                            asset.setAssetId(assetId);
                            asset.setUrl(url);
                        }
                        result = new SetIndicatorItem(messageId, asset, persistVisualIndicator, playAudioIndicator);
                        break;
                    }
                    case Constant.DIRECTIVE_ADJUST_VOLUME: {
                        Long volume = null;
                        if (payload != null && payload.has("volume")) {
                            volume = payload.getLong("volume");
                        }
                        result = new AdjustVolumeItem(messageId, dialogRequestId, volume);
                        break;
                    }
                    case Constant.DIRECTIVE_SET_MUTE: {
                        Boolean mute = null;
                        if (payload != null && payload.has("mute")) {
                            mute = payload.getBoolean("mute");
                        }
                        result = new SetMuteItem(messageId, dialogRequestId, mute);
                        break;
                    }
                    case Constant.DIRECTIVE_SET_VOLUME: {
                        Long volume = null;
                        if (payload != null && payload.has("volume")) {
                            volume = payload.getLong("volume");
                        }
                        result = new SetVolumeItem(messageId, dialogRequestId, volume);
                        break;
                    }
                    case Constant.DIRECTIVE_EXPECT_SPEECH: {
                        Long timeoutInMilliseconds = null;
                        if (payload != null && payload.has("timeoutInMilliseconds")) {
                            timeoutInMilliseconds = payload.getLong("timeoutInMilliseconds");
                        }
                        // 2018.03.30 API仕様変更対応
//                        String initiator = null;
//                        if (payload != null && payload.has("initiator")) {
//                            initiator = payload.getString("initiator");
//                        }
//                        result = new ExpectSpeechItem(messageId, dialogRequestId, timeoutInMilliseconds, initiator);
                        Initiator initiator = null;
                        if (payload != null && payload.has("initiator")) {
                            JSONObject initiatorObject = payload.getJSONObject("initiator");
                            String type = null;
                            Initiator.Payload initiatorPayload = null;
                            if (initiatorObject != null && initiatorObject.has("type")) {
                                type = initiatorObject.getString("type");
                            }
                            if (initiatorObject != null && initiatorObject.has("payload")) {
                                JSONObject payloadObject = payload.getJSONObject("payload");
                                Initiator.Payload.WakeWordIndices wakeWordIndices = null;
                                String payloadToken = null;
                                if (payloadObject != null && payloadObject.has("wakeWordIndices")) {
                                    JSONObject wakeWordIndicesObject = payload.getJSONObject("wakeWordIndices");
                                    long startIndexInSamples = 0L;
                                    long endIndexInSamples = 0L;
                                    if (wakeWordIndicesObject != null && wakeWordIndicesObject.has("startIndexInSamples")) {
                                        startIndexInSamples = wakeWordIndicesObject.getLong("startIndexInSamples");
                                        endIndexInSamples = wakeWordIndicesObject.getLong("endIndexInSamples");
                                    }
                                    wakeWordIndices = new Initiator.Payload.WakeWordIndices(startIndexInSamples, endIndexInSamples);
                                }
                                if (payloadObject != null && payloadObject.has("token")) {
                                    payloadToken = payloadObject.getString("token");
                                }
                                initiatorPayload = new Initiator.Payload(wakeWordIndices, payloadToken);
                            }
                            initiator = new Initiator(type, initiatorPayload);
                        }
                        result = new ExpectSpeechItem(messageId, dialogRequestId, timeoutInMilliseconds, initiator);
                        break;
                    }
                    case Constant.DIRECTIVE_STOP_CAPTURE: {
                        result = new StopCaptureItem(messageId, dialogRequestId);
                        break;
                    }
                    case Constant.DIRECTIVE_SPEAK: {
                        String url = null;
                        if (payload != null && payload.has("url")) {
                            url = payload.getString("url");
                        }
                        String format = null;
                        if (payload != null && payload.has("format")) {
                            format = payload.getString("format");
                        }

                        result = new SpeakItem(messageId, dialogRequestId, url, format, token);
                        break;
                    }
                    case Constant.DIRECTIVE_RESET_USER_INACTIVITY: {
                        result = new ResetUserInactivityItem(messageId);
                        break;
                    }
                    case Constant.DIRECTIVE_SET_ENDPOINT: {
                        String endpoint = null;
                        if (payload != null && payload.has("endpoint")) {
                            endpoint = payload.getString("endpoint");
                        }

                        result = new SetEndpointItem(messageId, endpoint);
                        break;
                    }
                    case Constant.DIRECTIVE_REPORT_SOFTWARE_INFO:{
                        result = new ReportSoftwareInfoItem();
                        break;
                    }

                    case Constant.DIRECTIVE_RENDER_PLAYER_INFO: {
                        String audioItemId = null;
                        if (payload != null && payload.has("audioItemId")) {
                            audioItemId = payload.getString("audioItemId");
                        }
                        AlexaIfDirectiveItem.Content content = new AlexaIfDirectiveItem.Content();
                        if (payload != null && payload.has("content")) {
                            JSONObject contentObject = payload.getJSONObject("content");
                            String title = null;
                            if (contentObject.has("title")) {
                                title = contentObject.getString("title");
                            }
                            String titleSubtext1 = null;
                            if (contentObject.has("titleSubtext1")) {
                                titleSubtext1 = contentObject.getString("titleSubtext1");
                            }
                            String titleSubtext2 = null;
                            if (contentObject.has("titleSubtext2")) {
                                titleSubtext2 = contentObject.getString("titleSubtext2");
                            }
                            String header2 = null;
                            if (contentObject.has("header")) {
                                header2 = contentObject.getString("header");
                            }
                            String headerSubtext1 = null;
                            if (contentObject.has("headerSubtext1")) {
                                headerSubtext1 = contentObject.getString("headerSubtext1");
                            }
                            Long mediaLengthInMilliseconds = null;
                            if (contentObject.has("mediaLengthInMilliseconds")) {
                                mediaLengthInMilliseconds = contentObject.getLong("mediaLengthInMilliseconds");
                            }


                            AlexaIfDirectiveItem.ImageStructure art = new AlexaIfDirectiveItem.ImageStructure();
                            if (contentObject != null && contentObject.has("art")) {
                                JSONObject artObject = contentObject.getJSONObject("art");
                                String contentDescription = null;
                                if (artObject.has("contentDescription")) {
                                    contentDescription = artObject.getString("contentDescription");
                                }
                                List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                                if (artObject.has("sources")) {
                                    JSONArray sourceArray = artObject.getJSONArray("sources");
                                    for (int index = 0; index < sourceArray.length(); index++) {
                                        JSONObject sourceObject = sourceArray.getJSONObject(index);
                                        String url = null;
                                        if (sourceObject.has("url")) {
                                            url = sourceObject.getString("url");
                                        }
                                        String darkBackgroundUrl = null;
                                        if (sourceObject.has("darkBackgroundUrl")) {
                                            darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                        }
                                        String size = null;
                                        if (sourceObject.has("size")) {
                                            size = sourceObject.getString("size");
                                        }
                                        Long widthPixels = null;
                                        if (sourceObject.has("widthPixels")) {
                                            widthPixels = sourceObject.getLong("widthPixels");
                                        }
                                        Long heightPixels = null;
                                        if (sourceObject.has("heightPixels")) {
                                            heightPixels = sourceObject.getLong("heightPixels");
                                        }

                                        AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                        source.setUrl(url);
                                        source.setDarkBackgroundUrl(darkBackgroundUrl);
                                        source.setSize(size);
                                        source.setWidthPixels(widthPixels);
                                        source.setHeightPixels(heightPixels);

                                        sources.add(source);
                                    }
                                }

                                art.setContentDescription(contentDescription);
                                art.setSources(sources);
                            }
                            AlexaIfDirectiveItem.ImageStructure pro = new AlexaIfDirectiveItem.ImageStructure();
                            AlexaIfDirectiveItem.Provider provider = new AlexaIfDirectiveItem.Provider();
                            if (contentObject != null && contentObject.has("provider")) {
                                JSONObject providerObject = contentObject.getJSONObject("provider");
                                String name = null;
                                if (providerObject.has("name")) {
                                    name = providerObject.getString("name");
                                }
                                String logo = null;
                                if (providerObject.has("logo")) {
                                    logo = providerObject.getString("logo");
                                    JSONObject logoObject = providerObject.getJSONObject("logo");
                                    List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                                    if (logoObject.has("sources")) {
                                        JSONArray sourceArray = logoObject.getJSONArray("sources");
                                        for (int index = 0; index < sourceArray.length(); index++) {
                                            JSONObject sourceObject = sourceArray.getJSONObject(index);
                                            String url = null;
                                            if (sourceObject.has("url")) {
                                                url = sourceObject.getString("url");
                                            }

                                            AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                            source.setUrl(url);

                                            sources.add(source);
                                        }
                                    }
                                    pro.setSources(sources);
                                }
                                provider.setName(name);
                                provider.setLogo(pro);

                            }

                            content.setTitle(title);
                            content.setTitleSubtext1(titleSubtext1);
                            content.setTitleSubtext2(titleSubtext2);
                            content.setHeader(header2);
                            content.setHeaderSubtext1(headerSubtext1);
                            content.setMediaLengthInMilliseconds(mediaLengthInMilliseconds);
                            content.setArt(art);
                            content.setProvider(provider);
                        }
                        List<AlexaIfDirectiveItem.Control> controls = new ArrayList<>();
                        if (payload != null && payload.has("controls")) {
                            JSONArray controlArray = payload.getJSONArray("controls");
                            for (int index = 0; index < controlArray.length(); index++) {
                                JSONObject controlObject = controlArray.getJSONObject(index);
                                String type = null;
                                if (controlObject.has("type")) {
                                    type = controlObject.getString("type");
                                }
                                String name = null;
                                if (controlObject.has("name")) {
                                    name = controlObject.getString("name");
                                }
                                Boolean enabled = null;
                                if (controlObject.has("enabled")) {
                                    enabled = controlObject.getBoolean("enabled");
                                }
                                Boolean selected = null;
                                if (controlObject.has("selected")) {
                                    selected = controlObject.getBoolean("selected");
                                }

                                AlexaIfDirectiveItem.Control control = new AlexaIfDirectiveItem.Control();
                                control.setType(type);
                                control.setName(name);
                                control.setEnabled(enabled);
                                control.setSelected(selected);

                                controls.add(control);
                            }
                        }

                        // TODO:第2引数にendpointがある -> 消さねば
                        result = new RenderPlayerInfoItem(messageId, "", dialogRequestId, audioItemId, content, controls);
                        break;
                    }
                    case Constant.DIRECTIVE_RENDER_TEMPLATE: {
                        String type = null;
                        if (payload != null && payload.has("title")) {
                            type = payload.getString("type");
                        }
                        AlexaIfDirectiveItem.Title title = new AlexaIfDirectiveItem.Title();
                        if (payload != null && payload.has("title")) {
                            JSONObject titleObject = payload.getJSONObject("title");
                            String mainTitle = null;
                            if (titleObject.has("mainTitle")) {
                                mainTitle = titleObject.getString("mainTitle");
                            }
                            String subTitle = null;
                            if (titleObject.has("subTitle")) {
                                subTitle = titleObject.getString("subTitle");
                            }

                            title.setMainTitle(mainTitle);
                            title.setSubTitle(subTitle);
                        }
                        AlexaIfDirectiveItem.ImageStructure skillIcon = new AlexaIfDirectiveItem.ImageStructure();
                        if (payload != null && payload.has("skillIcon")) {
                            JSONObject titleObject = payload.getJSONObject("skillIcon");
                            String contentDescription = null;
                            if (titleObject.has("contentDescription")) {
                                contentDescription = titleObject.getString("contentDescription");
                            }
                            List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                            if (payload != null && payload.has("sources")) {
                                JSONArray sourceArray = payload.getJSONArray("sources");
                                for (int index = 0; index < sourceArray.length(); index++) {
                                    JSONObject sourceObject = sourceArray.getJSONObject(index);
                                    String url = null;
                                    if (sourceObject.has("url")) {
                                        url = sourceObject.getString("url");
                                    }
                                    String darkBackgroundUrl = null;
                                    if (sourceObject.has("darkBackgroundUrl")) {
                                        darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                    }
                                    String size = null;
                                    if (sourceObject.has("size")) {
                                        size = sourceObject.getString("size");
                                    }
                                    Long widthPixels = null;
                                    if (sourceObject.has("widthPixels")) {
                                        widthPixels = sourceObject.getLong("widthPixels");
                                    }
                                    Long heightPixels = null;
                                    if (sourceObject.has("heightPixels")) {
                                        heightPixels = sourceObject.getLong("heightPixels");
                                    }

                                    AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                    source.setUrl(url);
                                    source.setDarkBackgroundUrl(darkBackgroundUrl);
                                    source.setSize(size);
                                    source.setWidthPixels(widthPixels);
                                    source.setHeightPixels(heightPixels);

                                    sources.add(source);
                                }
                            }

                            skillIcon.setContentDescription(contentDescription);
                            skillIcon.setSources(sources);
                        }
                        String textField = null;
                        if (payload != null && payload.has("textField")) {
                            textField = payload.getString("textField");
                        }
                        AlexaIfDirectiveItem.ImageStructure image = new AlexaIfDirectiveItem.ImageStructure();
                        if (payload != null && payload.has("image")) {
                            JSONObject imageObject = payload.getJSONObject("image");
                            String contentDescription = null;
                            if (imageObject.has("contentDescription")) {
                                contentDescription = imageObject.getString("contentDescription");
                            }
                            List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                            if (imageObject.has("sources")) {
                                JSONArray sourceArray = imageObject.getJSONArray("sources");
                                for (int index = 0; index < sourceArray.length(); index++) {
                                    JSONObject sourceObject = sourceArray.getJSONObject(index);
                                    String url = null;
                                    if (sourceObject.has("url")) {
                                        url = sourceObject.getString("url");
                                    }
                                    String darkBackgroundUrl = null;
                                    if (sourceObject.has("darkBackgroundUrl")) {
                                        darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                    }
                                    String size = null;
                                    if (sourceObject.has("size")) {
                                        size = sourceObject.getString("size");
                                    }
                                    Long widthPixels = null;
                                    if (sourceObject.has("widthPixels")) {
                                        widthPixels = sourceObject.getLong("widthPixels");
                                    }
                                    Long heightPixels = null;
                                    if (sourceObject.has("heightPixels")) {
                                        heightPixels = sourceObject.getLong("heightPixels");
                                    }

                                    AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                    source.setUrl(url);
                                    source.setDarkBackgroundUrl(darkBackgroundUrl);
                                    source.setSize(size);
                                    source.setWidthPixels(widthPixels);
                                    source.setHeightPixels(heightPixels);

                                    sources.add(source);
                                }
                            }

                            image.setContentDescription(contentDescription);
                            image.setSources(sources);
                        }
                        List<AlexaIfDirectiveItem.ListItem> listItems = new ArrayList<>();
                        if (payload != null && payload.has("listItems")) {
                            JSONArray listItemArray = payload.getJSONArray("listItems");
                            for (int index = 0; index < listItemArray.length(); index++) {
                                JSONObject listItemObject = listItemArray.getJSONObject(index);
                                String leftTextField = null;
                                if (listItemObject.has("leftTextField")) {
                                    leftTextField = listItemObject.getString("leftTextField");
                                }
                                String rightTextField = null;
                                if (listItemObject.has("rightTextField")) {
                                    rightTextField = listItemObject.getString("rightTextField");
                                }

                                AlexaIfDirectiveItem.ListItem listItem = new AlexaIfDirectiveItem.ListItem();
                                listItem.setLeftTextField(leftTextField);
                                listItem.setRightTextField(rightTextField);

                                listItems.add(listItem);
                            }
                        }
                        String currentWeather = null;
                        if (payload != null && payload.has("currentWeather")) {
                            currentWeather = payload.getString("currentWeather");
                        }
                        String description = null;
                        if (payload != null && payload.has("description")) {
                            description = payload.getString("description");
                        }
                        AlexaIfDirectiveItem.ImageStructure currentWeatherIcon = new AlexaIfDirectiveItem.ImageStructure();
                        if (payload != null && payload.has("art")) {
                            JSONObject artObject = payload.getJSONObject("art");
                            String contentDescription = null;
                            if (artObject.has("contentDescription")) {
                                contentDescription = artObject.getString("contentDescription");
                            }
                            List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                            if (artObject.has("sources")) {
                                JSONArray sourceArray = artObject.getJSONArray("sources");
                                for (int index = 0; index < sourceArray.length(); index++) {
                                    JSONObject sourceObject = sourceArray.getJSONObject(index);
                                    String url = null;
                                    if (sourceObject.has("url")) {
                                        url = sourceObject.getString("url");
                                    }
                                    String darkBackgroundUrl = null;
                                    if (sourceObject.has("darkBackgroundUrl")) {
                                        darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                    }
                                    String size = null;
                                    if (sourceObject.has("size")) {
                                        size = sourceObject.getString("size");
                                    }
                                    Long widthPixels = null;
                                    if (sourceObject.has("widthPixels")) {
                                        widthPixels = sourceObject.getLong("widthPixels");
                                    }
                                    Long heightPixels = null;
                                    if (sourceObject.has("heightPixels")) {
                                        heightPixels = sourceObject.getLong("heightPixels");
                                    }

                                    AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                    source.setUrl(url);
                                    source.setDarkBackgroundUrl(darkBackgroundUrl);
                                    source.setSize(size);
                                    source.setWidthPixels(widthPixels);
                                    source.setHeightPixels(heightPixels);

                                    sources.add(source);
                                }
                            }

                            currentWeatherIcon.setContentDescription(contentDescription);
                            currentWeatherIcon.setSources(sources);
                        }
                        AlexaIfDirectiveItem.Temperature highTemperature = new AlexaIfDirectiveItem.Temperature();
                        if (payload != null && payload.has("highTemperature")) {
                            JSONObject highTemperatureObject = payload.getJSONObject("highTemperature");
                            String value = null;
                            if (highTemperatureObject.has("value")) {
                                value = highTemperatureObject.getString("value");
                            }
                            AlexaIfDirectiveItem.ImageStructure arrow = new AlexaIfDirectiveItem.ImageStructure();
                            if (highTemperatureObject.has("arrow")) {
                                JSONObject arrowObject = highTemperatureObject.getJSONObject("arrow");
                                String contentDescription = null;
                                if (arrowObject.has("contentDescription")) {
                                    contentDescription = arrowObject.getString("contentDescription");
                                }
                                List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                                if (arrowObject.has("sources")) {
                                    JSONArray sourceArray = arrowObject.getJSONArray("sources");
                                    for (int index = 0; index < sourceArray.length(); index++) {
                                        JSONObject sourceObject = sourceArray.getJSONObject(index);
                                        String url = null;
                                        if (sourceObject.has("url")) {
                                            url = sourceObject.getString("url");
                                        }
                                        String darkBackgroundUrl = null;
                                        if (sourceObject.has("darkBackgroundUrl")) {
                                            darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                        }
                                        String size = null;
                                        if (sourceObject.has("size")) {
                                            size = sourceObject.getString("size");
                                        }
                                        Long widthPixels = null;
                                        if (sourceObject.has("widthPixels")) {
                                            widthPixels = sourceObject.getLong("widthPixels");
                                        }
                                        Long heightPixels = null;
                                        if (sourceObject.has("heightPixels")) {
                                            heightPixels = sourceObject.getLong("heightPixels");
                                        }

                                        AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                        source.setUrl(url);
                                        source.setDarkBackgroundUrl(darkBackgroundUrl);
                                        source.setSize(size);
                                        source.setWidthPixels(widthPixels);
                                        source.setHeightPixels(heightPixels);

                                        sources.add(source);
                                    }
                                }
                                arrow.setContentDescription(contentDescription);
                                arrow.setSources(sources);
                            }

                            highTemperature.setValue(value);
                            highTemperature.setArrow(arrow);
                        }
                        AlexaIfDirectiveItem.Temperature lowTemperature = new AlexaIfDirectiveItem.Temperature();
                        if (payload != null && payload.has("lowTemperature")) {
                            JSONObject lowTemperatureObject = payload.getJSONObject("lowTemperature");
                            String value = null;
                            if (lowTemperatureObject.has("value")) {
                                value = lowTemperatureObject.getString("value");
                            }
                            AlexaIfDirectiveItem.ImageStructure arrow = new AlexaIfDirectiveItem.ImageStructure();
                            if (lowTemperatureObject.has("arrow")) {
                                JSONObject arrowObject = lowTemperatureObject.getJSONObject("arrow");
                                String contentDescription = null;
                                if (arrowObject.has("contentDescription")) {
                                    contentDescription = arrowObject.getString("contentDescription");
                                }
                                List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                                if (arrowObject.has("sources")) {
                                    JSONArray sourceArray = arrowObject.getJSONArray("sources");
                                    for (int index = 0; index < sourceArray.length(); index++) {
                                        JSONObject sourceObject = sourceArray.getJSONObject(index);
                                        String url = null;
                                        if (sourceObject.has("url")) {
                                            url = sourceObject.getString("url");
                                        }
                                        String darkBackgroundUrl = null;
                                        if (sourceObject.has("darkBackgroundUrl")) {
                                            darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                        }
                                        String size = null;
                                        if (sourceObject.has("size")) {
                                            size = sourceObject.getString("size");
                                        }
                                        Long widthPixels = null;
                                        if (sourceObject.has("widthPixels")) {
                                            widthPixels = sourceObject.getLong("widthPixels");
                                        }
                                        Long heightPixels = null;
                                        if (sourceObject.has("heightPixels")) {
                                            heightPixels = sourceObject.getLong("heightPixels");
                                        }

                                        AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                        source.setUrl(url);
                                        source.setDarkBackgroundUrl(darkBackgroundUrl);
                                        source.setSize(size);
                                        source.setWidthPixels(widthPixels);
                                        source.setHeightPixels(heightPixels);

                                        sources.add(source);
                                    }
                                }
                                arrow.setContentDescription(contentDescription);
                                arrow.setSources(sources);
                            }

                            lowTemperature.setValue(value);
                            lowTemperature.setArrow(arrow);
                        }
                        List<AlexaIfDirectiveItem.WeatherForecast> weatherForecast = new ArrayList<>();
                        if (payload != null && payload.has("weatherForecast")) {
                            JSONArray weatherForecastArray = payload.getJSONArray("weatherForecast");
                            for (int index = 0; index < weatherForecastArray.length(); index++) {
                                JSONObject weatherForecastObject = weatherForecastArray.getJSONObject(index);
                                AlexaIfDirectiveItem.ImageStructure image2 = new AlexaIfDirectiveItem.ImageStructure();
                                if (weatherForecastObject.has("image")) {
                                    JSONObject imageObject = weatherForecastObject.getJSONObject("image");
                                    String contentDescription = null;
                                    if (imageObject.has("contentDescription")) {
                                        contentDescription = imageObject.getString("contentDescription");
                                    }
                                    List<AlexaIfDirectiveItem.Source> sources = new ArrayList<>();
                                    if (imageObject.has("sources")) {
                                        JSONArray sourceArray = imageObject.getJSONArray("sources");
                                        for (int index2 = 0; index2 < sourceArray.length(); index2++) {
                                            JSONObject sourceObject = sourceArray.getJSONObject(index2);
                                            String url = null;
                                            if (sourceObject.has("url")) {
                                                url = sourceObject.getString("url");
                                            }
                                            String darkBackgroundUrl = null;
                                            if (sourceObject.has("darkBackgroundUrl")) {
                                                darkBackgroundUrl = sourceObject.getString("darkBackgroundUrl");
                                            }
                                            String size = null;
                                            if (sourceObject.has("size")) {
                                                size = sourceObject.getString("size");
                                            }
                                            Long widthPixels = null;
                                            if (sourceObject.has("widthPixels")) {
                                                widthPixels = sourceObject.getLong("widthPixels");
                                            }
                                            Long heightPixels = null;
                                            if (sourceObject.has("heightPixels")) {
                                                heightPixels = sourceObject.getLong("heightPixels");
                                            }
                                            AlexaIfDirectiveItem.Source source = new AlexaIfDirectiveItem.Source();
                                            source.setUrl(url);
                                            source.setDarkBackgroundUrl(darkBackgroundUrl);
                                            source.setSize(size);
                                            source.setWidthPixels(widthPixels);
                                            source.setHeightPixels(heightPixels);

                                            sources.add(source);
                                        }
                                    }

                                    image2.setContentDescription(contentDescription);
                                    image2.setSources(sources);
                                }
                                String day = null;
                                if (weatherForecastObject.has("day")) {
                                    day = weatherForecastObject.getString("day");
                                }
                                String date = null;
                                if (weatherForecastObject.has("date")) {
                                    date = weatherForecastObject.getString("date");
                                }
                                String highTemperature2 = null;
                                if (weatherForecastObject.has("highTemperature")) {
                                    highTemperature2 = weatherForecastObject.getString("highTemperature");
                                }
                                String lowTemperature2 = null;
                                if (weatherForecastObject.has("lowTemperature")) {
                                    lowTemperature2 = weatherForecastObject.getString("lowTemperature");
                                }

                                AlexaIfDirectiveItem.WeatherForecast weatherForecastItem = new AlexaIfDirectiveItem.WeatherForecast();
                                weatherForecastItem.setImage(image2);
                                weatherForecastItem.setDay(day);
                                weatherForecastItem.setDate(date);
                                weatherForecastItem.setHighTemperature(highTemperature2);
                                weatherForecastItem.setLowTemperature(lowTemperature2);

                                weatherForecast.add(weatherForecastItem);
                            }
                        }

                        switch (type){
                            case "BodyTemplate2":
                                result = new RenderTemplateItem(messageId, dialogRequestId, token, type, title, skillIcon, textField, image);
                                break;
                            case "ListTemplate1":
                                result = new RenderTemplateItem(messageId, dialogRequestId, token, type, title, skillIcon, listItems);
                                break;
                            case "WeatherTemplate":
                                result = new RenderTemplateItem(messageId, dialogRequestId, token, type, title, skillIcon, currentWeather, description, currentWeatherIcon, highTemperature, lowTemperature, weatherForecast);
                                break;
                        }
                        break;
                    }
                    case Constant.DIRECTIVE_SET_DESTINATION:
                        Double latitude = null;
                        Double longitude = null;
                        String name = null;
                        if (payload != null && payload.has("destination")) {
                            JSONObject destinationObject = payload.getJSONObject("destination");
                            if (destinationObject.has("coordinate")) {
                                JSONObject streamObject = destinationObject.getJSONObject("coordinate");
                                if (streamObject.has("latitudeInDegrees")) {
                                    latitude = streamObject.getDouble("latitudeInDegrees");
                                }
                                if (streamObject.has("longitudeInDegrees")) {
                                    longitude = streamObject.getDouble("longitudeInDegrees");
                                }
                            }
                            if (destinationObject.has("name")) {
                                name = destinationObject.getString("name");
                            }
                        }
                        result = new SetDestinationItem(messageId, latitude, longitude, name);
                        break;
                    default: {
                        break;
                    }
                }
            }
        }

        return result;
    }

}
