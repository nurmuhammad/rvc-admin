package rvc.admin.model;

import rvc.admin.$;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author nurmuhammad
 */

public abstract class SettingsModel extends aModel {
    Map<String, String> settings;

    public String settings() {
        return getString("settings");
    }

    public void settings(String settings) {
        setString("settings", settings);
    }

    public Map<String, String> map() {
        if (settings == null) {
            settings = new LinkedHashMap<>($.settings2map(settings()));
        }
        return settings;
    }

    public void map2settings(Map map) {
        settings($.map2settings(map));
    }

    public String setting(String key) {
        return map().get(key);
    }

    public void setting(String key, String value) {
        map().put(key, value);
        map2settings(map());
    }
}
