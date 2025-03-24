package com.lv2cd.svlcf.service.generatepdf;

import static com.lv2cd.svlcf.util.CommonPdfMethods.*;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lv2cd.svlcf.config.SvlcfConfig;
import com.lv2cd.svlcf.entity.Stock;
import com.lv2cd.svlcf.entity.User;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class InvoiceService {

  private static final String BANK_DETAILS =
      "BANK DETAILS: INDIAN BANK, 6553392119, IDIB000C065, KongareddyPalli";

  private SvlcfConfig svlcfConfig;

  public String generateInvoice(
      List<Stock> productsList, Long invoiceNumber, User user, String... dateArray) {
    Pair<String, String> dateAndPath =
        getDateAndPath(svlcfConfig.getPdfOutputRootPath(), invoiceNumber.toString(), dateArray);
    String invoicePath = dateAndPath.getSecond();
    try (Document invoiceDocument = new Document(PageSize.A4)) {
      PdfWriter.getInstance(invoiceDocument, new FileOutputStream(invoicePath));
      invoiceDocument.open();
      setHeaderDetails(invoiceDocument, svlcfConfig.getPdfOutputRootPath());
      setInvoiceData(invoiceDocument, productsList, invoiceNumber, user, dateAndPath.getFirst());
      setSeparatorLine(invoiceDocument);
      setBankDetailsAtFooter(invoiceDocument);
      log.info(INVOICE_HEADING + HYPHEN_WITH_SPACE + VALUE_IN_LOG, invoicePath);
      return invoicePath.replace(svlcfConfig.getPdfOutputRootPath(), SERVER_FILES_PATH);
    } catch (FileNotFoundException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
  }

  private void setInvoiceData(
      Document invoiceDocument,
      List<Stock> productsList,
      Long invoiceNumber,
      User user,
      String date) {
    setInvoiceNumberAndDate(invoiceDocument, invoiceNumber.toString(), date);
    setCustomerDetailsInInvoice(invoiceDocument, user);
    setProductDetailsInInvoice(invoiceDocument, productsList);
    setTotalDetails(invoiceDocument, productsList, user.getBalance());
    setAuthorizedData(invoiceDocument);
  }

  private void setInvoiceNumberAndDate(
      Document invoiceDocument, String invoiceNumber, String date) {
    PdfPTable invoiceDataTable =
        createTable(
            I_THREE, I_ZERO, F_NINETY_EIGHT, new float[] {F_THIRTY_FIVE, F_THIRTY, F_THIRTY_FIVE});
    invoiceDataTable.addCell(
        setDetailsInCell(
            INVOICE_NUMBER_HEADER + invoiceNumber, F_THIRTEEN, Font.BOLD, false, false));
    invoiceDataTable.addCell(setDetailsInCell(BILL_TYPE, F_THIRTEEN, Font.ITALIC, false, false));
    invoiceDataTable.addCell(
        setDetailsInCell(DATE_TEXT + date, F_THIRTEEN, Font.BOLD, false, false));
    invoiceDocument.add(invoiceDataTable);
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setCustomerDetailsInInvoice(Document invoiceDocument, User user) {
    setCustomerDetailsText(invoiceDocument);
    setCustomerDetails(invoiceDocument, user);
  }

  private void setCustomerDetailsText(Document invoiceDocument) {
    PdfPTable invoiceCustomerTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    invoiceCustomerTable.addCell(
        setDetailsInCell(CUSTOMER_DETAILS_HEADER, F_FOURTEEN, Font.BOLD, false, false));
    invoiceDocument.add(invoiceCustomerTable);
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setCustomerDetails(Document invoiceDocument, User user) {
    PdfPTable invoiceCustomerTable =
        createTable(I_ONE, I_THIRTY, F_NINETY, new float[] {F_HUNDRED});
    invoiceCustomerTable.addCell(
        setDetailsInCell(
            user.getId()
                + HYPHEN_WITH_SPACE
                + user.getName()
                + HYPHEN_WITH_SPACE
                + user.getAddress()
                + HYPHEN_WITH_SPACE
                + user.getPhoneNumber(),
            F_TWELVE,
            Font.PLAIN,
            true,
            false));
    String gstin =
        (user.getGstin() == null || BLANK_STRING.equalsIgnoreCase(user.getGstin().trim()))
            ? BLANK_STRING
            : user.getGstin();
    invoiceCustomerTable.addCell(
        setDetailsInCell(GSTIN + gstin, F_TWELVE, Font.PLAIN, true, false));
    invoiceDocument.add(invoiceCustomerTable);
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setProductDetailsInInvoice(Document invoiceDocument, List<Stock> productsList) {
    PdfPTable productDetailsTable =
        createTable(
            I_FIVE,
            I_ZERO,
            F_NINETY_EIGHT,
            new float[] {F_FORTY_TWO, F_TEN, F_TEN, F_EIGHTEEN, F_TWENTY});
    List.of(ITEM_HEADING, BAGS_HEADING, KGS_HEADING, RATE_HEADING, AMOUNT_HEADING)
        .forEach(
            font -> productDetailsTable.addCell(setDetailsInCellWithBackGround(font, F_TWELVE)));
    List<String> emptyRowList =
        List.of(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
    productsList.forEach(
        product -> {
          int amount = product.getPrice() * product.getQuantity();
          List<String> list =
              List.of(
                  product.getBrand() + UNDER_SCORE + product.getName(),
                  product.getQuantity().toString(),
                  product.getVariant().toString(),
                  product.getPrice().toString(),
                  Integer.toString(amount));
          list.forEach(
              font ->
                  productDetailsTable.addCell(
                      setDetailsInCell(font, F_TWELVE, Font.PLAIN, false, false)));
          setEmptyCells(emptyRowList, productDetailsTable, F_TWO);
        });
    int count = productsList.size();
    int spaceAdjuster =
        (count > I_FIVE && count < I_TWELVE) ? (count / I_TWO) - I_ONE : (count / I_TWO);
    while (count < I_NINETEEN - spaceAdjuster) {
      setEmptyCells(emptyRowList, productDetailsTable, F_TWELVE);
      count++;
    }
    invoiceDocument.add(productDetailsTable);
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private void setTotalDetails(Document invoiceDocument, List<Stock> productsList, int balance) {
    PdfPTable totalItemsTable =
        createTable(
            I_THREE, I_ZERO, F_NINETY_EIGHT, new float[] {F_THIRTY_FIVE, F_THIRTY, F_THIRTY_FIVE});
    totalItemsTable.addCell(
        setDetailsInCell(PREVIOUS_BALANCE_TEXT + balance, F_THIRTEEN, Font.BOLD, false, false));
    totalItemsTable.addCell(
        setDetailsInCell(getTotalBags(productsList), F_THIRTEEN, Font.BOLD, false, false));
    totalItemsTable.addCell(
        setDetailsInCell(getBillAmount(productsList), F_THIRTEEN, Font.BOLD, false, false));
    invoiceDocument.add(totalItemsTable);
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  private String getTotalBags(List<Stock> listOfProducts) {
    return TOTAL_BAGS_TEXT
        + listOfProducts.stream().map(Stock::getQuantity).mapToInt(Integer::intValue).sum();
  }

  private String getBillAmount(List<Stock> productsList) {
    return TOTAL_BILL_AMOUNT_TEXT
        + productsList.stream()
            .map(stock -> stock.getPrice() * stock.getQuantity())
            .mapToInt(Integer::intValue)
            .sum()
        + AMOUNT_END;
  }

  private void setAuthorizedData(Document invoiceDocument) {
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
    invoiceDocument.add(new Paragraph(NEWLINE_STRING));
    PdfPTable authorizedDataTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    authorizedDataTable.addCell(setAuthorizedDetails());
    invoiceDocument.add(authorizedDataTable);
  }

  private PdfPCell setAuthorizedDetails() {
    PdfPCell cell = new PdfPCell(setTextData(AUTHORIZED_BY_TEXT, F_THIRTEEN, Font.BOLD));
    cell.setBorder(I_ZERO);
    cell.setPaddingRight(F_TWENTY_FIVE);
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

  private void setBankDetailsAtFooter(Document invoiceDocument) {
    PdfPTable bankDetailsTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    bankDetailsTable.addCell(setTextData(BANK_DETAILS, F_TWELVE, Font.PLAIN));
    invoiceDocument.add(bankDetailsTable);
  }
}
