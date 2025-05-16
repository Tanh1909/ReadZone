package vn.tnteco.common.utils.oldexcel;

import io.reactivex.rxjava3.core.Single;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.tools.csv.CSVReader;
import org.springframework.web.multipart.MultipartFile;
import vn.tnteco.common.config.annotation.ExcelField;
import vn.tnteco.common.core.exception.BusinessException;
import vn.tnteco.common.core.json.JsonObject;
import vn.tnteco.common.core.model.ExcelRow;
import vn.tnteco.common.core.template.RxTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Log4j2
@Deprecated
public class ReadExcelUtils {
    /**
     * Đọc data tù file excel
     *
     * @param clz  Tên class
     * @param path đường dẫn đên file
     * @return
     */
    public static <T> List<T> readExcel(Class<T> clz, String path) {
        System.out.println(path);
        if (null == path || "".equals(path)) {
            throw new BusinessException("Đường dẫn không hợp lệ");
        }
        InputStream is;
        Workbook xssfWorkbook;
        try {
            is = new FileInputStream(path);
            if (path.endsWith(".xls")) {
                xssfWorkbook = new HSSFWorkbook(is);
            } else if (path.endsWith(".xlsx")) {
                xssfWorkbook = new XSSFWorkbook(is);
            } else {
                throw new BusinessException("File không phải là định dạng Excel hợp lệ");
            }
            is.close();
            int sheetNumber = xssfWorkbook.getNumberOfSheets();
            List<T> allData = new ArrayList<>();
            for (int i = 0; i < sheetNumber; i++) {
                allData.addAll(transToObject(clz, xssfWorkbook, xssfWorkbook.getSheetName(i)).values());
            }
            return allData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Có lỗi xãy ra khi đọc file excel ：" + e.getMessage());
        }
    }

    public static <T extends ExcelRow> Single<List<T>> readFile(InputStream inputStream, Class<T> aClass) {
        return RxTemplate.rxSchedulerComputing(() -> {
            try {
                return readExcel(aClass, inputStream);
            } catch (Exception e) {
                log.error("Fail to read file", e);
                throw new BusinessException("Fail to read file");
            }
        });
    }

    private static <T extends ExcelRow> List<T> processInputFile(InputStream inputStream, Class<T> aClass)
            throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        List<T> results = new ArrayList<>();
        CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
        List<String[]> datas = csvReader.readAll();
        List<Object> heads = new ArrayList<>();
        heads.add("Handle");
        heads.addAll(Arrays.stream(datas.get(0)).skip(1).collect(toList()));

        Map<String, ExcelField> fieldExcelInfoMap = getFieldExcelInfoMap(aClass);
        Map<String, Method> headMethod = getSetMethod(aClass, heads, fieldExcelInfoMap);
        for (int i = 1; i < datas.size(); i++) {
            T newInstance = aClass.newInstance();
            setValue(newInstance, Arrays.stream(datas.get(i)).collect(toList()), heads, headMethod);
            results.add(newInstance);
        }
        return results;
    }

    private static <T> List<T> readExcel(Class<T> clz, InputStream inputStream) throws Exception {
        Workbook xssfWorkbook;

        try {
            xssfWorkbook = new XSSFWorkbook(inputStream);
        } catch (Exception e) {
            xssfWorkbook = new HSSFWorkbook(inputStream);
        }

        int sheetNumber = xssfWorkbook.getNumberOfSheets();
        List<T> allData = new ArrayList<T>();
        for (int i = 0; i < sheetNumber; i++) {
            allData.addAll(transToObject(clz, xssfWorkbook, xssfWorkbook.getSheetName(i)).values());
        }
        return allData;
    }

    /**
     * Đọc data từ một sheet vào một list object
     *
     * @param clz
     * @param file
     * @param sheetName
     * @return
     */
    public static <T> List<T> readExcel(Class<T> clz, MultipartFile file, String sheetName) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Workbook workbook;
        try (InputStream is = file.getInputStream()) {
            if (file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(is);
            } else if (file.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(is);
            } else {
                throw new BusinessException("Invalid file format: " + file.getOriginalFilename());
            }

            Map<Integer, T> rowDataMap = transToObject(clz, workbook, sheetName);
            return new ArrayList<>(rowDataMap.values());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Error occurred while reading sheet " + sheetName + ": " + e.getMessage());
        }
    }


    private static <T> Map<Integer, T>
    transToObject(Class<T> clz, Workbook xssfWorkbook, String sheetName)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
//        List<T> list = new ArrayList<T>();
        Map<Integer, T> rowDatta = new HashMap<>();
        Sheet xssfSheet = xssfWorkbook.getSheet(sheetName);
        Row firstRow = xssfSheet.getRow(0);
        if (firstRow == null) {
            return rowDatta;
        }
        List<Object> heads = getRow(xssfSheet,firstRow);
        Map<String, ExcelField> fieldExcelInfoMap = getFieldExcelInfoMap(clz);
        //heads.add("sheetName");
        Map<String, Method> headMethod = getSetMethod(clz, heads, fieldExcelInfoMap);
        log.info("excel last row : {} ", xssfSheet.getLastRowNum());

        for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
            try {
                Row xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow == null) {
                    continue;
                }
                T t = clz.newInstance();
                List<Object> data = getRow(xssfSheet,xssfRow);

                while (data.size() + 1 < heads.size()) {
                    data.add("");
                }
                data.add(sheetName);
                setValue(t, data, heads, headMethod);
                rowDatta.put(rowNum, t);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                log.error("Fail to trans to object", e);
            }
        }
        return rowDatta;
    }


    private static Map<String, ExcelField> getFieldExcelInfoMap(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .map(field -> Pair.of(field.getName(), field.getAnnotation(ExcelField.class)))
                .filter(pair -> pair.getRight() != null)
                .collect(toMap(Pair::getLeft, Pair::getRight));
    }

    private static Map<String, Method> getSetMethod(Class<?> clz, List<Object> heads,
                                                    Map<String, ExcelField> fieldExcelInfoMap) {
        Map<String, Method> map = new HashMap<>();
        Method[] methods = clz.getMethods();
        for (Object head : heads) {
            // boolean find = false;
            for (Method method : methods) {
                String field = capitalize(method.getName().replaceFirst("set", ""));
                ExcelField excelField = fieldExcelInfoMap.getOrDefault(field, null);
                if (excelField != null && excelField.name().trim().equalsIgnoreCase(head.toString())
                    && method.getParameterTypes().length == 1) {
                    map.put(head.toString(), method);
                    break;
                } else if (method.getName().toLowerCase().equals("set" + head.toString().toLowerCase())
                           && method.getParameterTypes().length == 1) {
                    map.put(head.toString(), method);
                    break;
                }
            }
        }
        return map;
    }

    private static String capitalize(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private static void setValue(Object obj, List<Object> data, List<Object> heads, Map<String, Method> methods)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            Object value = "";
            int dataIndex = heads.indexOf(entry.getKey());
            if (dataIndex < data.size()) {
                value = data.get(heads.indexOf(entry.getKey()));
            }
            Method method = entry.getValue();
            Class<?> param = method.getParameterTypes()[0];
            if (String.class.equals(param)) {
                method.invoke(obj, value);
            } else if (Integer.class.equals(param) || int.class.equals(param)) {
                if (StringUtils.isEmpty(value.toString())) {
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).intValue());
            } else if (Long.class.equals(param) || long.class.equals(param)) {
                if (isEmpty(value.toString())) {
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).longValue());
            } else if (Double.class.equals(param) || double.class.equals(param)) {
                if (isEmpty(value.toString())) {
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).doubleValue());
            } else if (Short.class.equals(param) || short.class.equals(param)) {
                if (isEmpty(value.toString())) {
                    value = 0;
                }
                method.invoke(obj, new BigDecimal(value.toString()).shortValue());
            } else if (Boolean.class.equals(param) || boolean.class.equals(param)) {
                method.invoke(obj, Boolean.parseBoolean(value.toString())
                                   || value.toString().toLowerCase().equals("y"));
            } else if (JsonObject.class.equals(param)) {
                method.invoke(obj, new JsonObject(value.toString()));
            } else {
                // Date
                method.invoke(obj, value);
            }
        }
    }

    private static List<Object> getRow(Sheet sheet, Row xssfRow) {
        List<Object> cells = new ArrayList<>();
        if (xssfRow != null) {
            for (int cellNum = 0; cellNum < xssfRow.getLastCellNum(); cellNum++) {
                Cell xssfCell = xssfRow.getCell(cellNum);
                if (xssfCell != null && isMergedCell(sheet, xssfCell)) {
                    String mergedCellValue = getValue(xssfCell);
                    cells.add(mergedCellValue);

                } else {
                    cells.add(getValue(xssfCell));
                }
            }
        }
        return cells;
    }

    private static boolean isMergedCell(Sheet sheet, Cell cell) {
        for (int i = 1; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return true;
            }
        }
        return false;
    }

    private static String getValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    double d = cell.getNumericCellValue();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(DateUtil.getJavaDate(d));
                    return (String.valueOf(cal.get(Calendar.YEAR))).substring(2) + "/" +
                            (cal.get(Calendar.MONTH) + 1) + "/" +
                            cal.get(Calendar.DAY_OF_MONTH);
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case FORMULA:
                try {
                    if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return String.valueOf(cell.getNumericCellValue());
                    } else if (cell.getCachedFormulaResultType() == CellType.STRING) {
                        return cell.getStringCellValue();
                    }
                } catch (IllegalStateException e) {
                    return cell.getCellFormula();
                }
            default:
                return cell.toString();
        }
    }

}
