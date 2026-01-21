package Ecommerce.service;

import Ecommerce.dto.response.ProductResponse;
import Ecommerce.dto.response.ProductVariantResponse;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.Product;
import Ecommerce.model.enums.ProductStatus;
import Ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> getProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        Page<Product> products = productRepository.findByFilters(
            ProductStatus.ACTIVE,
            categoryId,
            minPrice,
            maxPrice,
            pageable
        );

        return products.map(this::toProductResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .status(product.getStatus().name())
            .variants(product.getVariants().stream()
                .map(variant -> ProductVariantResponse.builder()
                    .id(variant.getId())
                    .size(variant.getSize())
                    .color(variant.getColor())
                    .price(variant.getPrice())
                    .stockQuantity(variant.getStockQuantity())
                    .status(variant.getStatus().name())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
