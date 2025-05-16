package vn.tnteco.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import vn.tnteco.storage.config.StorageFileConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageFactory {

    public IStorageService getStorage(StorageFileConfig config) {
        if (ObjectUtils.isEmpty(config)) {
            throw new IllegalArgumentException("Storage File Config required");
        }
        return switch (config.getStorage()) {
            case FTP -> new FtpStorageServiceImpl(config);
            case SFTP -> new SftpStorageServiceImpl();
            case MINIO -> new MinioStorageServiceImpl(config);
        };
    }
}