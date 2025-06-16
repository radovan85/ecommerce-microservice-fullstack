package com.radovan.play.services.impl;

import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.ProductDto;
import com.radovan.play.dto.ProductImageDto;
import com.radovan.play.entity.ProductImageEntity;
import com.radovan.play.exceptions.FileUploadException;
import com.radovan.play.repositories.ProductImageRepository;
import com.radovan.play.services.ProductImageService;
import com.radovan.play.services.ProductService;
import com.radovan.play.utils.FileValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ProductImageServiceImpl implements ProductImageService {

    private ProductImageRepository imageRepository;
    private ProductService productService;
    private TempConverter tempConverter;
    private FileValidator fileValidator;

    @Inject
    private void initialize(ProductImageRepository imageRepository, ProductService productService,
                                   TempConverter tempConverter, FileValidator fileValidator) {
        this.imageRepository = imageRepository;
        this.productService = productService;
        this.tempConverter = tempConverter;
        this.fileValidator = fileValidator;
    }

    @Override
    public ProductImageDto addImage(play.mvc.Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> file, Integer productId) {
        ProductDto product = productService.getProductById(productId);
        fileValidator.validateFile(file);
        Optional<ProductImageEntity> imageOptional = imageRepository.findByProductId(productId);
        imageOptional.ifPresent(productImageEntity -> deleteImage(productImageEntity.getId()));

        try {
            ProductImageDto image = new ProductImageDto();
            image.setProductId(productId);
            image.setName(Objects.requireNonNull(file.getFilename()));
            image.setContentType(file.getContentType());
            image.setSize(file.getFileSize());

            // Read file content as byte array using path() method
            Path path = file.getRef().path();
            byte[] fileData = Files.readAllBytes(path);
            image.setData(fileData);

            Optional<Integer> imageIdOptional = Optional.ofNullable(product.getImageId());
            imageIdOptional.ifPresent(image::setId);

            ProductImageEntity imageEntity = tempConverter.productImageDtoToEntity(image);
            ProductImageEntity storedImage = imageRepository.save(imageEntity);

            return tempConverter.productImageEntityToDto(storedImage);
        } catch (Exception e) {
            throw new FileUploadException(e.getMessage());
        }
    }

    @Override
    public List<ProductImageDto> listAll() {
        List<ProductImageEntity> allImages = imageRepository.listAll();
        return allImages.stream().map(tempConverter::productImageEntityToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteImage(Integer imageId) {
        imageRepository.deleteById(imageId);
    }
}
