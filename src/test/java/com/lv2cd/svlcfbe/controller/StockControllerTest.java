package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.entity.OldStock;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.model.StockRequest;
import com.lv2cd.svlcfbe.service.StockService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(StockController.class)
class StockControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private StockService stockService;

  @Test
  void testNewStock() throws Exception {
    StockRequest stockRequest = getSupplierStockReq();
    Stock stock = stockRequest.getStock();
    stock.setId(L_TEN_THOUSAND);
    when(stockService.newStock(stockRequest)).thenReturn(stock);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(NEW_STOCK_ENDPOINT)
                    .contentType(APPLICATION_JSON)
                    .content(asJsonString(stockRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        stock, asObjectFromString(mvcResult.getResponse().getContentAsString(), Stock.class));
    verify(stockService, times(I_ONE)).newStock(stockRequest);
  }

  @Test
  void testGetStock() throws Exception {
    List<Stock> expectedStockList = List.of(getStock(), getStock2());
    when(stockService.getStock()).thenReturn(expectedStockList);
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_STOCK_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_TWO, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(stockService, times(I_ONE)).getStock();
  }

  @Test
  void testUpdateStockPrice() throws Exception {
    Stock stock = getStock();
    stock.setPrice(I_THOUSAND_HUNDRED_AND_FIFTY);
    when(stockService.updateStockPrice(L_TEN_THOUSAND, I_THOUSAND_HUNDRED_AND_FIFTY))
        .thenReturn(stock);
    MvcResult mvcResult =
        mockMvc
            .perform(
                put(UPDATE_STOCK_PRICE_ENDPOINT, L_TEN_THOUSAND, I_THOUSAND_HUNDRED_AND_FIFTY)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        stock, asObjectFromString(mvcResult.getResponse().getContentAsString(), Stock.class));
    verify(stockService, times(I_ONE))
        .updateStockPrice(L_TEN_THOUSAND, I_THOUSAND_HUNDRED_AND_FIFTY);
  }

  @Test
  void testGetMatchedStock() throws Exception {
    Stock stock = getStock();
    when(stockService.getMatchedStock(ITEM_NAME, BRAND, I_FIFTY)).thenReturn(stock);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(GET_MATCHED_STOCK_ENDPOINT)
                    .contentType(APPLICATION_JSON)
                    .content(asJsonString(stock)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        stock, asObjectFromString(mvcResult.getResponse().getContentAsString(), Stock.class));
    verify(stockService, times(I_ONE)).getMatchedStock(ITEM_NAME, BRAND, I_FIFTY);
  }

  @Test
  void testGetBrandFilters() throws Exception {
    when(stockService.getBrandFilters(true)).thenReturn(List.of(BRAND));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_BRAND_FILTER_ENDPOINT, true))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(stockService, times(I_ONE)).getBrandFilters(true);
  }

  @Test
  void testGetItemFilters() throws Exception {
    when(stockService.getItemFilters(true, BRAND)).thenReturn(List.of(ITEM_NAME));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_ITEM_FILTER_ENDPOINT, true, BRAND))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(stockService, times(I_ONE)).getItemFilters(true, BRAND);
  }

  @Test
  void testGetVariantFilters() throws Exception {
    when(stockService.getVariantFilters(true, BRAND, ITEM_NAME)).thenReturn(List.of(I_FIFTY));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_VARIANT_FILTER_ENDPOINT, true, BRAND, ITEM_NAME))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(stockService, times(I_ONE)).getVariantFilters(true, BRAND, ITEM_NAME);
  }

  @Test
  void testUpdateStock() throws Exception {
    Stock stock = getStock();
    Stock expectedStock = getUpdateStockQuantity();
    when(stockService.updateStock(stock, L_TEN_THOUSAND_AND_THREE, I_HUNDRED, I_THOUSAND))
        .thenReturn(expectedStock);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(UPDATE_STOCK_ENDPOINT, L_TEN_THOUSAND_AND_THREE, I_HUNDRED, I_THOUSAND)
                    .contentType(APPLICATION_JSON)
                    .content(asJsonString(stock)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        expectedStock,
        asObjectFromString(mvcResult.getResponse().getContentAsString(), Stock.class));
    verify(stockService, times(I_ONE))
        .updateStock(stock, L_TEN_THOUSAND_AND_THREE, I_HUNDRED, I_THOUSAND);
  }

  @Test
  void testOldStock() throws Exception {
    Stock stock = getSupplierStockReq().getStock();
    Stock stock2 = getStock();
    when(stockService.oldStock(stock)).thenReturn(stock2);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(OLD_STOCK_ENDPOINT).contentType(APPLICATION_JSON).content(asJsonString(stock)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        stock2, asObjectFromString(mvcResult.getResponse().getContentAsString(), Stock.class));
    verify(stockService, times(I_ONE)).oldStock(stock);
  }

  @Test
  void testGetOldStock() throws Exception {
    OldStock oldStock = getOldStock();
    when(stockService.getOldStock()).thenReturn(List.of(oldStock));
    MvcResult mvcResult =
        mockMvc
            .perform(get(OLD_STOCK_ENDPOINT))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(stockService, times(I_ONE)).getOldStock();
  }
}
