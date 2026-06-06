package com.example.hospitalManagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service

public class R2Service {
    private final S3Client s3Client;

    public R2Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;
    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = "avatar/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).contentType(file.getContentType()).build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        return publicUrl + "/" + fileName;
    }


}
