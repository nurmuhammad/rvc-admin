package rvc.admin.controllers;

import rvc.admin.Database;
import rvc.admin.model.Department;
import rvc.ann.*;
import rvc.http.Request;
import rvc.http.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class DepartmentController {

    @GET("department")
    @Template(viewName = "department/index.html")
    Object index() {
        Database.open();
        List departments = Department.findAll().load();
        Map<String, Object> map = new HashMap<>();
        map.put("departments", departments);
        Database.close();
        return map;
    }

    @GET("department/add, department/edit/:id")
    @POST("department/add, department/edit/:id")
    @Template(viewName = "department/add.html")
    Object add() {
        if (!"post".equalsIgnoreCase(Request.get().requestMethod())) {
            try {
                String id = Request.get().params(":id");
                Long id2 = Long.valueOf(id);
                Database.open();
                Department department = Department.findFirst("id=?", id2);
                Database.close();
                Map<String, Object> map = new HashMap<>();
                map.put("department", department);
                return map;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        Database.open();
        String name = Request.get().queryParams("name");
        Department department;
        try {
            String id = Request.get().params(":id");
            Long id2 = Long.valueOf(id);
            department = Department.findFirst("id=?", id2);
        } catch (NumberFormatException e) {
            department = new Department();
        }
        department.name(name);

        department.saveIt();
        Database.close();

        Response.get().redirect("/department");
        return null;
    }

    @GET("department/delete/:id")
    void detele() {
        try {
            String id = Request.get().params(":id");
            Long id2 = Long.valueOf(id);
            Database.open();
            Department department = Department.findFirst("id=?", id2);
            department.delete();
            Database.close();
        } catch (NumberFormatException e) {
        }
        Response.get().redirect("/department");
    }


}