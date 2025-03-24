package com.lv2cd.svlcf.controller;

import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.model.SalesRequest;
import com.lv2cd.svlcf.service.SalesService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class SaleController {

  private SalesService salesService;

  /**
   * API is used to record the sale details in DB.
   *
   * @param salesRequest with stock details
   * @return invoice PDF
   */
  @PostMapping(value = CONFIRM_SALE_ENDPOINT, consumes = APPLICATION_JSON)
  public ResponseEntity<String> confirmSale(@RequestBody SalesRequest salesRequest) {
    return ResponseEntity.ok(salesService.confirmSale(salesRequest));
  }

  /**
   * API is used to get the last invoice details for the user based on the id
   *
   * @param userId unique id for the user
   * @return invoice pdf
   */
  @GetMapping(value = LAST_INVOICE_ENDPOINT)
  public ResponseEntity<String> getLastSaleInvoice(@PathVariable Long userId) {
    return ResponseEntity.ok(salesService.getLastSaleInvoice(userId));
  }

  /**
   * API is used to generate a new duplicate invoice or updates an invoice to technical user
   *
   * @param type replace/duplicate
   * @param invoiceId unique id for the invoice
   * @return invoice pdf
   */
  @GetMapping(value = DUPLICATE_OR_REPLACE_INVOICE_ENDPOINT)
  public ResponseEntity<String> generateDuplicateOrReplaceInvoice(
      @PathVariable String type, @PathVariable Long invoiceId) {
    return type.equalsIgnoreCase(REPLACE)
        ? ResponseEntity.ok(salesService.updateExistingInvoiceWithTech(invoiceId))
        : ResponseEntity.ok(salesService.generateDuplicateInvoice(invoiceId));
  }
}
