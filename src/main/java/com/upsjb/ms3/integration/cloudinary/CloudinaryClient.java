// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryClient.java
package com.upsjb.ms3.integration.cloudinary;

public interface CloudinaryClient {

    CloudinaryUploadResponse upload(CloudinaryUploadRequest request);

    CloudinaryDeleteResponse delete(CloudinaryDeleteRequest request);
}