package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonPdfMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.SaleDetailForDay;
import com.lv2cd.svlcfbe.model.SalesListWithDate;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Month;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SalesReportService {

  private SvlcfConfig svlcfConfig;

  public String generateSalesReport(List<SalesListWithDate> salesListWithDateList) {
    String salesPath = getSalesPath(salesListWithDateList);
    try (Document salesDocument = new Document(PageSize.A4)) {
      deleteIfFileExists(salesPath);
      PdfWriter.getInstance(salesDocument, new FileOutputStream(salesPath));
      salesDocument.open();
      setHeaderDetails(salesDocument, svlcfConfig.getPdfOutputRootPath());
      salesListWithDateList.forEach(
          salesListWithDate ->
              setSalesReportData(
                  salesDocument,
                  salesListWithDate.getSaleDetailForDayList(),
                  salesListWithDate.getDate()));
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
    String newSalesPath = removeBlankPagesFromPDF(salesPath);
    log.info(SALES_REPORT + VALUE_IN_LOG, newSalesPath);
    return newSalesPath.replace(svlcfConfig.getPdfOutputRootPath(), SERVER_FILES_PATH);
  }

  private String getSalesPath(List<SalesListWithDate> salesListWithDateList) {
    if (salesListWithDateList.size() == I_ONE
        && salesListWithDateList.get(I_ZERO).getSaleDetailForDayList().isEmpty()) {
      throw new CustomBadRequestException(
          NO_RECORDS_FOR_THE_REPORT + SALES_REPORT + salesListWithDateList.get(I_ZERO).getDate());
    }
    return salesListWithDateList.size() == I_ONE
        ? svlcfConfig.getPdfOutputRootPath()
            + SALES_REPORT_PATH
            + salesListWithDateList.get(I_ZERO).getDate()
            + TEMP
            + PDF_EXTENSION
        : svlcfConfig.getPdfOutputRootPath()
            + SALES_REPORT_PATH
            + Month.of(
                Integer.parseInt(
                    (salesListWithDateList.get(I_ZERO).getDate().split(HYPHEN))[I_ONE]))
            + TEMP
            + PDF_EXTENSION;
  }

  private void setSalesReportData(
      Document salesDocument, List<SaleDetailForDay> saleDetailForDay, String date) {
    if (!saleDetailForDay.isEmpty()) {
      setSaleReportTextAndDate(salesDocument, date);
      setDetailsInSalesReport(salesDocument, saleDetailForDay);
      salesDocument.add(new Paragraph(NEWLINE_STRING));
    }
  }

  private void setSaleReportTextAndDate(Document salesDocument, String date) {
    PdfPTable salesDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_NINETY_EIGHT});
    salesDataTable.addCell(
        setDetailsInCell(DAILY_SALES_REPORT_HEADING + date, F_THIRTEEN, Font.BOLD, false, false));
    salesDocument.add(salesDataTable);
    salesDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setDetailsInSalesReport(
      Document salesDocument, List<SaleDetailForDay> saleDetailForDay) {
    PdfPTable saleDetailsTable =
        createTable(
            I_FOUR,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_TEN, F_TWENTY_FIVE, F_TEN, F_FIFTY_FIVE});
    List.of(INVOICE_HEADING, CUSTOMER_HEADING, AMOUNT_HEADING, ITEM_HEADING)
        .forEach(font -> saleDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TEN)));
    List<String> emptyRowList = List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    saleDetailForDay.forEach(
        saleDetail -> {
          List<String> list =
              List.of(
                  saleDetail.getInvoiceNumber().toString(),
                  saleDetail.getUserId().toString() + UNDER_SCORE + saleDetail.getName(),
                  saleDetail.getSaleAmount().toString(),
                  saleDetail.getItemDetails());
          list.forEach(
              font ->
                  saleDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false)));
          setEmptyCells(emptyRowList, saleDetailsTable, F_FOUR);
        });
    salesDocument.add(saleDetailsTable);
    salesDocument.add(new Paragraph(NEWLINE_STRING));
    salesDocument.add(new Paragraph(NEWLINE_STRING));
  }
}
