package com.ngarden.hida.domain.file;

import com.ngarden.hida.global.error.NoExistException;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileServiceImpl implements FileService{
    private final String defaultFilePath = "C:\\Users\\SAMSUNG\\Desktop\\새 폴더\\User";

    @Override
    public File createOrOpenFileInPath(String userFilePath, String fileName) {
        File directory = new File(defaultFilePath + "\\" + userFilePath);

        //디랙토리 있으면 생성
        if (!directory.exists()) {
            try {
                System.out.println(userFilePath + "폴더 생성 여부 : " + directory.mkdirs());
            } catch (Exception e) {
                System.out.println(userFilePath + "폴더를 생성하지 못했습니다.");
                e.getStackTrace();
            }
        }

        String filePath = defaultFilePath + "\\" + userFilePath + "\\" + fileName;

        //파일 없으면 생성 있으면 오픈
        try{
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println(fileName + "파일 생성 여부 : " + file.createNewFile());
            } else {
                System.out.println(fileName + "파일 오픈");
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
    public void checkFileNotEmpty(File file){
        if (file.length() == 0) {
            throw new NoExistException("File is empty: " + file.toString());
        }
    }
}