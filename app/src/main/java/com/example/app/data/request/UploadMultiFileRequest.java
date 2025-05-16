package com.example.app.data.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UploadMultiFileRequest {

    private List<MultipartFile> files;

}
