package rvc.admin.pebble;

import com.mitchellbosecke.pebble.extension.Function;
import rvc.$;

import java.util.List;
import java.util.Map;

/**
 * Created by Nurmuhammad on 26.07.2016.
 */
public class DateFunction implements Function {
    @Override
    public Object execute(Map<String, Object> args) {

        try {
            Object o = args.get("0");
            if(o instanceof Number){
                int d = ((Number) o).intValue();
                return  $.date(d);
            }
        }
        catch (Exception ignored){}
        return null;
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }
}
