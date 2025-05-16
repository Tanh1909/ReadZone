package vn.tnteco.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@UtilityClass
public class UploadFileUtils {

    private static final Path CURRENT_FOLDER = Paths.get(System.getProperty("user.dir"));

    private static final Path RESOURCES_PATH = CURRENT_FOLDER.resolve(Paths.get("src/main/resources"));

    public static String saveFile(String fileName, String uploadPath, byte[] data) throws IOException {
        Path path = RESOURCES_PATH.resolve(Paths.get(uploadPath));
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Path filePath = path.resolve(fileName);
        File file = new File(filePath.toUri().getPath());
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            out.write(data);
            return file.toPath().toString();
        } catch (Exception e) {
            log.error("saveFile ERROR", e);
            throw e;
        }
    }

    public static void deleteFile(String path) throws IOException {
        Path resource = RESOURCES_PATH.resolve(Paths.get(path));
        Files.deleteIfExists(resource);
    }

    public static String joinPaths(String... paths) {
        StringBuilder builder = new StringBuilder();
        int pathsLength = paths.length;
        for (int i = 0; i < pathsLength; i++) {
            String path = paths[i];
            if (i != 0 && path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (!path.isEmpty()) {
                builder.append(path);
                if (i < pathsLength - 1 && !path.endsWith("/")) {
                    builder.append("/");
                }
            }
        }
        return builder.toString();
    }

    public static String getExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public static String removeExtension(String filename) {
        return FilenameUtils.removeExtension(filename);
    }

    public static InputStream fileToInputStream(File file) throws IOException {
        return org.apache.commons.io.FileUtils.openInputStream(file);
    }
}
