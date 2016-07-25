package rvc.admin.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nurmuhammad on 26.07.2016.
 */
public class RvcExtention extends AbstractExtension {
    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> map = new HashMap<>();
        map.put("d", new DateFunction());
        return map;
    }
}
