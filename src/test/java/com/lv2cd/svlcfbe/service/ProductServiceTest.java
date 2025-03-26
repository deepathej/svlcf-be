package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.PURCHASE;
import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.lv2cd.svlcfbe.entity.Product;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private SvlcfService svlcfService;
  @InjectMocks private ProductService productService;

  @Test
  void testAddProductToRepoMethodWhenStockIsSentThenDataIsAddedToDB() {
    Product expectedProduct = getProduct4();
    expectedProduct.setQuantity(12);
    expectedProduct.setAmount(15600);
    expectedProduct.setDate(getCurrentDate());
    when(svlcfService.getAvailableIdByName(PRODUCT_STRING)).thenReturn(L_TEN_THOUSAND);
    when(productRepository.save(expectedProduct)).thenReturn(expectedProduct);
    Product actualProduct =
        productService.addProductToRepo(
            getStock(), L_TEN_THOUSAND_AND_THREE, L_STOCK_INVOICE_NUMBER, PURCHASE);
    assertEquals(expectedProduct, actualProduct);
  }

  @Test
  void testGetProductsByDateMethodWhenDataIsAvailableThenReturned() {
    String date = getCurrentDate();
    Product product = getProduct();
    product.setDate(date);
    when(productRepository.findByDate(date)).thenReturn(List.of(product));
    List<Product> expectedProductList = productService.getProductsByDate(date);
    assertEquals(I_ONE, expectedProductList.size());
  }

  @Test
  void testGetProductByIdMethodWhenDataIsAvailableThenReturned() {
    Product product = getProduct();
    when(productRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(product));
    Product actualProduct = productService.getProductById(L_TEN_THOUSAND);
    assertEquals(product, actualProduct);
  }

  @Test
  void testGetProductByIdMethodWhenDBOperationFailedThenExceptionIsThrown() {
    when(productRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> productService.getProductById(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(PRODUCT_NOT_AVAILABLE, thrown.getMessage());
  }

  @Test
  void testGetProductByUserIdMethodWhenDataIsAvailableThenReturned() {
    Product product = getProduct();
    when(productRepository.getProductByUserId(L_TEN_THOUSAND)).thenReturn(List.of(product));
    List<Product> actualProduct = productService.getProductByUserId(L_TEN_THOUSAND);
    assertEquals(product, actualProduct.get(I_ZERO));
  }

  @Test
  void testUpdateTechDetailsForProduct() {
    doNothing().when(productRepository).updateTechDetailsForProduct(L_TEN_THOUSAND, L_TEN_THOUSAND, I_THOUSAND, I_ONE_LAKH);
    productService.updateTechDetailsForProduct(L_TEN_THOUSAND, L_TEN_THOUSAND, I_THOUSAND, I_ONE_LAKH);
    verify(productRepository, times(I_ONE)).updateTechDetailsForProduct(L_TEN_THOUSAND, L_TEN_THOUSAND, I_THOUSAND,
            I_ONE_LAKH);
  }
}
