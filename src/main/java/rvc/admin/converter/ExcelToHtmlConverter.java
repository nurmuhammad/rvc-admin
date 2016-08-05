/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package rvc.admin.converter;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts xls files (97-2007) to HTML file.
 *
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Beta
public class ExcelToHtmlConverter extends AbstractExcelConverter {

    private static final POILogger logger = POILogFactory
            .getLogger(ExcelToHtmlConverter.class);

    /**
     * Java main() interface to interact with {@link ExcelToHtmlConverter}
     * <p>
     * <p>
     * Usage: ExcelToHtmlConverter infile outfile
     * </p>
     * Where infile is an input .xls file ( Word 97-2007) which will be rendered
     * as HTML into outfile
     *
     * @throws TransformerException
     * @throws Exception
     */
    public static void main(String[] args)
            throws IOException, ParserConfigurationException, TransformerException {
        if (args.length < 2) {
            System.err
                    .println("Usage: ExcelToHtmlConverter <inputFile.xls> <saveTo.html>");
            return;
        }

        System.out.println("Converting " + args[0]);
        System.out.println("Saving output to " + args[1]);

        Document doc = ExcelToHtmlConverter.process(new File(args[0]));

        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File(args[1]));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        // TODO set encoding from a command argument
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "no");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);
    }

    /**
     * Converts Excel file (97-2007) into HTML file.
     *
     * @param xlsFile file to process
     * @return DOM representation of result HTML
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document process(File xlsFile) throws IOException, ParserConfigurationException {
        final Workbook workbook = ExcelToHtmlUtils.loadXls(xlsFile);
        return process(workbook);
    }

    public static Document process(Workbook workbook) throws IOException, ParserConfigurationException {

        ExcelToHtmlConverter excelToHtmlConverter = process0(workbook);
        Document doc = excelToHtmlConverter.getDocument();
//        workbook.close();
        return doc;
    }

    public static ExcelToHtmlConverter process0(Workbook workbook) throws IOException, ParserConfigurationException {

        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(
                XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument());
        excelToHtmlConverter.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        excelToHtmlConverter.processWorkbook(workbook, "Summary");
        return excelToHtmlConverter;
    }

    private String cssClassContainerCell = null;

    private String cssClassContainerDiv = null;

    private String cssClassPrefixCell = "c";

    private String cssClassPrefixDiv = "d";

    private String cssClassPrefixRow = "r";

    private String cssClassPrefixTable = "t";

    private Map<Short, String> excelStyleToClass = new LinkedHashMap<Short, String>();

    private final HtmlDocumentFacade htmlDocumentFacade;

    FormulaEvaluator evaluator;

    private boolean useDivsToSpan = false;

    public ExcelToHtmlConverter(Document doc) {
        htmlDocumentFacade = new HtmlDocumentFacade(doc);
    }

    public ExcelToHtmlConverter(HtmlDocumentFacade htmlDocumentFacade) {
        this.htmlDocumentFacade = htmlDocumentFacade;
    }

    protected String buildStyle(Workbook workbook, CellStyle cellStyle) {
        StringBuilder style = new StringBuilder();

        style.append("white-space:pre-wrap;");
        ExcelToHtmlUtils.appendAlign(style, cellStyle.getAlignment());

        switch (cellStyle.getFillPattern()) {
            // no fill
            case 0:
                break;
            case 1:
                final Color foregroundColor = cellStyle.getFillForegroundColorColor();
                if (foregroundColor == null) break;
                String fgCol = ExcelToHtmlUtils.getColor(foregroundColor);
                style.append("background-color:" + fgCol + ";");
                break;
            default:
                final Color backgroundColor = cellStyle.getFillBackgroundColorColor();
                if (backgroundColor == null) break;
                String bgCol = ExcelToHtmlUtils.getColor(backgroundColor);
                style.append("background-color:" + bgCol + ";");
                break;
        }

        /*buildStyle_border(workbook, style, "top", cellStyle.getBorderTop(),
                cellStyle.getTopBorderColor());
        buildStyle_border(workbook, style, "right",
                cellStyle.getBorderRight(), cellStyle.getRightBorderColor());
        buildStyle_border(workbook, style, "bottom",
                cellStyle.getBorderBottom(), cellStyle.getBottomBorderColor());
        buildStyle_border(workbook, style, "left", cellStyle.getBorderLeft(),
                cellStyle.getLeftBorderColor());*/

        Font font = workbook.getFontAt(cellStyle.getFontIndex());
        buildStyle_font(workbook, style, font);

        return style.toString();
    }

    public HtmlDocumentFacade getHtmlDocumentFacade() {
        return htmlDocumentFacade;
    }

    private void buildStyle_border(Workbook workbook, StringBuilder style,
                                   String type, short xlsBorder, short borderColor) {
        if (xlsBorder == CellStyle.BORDER_NONE) {
            return;
        }

        StringBuilder borderStyle = new StringBuilder();
        borderStyle.append(ExcelToHtmlUtils.getBorderWidth(xlsBorder));
        borderStyle.append(' ');
        borderStyle.append(ExcelToHtmlUtils.getBorderStyle(xlsBorder));

        Color color;
        if (workbook instanceof HSSFWorkbook) {
            color = ((HSSFWorkbook) workbook).getCustomPalette().getColor(borderColor);
        } else {
            XSSFCellStyle xssfCellStyle = ((XSSFWorkbook) workbook).createCellStyle();
            xssfCellStyle.setFillBackgroundColor(borderColor);
            xssfCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            color = xssfCellStyle.getFillForegroundXSSFColor();
        }
        if (color != null) {
            borderStyle.append(' ');
            borderStyle.append(ExcelToHtmlUtils.getColor(color));
        }

        style.append("border-" + type + ":" + borderStyle + ";");
    }

    void buildStyle_font(Workbook workbook, StringBuilder style, Font font) {
        switch (font.getBoldweight()) {
            case Font.BOLDWEIGHT_BOLD:
                style.append("font-weight:bold;");
                break;
            case Font.BOLDWEIGHT_NORMAL:
                // by default, not not increase HTML size
                // style.append( "font-weight: normal; " );
                break;
        }

        Color fontColor;// = workbook.getCustomPalette().getColor(font.getColor());
        if (workbook instanceof HSSFWorkbook) {
            fontColor = ((HSSFWorkbook) workbook).getCustomPalette().getColor(font.getColor());
        } else {
            if (font instanceof XSSFFont) {
                fontColor = ((XSSFFont) font).getXSSFColor();
            } else fontColor = new HSSFColor.BLACK();
        }


        if (fontColor != null)
            style.append("color: " + ExcelToHtmlUtils.getColor(fontColor)
                    + "; ");

        if (font.getFontHeightInPoints() != 0)
            style.append("font-size:" + font.getFontHeightInPoints() + "pt;");

        if (font.getItalic()) {
            style.append("font-style:italic;");
        }
    }

    public String getCssClassPrefixCell() {
        return cssClassPrefixCell;
    }

    public String getCssClassPrefixDiv() {
        return cssClassPrefixDiv;
    }

    public String getCssClassPrefixRow() {
        return cssClassPrefixRow;
    }

    public String getCssClassPrefixTable() {
        return cssClassPrefixTable;
    }

    public Document getDocument() {
        return htmlDocumentFacade.getDocument();
    }

    protected String getStyleClassName(Workbook workbook,
                                       CellStyle cellStyle) {
        final Short cellStyleKey = Short.valueOf(cellStyle.getIndex());

        String knownClass = excelStyleToClass.get(cellStyleKey);
        if (knownClass != null)
            return knownClass;

        String cssStyle = buildStyle(workbook, cellStyle);
        String cssClass = htmlDocumentFacade.getOrCreateCssClass(
                cssClassPrefixCell, cssStyle);
        excelStyleToClass.put(cellStyleKey, cssClass);
        return cssClass;
    }

    public boolean isUseDivsToSpan() {
        return useDivsToSpan;
    }

    String cellValue(CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                Double aDouble = cellValue.getNumberValue();
                long a = Math.round(aDouble * 100);
                double d = a / 100d;
                return String.valueOf(d);
            case Cell.CELL_TYPE_STRING:
                return cellValue.getStringValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cellValue.getBooleanValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_ERROR:
                return null;
        }
        return null;
    }

    protected boolean processCell(Cell cell, Element tableCellElement,
                                  int normalWidthPx, int maxSpannedWidthPx, float normalHeightPt) {
        final CellStyle cellStyle = cell.getCellStyle();
        CellValue cellValue = evaluator.evaluate(cell);
        String s = null;
        try {
            s = cellValue(cellValue);
        } catch (Exception ignored) {
        }

        String value = s;
        if (value == null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    // XXX: enrich
                    value = cell.getRichStringCellValue().getString();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case Cell.CELL_TYPE_STRING:
                            RichTextString str = cell.getRichStringCellValue();
                            if (str != null && str.length() > 0) {
                                value = (str.toString());
                            } else {
                                value = ExcelToHtmlUtils.EMPTY;
                            }
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            double nValue = cell.getNumericCellValue();
                            short df = cellStyle.getDataFormat();
                            String dfs = cellStyle.getDataFormatString();
                            value = _formatter.formatRawCellContents(nValue, df, dfs);
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            value = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            value = ErrorEval.getText(cell.getErrorCellValue());
                            break;
                        default:
                            logger.log(
                                    POILogger.WARN,
                                    "Unexpected cell cachedFormulaResultType ("
                                            + cell.getCachedFormulaResultType() + ")");
                            value = ExcelToHtmlUtils.EMPTY;
                            break;
                    }
                    break;
                case Cell.CELL_TYPE_BLANK:
                    value = ExcelToHtmlUtils.EMPTY;
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    value = _formatter.formatCellValue(cell);
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = String.valueOf(cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    value = ErrorEval.getText(cell.getErrorCellValue());
                    break;
                default:
                    logger.log(POILogger.WARN,
                            "Unexpected cell type (" + cell.getCellType() + ")");
                    return true;
            }
        }
        final boolean noText = ExcelToHtmlUtils.isEmpty(value);
        final boolean wrapInDivs = !noText && isUseDivsToSpan() && !cellStyle.getWrapText();

        if (cellStyle.getIndex() != 0) {
            @SuppressWarnings("resource")
            Workbook workbook = cell.getRow().getSheet().getWorkbook();
            String mainCssClass = getStyleClassName(workbook, cellStyle);

            if (wrapInDivs) {
                tableCellElement.setAttribute("class", mainCssClass + " "
                        + cssClassContainerCell);
            } else {
                tableCellElement.setAttribute("class", mainCssClass);
            }

            if (noText) {
                /*
                 * if cell style is defined (like borders, etc.) but cell text
                 * is empty, add "&nbsp;" to output, so browser won't collapse
                 * and ignore cell
                 */
                value = "\u00A0";
            }
        }

        if (isOutputLeadingSpacesAsNonBreaking() && value.startsWith(" ")) {
            StringBuilder builder = new StringBuilder();
            for (int c = 0; c < value.length(); c++) {
                if (value.charAt(c) != ' ')
                    break;
                builder.append('\u00a0');
            }

            if (value.length() != builder.length())
                builder.append(value.substring(builder.length()));

            value = builder.toString();
        }

        Text text = htmlDocumentFacade.createText(value);

        if (wrapInDivs) {
            Element outerDiv = htmlDocumentFacade.createBlock();
            outerDiv.setAttribute("class", this.cssClassContainerDiv);

            Element innerDiv = htmlDocumentFacade.createBlock();
            StringBuilder innerDivStyle = new StringBuilder();
            innerDivStyle.append("position:absolute;min-width:");
            innerDivStyle.append(normalWidthPx);
            innerDivStyle.append("px;");
            if (maxSpannedWidthPx != Integer.MAX_VALUE) {
                innerDivStyle.append("max-width:");
                innerDivStyle.append(maxSpannedWidthPx);
                innerDivStyle.append("px;");
            }
            innerDivStyle.append("overflow:hidden;max-height:");
            innerDivStyle.append(normalHeightPt);
            innerDivStyle.append("pt;white-space:nowrap;");
            ExcelToHtmlUtils.appendAlign(innerDivStyle, cellStyle.getAlignment());
            htmlDocumentFacade.addStyleClass(outerDiv, cssClassPrefixDiv,
                    innerDivStyle.toString());

            innerDiv.appendChild(text);
            outerDiv.appendChild(innerDiv);
            tableCellElement.appendChild(outerDiv);
        } else {
            tableCellElement.appendChild(text);
        }

        return ExcelToHtmlUtils.isEmpty(value) && (cellStyle.getIndex() == 0);
    }

    protected void processColumnHeaders(Sheet sheet, int maxSheetColumns,
                                        Element table) {
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        table.appendChild(tableHeader);

        Element tr = htmlDocumentFacade.createTableRow();

        if (isOutputRowNumbers()) {
            // empty row at left-top corner
            tr.appendChild(htmlDocumentFacade.createTableHeaderCell());
        }

        for (int c = 0; c < maxSheetColumns; c++) {
            if (!isOutputHiddenColumns() && sheet.isColumnHidden(c))
                continue;

            Element th = htmlDocumentFacade.createTableHeaderCell();
            String text = getColumnName(c);
            th.appendChild(htmlDocumentFacade.createText(text));
            tr.appendChild(th);
        }
        tableHeader.appendChild(tr);
    }

    /**
     * Creates COLGROUP element with width specified for all columns. (Except
     * first if <tt>{@link #isOutputRowNumbers()}==true</tt>)
     */
    protected void processColumnWidths(Sheet sheet, int maxSheetColumns,
                                       Element table) {
        // draw COLS after we know max column number
        Element columnGroup = htmlDocumentFacade.createTableColumnGroup();
        if (isOutputRowNumbers()) {
            columnGroup.appendChild(htmlDocumentFacade.createTableColumn());
        }
        for (int c = 0; c < maxSheetColumns; c++) {
            if (!isOutputHiddenColumns() && sheet.isColumnHidden(c))
                continue;

            Element col = htmlDocumentFacade.createTableColumn();
            col.setAttribute("width",
                    String.valueOf(getColumnWidth(sheet, c)));
            columnGroup.appendChild(col);
        }
        table.appendChild(columnGroup);
    }

    protected void processDocumentInformation(
            SummaryInformation summaryInformation) {
        if (ExcelToHtmlUtils.isNotEmpty(summaryInformation.getTitle()))
            htmlDocumentFacade.setTitle(summaryInformation.getTitle());

        if (ExcelToHtmlUtils.isNotEmpty(summaryInformation.getAuthor()))
            htmlDocumentFacade.addAuthor(summaryInformation.getAuthor());

        if (ExcelToHtmlUtils.isNotEmpty(summaryInformation.getKeywords()))
            htmlDocumentFacade.addKeywords(summaryInformation.getKeywords());

        if (ExcelToHtmlUtils.isNotEmpty(summaryInformation.getComments()))
            htmlDocumentFacade
                    .addDescription(summaryInformation.getComments());
    }

    /**
     * @return maximum 1-base index of column that were rendered, zero if none
     */
    protected int processRow(CellRangeAddress[][] mergedRanges, Row row,
                             Element tableRowElement) {
        final Sheet sheet = row.getSheet();
        final short maxColIx = row.getLastCellNum();
        if (maxColIx <= 0)
            return 0;

        final List<Element> emptyCells = new ArrayList<Element>(maxColIx);

        if (isOutputRowNumbers()) {
            Element tableRowNumberCellElement = htmlDocumentFacade
                    .createTableHeaderCell();
            processRowNumber(row, tableRowNumberCellElement);
            emptyCells.add(tableRowNumberCellElement);
        }

        int maxRenderedColumn = 0;
        for (int colIx = 0; colIx < maxColIx; colIx++) {
            if (!isOutputHiddenColumns() && sheet.isColumnHidden(colIx))
                continue;

            CellRangeAddress range = ExcelToHtmlUtils.getMergedRange(
                    mergedRanges, row.getRowNum(), colIx);

            if (range != null
                    && (range.getFirstColumn() != colIx || range.getFirstRow() != row
                    .getRowNum()))
                continue;

            Cell cell = row.getCell(colIx);

            int divWidthPx = 0;
            if (isUseDivsToSpan()) {
                divWidthPx = getColumnWidth(sheet, colIx);

                boolean hasBreaks = false;
                for (int nextColumnIndex = colIx + 1; nextColumnIndex < maxColIx; nextColumnIndex++) {
                    if (!isOutputHiddenColumns()
                            && sheet.isColumnHidden(nextColumnIndex))
                        continue;

                    if (row.getCell(nextColumnIndex) != null
                            && !isTextEmpty(row.getCell(nextColumnIndex))) {
                        hasBreaks = true;
                        break;
                    }

                    divWidthPx += getColumnWidth(sheet, nextColumnIndex);
                }

                if (!hasBreaks)
                    divWidthPx = Integer.MAX_VALUE;
            }

            Element tableCellElement = htmlDocumentFacade.createTableCell();

            if (range != null) {
                if (range.getFirstColumn() != range.getLastColumn())
                    tableCellElement.setAttribute(
                            "colspan",
                            String.valueOf(range.getLastColumn()
                                    - range.getFirstColumn() + 1));
                if (range.getFirstRow() != range.getLastRow())
                    tableCellElement.setAttribute(
                            "rowspan",
                            String.valueOf(range.getLastRow()
                                    - range.getFirstRow() + 1));
            }

            boolean emptyCell;
            if (cell != null) {
                emptyCell = processCell(cell, tableCellElement,
                        getColumnWidth(sheet, colIx), divWidthPx,
                        row.getHeight() / 20f);
            } else {
                emptyCell = true;
            }

            if (emptyCell) {
                emptyCells.add(tableCellElement);
            } else {
                for (Element emptyCellElement : emptyCells) {
                    tableRowElement.appendChild(emptyCellElement);
                }
                emptyCells.clear();

                tableRowElement.appendChild(tableCellElement);
                maxRenderedColumn = colIx;
            }
        }

        return maxRenderedColumn + 1;
    }

    protected void processRowNumber(Row row,
                                    Element tableRowNumberCellElement) {
        tableRowNumberCellElement.setAttribute("class", "rownumber");
        Text text = htmlDocumentFacade.createText(getRowName(row));
        tableRowNumberCellElement.appendChild(text);
    }

    protected void processSheet(Sheet sheet) {
        processSheetHeader(htmlDocumentFacade.getBody(), sheet);

        final int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        if (physicalNumberOfRows <= 0)
            return;

        Element table = htmlDocumentFacade.createTable();
        htmlDocumentFacade.addStyleClass(table, cssClassPrefixTable,
                "border-collapse:collapse;border-spacing:0;");

        Element tableBody = htmlDocumentFacade.createTableBody();

        final CellRangeAddress[][] mergedRanges = ExcelToHtmlUtils
                .buildMergedRangesMap(sheet);

        final List<Element> emptyRowElements = new ArrayList<>(
                physicalNumberOfRows);
        int maxSheetColumns = 1;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);

            if (row == null)
                continue;

            if (!isOutputHiddenRows() && row.getZeroHeight())
                continue;

            Element tableRowElement = htmlDocumentFacade.createTableRow();
            htmlDocumentFacade.addStyleClass(tableRowElement,
                    cssClassPrefixRow, "height:" + (row.getHeight() / 20f)
                            + "pt;");

            int maxRowColumnNumber = processRow(mergedRanges, row,
                    tableRowElement);

            if (maxRowColumnNumber == 0) {
                emptyRowElements.add(tableRowElement);
            } else {
                if (!emptyRowElements.isEmpty()) {
                    for (Element emptyRowElement : emptyRowElements) {
                        tableBody.appendChild(emptyRowElement);
                    }
                    emptyRowElements.clear();
                }

                tableBody.appendChild(tableRowElement);
            }
            maxSheetColumns = Math.max(maxSheetColumns, maxRowColumnNumber);
        }

        processColumnWidths(sheet, maxSheetColumns, table);

        if (isOutputColumnHeaders()) {
            processColumnHeaders(sheet, maxSheetColumns, table);
        }

        table.appendChild(tableBody);

        htmlDocumentFacade.getBody().appendChild(table);
    }

    protected void processSheetHeader(Element htmlBody, Sheet sheet) {
        if(1==1) return;
        Element h2 = htmlDocumentFacade.createHeader2();
        h2.appendChild(htmlDocumentFacade.createText(sheet.getSheetName()));
        htmlBody.appendChild(h2);
    }

    public void processWorkbook(Workbook workbook) {

        if (isUseDivsToSpan()) {
            // prepare CSS classes for later usage
            this.cssClassContainerCell = htmlDocumentFacade
                    .getOrCreateCssClass(cssClassPrefixCell,
                            "padding:0;margin:0;align:left;vertical-align:top;");
            this.cssClassContainerDiv = htmlDocumentFacade.getOrCreateCssClass(
                    cssClassPrefixDiv, "position:relative;");
        }

        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);
            processSheet(sheet);
        }

        htmlDocumentFacade.updateStylesheet();
    }

    public void processWorkbook(Workbook workbook, String sheetName) {

        if (isUseDivsToSpan()) {
            // prepare CSS classes for later usage
            this.cssClassContainerCell = htmlDocumentFacade
                    .getOrCreateCssClass(cssClassPrefixCell,
                            "padding:0;margin:0;align:left;vertical-align:top;");
            this.cssClassContainerDiv = htmlDocumentFacade.getOrCreateCssClass(
                    cssClassPrefixDiv, "position:relative;");
        }

        Sheet sheet = workbook.getSheet(sheetName);
        processSheet(sheet);

        htmlDocumentFacade.updateStylesheet();
    }

    public void setCssClassPrefixCell(String cssClassPrefixCell) {
        this.cssClassPrefixCell = cssClassPrefixCell;
    }

    public void setCssClassPrefixDiv(String cssClassPrefixDiv) {
        this.cssClassPrefixDiv = cssClassPrefixDiv;
    }

    public void setCssClassPrefixRow(String cssClassPrefixRow) {
        this.cssClassPrefixRow = cssClassPrefixRow;
    }

    public void setCssClassPrefixTable(String cssClassPrefixTable) {
        this.cssClassPrefixTable = cssClassPrefixTable;
    }

    /**
     * Allows converter to wrap content into two additional DIVs with tricky
     * styles, so it will wrap across empty cells (like in Excel).
     * <p>
     * <b>Warning:</b> after enabling this mode do not serialize result HTML
     * with INDENT=YES option, because line breaks will make additional
     * (unwanted) changes
     */
    public void setUseDivsToSpan(boolean useDivsToSpan) {
        this.useDivsToSpan = useDivsToSpan;
    }
}
