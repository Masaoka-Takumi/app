package jp.pioneer.mbg.alexa.AlexaInterface.common;

import org.json.JSONException;
import org.json.JSONObject;

public class Initiator {

    public String type = null;
    public Initiator.Payload payload = null;

    public Initiator(String type, Initiator.Payload payload) {
        this.type = type;
        this.payload = payload;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();

        if (this.type != null) {
            object.put("type", this.type);
        }
        if (this.payload != null) {
            object.put("payload", this.payload.toJsonObject());
        }

        return object;
    }

    public static class Payload {
        public Initiator.Payload.WakeWordIndices wakeWordIndices = null;
        public String token = null;

        public Payload(Initiator.Payload.WakeWordIndices wakeWordIndices, String token) {
            this.wakeWordIndices = wakeWordIndices;
            this.token = token;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject object = new JSONObject();

            if (this.wakeWordIndices != null) {
                object.put("wakeWordIndices", wakeWordIndices.toJsonObject());
            }
            if (this.token != null) {
                object.put("token", this.token);
            }

            return object;
        }

        public static class WakeWordIndices {
            public long startIndexInSamples = 0L;
            public long endIndexInSamples = 0L;

            public WakeWordIndices(long startIndexInSamples, long endIndexInSamples) {
                this.startIndexInSamples = startIndexInSamples;
                this.endIndexInSamples = endIndexInSamples;
            }

            public JSONObject toJsonObject() throws JSONException {
                JSONObject object = new JSONObject();

                object.put("startIndexInSamples", this.startIndexInSamples);
                object.put("endIndexInSamples", this.endIndexInSamples);

                return object;
            }

        }
    }

}
