package com.fadwa.fd_store.services;

import com.fadwa.fd_store.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductsService {
    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ProductsRepository productRepository;

    public byte[] getImageById(int productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        byte[] image = product.getImagefile();

        if (image == null) {
            System.out.println("Image not found for product ID: " + productId);
        }

        return image;
    }
}
