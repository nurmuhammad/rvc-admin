package rvc.admin;

import org.h2.tools.Server;
import rvc.RvcServer;
import rvc.admin.controllers.*;
import rvc.admin.init.Config;
import rvc.ann.Template;

public class Main {

    public static void main(String[] args) throws Exception {

//        Date date = DateUtil.getJavaDate(42217.0);
//        System.out.println(date);
//        System.exit(0);

        Database.backupSql();

        RvcServer rvcServer = new RvcServer()
                .addTemplate(Template.TemplateEngine.PEBBLE, Pebble.instance)
                .suffix(".html")
                .folder(Config.get("public.dir"))
                .port(Config.get("server.port", 8888));

        rvcServer.init();

        rvcServer.classes(
                LoginController.class,
                SimpleUserController.class,
                UserController.class,
                DepartmentController.class,
                AdminController.class
        );
        new Thread(() -> rvcServer.start()).start();

        // h2 db-manager server
        if (Application.debug()) {
            Server webServer = Server.createWebServer("-webAllowOthers", "-webPort", "8083").start();
        }

    }

}
