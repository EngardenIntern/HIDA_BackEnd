package com.ngarden.hida.domain.file;

import com.ngarden.hida.global.error.NoExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class FileServiceImpl implements FileService{
    private final String defaultFilePath = "/DiaryStorage";

    @Override
    public File createOrOpenFileInPath(String userFilePath, String fileName) {
        File directory = new File(defaultFilePath + File.separator + userFilePath);

        //디랙토리 있으면 생성
        if (!directory.exists()) {
            try {

                log.info("폴더 생성 여부 : " + directory.mkdirs() + ", 폴더 경로 : " + directory.getAbsolutePath());
            } catch (Exception e) {
                log.info("폴더를 생성하지 못했습니다." + " 폴더 경로 : " + directory.getAbsolutePath());
                e.getStackTrace();
            }
        }

        String filePath = defaultFilePath + File.separator + userFilePath + File.separator + fileName;

        //파일 없으면 생성 있으면 오픈
        try{
            File file = new File(filePath);
            if (!file.exists()) {
                log.info("파일 생성 여부 : " + file.createNewFile() + ", 파일 이름 : " + file.getAbsolutePath());
            } else {
                log.info("파일 오픈, 파일 이름 : " + file.getAbsolutePath());
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeStringInFile(File file, String content, Boolean append) {
        try (FileWriter writer = new FileWriter(file, append)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readStringInFile(File file){
        try {
            checkFileNotEmpty(file); //파일이 비어있는지 확인
        } catch (NoExistException e) {
            throw new RuntimeException(e);
        }

        StringBuilder content = new StringBuilder();

        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    @Override
    public void deleteFile(File file) {
        String absolutePath = file.getAbsolutePath();

        if (file.exists()) {
            if (file.delete()) {
                log.info("파일 삭제 성공: " + absolutePath);
            } else {
                log.info("파일 삭제 실패: " + absolutePath);
            }
        } else {
            log.info("파일이 존재하지 않음: " + absolutePath);
        }
    }

    @Override
    public void checkFileNotEmpty(File file){
        if (file.length() == 0) {
            throw new NoExistException("File is empty: " + file.toString());
        }
    }
}