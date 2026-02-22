    package com.smartlogi.smart_city_hub.service;

    import io.minio.*;
    import io.minio.errors.*;
    import io.minio.http.Method;
    import jakarta.annotation.PostConstruct;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.io.InputStream;
    import java.security.InvalidKeyException;
    import java.security.NoSuchAlgorithmException;
    import java.util.UUID;
    import java.util.concurrent.TimeUnit;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class MinioService {

        private final MinioClient minioClient;

        @Value("${minio.bucket}")
        private String bucketName;

        @PostConstruct
        public void init() {
            createBucketIfNotExists();
        }

        public void createBucketIfNotExists() {
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("Bucket '{}' created successfully", bucketName);
                } else {
                    log.info("Bucket '{}' already exists", bucketName);
                }
            } catch (Exception e) {
                log.error("Error checking/creating bucket: {}", e.getMessage());
            }
        }

        public String uploadFile(MultipartFile file, String folder) throws Exception {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String objectName = folder + "/" + UUID.randomUUID() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

                log.info("File uploaded successfully: {}", objectName);
                return objectName;
            }
        }

        public String getPresignedUrl(String objectName) throws Exception {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        }

        public String getPresignedUrlSafe(String objectName) {
            try {
                return getPresignedUrl(objectName);
            } catch (Exception e) {
                log.error("Error generating presigned URL for object: {}", objectName, e);
                return "";
            }
        }

        public String getPublicUrl(String objectName) {
            return String.format("%s/%s/%s",
                    getMinioEndpoint(),
                    bucketName,
                    objectName);
        }

        private String getMinioEndpoint() {
            try {
                java.lang.reflect.Field field = minioClient.getClass().getDeclaredField("baseUrl");
                field.setAccessible(true);
                Object baseUrl = field.get(minioClient);
                return baseUrl.toString().replaceAll("/$", "");
            } catch (Exception e) {
                return "http://localhost:9000";
            }
        }

        public void deleteFile(String objectName) throws Exception {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("File deleted successfully: {}", objectName);
        }

        public InputStream getFile(String objectName) throws Exception {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        }

        public boolean fileExists(String objectName) {
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
