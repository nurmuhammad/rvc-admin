package rvc.admin.controllers;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;

/**
 * Created by Nurmuhammad on 25.07.2016.
 */
public class XLS2HTMLParser {

    private static final String NEW_LINE = "\n";
    private static final String HTML_FILE_EXTENSION = ".html";
    private static final String TEMP_FILE_EXTENSION = ".tmp";
    private static final String HTML_SNNIPET_1 = "<!DOCTYPE html><html><head><title>";
    private static final String HTML_SNNIPET_2 = "</title></head><body><table>";
    private static final String HTML_SNNIPET_3 = "</table></body></html>";
    private static final String HTML_TR_S = "<tr>";
    private static final String HTML_TR_E = "</tr>";
    private static final String HTML_TD_S = "<td>";
    private static final String HTML_TD_E = "</td>";

    public static final String[] FILE_TYPES = new String[] { "xls", "xlsx" };


    private File file;

    XLS2HTMLParser(File file) {
        this.file = file;
    }

    void parse(File file) throws IOException {
        BufferedWriter writer;
        Workbook workbook;
        String fileName = file.getName();
        String folderName = file.getParent();
        if (fileName.toLowerCase().endsWith(FILE_TYPES[0])) {
            workbook = new HSSFWorkbook(new FileInputStream(file));
        } else {
            workbook = new XSSFWorkbook(new FileInputStream(file));
        }

        File tempFile = File.createTempFile(fileName + '-', HTML_FILE_EXTENSION
                + TEMP_FILE_EXTENSION, new File(folderName));
        writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(HTML_SNNIPET_1);
        writer.write(fileName);
        writer.write(HTML_SNNIPET_2);
        Sheet sheet = workbook.getSheet("Summary");
        Iterator<Row> rows = sheet.rowIterator();
        Iterator<Cell> cells = null;
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        while (rows.hasNext()) {
            Row row = rows.next();
            cells = row.cellIterator();
            writer.write(NEW_LINE);
            writer.write(HTML_TR_S);
            while (cells.hasNext()) {
                String value = "";
                try {
                    Cell cell = cells.next();
                    CellValue cellValue = evaluator.evaluate(cell);
                    value = cellValue.formatAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                writer.write(HTML_TD_S);
                writer.write(value);
                writer.write(HTML_TD_E);
            }
            writer.write(HTML_TR_E);
        }
        writer.write(NEW_LINE);
        writer.write(HTML_SNNIPET_3);
        writer.close();


        File newFile = new File(folderName + '\\' + fileName + '-'
                + System.currentTimeMillis() + HTML_FILE_EXTENSION);
        tempFile.renameTo(newFile);
    }
}