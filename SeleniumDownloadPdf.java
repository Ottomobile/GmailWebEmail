import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumDownloadPdf {

    public static void main(String[] args) {
        // String webpageUrl = "https://en.wikipedia.org/wiki/Canada";
        if(args.length != 1){
            System.out.println("Incorrect number of arguments");
            System.out.println("SeleniumDownloadPdf {webpageUrl}");
            return;
        }
        
        String webpageUrl = args[0];
        downloadWebpageAsPdf(webpageUrl);
    }
    
    public static void downloadWebpageAsPdf(String webpageUrl){
        WebDriver driver;
        String baseUrl;
        
        try{
            // Setup
            // driver = new FirefoxDriver();
            System.setProperty("webdriver.chrome.driver", "/Users/OL/Documents/GmailWebSeleniumPDF/chromedriver");
            driver = new ChromeDriver();
            
            // Open online webpage to pdf converter
            baseUrl = "http://pdfcrowd.com/";
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            
            // Download pdf
            driver.get(baseUrl + "/");
            driver.findElement(By.id("uri")).clear();
            driver.findElement(By.id("uri")).sendKeys(webpageUrl);
            driver.findElement(By.id("submit-button-url")).click();
           
            System.out.println("Requested webpage to pdf conversion");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
