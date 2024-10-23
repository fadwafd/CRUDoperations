package com.fadwa.fd_store.controllers;

import com.fadwa.fd_store.models.ProductDto;
import com.fadwa.fd_store.services.ProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import com.fadwa.fd_store.models.Product;
import com.fadwa.fd_store.services.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private ProductsService productService;
    @Value("${file.upload-dir}")
    private String uploadDir;


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

    @PostMapping("/create")
    public String createProduct (
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result) {

        if(productDto.getImagefile().isEmpty()) {
            result.addError(new FieldError("productDto", "imagefile", "The image file is required"));
        }

        if (result.hasErrors()) {
            return "products/createProduct";
        }

        // Save the image file and store its path
        MultipartFile image = productDto.getImagefile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            // Ensure the upload directory exists
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // create directory if it doesn't exist
            }

            // Save the image file in the directory
            try (InputStream inputStream = image.getInputStream()) {
                Path filePath = uploadPath.resolve(storageFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                System.out.println("Exception while saving image: " + ex.getMessage());
            }

            // Save the product data to the database
            Product product = new Product();
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setCreatedAt(createdAt);

            // Store only the image file name in the database (not the file bytes)
            product.setImagefile(storageFileName.getBytes()); // OR store only the filename as String

            productsRepository.save(product);

            return "redirect:/products";
        } catch (IOException e) {
            throw new RuntimeException("Error saving product image", e);
        }
    }

    @GetMapping("/edit")
    public String ShowEditPage ( Model model, @RequestParam int id) {
        try {
            Product product = productsRepository.findById(id).get();
            model.addAttribute("product",product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());

            model.addAttribute("productDto", productDto);
        }
        catch (Exception ex) {
            System.out.println("Exception: " +ex.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct (Model model,
                                 @RequestParam int id,
                                 @Valid @ModelAttribute ProductDto productDto,
                                 BindingResult result){

        try {
            Product product = productsRepository.findById(id).get();
            model.addAttribute("product",product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if(!productDto.getImagefile().isEmpty()) {
                //delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImagefile());

                try {
                    Files.delete(oldImagePath);
                }
                catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // Save new image file
                MultipartFile image = productDto.getImagefile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImagefile(storageFileName.getBytes());
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            productsRepository.save(product);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/products";

    }
}