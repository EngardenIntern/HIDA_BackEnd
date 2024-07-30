package com.ngarden.hida.domain.file;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileServiceImpl implements FileService{
    private final String defaultFilePath = "C:\\Users\\SAMSUNG\\Desktop\\새 폴더\\User";

    /**
     * @param userFilePath 기본파일경로 이후의 경로 String : "최정식userid" or "최정식userid\\최정식Diary"
     * @param fileName 파일이름 String : "최정식_2020112090.txt"
     * @return File 객체 반환
     */
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

    /**
     * @param file File 객체
     * @param content 파일 쓸 내용
     * @param append true: 기존 파일에 이어서 쓰겠다. false: 기존 파일을 덮어쓰겠다.
     */
    @Override
    public void writeStringInFile(File file, String content, Boolean append) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
