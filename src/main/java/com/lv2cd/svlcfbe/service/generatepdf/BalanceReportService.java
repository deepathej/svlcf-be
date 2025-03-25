package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonPdfMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.BalanceListWithDate;
import com.lv2cd.svlcfbe.model.BalanceStmtRecord;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Month;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class BalanceReportService {

  private SvlcfConfig svlcfConfig;

  public String generateBalanceReport(
      Map<String, Integer> balanceMap, List<BalanceListWithDate> balanceListWithDateList) {
    String balancePath = getBalancePath(balanceListWithDateList);
    try (Document balanceDocument = new Document(PageSize.A4)) {
      deleteIfFileExists(balancePath);
      PdfWriter.getInstance(balanceDocument, new FileOutputStream(balancePath));
      balanceDocument.open();
      setHeaderDetails(balanceDocument, svlcfConfig.getPdfOutputRootPath());
      setBalanceReportData(balanceDocument, balanceMap, balanceListWithDateList);
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
    String newBalancePath = removeBlankPagesFromPDF(balancePath);
    log.info(DAILY_BALANCE_REPORT_HEADING + VALUE_IN_LOG, newBalancePath);
    return newBalancePath.replace(svlcfConfig.getPdfOutputRootPath(), SERVER_FILES_PATH);
  }

  private String getBalancePath(List<BalanceListWithDate> balanceListWithDateList) {
    if (balanceListWithDateList.size() == I_ONE
        && balanceListWithDateList.get(I_ZERO).getOutAmountRecords().isEmpty()
        && balanceListWithDateList.get(I_ZERO).getInAmountRecords().isEmpty()) {
      throw new CustomBadRequestException(
          NO_TRANSACTIONS_FOR_THE_DATE + balanceListWithDateList.get(I_ZERO).getDate());
    }
    return balanceListWithDateList.size() == I_ONE
        ? svlcfConfig.getPdfOutputRootPath()
            + BALANCE_PATH
            + balanceListWithDateList.get(I_ZERO).getDate()
            + TEMP
            + PDF_EXTENSION
        : svlcfConfig.getPdfOutputRootPath()
            + BALANCE_PATH
            + Month.of(
                Integer.parseInt(
                    (balanceListWithDateList.get(I_ZERO).getDate().split(HYPHEN))[I_ONE]))
            + TEMP
            + PDF_EXTENSION;
  }

  private void setBalanceReportData(
      Document balanceDocument,
      Map<String, Integer> balanceMap,
      List<BalanceListWithDate> balanceListWithDateList) {
    if (balanceListWithDateList.size() == I_ONE) {
      setBalanceNumberAndDate(balanceDocument, balanceListWithDateList.get(I_ZERO).getDate());
      setPreSummaryDetails(balanceDocument, balanceMap);
      creditAndDebitDetailsForDay(balanceDocument, balanceListWithDateList.get(I_ZERO));
      setPostSummaryDetails(balanceDocument, balanceMap);
    } else {
      balanceListWithDateList.forEach(
          balanceListWithDate -> {
            if (!balanceListWithDate.getOutAmountRecords().isEmpty()
                && !balanceListWithDate.getInAmountRecords().isEmpty()) {
              setBalanceNumberAndDate(balanceDocument, balanceListWithDate.getDate());
              creditAndDebitDetailsForDay(balanceDocument, balanceListWithDate);
              balanceDocument.add(new Paragraph(NEWLINE_STRING));
            }
          });
    }
  }

  private void creditAndDebitDetailsForDay(
      Document balanceDocument, BalanceListWithDate balanceListWithDate) {
    setTableHeading(balanceDocument, AMOUNT_CREDITED_HEADING);
    setTransactionDetailsInBalanceReport(balanceDocument, balanceListWithDate.getInAmountRecords());
    setTableHeading(balanceDocument, AMOUNT_DEBITED_HEADING);
    setTransactionDetailsInBalanceReport(
        balanceDocument, balanceListWithDate.getOutAmountRecords());
  }

  private void setBalanceNumberAndDate(Document balanceDocument, String date) {
    PdfPTable balanceDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_NINETY_EIGHT});
    balanceDataTable.addCell(
        setDetailsInCell(DAILY_BALANCE_REPORT_HEADING + date, F_THIRTEEN, Font.BOLD, false, false));
    balanceDocument.add(balanceDataTable);
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private PdfPCell setDetailsInCellToRight(String font) {
    PdfPCell cell = new PdfPCell(setTextData(font, F_TWELVE, Font.PLAIN));
    cell.setBorder(I_ZERO);
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

  private void setPreSummaryDetails(Document balanceDocument, Map<String, Integer> balanceMap) {
    PdfPTable balanceCustomerTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(
            PREVIOUS_ACCOUNT_BALANCE + COLON_SPACE + balanceMap.get(PREVIOUS_ACCOUNT_BALANCE)));
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(
            PREVIOUS_CASH_BALANCE + COLON_SPACE + balanceMap.get(PREVIOUS_CASH_BALANCE)));
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(
            TOTAL_BALANCE
                + (balanceMap.get(PREVIOUS_ACCOUNT_BALANCE)
                    + balanceMap.get(PREVIOUS_CASH_BALANCE))));
    balanceDocument.add(balanceCustomerTable);
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setPostSummaryDetails(Document balanceDocument, Map<String, Integer> balanceMap) {
    PdfPTable balanceCustomerTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(ACCOUNT_BALANCE + COLON_SPACE + balanceMap.get(ACCOUNT_BALANCE)));
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(CASH_BALANCE + COLON_SPACE + balanceMap.get(CASH_BALANCE)));
    balanceCustomerTable.addCell(
        setDetailsInCellToRight(
            TOTAL_BALANCE + (balanceMap.get(CASH_BALANCE) + balanceMap.get(ACCOUNT_BALANCE))));
    balanceDocument.add(balanceCustomerTable);
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setTransactionDetailsInBalanceReport(
      Document balanceDocument, List<BalanceStmtRecord> amountRecords) {
    PdfPTable productDetailsTable =
        createTable(
            I_FOUR,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_TEN, F_FORTY_FIVE, F_TWENTY_FIVE, F_TWENTY});
    List.of(ID_HEADING, DETAILS_HEADING, PAYMENT_MODE_HEADING, AMOUNT_HEADING)
        .forEach(
            font -> productDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList = List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
    amountRecords.forEach(
        amountRecord -> {
          List<String> list =
              List.of(
                  amountRecord.getUserId() == null
                      ? EMPTY_STRING
                      : amountRecord.getUserId().toString(),
                  amountRecord.getDetails(),
                  amountRecord.getPaymentMode().toString(),
                  amountRecord.getAmount().toString());
          list.forEach(
              font ->
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false)));
          setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
        });
    balanceDocument.add(productDetailsTable);
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
  }
}
