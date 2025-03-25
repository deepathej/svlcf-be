package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.model.StockRequest;
import com.lv2cd.svlcfbe.service.StockService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class StockController {

  private StockService stockService;

  /**
   * API is used to add new stock to the DB
   *
   * @param stockRequest containing details of the stock
   * @return stock details including the id
   */
  @PostMapping(value = NEW_STOCK_ENDPOINT, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
  public ResponseEntity<Stock> newStock(@RequestBody StockRequest stockRequest) {
    return ResponseEntity.ok(stockService.newStock(stockRequest));
  }

  /**
   * API is used to get list of stock details
   *
   * @return list of stock
   */
  @GetMapping(value = GET_STOCK_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<Stock>> getStock() {
    return ResponseEntity.ok(stockService.getStock());
  }

  /**
   * API is used to get the matched stock details based on the brand, name and variant
   *
   * @param stock object without id
   * @return stock object including id
   */
  @PostMapping(
      value = GET_MATCHED_STOCK_ENDPOINT,
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON)
  public ResponseEntity<Stock> getMatchedStock(@RequestBody Stock stock) {
    return ResponseEntity.ok(
        stockService.getMatchedStock(stock.getName(), stock.getBrand(), stock.getVariant()));
  }

  /**
   * API is used to get all the unique brand names
   *
   * @param filterEmptyStock boolean value
   * @return list of brand names
   */
  @GetMapping(value = GET_BRAND_FILTER_ENDPOINT)
  public ResponseEntity<List<String>> getBrandFilters(@PathVariable boolean filterEmptyStock) {
    return ResponseEntity.ok(stockService.getBrandFilters(filterEmptyStock));
  }

  /**
   * API is used to get all the unique items for the brand
   *
   * @param filterEmptyStock boolean value
   * @param brand for the stock
   * @return list of item names
   */
  @GetMapping(value = GET_ITEM_FILTER_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<String>> getItemFilters(
      @PathVariable boolean filterEmptyStock, @PathVariable String brand) {
    return ResponseEntity.ok(stockService.getItemFilters(filterEmptyStock, brand));
  }

  /**
   * API is used to get the unique variants for the brand and item
   *
   * @param filterEmptyStock boolean value
   * @param brand for the stock
   * @param name for the stock
   * @return list of variants
   */
  @GetMapping(value = GET_VARIANT_FILTER_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<Integer>> getVariantFilters(
      @PathVariable boolean filterEmptyStock,
      @PathVariable String brand,
      @PathVariable String name) {
    return ResponseEntity.ok(stockService.getVariantFilters(filterEmptyStock, brand, name));
  }

  /**
   * API is used to update the details of the stock based on the id and the other details provided
   *
   * @param stock object with details without id
   * @param userId unique id for the user
   * @param newQuantity number of new stock quantity
   * @param newPrice updated price
   * @return stock details
   */
  @PostMapping(
      value = UPDATE_STOCK_ENDPOINT,
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON)
  public ResponseEntity<Stock> updateStock(
      @Valid @RequestBody Stock stock,
      @PathVariable Long userId,
      @PathVariable Integer newQuantity,
      @PathVariable Integer newPrice) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(stockService.updateStock(stock, userId, newQuantity, newPrice));
  }

  /**
   * APi is used to update the stock price based on the stock id
   *
   * @param stockId unique id for the stock
   * @param newPrice updated price
   * @return stock details
   */
  @PutMapping(value = UPDATE_STOCK_PRICE_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<Stock> updateStockPrice(
      @PathVariable Long stockId, @PathVariable Integer newPrice) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(stockService.updateStockPrice(stockId, newPrice));
  }

  /**
   * API is used to add the old stock details to the DB
   *
   * @param stock details without id
   * @return stock details
   */
  @PostMapping(value = OLD_STOCK_ENDPOINT, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
  public ResponseEntity<Stock> oldStock(@RequestBody Stock stock) {
    return ResponseEntity.ok(stockService.oldStock(stock));
  }

  /**
   * API is used to get the old stock details
   *
   * @return success message
   */
  @GetMapping(value = OLD_STOCK_ENDPOINT)
  public ResponseEntity<String> getOldStock() {
    return ResponseEntity.ok(writeAsJson(stockService.getOldStock()));
  }
}
