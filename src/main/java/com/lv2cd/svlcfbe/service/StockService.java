package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.PURCHASE;
import static com.lv2cd.svlcfbe.util.CommonMethods.capString;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.entity.OldStock;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.enums.UserType;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.StockRequest;
import com.lv2cd.svlcfbe.repository.OldStockRepository;
import com.lv2cd.svlcfbe.repository.StockRepository;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class StockService {

  private StockRepository stockRepository;
  private SvlcfService svlcfService;
  private ProductService productService;
  private UserService userService;
  private OldStockRepository oldStockRepository;

  /**
   * Checks if the empty stock is to be removed from the response
   *
   * @param filterEmptyStock boolean to check if filter needed explictly
   * @param stock details
   * @return filter needed or not
   */
  private static boolean doFilterEmptyStock(boolean filterEmptyStock, Stock stock) {
    return !filterEmptyStock || stock.getQuantity() > I_ZERO;
  }

  /**
   * Adding new stock to the DB
   *
   * @param stockRequest without stock id
   * @return stock with id
   */
  public Stock newStock(StockRequest stockRequest) {
    log.info("Adding new stock to the DB {}", stockRequest);
    User user = validateUserIdAndGetUser(stockRequest.getUserId());
    Stock stock = capStock(stockRequest.getStock());
    Stock stockFromRepo = stockRepository.save(updateStockId(stock));
    userService.updateUserBalance(
        user.getId(), stock.getPrice() * stock.getQuantity(), user.getBalance(), PURCHASE);
    productService.addProductToRepo(
        stock, stockRequest.getUserId(), L_STOCK_INVOICE_NUMBER, PURCHASE);
    return stockFromRepo;
  }

  /**
   * Capitalize the stock name and brand
   *
   * @param stock details
   * @return cap string for name and brand
   */
  private Stock capStock(Stock stock) {
    stock.setName(capString(stock.getName()));
    stock.setBrand(capString(stock.getBrand()));
    return stock;
  }

  /**
   * Updates the available id to the stock object
   *
   * @param stock without id
   * @return stock with id
   */
  private Stock updateStockId(Stock stock) {
    log.info("Adding available stock id to the stock details");
    if (stockRepository
        .findByNameAndBrandAndVariant(stock.getName(), stock.getBrand(), stock.getVariant())
        .isEmpty()) {
      Long availableId = svlcfService.getAvailableIdByName(STOCK_STRING);
      stock.setId(availableId);
      return stock;
    }
    throw new CustomBadRequestException(STOCK_UPDATE_INSTEAD);
  }

  /**
   * Gets list of all stock by ordering brand, name, variant
   *
   * @return stock list
   */
  public List<Stock> getStock() {
    log.info("Getting sorted stock list");
    return stockRepository.findAll().stream()
        .sorted(
            Comparator.comparing(Stock::getBrand)
                .thenComparing(Stock::getName)
                .thenComparing(Stock::getVariant))
        .toList();
  }

  /**
   * get the stock from DB
   *
   * @return stock list
   */
  public List<Stock> getStockWithOutSorting() {
    log.info("Getting stock list without sorting");
    return stockRepository.findAll();
  }

  public Stock updateStockPrice(Long stockId, Integer newPrice) {
    log.info("Update stock price to {} for id {}", newPrice, stockId);
    if (newPrice <= I_ZERO) {
      throw new CustomBadRequestException(PRICE_CANNOT_BE_NULL + newPrice);
    }
    if (stockRepository.updatePriceForStock(stockId, newPrice) == I_ZERO) {
      throw new CustomInternalServerException(PRICE_UPDATE_FAILED);
    }
    return getStockById(stockId);
  }

  public Stock getMatchedStock(String name, String brand, Integer variant) {
    log.info("Get matched stock by Brand : {}, Name : {} and Variant : {}", brand, name, variant);
    return stockRepository.findByNameAndBrandAndVariant(name, brand, variant).stream()
        .findFirst()
        .orElseThrow(() -> new CustomBadRequestException(STOCK_NOT_EXIST));
  }

  private User validateUserIdAndGetUser(Long userId) {
    log.info("Get user details by id : {} for validating it against SUPPLIER", userId);
    User user = userService.getUserById(userId);
    if (user.getUserType() == UserType.SUPPLIER) {
      return user;
    }
    throw new CustomBadRequestException(USER_CANNOT_ADD_OR_UPDATE_STOCK);
  }

  public Stock updateStock(Stock stock, Long userId, Integer newQuantity, Integer newPrice) {
    if (newQuantity <= I_ZERO) {
      throw new CustomBadRequestException(QUANTITY_CANNOT_BE_NULL + newQuantity);
    }
    log.info("Updating the stock for the user {} new quantity {} and price {}", userId, newQuantity, newPrice);
    User user = validateUserIdAndGetUser(userId);
    userService.updateUserBalance(
        user.getId(), newPrice * newQuantity, user.getBalance(), PURCHASE);
    updateQuantityOfStock(stock.getId(), newQuantity + stock.getQuantity());
    updateStockPrice(stock.getId(), newPrice);
    stock.setQuantity(newQuantity);
    stock.setPrice(newPrice);
    productService.addProductToRepo(stock, userId, L_STOCK_INVOICE_NUMBER, PURCHASE);
    return getStockById(stock.getId());
  }

  public void updateQuantityOfStock(Long stockId, Integer quantity) {
    log.info("update stock quantity to {} for id {}", quantity, stockId);
    if (stockRepository.updateQuantityOfStock(stockId, quantity) == I_ZERO) {
      throw new CustomInternalServerException(QUANTITY_UPDATE_FAILED);
    }
  }

  public Stock getStockById(Long stockId) {
    log.info("Get stock by id : {}", stockId);
    return stockRepository
        .findById(stockId)
        .orElseThrow(() -> new CustomInternalServerException(FAILED_DB_OPERATION + STOCK_STRING));
  }

  public List<String> getBrandFilters(boolean filterEmptyStock) {
    log.info("Get available Brands without empty Stock : {}", filterEmptyStock);
    return getStockWithOutSorting().stream()
        .filter(stock -> doFilterEmptyStock(filterEmptyStock, stock))
        .map(Stock::getBrand)
        .distinct()
        .sorted()
        .toList();
  }

  public List<String> getItemFilters(boolean filterEmptyStock, String brand) {
    log.info("Get available Items without empty Stock : {} for Brand : {}", filterEmptyStock, brand);
    return getStockWithOutSorting().stream()
        .filter(
            stock ->
                doFilterEmptyStock(filterEmptyStock, stock)
                    && stock.getBrand().equalsIgnoreCase(brand))
        .map(Stock::getName)
        .distinct()
        .sorted()
        .toList();
  }

  public List<Integer> getVariantFilters(boolean filterEmptyStock, String brand, String name) {
    log.info(
        "Get available variants without empty Stock : {} for Brand : {}, Name : {}",
        filterEmptyStock,
        brand,
        name);
    return getStockWithOutSorting().stream()
        .filter(
            stock ->
                stock.getBrand().equalsIgnoreCase(brand)
                    && stock.getName().equalsIgnoreCase(name)
                    && doFilterEmptyStock(filterEmptyStock, stock))
        .map(Stock::getVariant)
        .distinct()
        .sorted()
        .toList();
  }

  /**
   * Adding old stock details to the DB
   *
   * @param stock without id
   * @return stock with id
   */
  public Stock oldStock(Stock stock) {
    log.info("Adding old stock to DB");
    capStock(stock);
    log.info("Adding old stock to stock table");

    Stock stockFromRepo = stockRepository.save(updateStockId(stock));
    log.info("Adding old stock to old stock table");
    oldStockRepository.save(
        OldStock.builder()
            .name(
                stock.getBrand() + UNDER_SCORE + stock.getName() + UNDER_SCORE + stock.getVariant())
            .quantity(stock.getQuantity())
            .build());
    return stockFromRepo;
  }

  /**
   * Gets the list of all old stock
   *
   * @return list of old stock
   */
  public List<OldStock> getOldStock() {
    log.info("Getting old stock");
    return oldStockRepository.findAll();
  }
}
