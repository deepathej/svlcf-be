package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonPdfMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class StockReportService {

  private SvlcfConfig svlcfConfig;

  public String generateStockReport(
      String date,
      Map<String, Integer> previousStock,
      Map<String, Integer> inStockRecords,
      Map<String, Integer> outStockRecords,
      Map<String, Integer> currentStock) {
    String stockPath =
        svlcfConfig.getPdfOutputRootPath() + STOCK_PATH + date + TEMP + PDF_EXTENSION;
    try (Document stockDocument = new Document(PageSize.A4)) {
      deleteIfFileExists(stockPath);
      PdfWriter.getInstance(stockDocument, new FileOutputStream(stockPath));
      stockDocument.open();
      setHeaderDetails(stockDocument, svlcfConfig.getPdfOutputRootPath());
      setStockReportData(
          stockDocument, previousStock, inStockRecords, outStockRecords, currentStock, date);
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
    String newStockPath = removeBlankPagesFromPDF(stockPath);
    log.info(STOCK_REPORT + VALUE_IN_LOG, newStockPath);
    return newStockPath.replace(svlcfConfig.getPdfOutputRootPath(), SERVER_FILES_PATH);
  }

  private void setStockReportData(
      Document stockDocument,
      Map<String, Integer> previousStock,
      Map<String, Integer> inStockRecords,
      Map<String, Integer> outStockRecords,
      Map<String, Integer> currentStock,
      String date) {
    setStockNumberAndDate(stockDocument, date);
    setTableHeading(stockDocument, STOCK_SUMMARY_HEADING);
    setStockSummary(stockDocument, previousStock, currentStock, inStockRecords, outStockRecords);
  }

  private void setStockNumberAndDate(Document stockDocument, String date) {
    PdfPTable stockDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_NINETY_EIGHT});
    stockDataTable.addCell(
        setDetailsInCell(DAILY_STOCK_REPORT_HEADING + date, F_THIRTEEN, Font.BOLD, false, false));
    stockDocument.add(stockDataTable);
    stockDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setStockSummary(
      Document stockDocument,
      Map<String, Integer> previousStock,
      Map<String, Integer> currentStock,
      Map<String, Integer> inStockRecords,
      Map<String, Integer> outStockRecords) {
    List<String> stockList = new ArrayList<>(currentStock.keySet().stream().sorted().toList());
    // this remove and add is done to move the value to end
    stockList.remove(TOTAL_STOCK);
    stockList.add(TOTAL_STOCK);
    PdfPTable productDetailsTable =
        createTable(
            I_FIVE,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_FORTY_TWO, F_FIFTEEN, F_FOURTEEN, F_FOURTEEN, F_FIFTEEN});
    List.of(
            ITEM_HEADING,
            PREVIOUS_STOCK_HEADING,
            IN_STOCK_HEADING,
            OUT_STOCK_HEADING,
            CURRENT_STOCK_HEADING)
        .forEach(
            font -> productDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList =
        List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
    stockList.forEach(
        stockName -> {
          List<String> list =
              List.of(
                  stockName,
                  makeZeroIfNull(previousStock.get(stockName)).toString(),
                  makeZeroIfNull(inStockRecords.get(stockName)).toString(),
                  makeZeroIfNull(outStockRecords.get(stockName)).toString(),
                  makeZeroIfNull(currentStock.get(stockName)).toString());
          list.forEach(
              font -> {
                if (font.equalsIgnoreCase(stockName)) {
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, true, false));
                } else {
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false));
                }
              });
          setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
        });
    stockDocument.add(productDetailsTable);
    stockDocument.add(new Paragraph(NEWLINE_STRING));
    stockDocument.add(new Paragraph(NEWLINE_STRING));
  }
}
