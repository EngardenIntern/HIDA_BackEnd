package com.ngarden.hida.domain.file;

import java.io.File;

public interface FileService {
    /**
     * @param path 기본파일경로 이후의 경로 String : "id\\diary" or "id\\summary"
     * @param fileName 파일이름 String : "2012-02-12.json" or "02.json"
     * @return File 객체 반환
     */
    File createOrOpenFileInPath(String path, String fileName);

    /**
     * @param file File 객체
     * @param content 파일 쓸 내용
     * @param append true: 기존 파일에 이어서 쓰겠다. false: 기존 파일을 덮어쓰겠다.
     */
    void writeStringInFile(File file, String content, Boolean append);

    String readStringInFile(File file);

    void deleteFile(File file);

    void checkFileNotEmpty(File file);
}
