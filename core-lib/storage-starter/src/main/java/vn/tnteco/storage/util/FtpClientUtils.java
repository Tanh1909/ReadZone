package vn.tnteco.storage.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
@UtilityClass
public class FtpClientUtils {

    /**
     * Determines whether a directory exists or not
     *
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    public static boolean checkDirectoryExists(FTPClient ftpClient, String dirPath) throws IOException {
        ftpClient.changeWorkingDirectory(dirPath);
        int returnCode = ftpClient.getReplyCode();
        return returnCode != 550;
    }

    /**
     * Determines whether a file exists or not
     *
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    public static boolean checkFileExists(FTPClient ftpClient, String filePath) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(filePath);
        int returnCode = ftpClient.getReplyCode();
        return inputStream != null && returnCode != 550;
    }

    /**
     * Creates a nested directory structure on a FTP server
     *
     * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param dirPath   Path of the directory, i.e /projects/java/ftp/demo
     * @return true if the directory was created successfully, false otherwise
     * @throws IOException if any error occurred during client-server communication
     */
    public static boolean makeDirectories(FTPClient ftpClient, String dirPath)
            throws IOException {
        String[] pathElements = dirPath.split("/");
        StringBuilder newDir = new StringBuilder();
        for (String singleDir : pathElements) {
            if (singleDir.isEmpty()) continue;
            newDir.append("/").append(singleDir);
            ftpClient.makeDirectory(newDir.toString());
        }
        return true;
    }
}
