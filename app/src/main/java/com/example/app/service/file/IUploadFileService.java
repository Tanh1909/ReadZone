package com.example.app.service.file;

import com.example.app.data.request.UploadMultiFileRequest;
import io.reactivex.rxjava3.core.Single;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUploadFileService {

    Single<String> uploadFile(MultipartFile file);

    Single<List<String>> uploadMultiFile(UploadMultiFileRequest uploadMultiFileRequest);

}
