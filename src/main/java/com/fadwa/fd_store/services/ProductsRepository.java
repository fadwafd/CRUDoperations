package com.fadwa.fd_store.services;

import com.fadwa.fd_store.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product, Integer> {



}
