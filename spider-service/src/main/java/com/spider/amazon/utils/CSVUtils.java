package com.spider.amazon.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Denebola
 * @Date: 2019/7/18-16:48
 * @Description: CSV工具类
 **/

public class CSVUtils {
    private static Logger logger = LoggerFactory.getLogger(CSVUtils.class);
    //行尾分隔符定义
    private final static String NEW_LINE_SEPARATOR = "\n";
    //上传文件的存储位置
    private final static URL PATH = Thread.currentThread().getContextClassLoader().getResource("");

    /**
     * @return File
     * @Description 创建CSV文件
     * @Param fileName 文件名，head 表头，values 表体
     **/
    public static File makeTempCSV(String fileName, String[] head, List<String[]> values) throws IOException {
//        创建文件
        File file = File.createTempFile(fileName, ".csv", new File(PATH.getPath()));
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        CSVPrinter printer = new CSVPrinter(bufferedWriter, formator);

//        写入表头
        printer.printRecord(head);

//        写入内容
        for (String[] value : values) {
            printer.printRecord(value);
        }

        printer.close();
        bufferedWriter.close();
        return file;
    }

    /**
     * @return boolean
     * @Description 下载文件
     * @Param response，file
     **/
    public static boolean downloadFile(HttpServletResponse response, File file) {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream os = null;
        try {
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            os = response.getOutputStream();
            //MS产本头部需要插入BOM
            //如果不写入这几个字节，会导致用Excel打开时，中文显示乱码
            os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            byte[] buffer = new byte[1024];
            int i = bufferedInputStream.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bufferedInputStream.read(buffer);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            //关闭流
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            file.delete();
        }
        return false;
    }

    /**
     * @return File
     * @Description 上传文件
     * @Param multipartFile
     **/
    public static File uploadFile(MultipartFile multipartFile) {
        String path = PATH.getPath() + multipartFile.getOriginalFilename();
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            multipartFile.transferTo(file);
            logger.info("上传文件成功，文件名===>" + multipartFile.getOriginalFilename() + ", 路径===>" + file.getPath());
            return file;
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage(), e);
            return null;
        }

    }

    /**
     * @return List<List < String>>
     * @Description 读取CSV文件的内容（不含表头）
     * @Param filePath 文件存储路径，colNum 列数
     **/
    public static List<List<String>> readCSV(String filePath, int colNum) {
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            CSVParser parser = CSVFormat.DEFAULT.parse(bufferedReader);
//          表内容集合，外层List为行的集合，内层List为字段集合
            List<List<String>> values = new ArrayList<>();
            int rowIndex = 0;

            for (CSVRecord record : parser.getRecords()) {
//              跳过表头
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
//              每行的内容
                List<String> value = new ArrayList<>(colNum + 1);
                for (int i = 0; i < colNum; i++) {
                    value.add(record.get(i));
                }
                values.add(value);
                rowIndex++;
            }
            return values;
        } catch (IOException e) {
            logger.error("解析CSV内容失败" + e.getMessage(), e);
        } finally {
            //关闭流
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * @return List<List < String>>
     * @Description 读取CSV文件的内容
     * @Param filePath 文件存储路径，rowNum行数，colNum 列数
     **/
    public static List<List<String>> readCSVAdv(String filePath, int startRowNum, int endRowNum, int colNum) {
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            CSVParser parser = CSVFormat.DEFAULT.parse(bufferedReader);
//          表内容集合，外层List为行的集合，内层List为字段集合
            List<List<String>> values = new ArrayList<>();
            int rowIndex = 0;

            for (CSVRecord record : parser.getRecords()) {
                // 尾行
                if (rowIndex >= endRowNum) {
                    break;
                }
                // 起始行
                if (rowIndex < startRowNum) {
                    rowIndex++;
                    continue;
                }

//              每行的内容
                List<String> value = new ArrayList<>(colNum + 1);
                for (int i = 0; i < colNum; i++) {
                    if (!record.get(i).isEmpty()) {
                        value.add(record.get(i));
                    } else {
                        value.add("");
                    }
                }
                values.add(value);
                rowIndex++;
            }
            return values;
        } catch (IOException e) {
            logger.error("解析CSV内容失败" + e.getMessage(), e);
        } finally {
            //关闭流
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * @return Map<String,Object>
     * @Description 收集CSV制定行列的数据桶
     * @Param filePath 文件存储路径，startRowNum 初始行数,endRowNum 结束行数, startColNum 开始列数，endrow 结束行数
     **/
    public static Map<String,Object> readCSVBuildMap(String filePath, String sheetName, int startRowNum, int endRowNum, int startColNum ,int endColNum,List<String> allList) {
        ExcelReader reader = ExcelUtil.getReader(filePath);
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = workbook.getSheet(sheetName); // 获取第二个工作簿
        Map<String,Object> values = new LinkedHashMap<>();
//        System.out.println(sheet.getRow(2).getCell(157).getStringCellValue());
        try {

            int rowIndex = 0;
            for (;rowIndex< sheet.getLastRowNum();) {
                // 尾行
                if (rowIndex >= endRowNum) {
                    break;
                }
                // 起始行
                if (rowIndex < startRowNum) {
                    rowIndex++;
                    continue;
                }
//              每行的内容
                Row row= sheet.getRow(rowIndex);
                for (int i = startColNum; i < endColNum; i++) {
                    if ( ObjectUtil.isNotEmpty(row.getCell(i))) {
                        allList.add(getCellValueByCell(row.getCell(i)));
                        if (!values.containsKey(getCellValueByCell(row.getCell(i)))) {
                            values.put(getCellValueByCell(row.getCell(i)),"");
                        }
                    }
                }
                rowIndex++;
            }
            return values;
        } finally {
            //关闭
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    //获取单元格各类型值，返回字符串类型
    public static String getCellValueByCell(Cell cell) {
        //判断是否为null或空串
        if (ObjectUtil.isEmpty(cell) || cell.toString().trim().equals("")) {
            return "";
        }
        String cellValue = "";
        int cellType = cell.getCellType();

        // 以下是判断数据的类型
        switch (cellType) {
            case HSSFCell.CELL_TYPE_NUMERIC: // 数字

                if (0 == cell.getCellType()) {//判断单元格的类型是否则NUMERIC类型
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否为日期类型
                        cellValue = DateUtil.format(cell.getDateCellValue(), com.spider.amazon.cons.DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss) ;
                    } else {
                        cellValue = cell.getNumericCellValue() + "";
                    }
                }
                break;


            case HSSFCell.CELL_TYPE_STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;


            case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;


            case HSSFCell.CELL_TYPE_FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;


            case HSSFCell.CELL_TYPE_BLANK: // 空值
                cellValue = "";
                break;


            case HSSFCell.CELL_TYPE_ERROR: // 故障
                cellValue = "非法字符";
                break;


            default:
                cellValue = "未知类型";
                break;

        }
        return cellValue;
    }


    /**
     * @return List<List<String>>
     * @Description 指定sheet收集数据
     * @Param filePath 文件存储路径，startRowNum 初始行数,endRowNum 结束行数, startColNum 开始列数，endrow 结束行数
     **/
    public static List<List<String>> readCSVBySheetName(String filePath, String sheetName, int startRowNum, int endRowNum, int startColNum ,int endColNum) {
        List<List<String>> values = new ArrayList<>();

        ExcelReader reader = ExcelUtil.getReader(filePath);
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = workbook.getSheet(sheetName); // 获取指定工作簿
        if (endRowNum==0) {
            endRowNum=sheet.getLastRowNum();
        }
        try {
            int rowIndex = 0;
            for (;rowIndex<= sheet.getLastRowNum();) {
                // 尾行
                if (rowIndex > endRowNum) {
                    break;
                }
                // 起始行
                if (rowIndex < startRowNum) {
                    rowIndex++;
                    continue;
                }
//              每行的内容
                Row row= sheet.getRow(rowIndex);
                List<String> singleValue=new ArrayList<>();
                values.add(singleValue);
                for (int i = startColNum; i < endColNum; i++) {
                    if ( ObjectUtil.isNotEmpty(row.getCell(i))) {
                        singleValue.add(getCellValueByCell(row.getCell(i)));
                    }
                }

                rowIndex++;
            }
            return values;
        } finally {
            //关闭
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }


    public static void main(String[] args) {
//        List<List<String>> result = readCSVAdv("/Users/zhucan/Downloads/Sales Diagnostic_Detail View_US.csv", 0, 1, 11);
//        String reportingRange=result.get(0).get(7);
//        String viewing=result.get(0).get(8);
//        reportingRange=reportingRange.substring(reportingRange.indexOf("[")+1,reportingRange.indexOf("]"));
//        viewing=viewing.substring(viewing.indexOf("[")+1,viewing.indexOf("]"));
//        System.out.println("reportingRange:"+reportingRange);
//        System.out.println("viewing:"+viewing);


//        Map<String,Object> resultMap=new HashMap<>();
//        // 读取文件第一行
//        List<List<String>>  csvRowList= CSVUtils.readCSVAdv("/Users/zhucan/Downloads/Inventory Health_US.csv", 0,1,9);
//        // 获取报表维度及时间


//        List<List<String>> matchList = readCSVBySheetName("/Users/zhucan/Downloads/BIUploadFile/Test 1.2.2020.xlsm","ASIN - SKU Match",0,0,0,4);;

        return;
    }

}

