package com.shopwave;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShopwaveApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void cssIsServedFromStaticResources() throws Exception {
        mockMvc.perform(get("/css/styles.css"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(":root")))
            .andExpect(content().string(containsString(".product-grid")));
    }

    @Test
    void loginPageLoads() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Login")))
            .andExpect(content().string(containsString("Create your account")));
    }

    @Test
    void registerPageLoads() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Create account")))
            .andExpect(content().string(containsString("Login now")));
    }

    @Test
    void categorySearchLoadsProducts() throws Exception {
        mockMvc.perform(get("/").param("query", "Electronics"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Search results for Electronics")))
            .andExpect(content().string(containsString("Noise-Free Headphones")));
    }

    @Test
    void productDetailPageLoads() throws Exception {
        mockMvc.perform(get("/products/1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Add to Cart")))
            .andExpect(content().string(containsString("Buy Now")));
    }

    @Test
    void dealsAndDeliveryPagesLoad() throws Exception {
        mockMvc.perform(get("/deals"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Today's Deals")));

        mockMvc.perform(get("/delivery"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Delivery options")));
    }

    @Test
    void cartAddAndCheckoutFlowWorks() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/1").session(session).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("AirLite Sneakers")))
            .andExpect(content().string(containsString("Checkout")));

        mockMvc.perform(post("/checkout")
                .session(session)
                .with(user("test@example.com").roles("USER"))
                .with(csrf())
                .param("customerName", "Test User")
                .param("email", "test@example.com")
                .param("address", "123 Test Street"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/orders/*"));
    }

    @Test
    void ordersPageLoads() throws Exception {
        mockMvc.perform(get("/orders").with(user("test@example.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Your orders")));
    }

    @Test
    void checkoutRequiresLogin() throws Exception {
        mockMvc.perform(get("/checkout"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void registrationCreatesDatabaseUser() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("name", "Resume User")
                .param("email", "resume-user@example.com")
                .param("password", "secret123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
