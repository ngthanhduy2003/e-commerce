package Ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Ecommerce.dto.response.ProductResponse;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.Category;
import Ecommerce.model.entity.Product;
import Ecommerce.model.entity.ProductVariant;
import Ecommerce.model.enums.ProductStatus;
import Ecommerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .category(testCategory)
                .status(ProductStatus.ACTIVE)
                .variants(new ArrayList<>())
                .build();

        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(testProduct)
                .size(42)
                .color("Red")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .status(ProductStatus.ACTIVE)
                .build();

        testProduct.getVariants().add(variant);
    }

    @Test
    void getProductById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductResponse response = productService.getProductById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("Electronics", response.getCategoryName());
        assertEquals(1, response.getVariants().size());
        assertEquals(new BigDecimal("99.99"), response.getVariants().get(0).getPrice());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });

        verify(productRepository, times(1)).findById(999L);
    }
}
