package com.lv2cd.svlcf.service.generatepdf;

import static com.lv2cd.svlcf.enums.PaymentMode.DISCOUNT;
import static com.lv2cd.svlcf.util.CommonPdfMethods.*;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcf.config.SvlcfConfig;
import com.lv2cd.svlcf.entity.Payment;
import com.lv2cd.svlcf.entity.PreBalance;
import com.lv2cd.svlcf.entity.Product;
import com.lv2cd.svlcf.entity.User;
import com.lv2cd.svlcf.exception.CustomBadRequestException;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserReportService {

  private SvlcfConfig svlcfConfig;

  public String generateUserReport(
      User user,
      List<Product> productList,
      List<Payment> paymentList,
      PreBalance preBalance,
      boolean isPartOfMultipleCalls) {
    if (productList.isEmpty() && paymentList.isEmpty() && preBalance == null) {
      String errorMessage = NO_TRANSACTIONS_FOR_THE_USER + user.getId();
      if (isPartOfMultipleCalls) {
        return errorMessage;
      }
      throw new CustomBadRequestException(errorMessage);
    }
    String userReportPath =
        svlcfConfig.getPdfOutputRootPath() + USER_REPORT_PATH + user.getId() + TEMP + PDF_EXTENSION;
    try (Document userDocument = new Document(PageSize.A4)) {
      deleteIfFileExists(userReportPath);
      PdfWriter.getInstance(userDocument, new FileOutputStream(userReportPath));
      userDocument.open();
      setHeaderDetails(userDocument, svlcfConfig.getPdfOutputRootPath());
      setUserReportData(userDocument, user, productList, paymentList, preBalance);
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
    String newUserReportPath = removeBlankPagesFromPDF(userReportPath);
    if (!isPartOfMultipleCalls) {
      log.info(USER_REPORT_HEADING + HYPHEN_WITH_SPACE + VALUE_IN_LOG, newUserReportPath);
    }
    return newUserReportPath.replace(svlcfConfig.getPdfOutputRootPath(), SERVER_FILES_PATH);
  }

  private void setUserReportData(
      Document userDocument,
      User user,
      List<Product> productList,
      List<Payment> paymentList,
      PreBalance preBalance) {
    setUserNumberAndDate(userDocument);
    setCustomerDetailsInInvoice(userDocument, user);
    setTableHeading(userDocument, SALES_HEADING);
    setProductDetailsInUser(userDocument, productList);
    setTableHeading(userDocument, PAYMENT_HEADING);
    setPaymentDetailsInUser(userDocument, paymentList);
    List<Integer> balances = validateUserBalance(user, productList, paymentList, preBalance);
    setPostSummaryDetails(userDocument, balances);
  }

  private void setPostSummaryDetails(Document userDocument, List<Integer> balances) {
    PdfPTable userSummaryTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    userSummaryTable.addCell(
        setDetailsInCell(
            PREVIOUS_YEAR_BALANCE + COLON_SPACE + balances.get(0).toString(),
            F_TWELVE,
            Font.PLAIN,
            false,
            true));
    userSummaryTable.addCell(
        setDetailsInCell(
            PURCHASE_AMOUNT + balances.get(1).toString(), F_TWELVE, Font.PLAIN, false, true));
    userSummaryTable.addCell(
        setDetailsInCell(
            PAYMENT_AMOUNT + balances.get(2).toString(), F_TWELVE, Font.PLAIN, false, true));
    userSummaryTable.addCell(
        setDetailsInCell(
            TOTAL_DISCOUNT + balances.get(3).toString(), F_TWELVE, Font.PLAIN, false, true));
    userSummaryTable.addCell(
        setDetailsInCell(
            TOTAL_BALANCE + balances.get(4).toString(), F_TWELVE, Font.PLAIN, false, true));
    userDocument.add(userSummaryTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private List<Integer> validateUserBalance(
      User user, List<Product> productList, List<Payment> paymentList, PreBalance preBalance) {
    int purchaseAmount =
        productList.stream().map(Product::getAmount).mapToInt(Integer::intValue).sum();
    int paymentAmount =
        paymentList.stream()
            .filter(payment -> payment.getPaymentMode() != DISCOUNT)
            .map(Payment::getPaymentAmount)
            .mapToInt(Integer::intValue)
            .sum();
    int previousBalance = (preBalance == null) ? 0 : preBalance.getBalanceAmount();
    int discountAmount =
        paymentList.stream()
            .filter(payment -> payment.getPaymentMode() == DISCOUNT)
            .map(Payment::getPaymentAmount)
            .mapToInt(Integer::intValue)
            .sum();
    if (user.getBalance() != (previousBalance + purchaseAmount - paymentAmount - discountAmount)) {
      log.error(
          user.getId()
              + HYPHEN_WITH_SPACE
              + user.getUserType()
              + HYPHEN_WITH_SPACE
              + user.getName()
              + HYPHEN_WITH_SPACE
              + user.getBalance()
              + HYPHEN_WITH_SPACE
              + previousBalance
              + HYPHEN_WITH_SPACE
              + purchaseAmount
              + HYPHEN_WITH_SPACE
              + paymentAmount
              + HYPHEN_WITH_SPACE
              + discountAmount);
      throw new CustomInternalServerException(DB_DATA_ISSUE + USER_BALANCE_MISMATCH);
    }
    return List.of(
        previousBalance, purchaseAmount, paymentAmount, discountAmount, user.getBalance());
  }

  private void setCustomerDetailsInInvoice(Document userDocument, User user) {
    setCustomerDetailsText(userDocument);
    setCustomerDetails(userDocument, user);
  }

  private void setCustomerDetailsText(Document userDocument) {
    PdfPTable userTable = createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    userTable.addCell(
        setDetailsInCell(CUSTOMER_DETAILS_HEADER, F_FOURTEEN, Font.BOLD, false, false));
    userDocument.add(userTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setCustomerDetails(Document userDocument, User user) {
    PdfPTable userTable = createTable(I_ONE, I_THIRTY, F_NINETY, new float[] {F_HUNDRED});
    userTable.addCell(
        setDetailsInCell(
            user.getName()
                + HYPHEN_WITH_SPACE
                + user.getAddress()
                + HYPHEN_WITH_SPACE
                + user.getPhoneNumber(),
            F_TWELVE,
            Font.PLAIN,
            false,
            false));
    userTable.addCell(
        setDetailsInCell(GSTIN + user.getGstin(), F_TWELVE, Font.PLAIN, false, false));
    userDocument.add(userTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setUserNumberAndDate(Document userDocument) {
    PdfPTable userReportDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_NINETY_EIGHT});
    userReportDataTable.addCell(
        setDetailsInCell(USER_REPORT_HEADING, F_THIRTEEN, Font.BOLD, false, false));
    userDocument.add(userReportDataTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setProductDetailsInUser(Document userDocument, List<Product> productList) {
    PdfPTable productDetailsTable =
        createTable(
            I_SIX,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_TWELVE, F_SEVENTEEN, F_TWENTY_FIVE, F_TWENTY, F_TWELVE, F_FOURTEEN});
    List.of(
            INVOICE_HEADING,
            DATE_HEADING,
            ITEM_HEADING,
            AMOUNT_HEADING,
            VARIANT_HEADING,
            QUANTITY_HEADING)
        .forEach(
            font -> productDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList =
        List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
    productList.forEach(
        product -> {
          List<String> list =
              List.of(
                  product.getInvoiceNumber().toString(),
                  product.getDate(),
                  product.getItem(),
                  product.getAmount().toString(),
                  product.getVariant().toString(),
                  product.getQuantity().toString());
          list.forEach(
              font ->
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false)));
          setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
        });
    userDocument.add(productDetailsTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setPaymentDetailsInUser(Document userDocument, List<Payment> paymentList) {
    PdfPTable productDetailsTable =
        createTable(
            I_FIVE,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_TEN, F_TWENTY_FIVE, F_TWENTY_FIVE, F_TWENTY, F_TWENTY});
    List.of(ID_HEADING, DATE_HEADING, TIME_HEADING, AMOUNT_HEADING, PAYMENT_MODE_HEADING)
        .forEach(
            font -> productDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList =
        List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
    paymentList.forEach(
        payment -> {
          List<String> list =
              List.of(
                  payment.getId().toString(),
                  payment.getDate(),
                  payment.getTime(),
                  payment.getPaymentAmount().toString(),
                  payment.getPaymentMode().toString());
          list.forEach(
              font ->
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false)));
          setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
        });
    userDocument.add(productDetailsTable);
    userDocument.add(new Paragraph(NEWLINE_STRING));
    userDocument.add(new Paragraph(NEWLINE_STRING));
  }
}
