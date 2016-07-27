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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hwpf.converter.AbstractWordConverter;
import org.apache.poi.hwpf.converter.DefaultFontReplacer;
import org.apache.poi.hwpf.converter.FontReplacer;
import org.apache.poi.hwpf.converter.NumberFormatter;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Beta;
import org.w3c.dom.Document;

/**
 * Common class for {@link ExcelToFoConverter} and {@link ExcelToHtmlConverter}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 * @see AbstractWordConverter
 */
@Beta
public abstract class AbstractExcelConverter
{
    protected static int getColumnWidth(Sheet sheet, int columnIndex )
    {
        return ExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getColumnWidth( columnIndex ) );
    }

    protected static int getDefaultColumnWidth( Sheet sheet )
    {
        return ExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getDefaultColumnWidth() );
    }

    protected final HSSFDataFormatter _formatter = new HSSFDataFormatter();

    private FontReplacer fontReplacer = new DefaultFontReplacer();

    private boolean outputColumnHeaders = false;

    private boolean outputHiddenColumns = false;

    private boolean outputHiddenRows = false;

    private boolean outputLeadingSpacesAsNonBreaking = true;

    private boolean outputRowNumbers = false;

    /**
     * Generates name for output as column header in case
     * <tt>{@link #isOutputColumnHeaders()} == true</tt>
     * 
     * @param columnIndex
     *            0-based column index
     */
    protected String getColumnName( int columnIndex )
    {
        return NumberFormatter.getNumber( columnIndex + 1, 3 );
    }

    protected abstract Document getDocument();

    public FontReplacer getFontReplacer()
    {
        return fontReplacer;
    }

    /**
     * Generates name for output as row number in case
     * <tt>{@link #isOutputRowNumbers()} == true</tt>
     */
    protected String getRowName( Row row )
    {
        return String.valueOf( row.getRowNum() + 1 );
    }

    public boolean isOutputColumnHeaders()
    {
        return outputColumnHeaders;
    }

    public boolean isOutputHiddenColumns()
    {
        return outputHiddenColumns;
    }

    public boolean isOutputHiddenRows()
    {
        return outputHiddenRows;
    }

    public boolean isOutputLeadingSpacesAsNonBreaking()
    {
        return outputLeadingSpacesAsNonBreaking;
    }

    public boolean isOutputRowNumbers()
    {
        return outputRowNumbers;
    }

    protected boolean isTextEmpty( Cell cell )
    {
        final String value;
        switch ( cell.getCellType() )
        {
        case Cell.CELL_TYPE_STRING:
            // XXX: enrich
            value = cell.getRichStringCellValue().getString();
            break;
        case Cell.CELL_TYPE_FORMULA:
            switch ( cell.getCachedFormulaResultType() )
            {
            case Cell.CELL_TYPE_STRING:
                RichTextString str = cell.getRichStringCellValue();
                if ( str == null || str.length() <= 0 )
                    return false;

                value = str.toString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                CellStyle style = cell.getCellStyle();
                double nval = cell.getNumericCellValue();
                short df = style.getDataFormat();
                String dfs = style.getDataFormatString();
                value = _formatter.formatRawCellContents(nval, df, dfs);
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case Cell.CELL_TYPE_ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
                value = ExcelToHtmlUtils.EMPTY;
                break;
            }
            break;
        case Cell.CELL_TYPE_BLANK:
            value = ExcelToHtmlUtils.EMPTY;
            break;
        case Cell.CELL_TYPE_NUMERIC:
            value = _formatter.formatCellValue( cell );
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            value = String.valueOf( cell.getBooleanCellValue() );
            break;
        case Cell.CELL_TYPE_ERROR:
            value = ErrorEval.getText( cell.getErrorCellValue() );
            break;
        default:
            return true;
        }

        return ExcelToHtmlUtils.isEmpty( value );
    }

    public void setFontReplacer( FontReplacer fontReplacer )
    {
        this.fontReplacer = fontReplacer;
    }

    public void setOutputColumnHeaders( boolean outputColumnHeaders )
    {
        this.outputColumnHeaders = outputColumnHeaders;
    }

    public void setOutputHiddenColumns( boolean outputZeroWidthColumns )
    {
        this.outputHiddenColumns = outputZeroWidthColumns;
    }

    public void setOutputHiddenRows( boolean outputZeroHeightRows )
    {
        this.outputHiddenRows = outputZeroHeightRows;
    }

    public void setOutputLeadingSpacesAsNonBreaking(
            boolean outputPrePostSpacesAsNonBreaking )
    {
        this.outputLeadingSpacesAsNonBreaking = outputPrePostSpacesAsNonBreaking;
    }

    public void setOutputRowNumbers( boolean outputRowNumbers )
    {
        this.outputRowNumbers = outputRowNumbers;
    }

}
