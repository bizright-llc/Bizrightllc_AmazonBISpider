package com.spider.amazon.utils;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * POI工具类:
 * 获取指定位置单元格的值
 * 设置指定位置单元格的值
 */
public class MyPOIUtils {

    public static Workbook workbook ;

    public static FileInputStream fileInputStream ;

    public static FileOutputStream out = null;

    /**
     * 确认读取文件的版本类型
     * @param filePath
     */
    public static void insureExcelType(String filePath) {
        try {
            fileInputStream = new FileInputStream(filePath);
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fileInputStream);
            } else if (filePath.endsWith(".xls")) {
                POIFSFileSystem fileSystem = new POIFSFileSystem(fileInputStream);
                workbook = new HSSFWorkbook(fileSystem);
            } else {
                throw new RuntimeException("错误提示: 您设置的Excel文件名不合法!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取第一个Sheet中的单元格
     * @param rowIndex
     * @param colIndex
     * @return
     */
    public static Cell getCell(int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(rowIndex - 1);
        Cell cell = row.getCell(colIndex - 1);
        return cell ;
    }

    /**
     * 获取指定Sheet中的单元格
     * @param st
     * @param rowIndex
     * @param colIndex
     * @return
     */
    public static Cell getCell(int st,int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheetAt(st);
        Row row = sheet.getRow(rowIndex - 1);
        Cell cell = row.getCell(colIndex - 1);
        return cell ;
    }

    /**
     * 根据行和列获取单元格内容
     //     * @param filePath文件路径
     //     * @param rowIndex行号
     //     * @param colIndex列号
     * @return
     * @throws Exception
     */
    public static String getValueAt(String filePath,int rowIndex, int colIndex) throws Exception {

        insureExcelType(filePath);

        Cell cell = getCell(rowIndex,colIndex);
        String cellValue = getCellValue(cell);
        return cellValue ;
    }
    public static String getCellValue(Cell cell) {
        String value = "";
        // 以下是判断数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                value = cell.getNumericCellValue() + "";
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    if (date != null) {
                        value = new SimpleDateFormat("d-m-yy").format(date);
                    } else {
                        value = "";
                    }
                } else {
//                    value = new DecimalFormat("0").format(cell.getNumericCellValue());
                    value = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_STRING: // 字符串
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BOOLEAN: // Boolean
                value = cell.getBooleanCellValue() + "";
                break;
            case Cell.CELL_TYPE_FORMULA: // 公式
                value = cell.getCellFormula() + "";
                break;
            case Cell.CELL_TYPE_BLANK: // 空值
                value = "";
                break;
            case Cell.CELL_TYPE_ERROR: // 故障
                value = "非法字符";
                break;
            default:
                value = "未知类型";
                break;
        }
        return value;
    }

    /**
     * 设置指定位置的单元格内容
     * @param filePath  源文件地址
     * @param descPath  保存文件的目的地
     * @param rowIndex  需要设置单元格的行号(根据单元格行号写即可)
     * @param colIndex  需要设置的单元格的列号(将单元格的列对应的字母转换为数字即可)
     * @param object    需要设置的内容
     * @throws Exception
     */
    public static void setValueAt(String filePath,String descPath,int rowIndex, int colIndex,Object object) throws Exception {

        insureExcelType(filePath);
        Cell cell = getCell(rowIndex,colIndex);
        setCellValue(workbook,cell,object);

        try{
            out = new FileOutputStream(descPath);
            workbook.write(out);
        }catch(IOException e){
            System.out.println(e.toString());
        }finally{
            try {
                out.close();
            }catch(IOException e){
                System.out.println(e.toString());
            }
        }
    }
//    public static Workbook setValueAt(String filePath, Pass pass) throws Exception {
//
//        insureExcelType(filePath);
//        Cell cell = getCell(14,2);
//        setCellValue(workbook,cell,pass.getCompany_name());
//
//        Cell cell1 = getCell(15,2);
//        setCellValue(workbook,cell1,pass.getOpening_bank());
//
//        Cell cell2 = getCell(16,2);
//        setCellValue(workbook,cell2,pass.getCard_code());
//
//        return workbook;
//    }

    public static Workbook getWorkbook(String filePath) throws Exception {

        insureExcelType(filePath);

        return workbook;
    }
    /**
     *
     * 设置单元格值
     *
     * @param cell
     *            需要设置的单元格
     * @param value
     *            设置给单元格cell的值
     * @return 设置好的单元格列对象
     */
    public static Cell setCellValue(Workbook workbook,Cell cell, Object value) {
        if (value instanceof String) {
            cell.setCellValue((String) value);
            cell.setCellType(Cell.CELL_TYPE_STRING);
        } else if (value instanceof Date) {
            //获取设置时间格式对象
            CreationHelper creationHelper = workbook.getCreationHelper();
            //获取单元格样式对象
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("d-m-yy"));
            cell.setCellValue((Date) value);
            // cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellStyle(cellStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        } else if (value instanceof Calendar) {
            //获取设置时间格式对象
            CreationHelper creationHelper = workbook.getCreationHelper();
            //获取单元格样式对象
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("d-m-yy"));
            cell.setCellValue((Calendar) value);
            //cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellStyle(cellStyle);
        } else if (value instanceof RichTextString) {
            cell.setCellValue((RichTextString) value);
            cell.setCellType(Cell.CELL_TYPE_STRING);
        } else {
            cell.setCellValue(String.valueOf(value));
            cell.setCellType(Cell.CELL_TYPE_STRING);
            // System.out.println("错误提示: 您设置的单元格内容【"+value+"】不符合要求,不是常用类型!");
        }
        return cell;
    }

    public static void main(String[] args) throws Exception {
        String valueAt = getValueAt("F:/B337.xlsx", 29, 10);
        System.out.println(valueAt);

    }
}


