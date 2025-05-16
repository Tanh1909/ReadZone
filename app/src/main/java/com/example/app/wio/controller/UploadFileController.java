package com.example.app.wio.controller;

import com.example.app.data.request.UploadMultiFileRequest;
import com.example.app.service.file.IUploadFileService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.tnteco.spring.model.DfResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class UploadFileController {

    private final IUploadFileService uploadFileService;

    @PostMapping("/upload")
    public Single<DfResponse<String>> upload(@Valid @NotNull MultipartFile file) {
        return uploadFileService.uploadFile(file).map(DfResponse::ok);
    }

    @PostMapping("/multi-upload")
    public Single<DfResponse<List<String>>> uploadMulti(@Valid UploadMultiFileRequest request) {
        return uploadFileService.uploadMultiFile(request).map(DfResponse::ok);
    }

}
