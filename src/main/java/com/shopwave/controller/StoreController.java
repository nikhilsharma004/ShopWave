package com.shopwave.controller;

import com.shopwave.cart.Cart;
import com.shopwave.dto.CheckoutForm;
import com.shopwave.dto.RegisterForm;
import com.shopwave.model.AppUser;
import com.shopwave.model.CustomerOrder;
import com.shopwave.model.OrderItem;
import com.shopwave.model.Product;
import com.shopwave.repository.OrderRepository;
import com.shopwave.repository.ProductRepository;
import com.shopwave.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("cart")
public class StoreController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StoreController(
        ProductRepository productRepository,
        OrderRepository orderRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute("cart")
    Cart shoppingCart() {
        return new Cart();
    }

    @GetMapping("/")
    public String home(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String keyword,
        Model model
    ) {
        String searchTerm = keyword != null && !keyword.isBlank() ? keyword : query;
        List<Product> products = searchTerm == null || searchTerm.isBlank()
            ? productRepository.findAll()
            : productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(searchTerm, searchTerm);

        model.addAttribute("products", products);
        model.addAttribute("query", searchTerm);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
        @Valid @ModelAttribute RegisterForm registerForm,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (userRepository.existsByEmail(registerForm.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email is already registered");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        AppUser user = new AppUser(
            registerForm.getName(),
            registerForm.getEmail(),
            passwordEncoder.encode(registerForm.getPassword()),
            "ROLE_USER"
        );
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Account created. Please login to continue.");
        return "redirect:/login";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElseThrow();
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/deals")
    public String deals(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("query", "Today's Deals");
        return "deals";
    }

    @GetMapping("/delivery")
    public String delivery() {
        return "delivery";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, @ModelAttribute("cart") Cart cart, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElseThrow();
        cart.add(product);
        redirectAttributes.addFlashAttribute("message", product.getName() + " added to cart");
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @PostMapping("/cart/increase/{id}")
    public String increase(@PathVariable Long id, @ModelAttribute("cart") Cart cart) {
        cart.increase(id);
        return "redirect:/cart";
    }

    @PostMapping("/cart/decrease/{id}")
    public String decrease(@PathVariable Long id, @ModelAttribute("cart") Cart cart) {
        cart.decrease(id);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String remove(@PathVariable Long id, @ModelAttribute("cart") Cart cart) {
        cart.remove(id);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(@ModelAttribute("cart") Cart cart, Model model) {
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("checkoutForm", new CheckoutForm());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
        @Valid @ModelAttribute CheckoutForm checkoutForm,
        BindingResult bindingResult,
        @ModelAttribute("cart") Cart cart,
        RedirectAttributes redirectAttributes
    ) {
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        if (bindingResult.hasErrors()) {
            return "checkout";
        }

        List<OrderItem> items = cart.getItems().stream()
            .map(item -> new OrderItem(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice()
            ))
            .toList();

        CustomerOrder order = new CustomerOrder(
            checkoutForm.getCustomerName(),
            checkoutForm.getEmail(),
            checkoutForm.getAddress(),
            cart.getTotal(),
            items
        );
        orderRepository.save(order);
        cart.clear();

        redirectAttributes.addFlashAttribute("message", "Order #" + order.getId() + " placed successfully");
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/orders/{id}")
    public String orderConfirmation(@PathVariable Long id, Model model) {
        CustomerOrder order = orderRepository.findById(id).orElseThrow();
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "orders";
    }
}
