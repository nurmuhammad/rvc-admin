package rvc.admin.controllers;

import rvc.admin.$;
import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.*;
import rvc.http.Cookie;
import rvc.http.Request;
import rvc.http.Response;
import rvc.http.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class LoginController {

    @GET("/")
    void index() {
        User user = Session.get().attribute("user");
        if (user == null) {
            Response.get().redirect("/login");
        } else {
            Response.get().redirect("/administer");
        }
    }

    @GET
    @Template
    public Object login() {
        Map<String, Object> map = new HashMap<>();
        String username = null;
        try {
            username = Cookie.cookies().get("username");
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("username", username);
        return map;
    }

    @POST("login")
    @Template(viewName = "login.html")
    public void loginPost() {
        String email = Request.get().queryParams("email");
        String password = Request.get().queryParams("password");
        Database.open();
        User user = User.findFirst("email=?", email);

        if (user != null) {
            if ($.matches(password, user.password()) && user.status()) {
                Session.get().attribute("user", user);
                user.lastLogin($.timestamp());
                user.lastIp(Request.get().ip());
                user.saveIt();
                try {
                    Cookie.cookie("username", user.email());
                } catch (Exception e) {}

                if("admin".equals(user.roles())){
                    Response.get().redirect("/administer");
                } else {
                    Response.get().redirect("/simple");
                }
            }
        }
        if(!Response.get().isRedirected()){
            Response.get().redirect("/login");
        }
        Database.close();
    }

    @GET
    void logout() {
        Session.get().attribute("user", null);
        Response.get().redirect("/login");
    }

    @Before("administer, administer/*, user, user/*, department, department/*, simple, simple/*")
    public void before() {
        User user = Session.get().attribute("user");
        if (user == null) {
            Response.get().redirect("/login");
        }
    }

}