package vn.tnteco.storage.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import vn.tnteco.storage.constant.FileExtensionEnum;
import vn.tnteco.storage.data.FileEntryDTO;

import java.io.InputStream;
import java.util.List;

@Log4j2
public class SftpStorageServiceImpl implements IStorageService, AutoCloseable {

    @Override
    public List<FileEntryDTO> listFiles(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FileEntryDTO> listFiles(String path, FileExtensionEnum extension) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeDirectory(String dirPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteArrayResource readFile(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readFileAsUrl(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFile(InputStream is, String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFile(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendFiles(String outputFilePath, List<String> filePaths) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveFile(String sourceFilePath, String destinationFilePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
