package kr.co.programmers.collabond.api.file.application;

import kr.co.programmers.collabond.api.file.domain.File;
import kr.co.programmers.collabond.api.file.infrastructure.FileRepository;
import kr.co.programmers.collabond.api.file.interfaces.FileMapper;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.InvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    @Value("${custom.file.path}")
    private String fileDir;

    @Transactional
    public File saveFile(MultipartFile multipartFile) {
        String originFileName = multipartFile.getOriginalFilename();
        String savedFileName = createStoreFileName(originFileName);
        try {
            multipartFile.transferTo(new java.io.File(getFullPath(savedFileName)));
        } catch (IOException e) {
            throw new InvalidException(ErrorCode.INVALID_REQUEST);
        }

        File file = FileMapper.toEntity(originFileName, savedFileName);

        return fileRepository.save(file);
    }

    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    @Transactional
    public List<File> saveFiles(List<MultipartFile> files) {
        ArrayList<File> savedFilesResult = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                savedFilesResult.add(saveFile(file));
            }
        }
        return savedFilesResult;
    }
}
