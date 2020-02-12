package com.onesignal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class OSInAppMessageAction {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String URL = "url";
    private static final String URL_TARGET = "url_target";
    private static final String CLOSE = "close";
    private static final String CLICK_NAME = "click_name";
    private static final String CLICK_URL = "click_url";
    private static final String FIRST_CLICK = "first_click";
    private static final String CLOSES_MESSAGE = "closes_message";
    private static final String OUTCOME = "outcome";

    /** UUID assigned by OneSignal for internal use.
     * Package-private to track which element was tapped to report to the OneSignal dashboard. */
    @NonNull
    String clickId;

    /** An optional click name entered defined by the app developer when creating the IAM */
    @Nullable
    public String clickName;

    /** Determines where the URL is opened, ie. Default browser. */
    @Nullable
    public OSInAppMessageActionUrlType urlTarget;

    /** An optional URL that opens when the action takes place */
    @Nullable
    public String clickUrl;

    /** Outcome for action */
    public OSInAppMessageOutcome outcome;

    /** Determines if this was the first action taken on the in app message */
    public boolean firstClick;

    /** Determines if tapping on the element should close the In-App Message. */
    public boolean closesMessage;

    OSInAppMessageAction(@NonNull JSONObject json) throws JSONException {
        clickId = json.optString(ID, null);
        clickName = json.optString(NAME, null);
        clickUrl = json.optString(URL, null);
        urlTarget = OSInAppMessageActionUrlType.fromString(json.optString(URL_TARGET, null));
        if (urlTarget == null)
            urlTarget = OSInAppMessageActionUrlType.IN_APP_WEBVIEW;

        closesMessage = json.optBoolean(CLOSE, true);

        if (json.has(OUTCOME)) {
            outcome = new OSInAppMessageOutcome(json.getJSONObject(OUTCOME));
        }
    }

    public JSONObject toJSONObject() {
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put(CLICK_NAME, clickName);
            mainObj.put(CLICK_URL, clickUrl);
            mainObj.put(FIRST_CLICK, firstClick);
            mainObj.put(CLOSES_MESSAGE, closesMessage);

            if (outcome != null) {
                mainObj.put(OUTCOME, outcome.toJSONObject());
            }
            // Omitted for now until necessary
//            if (urlTarget != null)
//                mainObj.put("url_target", urlTarget.toJSONObject());

        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return mainObj;
    }

    /**
     * An enumeration of the possible places action URL's can be loaded,
     * such as an in-app webview
     */
    public enum OSInAppMessageActionUrlType {
        // Opens in an in-app webview
        IN_APP_WEBVIEW("webview"),

        // Moves app to background and opens URL in browser
        BROWSER("browser"),

        // Loads the URL on the in-app message webview itself
        REPLACE_CONTENT("replacement");

        private String text;

        OSInAppMessageActionUrlType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }

        public static OSInAppMessageActionUrlType fromString(String text) {
            for (OSInAppMessageActionUrlType type : OSInAppMessageActionUrlType.values()) {
                if (type.text.equalsIgnoreCase(text))
                    return type;
            }

            return null;
        }

        public JSONObject toJSONObject() {
            JSONObject mainObj = new JSONObject();
            try {
                mainObj.put("url_type", text);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mainObj;
        }
    }
}