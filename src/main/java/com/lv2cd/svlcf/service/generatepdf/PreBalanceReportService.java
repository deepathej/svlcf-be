package com.lv2cd.svlcf.service.generatepdf;

import static com.lv2cd.svlcf.util.CommonPdfMethods.*;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcf.config.SvlcfConfig;
import com.lv2cd.svlcf.enums.UserType;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PreBalanceReportService {

  private SvlcfConfig svlcfConfig;

  public String generatePreBalanceReport(
      Map<String, String> userDetailsAndPreBalanceMap, UserType userType) {
    String preBalancePath =
        svlcfConfig.getPdfOutputRootPath() + BALANCE_PATH + userType + TEMP + PDF_EXTENSION;
    try (Document preBalanceDocument = new Document(PageSize.A4)) {
      deleteIfFileExists(preBalancePath);
      PdfWriter.getInstance(preBalanceDocument, new FileOutputStream(preBalancePath));
      preBalanceDocument.open();
      setHeaderDetails(preBalanceDocument, svlcfConfig.getPdfOutputRootPath());
      setPreBalanceReportData(preBalanceDocument, userDetailsAndPreBalanceMap, userType);
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
    String newPreBalancePath = removeBlankPagesFromPDF(preBalancePath);
    log.info(PREVIOUS_YEAR_BALANCE + HYPHEN_WITH_SPACE + VALUE_IN_LOG, newPreBalancePath);
    return newPreBalancePath;
  }

  private void setPreBalanceReportData(
      Document preBalanceDocument,
      Map<String, String> userDetailsAndPreBalanceMap,
      UserType userType) {
    setPreBalanceText(preBalanceDocument, userType);
    setPreBalanceDetails(preBalanceDocument, userDetailsAndPreBalanceMap);
  }

  private void setPreBalanceText(Document preBalanceDocument, UserType userType) {
    PdfPTable balanceDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_NINETY_EIGHT});
    balanceDataTable.addCell(
        setDetailsInCell(
            PRE_BALANCE_REPORT_HEADING + userType, F_THIRTEEN, Font.BOLD, false, false));
    preBalanceDocument.add(balanceDataTable);
    preBalanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setPreBalanceDetails(
      Document preBalanceDocument, Map<String, String> userDetailsAndPreBalanceMap) {
    PdfPTable preBalanceTable =
        createTable(I_TWO, I_ZERO, F_NINETY_EIGHT, new float[] {F_FIFTY, F_FIFTY});
    List.of(USER_HEADING, BALANCE_HEADING)
        .forEach(font -> preBalanceTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList = List.of(EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, preBalanceTable, F_TWO);
    userDetailsAndPreBalanceMap.forEach(
        (key, value) -> {
          setDataInPreBalanceTable(preBalanceTable, key);
          setDataInPreBalanceTable(preBalanceTable, value);
          setEmptyCells(emptyRowList, preBalanceTable, F_TWO);
        });
    preBalanceDocument.add(preBalanceTable);
    preBalanceDocument.add(new Paragraph(NEWLINE_STRING));
    preBalanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setDataInPreBalanceTable(PdfPTable preBalanceTable, String font) {
    preBalanceTable.addCell(setDetailsInCell(font, F_TWELVE, Font.PLAIN, true, false));
  }
}
