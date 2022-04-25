import com.excel.lib.util.Xls_Reader;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//get only today and previous date
//convert old and today price to int
//upload to github

public class TheFlyScraping {
    public static void SetUp() {

        System.setProperty("webdriver.gecko.driver", "C:/TheFly/Drivers/geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "C:/TheFly/Drivers/chromedriver.exe");
    }

    public static void Scroll(WebDriver driver) {
        for (int i = 0; i < 20; i++) {

            driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println();
            }

            if (i == 0) {
                ((JavascriptExecutor) driver).executeScript("scroll(0,-200)");
            }

            System.out.print(((i * 5) + 5) + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        SetUp();
        //headless mode commands, must come above WebDriver driver = new ChromeDriver(options);
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        //must add (options) to ChromeDriver for headless
        WebDriver driver = new ChromeDriver();
        driver.get("https://thefly.com/news.php");
        System.out.println("thefly.com has been loaded \nscrolling through page");
        Scroll(driver);
        System.out.println("completed scrolling through page");
        System.out.println("scraping elements from page");

        List<WebElement> HeadlineList = driver.findElements(By.xpath("//span[contains (text(), 'price target raised' ) ]"));
        List<WebElement> TickerList = driver.findElements(By.xpath("//span[contains(text(), 'price target raised')]/parent::a/following-sibling::div[@class = 'simbolos_wrapper']/span[1]"));
        List<WebElement> TimeList = driver.findElements(By.xpath("//span[contains (text(), 'price target raised' ) ]/../..//span[@class='fpo_overlay soloHora']"));
        List<WebElement> DateList = driver.findElements(By.xpath("//span[contains(text(), 'price target raised')]/parent::a/following-sibling::span[@class = 'time_date']//span[@class = 'fpo_overlay soloHora']/div[@class = 'fpo_overlay_ticker']"));
        System.out.println("completed scraping elements from page");

        DateTimeFormatter bro = DateTimeFormatter.ofPattern("MM/dd/yy");
        LocalDateTime uh = LocalDateTime.now();
        String dateCheck = bro.format(uh);
        System.out.println(dateCheck);
        int changeNumber = 0;
        for (changeNumber = 0; changeNumber < DateList.size(); changeNumber++){
            if(DateList.get(changeNumber).getAttribute("innerText").equals(dateCheck)) {
                System.out.print("");
            }
            else {
                break;
            }
        }

        System.out.println("splitting headline elements");

        String[][] StringArray = new String[HeadlineList.size()][5];

        for (int j = 0; j < HeadlineList.size(); j++) {
            Pattern pattern = Pattern.compile("^(.*) price.*to (.*?)\\s?([\\d,.]+)\\s?(.*?) from (.*?)\\s?([\\d,.]+)\\s?(.*?) at (.*)$");
            Matcher matcher = pattern.matcher(HeadlineList.get(j).getText());
                while (matcher.find()) {
                    StringArray[j][0] = matcher.group(1); //company name
                    StringArray[j][1] = matcher.group(3); //new price
                    StringArray[j][2] = matcher.group(6); //old price
                    StringArray[j][3] = matcher.group(2); //currency
                    if(StringArray[j][3].equals("")){
                        StringArray[j][3] = matcher.group(4);
                    }
                    StringArray[j][4] = matcher.group(8); //analyst
                }

                if(StringArray[j][0] == null) {
                    pattern = Pattern.compile("^(.*) price.*to (.*?)\\s?([\\d,.]+)\\s?(.*?) at (.*)$");
                    matcher = pattern.matcher(HeadlineList.get(j).getText());
                    while (matcher.find()) {
                        StringArray[j][0] = matcher.group(1); //company name
                        StringArray[j][1] = matcher.group(3); //new price
                        StringArray[j][2] = "";
                        StringArray[j][3] = matcher.group(2); //currency
                        if(StringArray[j][3].equals("")){
                            StringArray[j][3] = matcher.group(4);
                        }
                        StringArray[j][4] = matcher.group(5); //analyst
                    }
                }
            }

        System.out.println("completed splitting headline elements");

//        System.out.println("converting prices to integers");
//
//        double[][] intArray = new double[HeadlineList.size()][5];
//
//        System.out.println("Converting Strings to Ints");
//        for (int i = 0; i < HeadlineList.size(); i++) {
//            intArray[i][1] = Double.parseDouble(StringArray[i][1].replaceAll(",", ""));
//            if(StringArray[i][2].equals("")) {
//                System.out.print("");
//            } else {
//                intArray[i][2] = Double.parseDouble(StringArray[i][2].replaceAll(",", ""));
//            }
//        }
//        System.out.println("Completed Converting Strings to Ints");


        Xls_Reader reader = new Xls_Reader("C:/TheFly/StockData.xlsx"); //"C:/Users/sahar/Downloads/Test.xlsx"

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM dd yyyy");
        LocalDateTime now = LocalDateTime.now();
        String sheetName = dtf.format(now);



        if(reader.isSheetExist(sheetName)) {
            reader.removeSheet(sheetName);
        }

        reader.addSheet(sheetName);

        System.out.println("Created new sheet");

        reader.addColumn(sheetName, "Ticker");
        reader.addColumn(sheetName, "Company Name");
        reader.addColumn(sheetName, "New Price Target");
        reader.addColumn(sheetName, "Old Price Target");
        reader.addColumn(sheetName, "Currency");
        reader.addColumn(sheetName, "Analyst");
        reader.addColumn(sheetName, "Time");
        reader.addColumn(sheetName, "Date");
        System.out.println("Completed adding columns");

        System.out.println("Adding element data to sheet");

        for (int i = 0; i < changeNumber; i++) {
            reader.setCellData(sheetName, "Company Name", i + 2, StringArray[i][0]);
            reader.setCellData(sheetName, "New Price Target", i + 2, StringArray[i][1]);
            reader.setCellData(sheetName, "Old Price Target", i + 2, StringArray[i][2]);
            reader.setCellData(sheetName, "Currency", i + 2, StringArray[i][3]);
            reader.setCellData(sheetName, "Analyst", i + 2, StringArray[i][4]);
            reader.setCellData(sheetName, "Ticker", i + 2, (TickerList.get(i).getText()));
            reader.setCellData(sheetName, "Time", i + 2, (TimeList.get(i).getText()));
            reader.setCellData(sheetName, "Date", i + 2, (DateList.get(i).getAttribute("innerText")));

            if(i%5==0) {
                System.out.println(((double) i / changeNumber * 100) + " ");
            }
        }

        System.out.println("Finished adding element data to sheet");

        driver.close();
        driver.quit();
        System.out.println("Chrome window closed");
        System.out.println("Program has finished");
    }

}
//excel gets corrupted because if its open, it will be keep making new sheets and trying to see if error goes away
//removed while loop