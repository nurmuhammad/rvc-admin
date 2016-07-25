package rvc.admin.controllers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rvc.admin.$;
import rvc.admin.Database;
import rvc.admin.model.User;
import rvc.ann.*;
import rvc.http.Request;
import rvc.http.Response;
import rvc.http.Session;

import java.io.FileInputStream;

/**
 * @author nurmuhammad
 */

public class LoginController2 {

    public Object index() throws Exception {
        String fileName = "d:/Projects/rvcjava-admin/Dashboard Nestle 22.07.2016.xlsx";

        FileInputStream fis = new FileInputStream(fileName);

//        XLS2HTMLParser parser = new XLS2HTMLParser(new File(fileName));
//        parser.parse(new File(fileName));

        ExcelToHtml html = new ExcelToHtml(fis, "Summary");
        String s = html.getHTML();
        if(1==1) return s;

        Workbook wb = new XSSFWorkbook(fis);
        Sheet sheet = wb.getSheet("Info");
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Cell cell = null;
        try {
            CellReference cellReference = new CellReference("a5");
            Row row = sheet.getRow(cellReference.getRow());
            cell = row.getCell(cellReference.getCol());
//            cell.setCellValue("Kamoliddin Sirojiddinov");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CellReference cellReference = new CellReference("a5");
        Row row = sheet.getRow(cellReference.getRow());
        cell = row.getCell(cellReference.getCol());

        CellValue cellValue = evaluator.evaluate(cell);

        switch (cellValue.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                System.out.println(cellValue.getBooleanValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                System.out.println(cellValue.getNumberValue());
                return cellValue.getNumberValue();
            case Cell.CELL_TYPE_STRING:
                System.out.println(cellValue.getStringValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                break;
            case Cell.CELL_TYPE_ERROR:
                break;

            // CELL_TYPE_FORMULA will never happen
            case Cell.CELL_TYPE_FORMULA:
                break;
        }
        return cellValue.getStringValue();
    }


}