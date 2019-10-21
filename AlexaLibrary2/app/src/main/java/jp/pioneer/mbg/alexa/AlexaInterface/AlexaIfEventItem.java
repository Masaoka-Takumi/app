package jp.pioneer.mbg.alexa.AlexaInterface;

import android.util.Log;

import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class AlexaIfEventItem extends AlexaIfItem {
    public AlexaIfEventItem(String namespace) {
        super(namespace);
        setHeader();
        setPayLoad();
    }

    /** 各Interfaceのヘッダーアイテムを管理するHashMap. */
    protected Header header;
    /** 各Interfaceのペイロードアイテムを管理するHashMap. */
    protected PayLoad payLoad;

    public void setContext() {}
    protected void setHeader() {}
    protected void setPayLoad() {}

    public static class Header {

        private String namespace;
        private String name;
        private String messageId;
        private String dialogRequestId;

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

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (namespace != null) {
                object.put("namespace", namespace);
            }
            if (name != null) {
                object.put("name", name);
            }
            if (messageId != null) {
                object.put("messageId", messageId);
            }
            if (dialogRequestId != null) {
                object.put("dialogRequestId", dialogRequestId);
            }
            return object;
        }
    }

    public static class PayLoad {
        private String token;
        private String profile;
        private String format;
        private Boolean muted;
        private Long volume;
        private Long offsetInMilliseconds;
        private Long inactiveTimeInSeconds;
        private String unparsedDirective;
        private Error error;
        private List<Setting> settings;
        private CurrentPlaybackState currentPlaybackState;
        private MetaData metadata;
        private String firmwareVersion;

        // 2018.03.30 API仕様変更対応
        private Initiator initiator = null;

        public void setToken(String token) {
            this.token = token;
        }
        public String getToken() {
            return token;
        }
        public void setProfile(String profile) {
            this.profile = profile;
        }
        public String getProfile() {
            return profile;
        }
        public void setFormat(String format) {
            this.format = format;
        }
        public String getFormat() {
            return format;
        }
        public void setMuted(Boolean muted) {
            this.muted = muted;
        }
        public Boolean getMuted() {
            return muted;
        }
        public void setVolume(Long volume) {
            this.volume = volume;
        }
        public Long getVolume() {
            return volume;
        }
        public void setOffsetInMilliseconds(Long offsetInMilliseconds) {
            this.offsetInMilliseconds = offsetInMilliseconds;
        }
        public Long getOffsetInMilliseconds() {
            return offsetInMilliseconds;
        }
        public void setInactiveTimeInSeconds(Long inactiveTimeInSeconds) {
            this.inactiveTimeInSeconds = inactiveTimeInSeconds;
        }
        public void setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }
        public Long getInactiveTimeInSeconds() {
            return inactiveTimeInSeconds;
        }
        public void setUnparsedDirective(String unparsedDirective) {
            this.unparsedDirective = unparsedDirective;
        }
        public String getUnparsedDirective() {
            return unparsedDirective;
        }
        public void setError(Error error) {
            this.error = error;
        }
        public Error getError() {
            return error;
        }
        public void setSetting(List<Setting> settings) {
            this.settings = settings;
        }
        public List<Setting> getSetting() {
            return settings;
        }

        public void setCurrentPlaybackState(CurrentPlaybackState currentPlaybackState) {
            this.currentPlaybackState = currentPlaybackState;
        }

        public CurrentPlaybackState getCurrentPlaybackState() {
            return currentPlaybackState;
        }

        public void setMetadata(MetaData metadata) {
            this.metadata = metadata;
        }

        public MetaData getMetadata() {
            return this.metadata;
        }
        public void setInitiator(Initiator initiator) {
            this.initiator = initiator;
        }
        public Initiator getInitiator() {
            return this.initiator;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (token != null) {
                object.put("token", token);
            }
            if (profile != null) {
                object.put("profile", profile);
            }
            if (format != null) {
                object.put("format", format);
            }
            if (muted != null) {
                object.put("muted", muted);
            }
            if (volume != null) {
                object.put("volume", volume);
            }
            if (offsetInMilliseconds != null) {
                object.put("offsetInMilliseconds", offsetInMilliseconds);
            }
            if (inactiveTimeInSeconds != null) {
                object.put("inactiveTimeInSeconds", inactiveTimeInSeconds);
            }
            if (firmwareVersion != null) {
                object.put("firmwareVersion", firmwareVersion);
            }
            if (error != null) {
                object.put("error", error.toJsonObject());
            }
            if (settings != null) {
                JSONArray settingsArray = new JSONArray();
                for (Setting setting : settings) {
                    settingsArray.put(setting.toJsonObject());
                }
                object.put("settings", settingsArray);
            }
            if (currentPlaybackState != null) {
                object.put("currentPlaybackState", currentPlaybackState.toJsonObject());
            }
            if (metadata != null) {
                object.put("metadata", metadata.toJsonObject());
            }
            if (this.initiator != null) {
                object.put("initiator", initiator.toJsonObject());
            }
            return object;
        }
    }

    public static class Error {
        public Error(String type, String message) {
            this.type = type;
            this.message = message;
        }
        private String type;
        private String message;

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (type != null) {
                object.put("type", type);
            }
            if (message != null) {
                object.put("message", message);
            }
            return object;
        }
    }

    public static class Setting {
        public Setting(String key, String value) {
            this.key = key;
            this.value = value;
        }
        private String key;
        private String value;

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (key != null) {
                object.put("key", key);
            }
            if (value != null) {
                object.put("value", value);
            }
            return object;
        }
    }

    public static class CurrentPlaybackState {
        public CurrentPlaybackState(String token, Long offsetInMilliseconds, String playerActivity) {
            this.token = token;
            this.offsetInMilliseconds = offsetInMilliseconds;
            this.playerActivity = playerActivity;
        }
        private String token;
        private Long offsetInMilliseconds;
        private String playerActivity;

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (token != null) {
                object.put("token", token);
            }
            if (offsetInMilliseconds != null) {
                object.put("offsetInMilliseconds", offsetInMilliseconds);
            }
            if (playerActivity != null) {
                object.put("playerActivity", playerActivity);
            }
            return object;
        }
    }

    public static class MetaData {
        public MetaData() {
        }
        private HashMap<String, String> data = new HashMap<>();

        public void addData(String key, String value) {
            data.put(key, value);
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();
            if (data != null) {
                String[] keyArray = data.keySet().toArray(new String[data.size()]);

                for (String key : keyArray) {
                    object.put(key, data.get(key));
                }
            }
            return object;
        }
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();

        setHeader();
        setPayLoad();

        object.put("header", header.toJsonObject());
        object.put("payload", payLoad.toJsonObject());
        return object;
    }

    // TODO JSON生成確認用(後で消す)↓
//    public JSONObject toTestJsonObject() throws JSONException {
//        payLoad.setToken("token");
//        payLoad.setProfile("Profile");
//        payLoad.setFormat("Format");
//        payLoad.setMuted(false);
//        payLoad.setVolume(100L);
//        payLoad.setOffsetInMilliseconds(Long.MAX_VALUE);
//        payLoad.setUnparsedDirective("directive");
//        payLoad.setError(new Error("type", "message"));
//        List<Setting> Settings = new ArrayList<>();
//        Settings.add(new Setting("a","b"));
//        Settings.add(new Setting("c","d"));
//        payLoad.setSetting(Settings);
//        payLoad.setCurrentPlaybackState(new CurrentPlaybackState("token", Long.MAX_VALUE, "activity"));
//        MetaData metaData = new MetaData();
//        metaData.addData("a", "b");
//        payLoad.setMetadata(metaData);
//
//        JSONObject object = new JSONObject();
//        object.put("header", header.toJsonObject());
//        object.put("payload", payLoad.toJsonObject());
//        return object;
//    }
    // TODO JSON生成確認用(後で消す)↑
}