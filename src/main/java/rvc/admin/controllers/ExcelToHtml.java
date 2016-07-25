package rvc.admin.controllers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 *    http://allstarnix.blogspot.com/2013/05/use-java-to-convert-excel-to-html.html
 */

public class ExcelToHtml {

    final private StringBuilder out = new StringBuilder(65536);
    final private XSSFWorkbook book;
    final private FormulaEvaluator evaluator;
    private short colIndex;
    private int rowIndex, mergeStart, mergeEnd;

    /**
     * Generates HTML from the InputStream of an Excel file. Generates sheet
     * name in HTML h1 element.
     *
     * @param in InputStream of the Excel file.
     * @throws IOException When POI cannot read from the input stream.
     */
    public ExcelToHtml(final InputStream in, String sheetName) throws IOException {
        if (in == null) {
            book = null;
            evaluator = null;
            return;
        }
        book = new XSSFWorkbook(in);
        evaluator = book.getCreationHelper().createFormulaEvaluator();

        table(book.getSheet(sheetName));

    }

    /**
     * (Each Excel sheet produces an HTML table) Generates an HTML table with no
     * cell, border spacing or padding.
     *
     * @param sheet The Excel sheet.
     */
    private void table(final Sheet sheet) {
        if (sheet == null) {
            return;
        }

        out.append("<table border=1>\n");
        for (rowIndex = 0; rowIndex < 150; ++rowIndex) {
            tr(sheet.getRow(rowIndex));
        }
        out.append("</table>\n");
    }

    /**
     * (Each Excel sheet row becomes an HTML table row) Generates an HTML table
     * row which has the same height as the Excel row.
     *
     * @param row The Excel row.
     */
    private void tr(final Row row) {
        if (row == null) {
            return;
        }
        out.append("<tr ");
        // Find merged cells in current row.
        for (int i = 0; i < row.getSheet().getNumMergedRegions(); ++i) {
            final CellRangeAddress merge = row.getSheet().getMergedRegion(i);
            if (rowIndex >= merge.getFirstRow()
                    && rowIndex <= merge.getLastRow()) {
                mergeStart = merge.getFirstColumn();
                mergeEnd = merge.getLastColumn();
                break;
            }
        }
        out.append("style='");
        if (row.getHeight() != -1) {
            out.append("height: ")
                    .append(Math.round(row.getHeight() / 20.0 * 1.33333))
                    .append("px; ");
        }
        out.append("'>\n");
        for (colIndex = 0; colIndex < row.getLastCellNum(); ++colIndex) {
            td(row.getCell(colIndex));
        }
        out.append("</tr>\n");
    }

    /**
     * (Each Excel sheet cell becomes an HTML table cell) Generates an HTML
     * table cell which has the same font styles, alignments, colours and
     * borders as the Excel cell.
     *
     * @param cell The Excel cell.
     */
    private void td(final Cell cell) {
        int colspan = 1;
        if (colIndex == mergeStart) {
            // First cell in the merging region - set colspan.
            colspan = mergeEnd - mergeStart + 1;
        } else if (colIndex == mergeEnd) {
            // Last cell in the merging region - no more skipped cells.
            mergeStart = -1;
            mergeEnd = -1;
            return;
        } else if (mergeStart != -1 && mergeEnd != -1 && colIndex > mergeStart
                && colIndex < mergeEnd) {
            // Within the merging region - skip the cell.
            return;
        }
        out.append("<td ");
        if (colspan > 1) {
            out.append("colspan='").append(colspan).append("' ");
        }
        if (cell == null) {
            out.append("/>\n");
            return;
        }
        out.append("style='");
        final CellStyle style = cell.getCellStyle();
        // Text alignment
        switch (style.getAlignment()) {
            case CellStyle.ALIGN_LEFT:
                out.append("text-align: left; ");
                break;
            case CellStyle.ALIGN_RIGHT:
                out.append("text-align: right; ");
                break;
            case CellStyle.ALIGN_CENTER:
                out.append("text-align: center; ");
                break;
            default:
                break;
        }

        out.append("'>");
        String val = "";
        try {
            CellValue cellValue = evaluator.evaluate(cell);
            val = cellValue.formatAsString();
        } catch (final Exception e) {
            val = e.getMessage();
        }
        if ("null".equals(val)) {
            val = "";
        }

        out.append(val);
        out.append("</td>\n");
    }

    public String getHTML() {
        return out.toString();
    }

}
