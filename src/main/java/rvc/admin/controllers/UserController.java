package rvc.admin.controllers;

import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.Controller;
import rvc.ann.GET;
import rvc.ann.Template;
import rvc.http.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class UserController {

    @GET("user")
    @Template(viewName = "users/index.html")
    Object index(){
        Database.open();
        List users = User.findAll().orderBy("id").load();
        Map<String, Object> map = new HashMap<>();
        map.put("users", users);
        Database.close();
        return map;
    }

    @GET("user/data/:id")
    @Template(viewName = "admin/data.html")
    Object data(){
        Database.open();
        String result = null;
        try {
            String id = Request.get().params(":id");
            Long intId = Long.valueOf(id);
            User user = User.findById(intId);
            result = user.data();
        } catch (Exception ignored) {}
        Database.close();

        Map map = new HashMap();
        map.put("content", result);
        return map;
    }

}