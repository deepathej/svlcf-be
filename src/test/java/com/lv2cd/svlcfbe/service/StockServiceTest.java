package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.PURCHASE;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.OldStock;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.StockRequest;
import com.lv2cd.svlcfbe.repository.OldStockRepository;
import com.lv2cd.svlcfbe.repository.StockRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

  @Mock private StockRepository stockRepository;
  @Mock private OldStockRepository oldStockRepository;
  @Mock private UserService userService;
  @Mock private SvlcfService svlcfService;
  @Mock private ProductService productService;
  @InjectMocks private StockService stockService;

  @Test
  void testNewStockMethodWhenConsumerThenExceptionIsThrown() {
    StockRequest stockRequest = getConsumerStockReq();
    when(userService.getUserById(L_TEN_THOUSAND_AND_ONE)).thenReturn(getConsumerWithBalance());
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.newStock(stockRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(USER_CANNOT_ADD_OR_UPDATE_STOCK, exception.getMessage());
  }

  @Test
  void testNewStockWhenStockAlreadyExistsInDBThenExceptionIsThrown() {
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(getSupplierWithBalance());
    when(stockRepository.findByNameAndBrandAndVariant(ITEM_NAME, BRAND, I_FIFTY))
        .thenReturn(List.of(getStock()));
    StockRequest stockRequest = getSupplierStockReq();
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.newStock(stockRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(STOCK_UPDATE_INSTEAD, exception.getMessage());
  }

  @Test
  void testNewStockMethodWhenValidDataIsSentThenDataAddedToDB() {
    StockRequest stockRequest = getSupplierStockReq();
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(getSupplierWithBalance());
    when(stockRepository.findByNameAndBrandAndVariant(ITEM_NAME, BRAND, I_FIFTY))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(STOCK_STRING)).thenReturn(L_TEN_THOUSAND);
    Stock stock = stockRequest.getStock();
    stock.setId(L_TEN_THOUSAND);
    when(stockRepository.save(getStock())).thenReturn(stock);
    when(userService.updateUserBalance(L_TEN_THOUSAND_AND_THREE, 15600, I_TEN_THOUSAND, PURCHASE))
        .thenReturn(I_TEN_THOUSAND);
    when(productService.addProductToRepo(
            stock, stockRequest.getUserId(), L_STOCK_INVOICE_NUMBER, PURCHASE))
        .thenReturn(getProduct3());
    Stock actualStock = stockService.newStock(stockRequest);
    assertEquals(stock, actualStock);
  }

  @Test
  void testGetStockMethodWhenDataAvailableThenReturned() {
    List<Stock> expectedStockList = List.of(getStock(), getStock2());
    when(stockRepository.findAll()).thenReturn(expectedStockList);
    List<Stock> actualStockList = stockService.getStock();
    assertEquals(expectedStockList, actualStockList);
  }

  @Test
  void testUpdateStockPriceMethodWhenPriceIsLessThanOneThenExceptionIsThrown() {
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.updateStockPrice(L_TEN_THOUSAND, I_ZERO),
            EXCEPTION_NOT_THROWN);
    assertEquals(PRICE_CANNOT_BE_NULL + I_ZERO, exception.getMessage());
  }

  @Test
  void testUpdateStockPriceMethodWhenPriceUpdateDBOperationFailedThenExceptionIsThrown() {
    Integer newPrice = 1150;
    when(stockRepository.updatePriceForStock(L_TEN_THOUSAND, newPrice)).thenReturn(I_ZERO);
    Exception exception =
        assertThrows(
            CustomInternalServerException.class,
            () -> stockService.updateStockPrice(L_TEN_THOUSAND, newPrice),
            EXCEPTION_NOT_THROWN);
    assertEquals(PRICE_UPDATE_FAILED, exception.getMessage());
  }

  @Test
  void testUpdateStockPriceMethodWhenValidDataIsSentThenPriceForTheStockIsUpdated() {
    Stock stock = getStock();
    Integer oldPrice = stock.getPrice();
    when(stockRepository.updatePriceForStock(L_TEN_THOUSAND, I_THOUSAND_HUNDRED_AND_FIFTY))
        .thenReturn(I_ONE);
    stock.setPrice(I_THOUSAND_HUNDRED_AND_FIFTY);
    when(stockRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(stock));
    stock.setPrice(oldPrice);
    Stock actualStock = stockService.updateStockPrice(L_TEN_THOUSAND, I_THOUSAND_HUNDRED_AND_FIFTY);
    stock.setPrice(I_THOUSAND_HUNDRED_AND_FIFTY);
    assertEquals(stock, actualStock);
  }

  @Test
  void testGetMatchedStockMethodWhenStockNotExistsInDBThenExceptionIsThrown() {
    Stock stock = getStock();
    when(stockRepository.findByNameAndBrandAndVariant(
            stock.getName(), stock.getBrand(), stock.getVariant()))
        .thenReturn(List.of());
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () ->
                stockService.getMatchedStock(stock.getName(), stock.getBrand(), stock.getVariant()),
            EXCEPTION_NOT_THROWN);
    assertEquals(STOCK_NOT_EXIST, exception.getMessage());
  }

  @Test
  void testGetMatchedStockMethodWhenStockIsAvailableThenReturnTheStock() {
    Stock stock = getStock();
    when(stockRepository.findByNameAndBrandAndVariant(
            stock.getName(), stock.getBrand(), stock.getVariant()))
        .thenReturn(List.of(stock));
    Stock actualStock =
        stockService.getMatchedStock(stock.getName(), stock.getBrand(), stock.getVariant());
    assertEquals(stock, actualStock);
  }

  @Test
  void testUpdateStockQuantityMethodWhenQuantityIsLessThanOneThenExceptionIsThrown() {
    Stock stock = getStock();
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.updateStock(stock, L_TEN_THOUSAND, I_ZERO, I_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(QUANTITY_CANNOT_BE_NULL + I_ZERO, exception.getMessage());
  }

  @Test
  void testUpdateStockMethodWhenConsumerTriesToUpDateThenExceptionIsThrown() {
    Stock stock = getStock();
    User user = getConsumerWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_ONE)).thenReturn(user);
    Exception exception =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.updateStock(stock, L_TEN_THOUSAND_AND_ONE, I_HUNDRED, I_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(USER_CANNOT_ADD_OR_UPDATE_STOCK, exception.getMessage());
  }

  @Test
  void testUpdateStockQuantityMethodWhenUpdateDBOperationFailedThenExceptionIsThrown() {
    Stock stock = getStock();
    User user = getSupplierWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(user);
    when(stockRepository.updateQuantityOfStock(stock.getId(), I_HUNDRED_AND_TWELVE))
        .thenReturn(I_ZERO);
    Exception exception =
        assertThrows(
            CustomInternalServerException.class,
            () -> stockService.updateStock(stock, L_TEN_THOUSAND_AND_THREE, I_HUNDRED, I_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(QUANTITY_UPDATE_FAILED, exception.getMessage());
  }

  @Test
  void testUpdateStockMethodWhenValidDataSentThenDataIsUpdatedInDB() {
    Stock stock = getStock();
    Stock expectedStock = getUpdateStockQuantity();
    User user = getSupplierWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(user);
    when(stockRepository.updateQuantityOfStock(stock.getId(), I_HUNDRED_AND_TWELVE))
        .thenReturn(I_ONE);
    when(userService.updateUserBalance(
            L_TEN_THOUSAND_AND_THREE, I_ONE_LAKH, I_TEN_THOUSAND, PURCHASE))
        .thenReturn(I_TEN_THOUSAND);
    when(productService.addProductToRepo(
            stock, L_TEN_THOUSAND_AND_THREE, L_STOCK_INVOICE_NUMBER, PURCHASE))
        .thenReturn(getProduct());
    when(stockRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(expectedStock));
    when(stockRepository.updatePriceForStock(L_TEN_THOUSAND, I_THOUSAND)).thenReturn(I_ONE);
    Stock actualStock =
        stockService.updateStock(stock, L_TEN_THOUSAND_AND_THREE, I_HUNDRED, I_THOUSAND);
    assertEquals(expectedStock, actualStock);
  }

  @Test
  void testGetStockByIdMethodWhenFailedDBOperationThenExceptionIsThrown() {
    when(stockRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> stockService.getStockById(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + STOCK_STRING, thrown.getMessage());
  }

  @Test
  void testGetStockByIdMethodWhenMatchingDataAvailableThenReturn() {
    Stock expectedStock = getStock();
    when(stockRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(expectedStock));
    Stock actualStock = stockService.getStockById(L_TEN_THOUSAND);
    assertEquals(expectedStock, actualStock);
  }

  @Test
  void testGetBrandFiltersMethodWhenMatchingDataAvailableThenReturn() {
    List<Stock> expectedStockList = List.of(getStock(), getStock2());
    when(stockRepository.findAll()).thenReturn(expectedStockList);
    List<String> brands = stockService.getBrandFilters(true);
    assertIterableEquals(List.of(BRAND), brands);
  }

  @Test
  void testGetItemFiltersMethodWhenMatchingDataAvailableThenReturn() {
    List<Stock> expectedStockList = List.of(getStock(), getStock2());
    when(stockRepository.findAll()).thenReturn(expectedStockList);
    List<String> items = stockService.getItemFilters(true, BRAND);
    assertIterableEquals(List.of(ITEM_NAME, ITEM_NAME2), items);
  }

  @Test
  void testGetVariantFiltersMethodWhenMatchingDataAvailableThenReturn() {
    List<Stock> expectedStockList = List.of(getStock(), getStock3());
    when(stockRepository.findAll()).thenReturn(expectedStockList);
    List<Integer> variants = stockService.getVariantFilters(true, BRAND, ITEM_NAME);
    assertIterableEquals(List.of(I_FORTY, I_FIFTY), variants);
  }

  @Test
  void testGetStockByBrandNameAndVariantWhenMatchingDataAvailableThenReturn() {
    List<Stock> expectedStockList = List.of(getStock());
    when(stockRepository.findByNameAndBrandAndVariant(ITEM_NAME, BRAND, I_FIFTY))
        .thenReturn(expectedStockList);
    assertEquals(
        expectedStockList.get(I_ZERO),
        stockService.getMatchedStock(ITEM_NAME, BRAND, I_FIFTY));
  }

  @Test
  void testGetStockByBrandNameAndVariantWhenMatchingNotDataAvailableThenThrowException() {
    List<Stock> expectedStockList = List.of();
    when(stockRepository.findByNameAndBrandAndVariant(ITEM_NAME, BRAND, I_FIFTY))
        .thenReturn(expectedStockList);
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> stockService.getMatchedStock(ITEM_NAME, BRAND, I_FIFTY),
            EXCEPTION_NOT_THROWN);
    assertEquals(STOCK_NOT_EXIST, thrown.getMessage());
  }

  @Test
  void testOldStockMethodWhenValidDataIsSentThenAddItToDB() {
    Stock stock = getOldStockReq();
    when(stockRepository.findByNameAndBrandAndVariant(ITEM_NAME, BRAND, I_FIFTY))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(STOCK_STRING)).thenReturn(L_TEN_THOUSAND);
    when(stockRepository.save(getStock())).thenReturn(stock);
    OldStock oldStock = getOldStock();
    when(oldStockRepository.save(oldStock)).thenReturn(oldStock);
    Stock actualStock = stockService.oldStock(stock);
    assertEquals(stock, actualStock);
  }

  @Test
  void testGetOldStockMethodWhenDataAvailableThenReturn() {
    when(oldStockRepository.findAll()).thenReturn(List.of(getOldStock()));
    List<OldStock> oldStockList = stockService.getOldStock();
    assertEquals(I_ONE, oldStockList.size());
  }
}
