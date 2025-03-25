package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.PURCHASE;
import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.entity.Product;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.enums.PaymentType;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.repository.ProductRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

  private ProductRepository productRepository;
  private SvlcfService svlcfService;

  private static boolean isPurchase(PaymentType paymentType) {
    return paymentType == PURCHASE;
  }

  public Product addProductToRepo(
      Stock stock, Long userId, Long invoiceNumber, PaymentType paymentType) {
    Long availableId = svlcfService.getAvailableIdByName(PRODUCT_STRING);
    Product productFromRepo =
        productRepository.save(
            Product.builder()
                .id(availableId)
                .userId(userId)
                .invoiceNumber(invoiceNumber)
                .item(stock.getBrand() + UNDER_SCORE + stock.getName())
                .quantity(stock.getQuantity())
                .variant(stock.getVariant())
                .price(stock.getPrice())
                .amount(stock.getQuantity() * stock.getPrice())
                .date(getCurrentDate())
                .build());
    log.info(
        isPurchase(paymentType) ? FOR_PURCHASE : FOR_SALES + VALUE_IN_LOG,
        writeAsJson(productFromRepo));
    return productFromRepo;
  }

  public List<Product> getProductsByDate(String date) {
    return productRepository.findByDate(date);
  }

  public Product getProductById(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new CustomBadRequestException(PRODUCT_NOT_AVAILABLE));
  }

  public List<Product> getProductByUserId(Long userId) {
    return productRepository.getProductByUserId(userId);
  }

  public void updateTechDetailsForProduct(
      Long productId, Long userId, Integer quantity, Integer amount) {
    productRepository.updateTechDetailsForProduct(productId, userId, quantity, amount);
  }
}
