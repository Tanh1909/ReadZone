package vn.tnteco.storage.service;

import com.google.common.io.ByteStreams;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import vn.tnteco.storage.config.StorageFileConfig;
import vn.tnteco.storage.constant.FileExtensionEnum;
import vn.tnteco.storage.data.FileEntryDTO;
import vn.tnteco.storage.util.FileUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static vn.tnteco.storage.util.FileUtils.FORWARD_SLASH;

@Log4j2
@SuppressWarnings({"java:S112"})
public class MinioStorageServiceImpl implements IStorageService, AutoCloseable {

    private MinioClient minioClient;

    private final String bucketName;

    private final Integer expireTime;

    @SneakyThrows
    public MinioStorageServiceImpl(StorageFileConfig config) {
        String url = config.getUrl();
        String accessKey = config.getUsername();
        String secretKey = config.getPassword();
        this.bucketName = config.getMinioBucketName();
        this.expireTime = config.getUrlExpireTime();
        Objects.requireNonNull(url, "minio url not empty");
        Objects.requireNonNull(accessKey, "minio access key not empty");
        Objects.requireNonNull(secretKey, "minio secret key not empty");
        Objects.requireNonNull(this.bucketName, "minio bucket name not empty");
        try {
            minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();
            minioClient.setTimeout(30000, 60000, 60000);
            minioClient.ignoreCertCheck();
        } catch (Exception e) {
            log.error("Connect MinIO have exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<FileEntryDTO> listFiles(String path) {
        Objects.requireNonNull(path, "Directory path must not be null");
        if (!path.endsWith(FORWARD_SLASH)) {
            path += FORWARD_SLASH;
        }
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(path)
                .build());
        return StreamSupport.stream(results.spliterator(), false)
                .map(result -> {
                    try {
                        Item item = result.get();
                        if (item.isDir()) return null;
                        return FileEntryDTO.builder()
                                .filePath(item.objectName())
                                .fileName(FileUtils.getFilename(item.objectName()))
                                .lastModifiedDate(item.lastModified().toLocalDateTime())
                                .build();
                    } catch (Exception e) {
                        log.error("[MinIO] Error fetching item: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<FileEntryDTO> listFiles(String path, FileExtensionEnum extension) {
        return this.listFiles(path).stream()
                .filter(dto -> dto.getFileName().endsWith(extension.value()))
                .toList();
    }

    @Override
    @SneakyThrows
    public void makeDirectory(String dirPath) {
        Objects.requireNonNull(dirPath, "Directory path ust not be null");
        if (!dirPath.endsWith(FORWARD_SLASH)) {
            dirPath += FORWARD_SLASH;
        }
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(dirPath)
                    .stream(InputStream.nullInputStream(), 0, -1)
                    .build());
            log.debug("[MinIO] Directory {} create successfully.", dirPath);
        } catch (Exception e) {
            log.error("[MinIO] Directory {} create error: {}", dirPath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public ByteArrayResource readFile(String filePath) {
        Objects.requireNonNull(filePath, "File path must not be null");
        try {
            InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            byte[] content = ByteStreams.toByteArray(is);
            is.close();
            log.info("File {} read successfully.", filePath);
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return FileUtils.getFilename(filePath);
                }
            };
        } catch (Exception e) {
            log.error("[MinIO] File {} read error: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public String readFileAsUrl(String filePath) {
        Objects.requireNonNull(filePath, "File path must not be null");
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filePath)
                            .expiry(this.expireTime, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("[MinIO] File {} read error: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public void writeFile(InputStream is, String filePath) {
        Objects.requireNonNull(is, "Input stream must not be null");
        Objects.requireNonNull(filePath, "File path must not be null");
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(is, is.available(), 0L)
                    .build());
            log.debug("[MinIO] File {} upload successfully.", filePath);
        } catch (Exception e) {
            log.error("[MinIO] File {} upload error: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public void deleteFile(String filePath) {
        Objects.requireNonNull(filePath, "File path must not be null");
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            log.debug("[MinIO] File {} delete successfully.", filePath);
        } catch (Exception e) {
            log.error("[MinIO] File {} delete error: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public void appendFiles(String outputFilePath, List<String> filePaths) {
        Objects.requireNonNull(outputFilePath, "Output file path must not be null");
        Objects.requireNonNull(filePaths, "File path must not be null");
        try {
            List<ComposeSource> composeSources = new ArrayList<>();
            filePaths.forEach(filePath -> composeSources.add(ComposeSource.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build()));
            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucketName)
                    .object(outputFilePath)
                    .sources(composeSources)
                    .build());
        } catch (Exception e) {
            log.error("[MinIO] Append files error: {}", e.getMessage());
            throw e;
        }

    }

    @Override
    @SneakyThrows
    public void moveFile(String sourceFilePath, String destinationFilePath) {
        Objects.requireNonNull(sourceFilePath, "Source file path must not be null");
        Objects.requireNonNull(destinationFilePath, "Destination file path must not be null");
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(destinationFilePath)
                .source(CopySource.builder()
                        .bucket(bucketName)
                        .object(sourceFilePath)
                        .build())
                .build());
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sourceFilePath)
                    .build());
        } catch (Exception e) {
            log.error("[MinIO] Move to delete file error: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        if (this.minioClient != null) {
            this.minioClient = null;
        }
    }
}
