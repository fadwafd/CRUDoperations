package com.fadwa.fd_store.controllers;

import com.fadwa.fd_store.models.ProductDto;
import com.fadwa.fd_store.services.ProductsService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import com.fadwa.fd_store.models.Product;
import com.fadwa.fd_store.services.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private ProductsService productService;

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable int id) {
        byte[] image = productService.getImageById(id);  // Ensure this returns the image data correctly

        if (image != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)  // Change to IMAGE_PNG if necessary
                    .body(image);
        } else {
            // Return a 404 status if the image is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    //{"", "/"} means that this method will handle requests to the /products URL and /products
    // / (with or without a trailing slash).
    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = productsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // Log the products to check if createdAt is populated
        for (Product product : products) {
            System.out.println("Product ID: " + product.getId());
            System.out.println("Name: " + product.getName());
            System.out.println("Created At: " + product.getCreatedAt());
        }

        model.addAttribute("products",products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage (Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto",productDto);
        return "products/CreateProduct";

    }
}
