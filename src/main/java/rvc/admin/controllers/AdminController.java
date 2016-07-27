package rvc.admin.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rvc.$;
import rvc.admin.Database;
import rvc.admin.converter.ExcelToHtmlConverter;
import rvc.admin.model.Department;
import rvc.admin.model.User;
import rvc.ann.Controller;
import rvc.ann.GET;
import rvc.ann.POST;
import rvc.ann.Template;
import rvc.http.Request;
import rvc.http.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nurmuhammad
 */

@Controller
public class AdminController {

    @GET
    @Template(viewName = "admin/index.html")
    Object administer() {
        Database.open();

        Map map = new HashMap();
        List<Department> departments = Department.findAll().load();
        map.put("departments", departments);
        Database.close();

        return map;
    }

    @POST("administer/upload")
    Object upload() throws Exception {
        Request req = Request.get();
        if (req.raw().getAttribute("org.eclipse.jetty.multipartConfig") == null) {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
        }

        String id = req.queryParams("department");
        Long intID = Long.valueOf(id);
        Database.open();
        Department department = Department.findFirst("id=?", intID);

        Part file = req.raw().getPart("file");
        String filename = file.getSubmittedFileName();
        InputStream inputStream = file.getInputStream();

        User.delete("department=? and email!=?", department.name(), "admin");

        fill(inputStream, department);

        Database.close();
        Response.get().redirect("/user");
        return "Success & Done! <br/>" + filename + "<br/>" + department.name();
    }

    void fill(InputStream inputStream, Department department) throws Exception {
        Workbook wb = new XSSFWorkbook(inputStream);

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheet("Info");

        //Userlarni info sheetdan olib bazaga saqlash {1};
        List<User> users = new ArrayList<>();
        try {
            Cell cell;
            CellReference cellReference;
            Row row;
            int i = 2;
            while (true) {
                cellReference = new CellReference("a" + i);
                row = sheet.getRow(cellReference.getRow());
                cell = row.getCell(cellReference.getCol());
                String name = cell.getStringCellValue();
                if (name != null && name.trim().toLowerCase().contains("guest")) {
                    break;
                } else if ($.isEmpty(name) || i > 500) {
                    break;
                }
                cellReference = new CellReference("j" + i);
                row = sheet.getRow(cellReference.getRow());
                cell = row.getCell(cellReference.getCol());
                String password = cell.getStringCellValue();
                User user = new User();
                user.email(name);
                user.password($.encode(password));
                user.roles("sales_rep");
                user.created($.timestamp());
                user.changed($.timestamp());
                user.status(true);
                user.department(department.name());
                user.saveIt();
                users.add(user);
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //  {1} tugadi

        // Tegishli userni Summary sheetdan kerakli datalarini olib bazaga update qilish {2}
        sheet = wb.getSheet("Summary");

        CellReference cellReference;
        Row row;
        Cell cell;
        CellValue cellValue;

        for (User user : users) {

            cellReference = new CellReference("d1");
            row = sheet.getRow(cellReference.getRow());
            cell = row.getCell(cellReference.getCol());
            cell.setCellValue(user.email());
            try {
                int i = 1;
                while (true) {
                    cellReference = new CellReference("b" + i);
                    row = sheet.getRow(cellReference.getRow());
                    cell = row.getCell(cellReference.getCol());
                    cellValue = evaluator.evaluate(cell);
                    String key = cellValue(cellValue);

                    cellReference = new CellReference("d" + i);
                    row = sheet.getRow(cellReference.getRow());
                    cell = row.getCell(cellReference.getCol());
                    cellValue = evaluator.evaluate(cell);
                    String value = cellValue(cellValue);
                    user.setting(key, value);

                    i++;
                    if (i > 10) break;
                }
            } catch (Exception ignored) {
            }


            ExcelToHtmlConverter converter = ExcelToHtmlConverter.process0(wb);
            Element stylesheetElement = converter.getHtmlDocumentFacade().getStylesheetElement();
            NodeList nodeList = converter.getHtmlDocumentFacade().getBody().getElementsByTagName("table");
            Node node = nodeList.item(0);
            System.out.println(node.getTextContent());
            System.out.println(stylesheetElement.getTextContent());

            DOMSource domSource = new DOMSource(node);
            StringWriter writer = new StringWriter();

            StreamResult streamResult = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "no");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");

            streamResult.getWriter().append("<style>");
            streamResult.getWriter().append(stylesheetElement.getTextContent());
            streamResult.getWriter().append("</style>");
            serializer.transform(domSource, streamResult);

            user.data(((StringWriter)streamResult.getWriter()).getBuffer().toString());

            user.saveIt();

        }
        //          {2} tugadi

    }

    String cellValue(CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(cellValue.getNumberValue());
            case Cell.CELL_TYPE_STRING:
                return cellValue.getStringValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cellValue.getBooleanValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_ERROR:
                return ErrorEval.getText(cellValue.getErrorValue());
        }
        return "<error unexpected cell type " + cellValue.getCellType() + ">";
    }
}
