package com.radovan.play.utils;

import java.util.Optional;

import jakarta.inject.Singleton;
import org.apache.commons.io.FilenameUtils;
import play.mvc.Http.MultipartFormData.FilePart;

import com.radovan.play.exceptions.DataNotValidatedException;

@Singleton
public class FileValidator {

    public Boolean validateFile(FilePart<play.libs.Files.TemporaryFile> file) {

        Boolean returnValue = false;
        String extension = FilenameUtils.getExtension(file.getFilename());

        if (isSupportedExtension(extension)) {
            returnValue = true;
        } else {
            throw new DataNotValidatedException("The file is not valid!");
        }

        return returnValue;
    }

    private Boolean isSupportedExtension(String extension) {
        Boolean returnValue = false;
        Optional<String> extensionOptional = Optional.ofNullable(extension);
        if (extensionOptional.isPresent()) {
            if (extension.equals("png") || extension.equals("jpeg") || extension.equals("jpg")) {
                returnValue = true;
            }
        }
        return returnValue;
    }
}
