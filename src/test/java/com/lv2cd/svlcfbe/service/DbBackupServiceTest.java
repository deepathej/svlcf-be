package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.entity.*;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.repository.*;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class DbBackupServiceTest {

  private static final Logger log = LoggerFactory.getLogger(DbBackupServiceTest.class);
  @InjectMocks private DbBackupService dbBackupService;
  @Mock private UserRepository userRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private BalanceReportRepository balanceReportRepository;
  @Mock private CashDepositRepository cashDepositRepository;
  @Mock private ExpenseRepository expenseRepository;
  @Mock private OldStockRepository oldStockRepository;
  @Mock private PreBalanceRepository preBalanceRepository;
  @Mock private ProductRepository productRepository;
  @Mock private SalesRepository salesRepository;
  @Mock private StockReportRepository stockReportRepository;
  @Mock private StockRepository stockRepository;
  @Mock private SvlcfRepository svlcfRepository;
  @Mock private DeletedTransactionRepo deletedTransactionRepo;
  @Mock private ValidStatusRepo validStatusRepo;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testBackUpDBDataMethodWhenInvalidFilePathThenExceptionIsThrown() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> dbBackupService.backUpDBData(),
            EXCEPTION_NOT_THROWN);
    assertThat(thrown.getMessage()).contains(SYSTEM_CANNOT_FIND_PATH);
  }

  @Test
  void testBackUpDBDataMethodWhenValidFilePathThenDataBackupHappens() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    when(userRepository.findAll()).thenReturn(List.of(getConsumerWithBalance()));
    when(paymentRepository.findAll()).thenReturn(List.of(getPayment()));
    when(stockRepository.findAll()).thenReturn(List.of(getStock()));
    when(productRepository.findAll()).thenReturn(List.of(getProduct()));
    when(svlcfRepository.findAll()).thenReturn(List.of(getSvlcf()));
    when(stockReportRepository.findAll()).thenReturn(List.of(getStockReport()));
    when(balanceReportRepository.findAll()).thenReturn(List.of(getBalanceReport()));
    when(cashDepositRepository.findAll()).thenReturn(List.of(getCashDeposit()));
    when(oldStockRepository.findAll()).thenReturn(List.of(getOldStock()));
    when(preBalanceRepository.findAll()).thenReturn(List.of(getConsumerWithPreBalance()));
    when(expenseRepository.findAll()).thenReturn(List.of(getExpense()));
    when(salesRepository.findAll()).thenReturn(List.of(getSales()));
    when(deletedTransactionRepo.findAll()).thenReturn(List.of(getDelTrans()));
    String backupFile = dbBackupService.backUpDBData();
    assertNotNull(backupFile);
    File file = new File(backupFile);
    if (file.delete()) {
      log.info(TEST_REPORT_FILE_DELETED);
    }
  }
}
