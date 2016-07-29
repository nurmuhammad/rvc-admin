package rvc.admin.controllers;

import org.apache.poi.ss.usermodel.DateUtil;
import rvc.$;
import rvc.admin.Database;
import rvc.admin.model.Department;
import rvc.admin.model.User;
import rvc.ann.*;
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

    @GET("user/add, user/edit/:id")
    @POST("user/add, user/edit/:id")
    @Template(viewName = "users/add.html")
    Object add() {
        Request req = Request.get();
        if (!"post".equalsIgnoreCase(req.requestMethod())) {
            try {
                Database.open();
                String id = req.params(":id");
                User user;
                if(!$.isEmpty(id)){
                    Long id2 = Long.valueOf(id);
                    user = User.findFirst("id=?", id2);
                } else {
                    user = new User();
                }
                List<Department> departments = Department.findAll().load();

                Map<String, Object> map = new HashMap<>();
                map.put("departments", departments);
                map.put("user", user);
                return map;
            } catch (Exception e) {
                return null;
            } finally {
                Database.close();
            }
        }

        try {
            Database.open();
            if("admin".equals(req.queryParams("name"))) {
                Response.get().redirect("/user");
                return null;
            }
            User user;
            try {
                String id = req.params(":id");
                Long id2 = Long.valueOf(id);
                user = User.findFirst("id=?", id2);
            } catch (NumberFormatException e) {
                user = new User();
            }
            user.email(req.queryParams("name"));
            String password = req.queryParams("password");
            if(!$.isEmpty(password)){
                user.password($.encode(password));
            }
            user.roles(req.queryParams("role"));
            user.department(req.queryParams("department"));
            user.status(!$.isEmpty(req.queryParams("status")));

            user.saveIt();
        } catch (Exception ignored) {}
        finally {
            Database.close();
        }

        Response.get().redirect("/user");
        return null;
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
        Response.get().redirect("/user");
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