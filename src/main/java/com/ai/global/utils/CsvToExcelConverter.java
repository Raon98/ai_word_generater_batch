package com.ai.global.utils;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvToExcelConverter {
    /**
    CSV 파일을 읽어 엑셀(.xlsx) 파일로 변환하여 저장합니다.
    @return 변환된 엑셀 파일의 절대 경로
     */
    public static String convertCsvToExcel(String csvFilePath) {
        String excelFilePath = csvFilePath.replace(".csv", ".xlsx");
        if (csvFilePath.equals(excelFilePath)) {
            excelFilePath = csvFilePath + ".xlsx";
        }
        try {
           
            List<AnalysisRequestDto> data = readCsvData(csvFilePath, Charset.forName("UTF-8"));
            if (data.isEmpty()) {
                 data = readCsvData(csvFilePath, StandardCharsets.UTF_8);
            }
            EasyExcel.write(excelFilePath, AnalysisRequestDto.class)
                    .sheet("ConvertedData")
                    .doWrite(data);
            log.info("CSV -> Excel 변환 성공: {}", excelFilePath);
            return excelFilePath;
        } catch (Exception e) {
            log.error("CSV 변환 실패", e);
            throw new RuntimeException("CSV를 엑셀로 변환하는 중 오류 발생: " + e.getMessage());
        }
    }
    private static List<AnalysisRequestDto> readCsvData(String path, Charset encoding) {
        List<AnalysisRequestDto> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                // 헤더 건너뛰기
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
               
                String[] columns = line.split(",", -1);

                if (columns.length >= 1) {
                    AnalysisRequestDto dto = new AnalysisRequestDto();
                    dto.setExpression(columns.length > 0 ? columns[0] : "");
                    dto.setReading(columns.length > 1 ? columns[1] : "");
                    dto.setOriginalMeaning(columns.length > 2 ? columns[2] : "");
                    dto.setTags(columns.length > 3 ? columns[3] : "");
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            log.warn("{} 인코딩으로 읽기 시도 중 에러 (무시하고 빈 리스트 리턴)", encoding);
            return new ArrayList<>();
        }
        return list;
    }
}