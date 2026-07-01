package com.shopwave.config;

import com.shopwave.model.AppUser;
import com.shopwave.model.Product;
import com.shopwave.repository.ProductRepository;
import com.shopwave.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ProductRepository productRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("demo@shopwave.com")) {
            userRepository.save(new AppUser(
                "Demo User",
                "demo@shopwave.com",
                passwordEncoder.encode("password123"),
                "ROLE_USER"
            ));
        }

        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
            new Product(
                "AirLite Sneakers",
                "Breathable everyday sneakers with cloud-soft cushioning.",
                new BigDecimal("2499.00"),
                "Footwear",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
                42
            ),
            new Product(
                "Urban Backpack",
                "Water-resistant backpack with padded laptop storage and travel pockets.",
                new BigDecimal("1899.00"),
                "Bags",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80",
                25
            ),
            new Product(
                "Noise-Free Headphones",
                "Wireless over-ear headphones tuned for work, music, and calls.",
                new BigDecimal("5499.00"),
                "Electronics",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
                18
            ),
            new Product(
                "Smart Desk Lamp",
                "Dimmable LED lamp with warm/cool modes and USB-C charging.",
                new BigDecimal("1299.00"),
                "Home",
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80",
                37
            ),
            new Product(
                "Cotton Overshirt",
                "Relaxed cotton overshirt for layered casual styling.",
                new BigDecimal("1599.00"),
                "Fashion",
                "https://images.unsplash.com/photo-1523398002811-999ca8dec234?auto=format&fit=crop&w=900&q=80",
                31
            ),
            new Product(
                "Ceramic Coffee Set",
                "Minimal four-piece coffee mug set with a satin glaze finish.",
                new BigDecimal("999.00"),
                "Kitchen",
                "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=900&q=80",
                54
            )
        ));
    }
}
