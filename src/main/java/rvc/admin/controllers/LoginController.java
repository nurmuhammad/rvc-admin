package rvc.admin.controllers;

import rvc.admin.$;
import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.*;
import rvc.http.Request;
import rvc.http.Response;
import rvc.http.Session;

/**
 * @author nurmuhammad
 */

@Controller
public class LoginController {

    @GET("/")
    void index(){
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
        return "";
    }

    @POST("login")
    public Object loginPost() {
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
                Response.get().redirect("/administer");
            }
        } else {
            Response.get().redirect("/login");
        }
        Database.close();
        return null;

    }

    @GET
    void logout(){
        Session.get().attribute("user", null);
        Response.get().redirect("/login");
    }

    @Before("administer, administer/*, user, user/*, department, department/*")
    public void before() {
        User user = Session.get().attribute("user");
        if (user == null) {
            Response.get().redirect("/login");
        }
    }

}