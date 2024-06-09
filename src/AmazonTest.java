import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieManager {

    public static void saveCookies(WebDriver driver, String filePath) {
        Set<Cookie> cookies = driver.manage().getCookies();
        try (FileWriter fileWriter = new FileWriter(new File(filePath))) {
            for (Cookie cookie : cookies) {
                fileWriter.write(cookie.getName() + ";" + cookie.getValue() + ";" + cookie.getDomain() + ";" + cookie.getPath() + ";" + cookie.getExpiry() + ";" + cookie.isSecure() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCookies(WebDriver driver, String filePath) {
        try (FileReader fileReader = new FileReader(new File(filePath))) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(";");
                Cookie cookie = new Cookie(parts[0], parts[1], parts[2], parts[3], null, Boolean.parseBoolean(parts[5]));
                driver.manage().addCookie(cookie);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Nested
    class AmazonTest {

        WebDriver driver;

        @BeforeEach
        public void setUp() {
            // Set the path to your WebDriver executable
            System.setProperty("webdriver.chrome.driver", "src/chromedriver-mac-arm64/chromedriver");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            driver = new ChromeDriver(options);

            // Load Amazon and log in manually
            driver.get("https://www.amazon.com/");
            // Perform manual login steps here
            saveCookies(driver, "cookies.data");

            // Load cookies for subsequent tests
            loadCookies(driver, "cookies.data");
            driver.get("https://www.amazon.com/");
        }

        @Test
        public void testAmazonLaptopSearch() {
            // Step 1: Enter amazon.com and check the homepage
            driver.get("https://www.amazon.com/");
            assertTrue(driver.getTitle().contains("Amazon"));

            // Step 2: Search by the word “laptop”
            WebElement searchBox = driver.findElement(By.id("twotabsearchtextbox"));
            searchBox.sendKeys("laptop");
            driver.findElement(By.id("nav-search-submit-button")).click();


            // Step 3: Add non-discounted products in stock on the first page to the cart
            List<WebElement> products = driver.findElements(By.cssSelector(".s-main-slot .s-result-item"));
            for (WebElement product : products) {
               // WebElement price = product.findElement(By.cssSelector(".a-price-whole"));
               // WebElement inStock = product.findElement(By.cssSelector(".s-availability .a-color-success"));
                WebElement discount = product.findElement(By.cssSelector("[data-a-strike=\"true\"]"));

                if ( discount == null) {
                    WebElement addToCartButton = product.findElement(By.cssSelector(".s-add-to-cart-button"));
                    addToCartButton.click();

                }
            }
            // Step 4: Go to the cart and check if the products are correct
            driver.get("https://www.amazon.com/gp/cart/view.html?ref_=nav_cart");
            List<WebElement> cartItems = driver.findElements(By.cssSelector(".sc-list-item-content"));

            assertFalse(cartItems.isEmpty()); // Check if there are items in the cart
            // Further assertions can be added to verify the correct products
        }

        @AfterEach
        public void tearDown() {
            if (driver != null) {
                driver.quit();
            }
        }
    }}