package com.radovan.play.services;

import com.radovan.play.dto.ProductImageDto;

import java.util.List;

public interface ProductImageService {

    void deleteImage(Integer imageId);

    ProductImageDto addImage(play.mvc.Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> file, Integer productId);

    List<ProductImageDto> listAll();
}
