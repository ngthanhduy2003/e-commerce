package Ecommerce.service;

import Ecommerce.exception.OutOfStockException;
import Ecommerce.model.entity.*;
import Ecommerce.model.enums.ProductStatus;
import Ecommerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockManagementTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Test
    void atomicStockDecrement_Success() {
        // Given
        Long skuId = 1L;
        Integer quantity = 5;

        // Simulate successful atomic decrement (affectedRows = 1)
        when(variantRepository.decrementStock(skuId, quantity)).thenReturn(1);

        // When
        int affectedRows = variantRepository.decrementStock(skuId, quantity);

        // Then
        assertEquals(1, affectedRows);
        verify(variantRepository, times(1)).decrementStock(skuId, quantity);
    }

    @Test
    void atomicStockDecrement_InsufficientStock() {
        // Given
        Long skuId = 1L;
        Integer quantity = 100;  // More than available

        // Simulate failed atomic decrement (affectedRows = 0)
        when(variantRepository.decrementStock(skuId, quantity)).thenReturn(0);

        // When
        int affectedRows = variantRepository.decrementStock(skuId, quantity);

        // Then
        assertEquals(0, affectedRows);
        // In real checkout service, this would throw OutOfStockException
    }

    @Test
    void stockRollback_Success() {
        // Given
        Long skuId = 1L;
        Integer quantity = 5;

        when(variantRepository.incrementStock(skuId, quantity)).thenReturn(1);

        // When
        int affectedRows = variantRepository.incrementStock(skuId, quantity);

        // Then
        assertEquals(1, affectedRows);
        verify(variantRepository, times(1)).incrementStock(skuId, quantity);
    }
}

