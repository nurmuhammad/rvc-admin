package rvc.admin.controllers;

import org.apache.poi.ss.usermodel.DateUtil;
import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.Controller;
import rvc.ann.GET;
import rvc.ann.Template;
import rvc.http.Request;
import rvc.http.Response;
import rvc.http.Session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class SimpleUserController {

    @GET("simple")
    @Template(viewName = "admin/data.html")
    Object data() {
        User user = Session.get().attribute("user");
        if(user==null) {
            Response.get().redirect("/login");
            return null;
        }

        if("admin".equals(user.roles())){
            Response.get().redirect("/user");
            return null;
        }

        Database.open();
        String result = null;
        String start = null;
        try {
            user = User.findById(user.id());
            start = user.setting("Дата начало работы");
            result = user.data();
        } catch (Exception ignored) {
        }
        Database.close();

        Map map = new HashMap();
        map.put("content", result);

        try {
            Date date = DateUtil.getJavaDate(Double.valueOf(start));
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            start = dateFormat.format(date);
            map.put("start", start);
        } catch (Exception ignored) {}
        map.put("simple", true);
        return map;
    }

}