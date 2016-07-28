package rvc.admin.controllers;

import org.apache.poi.ss.usermodel.DateUtil;
import rvc.admin.Database;
import rvc.admin.model.Department;
import rvc.admin.model.User;
import rvc.ann.Before;
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
public class UserController {

    @Before("user, user/*")
    void before(){
        User user = Session.get().attribute("user");
        if (user == null) {
            Response.get().redirect("/login");
            return;
        }
        if(!"admin".equals(user.roles())){
            Response.get().redirect("/login");
        }
    }

    @GET("user")
    @Template(viewName = "users/index.html")
    Object index() {
        Database.open();
        List users = User.findAll().orderBy("id").load();
        Map<String, Object> map = new HashMap<>();
        map.put("users", users);
        Database.close();
        return map;
    }

    @GET("user/delete/:id")
    void detele() {
        try {
            String id = Request.get().params(":id");
            Long id2 = Long.valueOf(id);
            Database.open();
            User user = User.findFirst("id=?", id2);
            if("admin".equals(user.email())){
                // you can't delete admin user
            } else {
                user.delete();
            }
            Database.close();
        } catch (Exception e) {
        }
        Response.get().redirect("/department");
    }

    @GET("user/data/:id")
    @Template(viewName = "admin/data.html")
    Object data() {
        Database.open();
        String result = null;
        String start = null;
        try {
            String id = Request.get().params(":id");
            Long intId = Long.valueOf(id);
            User user = User.findById(intId);
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
        map.put("simple", false);
        return map;
    }

}