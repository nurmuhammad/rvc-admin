package rvc.admin.controllers;

import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.Controller;
import rvc.ann.GET;
import rvc.ann.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class DepartmentController {

    @GET("departments")
    @Template(viewName = "department/index.html")
    Object users(){
        Database.open();
        List users = User.findAll().load();
        Map<String, Object> map = new HashMap<>();
        map.put("users", users);
        Database.close();
        return map;
    }

}