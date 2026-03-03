package com.nestle.blend.api.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

public class ExcelStreamHelper {

    private String fontName = "SansSerif";

    public XSSFCellStyle generateCellStyleHeader(SXSSFWorkbook workbook, Row row){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeight(14);
        font.setColor(this.getWhiteColor());
        font.setFontName(this.fontName);
        style.setFont(font);
        style.setFillForegroundColor(this.getGreenColor());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        row.setHeight((short) 1050);

        return style;
    }
    public XSSFCellStyle generateCellStyleHeaderGr(SXSSFWorkbook workbook, Row row){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeight(12);
        font.setColor(this.getWhiteColor());
        font.setFontName(this.fontName);
        style.setFont(font);
        style.setFillForegroundColor(this.getBlackGrayColor());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.WHITE.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.WHITE.getIndex());

        row.setHeight((short) 500);

        return style;
    }
    public XSSFCellStyle generateCellStyleContentSummary(SXSSFWorkbook workbook){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontName(this.fontName);
        font.setFontHeight(14);
        style.setFont(font);
        return style;
    }

    public XSSFCellStyle generateCellStyleContent(SXSSFWorkbook workbook){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontName(this.fontName);
        font.setFontHeight(14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public XSSFCellStyle generateCellStyleContent(SXSSFWorkbook workbook,int fontSize, boolean bold){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontName(this.fontName);
        font.setFontHeight(fontSize);
        font.setBold(bold);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);


        return style;
    }

    public XSSFCellStyle generateCellStyleBgRed(SXSSFWorkbook workbook) {
        XSSFCellStyle styleDanger = this.generateCellStyleContent(workbook);
        Font f = styleDanger.getFont();
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.WHITE.getIndex());
        styleDanger.setFillForegroundColor(this.getRedColor());
        styleDanger.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleDanger.setFont(f);
        styleDanger.setAlignment(HorizontalAlignment.CENTER);

        return styleDanger;
    }

    public void createCell(Sheet sheet, Row row, int columnCount, Object value, XSSFCellStyle style) {
//        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    public void setBorderRangeAddress(int colorIndex, CellRangeAddress rangeAddress, Sheet sheet){
        RegionUtil.setBorderTop(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setTopBorderColor(colorIndex, rangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setRightBorderColor(colorIndex, rangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBottomBorderColor(colorIndex, rangeAddress, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setLeftBorderColor(colorIndex, rangeAddress, sheet);
    }

    public XSSFColor getWhiteColor(){
        XSSFColor color = new XSSFColor();
        color.setARGBHex("FFFFFF");

        return color;
    }
    public XSSFColor getGreenColor(){
        XSSFColor color = new XSSFColor();
        color.setARGBHex("289595");

        return color;
    }
    public XSSFColor getRedColor(){
        XSSFColor color = new XSSFColor();
        color.setARGBHex("C70000");

        return color;
    }
    public XSSFColor getBlackGrayColor(){
        XSSFColor color = new XSSFColor();
        color.setARGBHex("434343");

        return color;
    }
}
