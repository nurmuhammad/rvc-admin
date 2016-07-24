package rvc.admin;

import org.h2.tools.Server;
import rvc.RvcServer;
import rvc.admin.controllers.AdminController;
import rvc.admin.controllers.LoginController;
import rvc.admin.init.Config;
import rvc.ann.Template;

public class Main {

    public static void main(String[] args) throws Exception {

        Database.backupSql();

        RvcServer rvcServer = new RvcServer()
                .addTemplate(Template.TemplateEngine.PEBBLE, Pebble.instance)
                .suffix(".html")
                .port(Config.get("server.port", 8888));

        rvcServer.init();

        rvcServer.classes(
                LoginController.class,
                AdminController.class
        );

        new Thread(() -> rvcServer.start()).start();

        // h2 db-manager server
        if (Application.debug()) {
            Server webServer = Server.createWebServer("-webAllowOthers", "-webPort", "8083").start();
        }

    }

}
