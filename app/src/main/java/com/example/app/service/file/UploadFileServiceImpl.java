package com.example.app.service.file;

import com.cloudinary.Cloudinary;
import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.request.UploadMultiFileRequest;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.tnteco.common.core.exception.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Service
@RequiredArgsConstructor
public class UploadFileServiceImpl implements IUploadFileService {

    private final Cloudinary cloudinary;

    @Override
    public Single<String> uploadFile(MultipartFile file) {
        return rxSchedulerIo(() -> {
            try {
                Map<?, ?> data = cloudinary.uploader().upload(file.getBytes(), Map.of());
                return (String) data.get("url");
            } catch (IOException e) {
                throw new ApiException(AppErrorResponse.UPLOAD_FILE_FAIL);
            }
        });
    }

    @Override
    public Single<List<String>> uploadMultiFile(UploadMultiFileRequest multiFileRequest) {
        return rxSchedulerIo(() -> {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : multiFileRequest.getFiles()) {
                try {
                    Map<?, ?> data = cloudinary.uploader().upload(file.getBytes(), Map.of());
                    urls.add((String) data.get("url"));
                } catch (IOException e) {
                    throw new ApiException(AppErrorResponse.UPLOAD_FILE_FAIL);
                }
            }
            return urls;
        });
    }

}
