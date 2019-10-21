package jp.pioneer.mbg.alexa.manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

/**
 * Created by esft-hatori on 2018/05/11.
 */

class CapabiritiesItem {
    public String type;
    public String AlexaInterface;
    public String version;

    public CapabiritiesItem(String key, String value) {
        this.type = "AlexaInterface";
        this.AlexaInterface = key;
        this.version = value;
    }

    public static JSONArray capabilitiesApiItem() {
        //capabilitiesのHashMap
        LinkedHashMap<String, String> capabiritiesMap = new LinkedHashMap<>();

        //TODO: Interface Versionが変更されたらここで追加/変更しよう!
        //capabiritiesMap.put("Alerts", "1.1");
        //capabiritiesMap.put("AudioActivityTracker", "1.0");
        capabiritiesMap.put("AudioPlayer", "1.0");
        //capabiritiesMap.put("Bluetooth", "1.0");
        capabiritiesMap.put("Notifications", "1.0");
        capabiritiesMap.put("PlaybackController", "1.0");
        capabiritiesMap.put("Settings", "1.0");
        //capabiritiesMap.put("Speaker", "1.0");
        capabiritiesMap.put("SpeechRecognizer", "2.0");
        capabiritiesMap.put("SpeechSynthesizer", "1.0");
        capabiritiesMap.put("System", "1.0");
        capabiritiesMap.put("TemplateRuntime", "1.0");
        //capabiritiesMap.put("VisualActivityTracker", "1.0");
        capabiritiesMap.put("Navigation", "1.2");
        //先にCapabilitiesItemのJSONを作成
        JSONArray capabilitiesArray = new JSONArray();
        for (LinkedHashMap.Entry<String, String> item : capabiritiesMap.entrySet()) {
            JSONObject capa = new JSONObject();
            CapabiritiesItem capabiritiesItem = new CapabiritiesItem(item.getKey(), item.getValue());
            try {
                capa.put("type", capabiritiesItem.type);
                capa.put("interface", capabiritiesItem.AlexaInterface);
                capa.put("version", capabiritiesItem.version);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            capabilitiesArray.put(capa);
        }
        return capabilitiesArray;
    }
}
