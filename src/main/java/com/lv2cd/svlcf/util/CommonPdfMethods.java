package com.lv2cd.svlcf.util;

import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.util.Pair;

public class CommonPdfMethods {

  private static final String CONTACT_TEXT = "Cell: 9705508173, 8309155733";
  private static final String NAME = "SREE VIJAYALAKSHMI CATTLE FEEDS";
  private static final String W_R = "Wholesale & Retail";
  private static final String ADDRESS_1 = "# 22-194/4, Tirupati Road, Kattamanchi";
  private static final String ADDRESS_2 = "CHITTOOR. (A.P) - 517 001";
  private static final String SHOP_PAN = "GSTIN/PAN: ADKFS3275J";

  private CommonPdfMethods() {}

  public static void setHeaderDetails(Document document, String pdfOutputRootPath) {
    PdfPTable headerTable =
        createTable(I_TWO, I_ZERO, F_NINETY_EIGHT, new float[] {F_TWENTY_FIVE, F_SEVENTY_FIVE});
    headerTable.addCell(setHeaderLogo(pdfOutputRootPath));
    headerTable.addCell(setTextData(NAME, F_SIXTEEN, Font.BOLD));
    headerTable.addCell(setTextData(W_R, F_TWELVE, Font.BOLD));
    headerTable.addCell(setTextData(ADDRESS_1, F_TWELVE, Font.PLAIN));
    headerTable.addCell(setTextData(ADDRESS_2, F_FOURTEEN, Font.PLAIN));
    headerTable.addCell(setTextData(CONTACT_TEXT, F_TEN, Font.PLAIN));
    headerTable.addCell(setTextData(SHOP_PAN, F_TEN, Font.PLAIN));
    document.add(headerTable);
    setSeparatorLine(document);
    document.add(new Paragraph(NEWLINE_STRING));
  }

  public static PdfPTable createTable(
      int columns, int horizontalAlignment, float widthPercent, float[] widths) {
    PdfPTable table = new PdfPTable(columns);
    table.getDefaultCell().setFixedHeight(F_HUNDRED);
    table.getDefaultCell().setBorder(I_ZERO);
    table.setHorizontalAlignment(horizontalAlignment);
    table.setWidthPercentage(widthPercent);
    table.setWidths(widths);
    return table;
  }

  @SneakyThrows
  private static PdfPCell setHeaderLogo(String pdfOutputRootPath) {
    PdfPCell cell =
        new PdfPCell(com.lowagie.text.Image.getInstance(pdfOutputRootPath + IMAGE_PATH), true);
    cell.setRowspan(I_SIX);
    cell.setBorder(I_ZERO);
    return cell;
  }

  public static PdfPCell setTextData(String font, float fontSize, int fontType) {
    PdfPCell cell =
        new PdfPCell(
            new Phrase(
                new Chunk(font, FontFactory.getFont(FontFactory.HELVETICA, fontSize, fontType))));
    cell.setBorder(I_ZERO);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_CENTER);
    return cell;
  }

  public static void setSeparatorLine(Document document) {
    document.add(
        new Paragraph(
            new Chunk(
                new LineSeparator(
                    F_ONE_AND_HALF, F_HUNDRED, Color.BLACK, Element.ALIGN_CENTER, F_ZERO))));
  }

  public static PdfPCell setDetailsInCell(
      String font, float fontSize, int fontType, boolean alignLeft, boolean alignRight) {
    PdfPCell cell = new PdfPCell(setTextData(font, fontSize, fontType));
    cell.setBorder(I_ZERO);
    if (alignLeft) {
      cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    } else if (alignRight) {
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    } else {
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    }
    return cell;
  }

  public static void setEmptyCells(
      List<String> emptyRowList, PdfPTable productDetailsTable, float size) {
    emptyRowList.forEach(
        font ->
            productDetailsTable.addCell(setDetailsInCell(font, size, Font.PLAIN, false, false)));
  }

  public static PdfPCell setDetailsInCellWithBackGround(String font, Float fontSize) {
    PdfPCell cell = new PdfPCell(setTextData(font, fontSize, Font.BOLD));
    cell.setBorder(I_ZERO);
    cell.setBackgroundColor(Color.LIGHT_GRAY);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    return cell;
  }

  public static void setTableHeading(Document balanceDocument, String headingText) {
    PdfPTable balanceCustomerTable =
        createTable(I_ONE, I_ZERO, F_NINETY_EIGHT, new float[] {F_HUNDRED});
    balanceCustomerTable.addCell(setDetailsInCell(headingText, F_TWELVE, Font.BOLD, false, false));
    balanceDocument.add(balanceCustomerTable);
    balanceDocument.add(new Paragraph(NEWLINE_STRING));
  }

  public static Integer makeZeroIfNull(Object obj) {
    return obj == null ? 0 : (Integer) obj;
  }

  public static String removeBlankPagesFromPDF(String pdfPath) {
    String newPdfPath = pdfPath.replace(TEMP, BLANK_STRING);
    try (PDDocument pdfDocument = Loader.loadPDF(new File(pdfPath))) {
      int lastPageNumber = pdfDocument.getPages().getCount() - 1;
      PDDocument lastPage = new Splitter().split(pdfDocument).get(lastPageNumber);
      if (new PDFTextStripper().getText(lastPage).trim().isEmpty()) {
        pdfDocument.removePage(lastPageNumber);
      }
      pdfDocument.save(newPdfPath);
      Files.delete(Path.of(pdfPath));
    } catch (Exception e) {
      throw new CustomInternalServerException(FAILED_TO_UPDATE_OR_DELETE_PDF + e.getMessage());
    }
    return newPdfPath;
  }

  public static void deleteIfFileExists(String reportPath) throws IOException {
    Path path = Path.of(reportPath.replace(TEMP, BLANK_STRING));
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }

  public static Pair<String, String> getDateAndPath(
      String rootPath, String additionalData, String... dateArray) {
    return dateArray.length > I_ZERO
        ? Pair.of(dateArray[I_ZERO], rootPath + DUPLICATE_PATH + additionalData + PDF_EXTENSION)
        : Pair.of(getCurrentDate(), rootPath + INVOICE_PATH + additionalData + PDF_EXTENSION);
  }
}
