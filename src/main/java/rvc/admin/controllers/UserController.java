package rvc.admin.controllers;

import rvc.ann.Controller;
import rvc.ann.GET;
import rvc.ann.Template;

/**
 * @author nurmuhammad
 */

@Controller
public class UserController {

    @GET("users")
    @Template(viewName = "users/index.html")
    Object users(){
        return "";
    }

}