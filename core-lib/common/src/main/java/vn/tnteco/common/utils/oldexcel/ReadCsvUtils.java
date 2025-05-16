package vn.tnteco.common.utils.oldexcel;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import vn.tnteco.common.config.annotation.ExcelField;
import vn.tnteco.common.core.json.JsonObject;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Deprecated
public class ReadCsvUtils {

    public static void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/data/crawler/logs/log.txt"))) {
            String line = reader.readLine();
            Set<String> location = new HashSet<>();
            while (line != null) {
                line = reader.readLine();
                try {
                    location.add(new JsonObject(line).getString("location"));
                } catch (Exception e) {
                    System.out.println(line);
                }
            }
            System.out.println(location.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> Single<List<T>> readCsvFomURL(Class<T> clz, String fileUrl) {
        return rxSchedulerIo(() -> readCsvFomURLBlocking(clz, fileUrl));
    }

    public static <T> List<T> readCsvFomURLBlocking(Class<T> clz, String fileUrl) throws CsvException, IOException {
        Map<String, String> headerFieldMap = extractHeader(clz.getDeclaredFields());
        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        Reader reader = new InputStreamReader(inputStream);
        return readToObjectFromReader(clz, headerFieldMap, reader);
    }

    public static <T> List<T> readCsvFomFileBlocking(Class<T> clz, File file) throws CsvException, IOException {
        Map<String, String> headerFieldMap = extractHeader(clz.getDeclaredFields());
        Reader reader = new InputStreamReader(new FileInputStream(file));
        return readToObjectFromReader(clz, headerFieldMap, reader);
    }

    public static <T> List<T> readCsvFomPathBlocking(Class<T> clz, String path) throws CsvException, IOException {
        return readCsvFomFileBlocking(clz, new File(path));
    }

    private static Map<String, String> extractHeader(Field[] clz) {
        return Arrays.stream(clz)
                .map(field -> {
                    if (field.getAnnotations() != null && field.getAnnotation(ExcelField.class) != null) {
                        ExcelField annotation = field.getAnnotation(ExcelField.class);
                        if (annotation.name() != null) return Pair.of(annotation.name(), field.getName());
                    }
                    return Pair.of(field.getName(), field.getName());
                })
                .collect(toMap(Pair::getLeft, Pair::getRight, (v1, v2) -> v1));
    }

    private static <T> List<T> readToObjectFromReader(Class<T> clz,
                                                      Map<String, String> headerFieldMap,
                                                      Reader reader) throws IOException, CsvException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .build();
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .build();
        String[] header = csvReader.readNext();
        List<String[]> lines = csvReader.readAll();
        List<T> results = lines.stream()
                .map(line -> convertToObject(clz, headerFieldMap, header, line))
                .collect(Collectors.toList());
        csvReader.close();
        return results;
    }

    private static <T> T convertToObject(Class<T> clz, Map<String, String> headerFieldMap, String[] header, String[] line) {
        JsonObject object = new JsonObject();
        for (int i = 0; i < header.length; i++) {
            if (headerFieldMap.containsKey(header[i])) {
                object.put(headerFieldMap.get(header[i])
                                .replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(),
                        line[i]);
            }
        }
        return object.mapTo(clz);
    }
}
