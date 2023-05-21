import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class SellerTest {

    private Seller seller;
    private DataManager dataManager;
    private JSONArray itemsJSON;

    private InputStream sysInBackup;
    private PrintStream sysOutBackup;

    @BeforeEach
    void setup() {

        this.dataManager = new DataManager("test");
        dataManager.readData();

        this.seller = new Seller("test", 20, dataManager);

        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;

        this.itemsJSON = new JSONArray(dataManager.getItemsJSON().toString());
    }

    @AfterEach
    void cleanup() {

        // Delete the reports that is written after each test.
        File availItemFile = new File("src/test/resources/reports/available_items.txt");
        File soldItemFile = new File("src/test/resources/reports/items_sold.txt");

        if (availItemFile.exists())
            availItemFile.delete();

        if (soldItemFile.exists())
            soldItemFile.delete();

        // write the unmodified itemsJSON back to the file.
        String path = String.format("src/test/resources/available_items.json");
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(path));
            out.write(itemsJSON.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();

        // Set the console back to normal
        System.setIn(this.sysInBackup);
        System.setOut(this.sysOutBackup);
    }

    /*
     * Helper method to get the input string and feed it into the input stream
     * and return the output string
     */
    public ByteArrayOutputStream writeInputOutput(String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        System.setIn(bais);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);
        return baos;
    }

    @Test
    void modifyItemValidInputsTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\ncategory\nChips\nname\n%s\nprice" +
                "\n2\namount\n15\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        items = dataManager.getItemsJSON();

        boolean modified = false;
        for (Object obj : items) {
            item = (JSONObject) obj;

            if (item.getInt("code") == code) {
                if (item.getString("name").equals(name))
                    modified = true;
            }
        }

        assertTrue(modified, "Error: The modify items failed with" +
                " a valid input.");

    }

    @Test
    void modifyItemInvalidCodeTest() {

        String userInput = "abcd\nDONE\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray items = dataManager.getItemsJSON();

        assertEquals(items.toString(), itemsJSON.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemNonExistentCodeTest() {

        String userInput = "-1001\nDONE\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray items = dataManager.getItemsJSON();

        assertEquals(items.toString(), itemsJSON.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemTooLargeCodeTest() {

        String userInput = "999999999999\nDONE\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray items = dataManager.getItemsJSON();

        assertEquals(items.toString(), itemsJSON.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemInvalidCategoryTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\ncategory\nRandomCategory\ncancel\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray newItems = dataManager.getItemsJSON();
        JSONObject newItem = null;

        for (Object obj : newItems) {
            JSONObject temp = (JSONObject) obj;

            if (temp.getInt("code") == code)
                newItem = temp;
        }

        assertEquals(newItem.toString(), item.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemInvalidNameTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\nname\n\ncancel\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray newItems = dataManager.getItemsJSON();
        JSONObject newItem = null;

        for (Object obj : newItems) {
            JSONObject temp = (JSONObject) obj;

            if (temp.getInt("code") == code)
                newItem = temp;
        }

        assertEquals(newItem.toString(), item.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemInvalidPriceTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\nprice\n-1\ncancel\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray newItems = dataManager.getItemsJSON();
        JSONObject newItem = null;

        for (Object obj : newItems) {
            JSONObject temp = (JSONObject) obj;

            if (temp.getInt("code") == code)
                newItem = temp;
        }

        assertEquals(newItem.toString(), item.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemInvalidAmountTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\namount\n-10\ncancel\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray newItems = dataManager.getItemsJSON();
        JSONObject newItem = null;

        for (Object obj : newItems) {
            JSONObject temp = (JSONObject) obj;

            if (temp.getInt("code") == code)
                newItem = temp;
        }

        assertEquals(newItem.toString(), item.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void modifyItemInvalidAttributeTest() {

        JSONArray items = dataManager.getItemsJSON();
        JSONObject item = items.getJSONObject(0);
        int code = item.getInt("code");

        String name = "random";

        String userInput = String.format("%d\nrandomAttribute\nDONE\nDONE\n", code, name);
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        seller.modifyItems(scan);


        dataManager.readData();
        JSONArray newItems = dataManager.getItemsJSON();
        JSONObject newItem = null;

        for (Object obj : newItems) {
            JSONObject temp = (JSONObject) obj;

            if (temp.getInt("code") == code)
                newItem = temp;
        }

        assertEquals(newItem.toString(), item.toString(),
                "Error: There should be no change after " +
                        "an invalid input.");

    }

    @Test
    void getItemItemExistsTest() {

        JSONArray itemsJSON = dataManager.getItemsJSON();
        JSONObject item = itemsJSON.getJSONObject(0);

        int code = -1;

        try {
            code = item.getInt("code");
        } catch (JSONException e) {
            System.out.println(String.format("Error: %s", e));
        }

        assertNotEquals(-1, code, "Error: Failed fetching" +
                " the code.");

        int result = seller.getItem(code, dataManager.getItemsJSON());

        assertEquals(0, result, "Error: Item should exist.");
    }

    @Test
    void getItemItemNotExistTest() {

        // There should not be an item with a negative code.
        int code = -1001;

        int result = seller.getItem(code, dataManager.getItemsJSON());

        assertEquals(-1, result, "Error: Item should " +
                "not exist.");
    }

    @Test
    void isValidCategoryEmptyInputTest() {

        boolean result = seller.isValidCategory("");

        assertFalse(result, "Error: Function should " +
                "reject empty input");
    }

    @Test
    void isValidCategoryNonExistentCategoryTest() {

        boolean result = seller.isValidCategory("RandomCategory");

        assertFalse(result, "Error: Function should " +
                "reject a non-existent category.");
    }

    @Test
    void isValidCategoryValidCategoryTest() {

        boolean result = seller.isValidCategory(seller.getCategories()[0]);

        assertTrue(result, "Error: Function should " +
                "accept a valid category.");
    }

    @Test
    void isValidNameEmptyInputTest() {

        boolean result = seller.isValidName("");

        assertFalse(result, "Error: Function should " +
                "reject empty input");
    }

    @Test
    void isValidNameInputLengthMoreThanMaxTest() {

        int max_word = seller.getMax_word();
        boolean result = seller.isValidName(
                StringUtils.repeat("a", max_word+1));

        assertFalse(result, "Error: Function should " +
                "reject input with length more than max");
    }

    @Test
    void isValidNameValidNameTest() {

        int max_word = seller.getMax_word();
        boolean result = seller.isValidName(
                StringUtils.repeat("a", max_word));

        assertTrue(result, "Error: Function should " +
                "accept a valid input");
    }

    @Test
    void isValidPriceEmptyInputTest() {

        float result = seller.isValidPrice("");

        assertEquals(result, -1,"Error: Function should " +
                "reject empty input");
    }

    @Test
    void isValidPriceNonFloatInputTest() {

        float result = seller.isValidPrice("abc");

        assertEquals(result, -1,"Error: Function should " +
                "reject a non-float input");
    }

    @Test
    void isValidPriceNegativeInputTest() {

        float result = seller.isValidPrice("-10");

        assertEquals(result, -1,"Error: Function should " +
                "a negative input");
    }

    @Test
    void isValidPriceTooLargeInputTest() {

        float result = seller.isValidPrice("999999999999");

        assertEquals(result, -1,"Error: Function should " +
                "an input that is too large");
    }

    @Test
    void isValidPriceValidPriceTest() {

        String priceStr = "10";

        float price = -1;

        try {
            price = Float.parseFloat(priceStr);
        } catch (NumberFormatException e) {
            System.out.println(String.format("Error: %s", e));
        }

        assertNotEquals(-1, price, "Error: Failed" +
                "in converting the priceStr to price float before " +
                "the actual test.");

        float result = seller.isValidPrice(priceStr);

        assertEquals(result, price,"Error: Function should " +
                "accept a valid price.");
    }

    @Test
    void isValidAmountEmptyInputTest() {

        int result = seller.isValidAmount("");

        assertEquals(result, -1, "Error: Function should " +
                "reject empty input");
    }

    @Test
    void isValidAmountNonIntegerInputTest() {

        int result = seller.isValidAmount("abc");

        assertEquals(result, -1, "Error: Function should " +
                "reject a non-integer input");
    }

    @Test
    void isValidAmountNegativeInputTest() {

        int result = seller.isValidAmount("-10");

        assertEquals(result, -1, "Error: Function should " +
                "reject a negative input");
    }

    @Test
    void isValidAmountMoreThan15InputTest() {

        int result = seller.isValidAmount("16");

        assertEquals(result, -1, "Error: Function should " +
                "reject an input above 15");
    }

    @Test
    void isValidAmountTooLargeInputTest() {

        int result = seller.isValidAmount("999999999999");

        assertEquals(result, -1, "Error: Function should " +
                "reject an input that is too large");
    }

    @Test
    void isValidAmountValidInputTest() {

        String amountStr = "10";
        int amount = -1;

        try {
            amount = Integer.parseInt(amountStr);
        } catch(NumberFormatException e) {
            System.out.println(String.format("Error: %s", e));
        }

        assertNotEquals(-1, amount, "Error: Failed in " +
                "converting amountStr to amount integer before the actual" +
                "test.");

        int result = seller.isValidAmount(amountStr);

        assertEquals(result, amount, "Error: Function should " +
                "accept a valid input.");
    }

    @Test
    void writeAvailableItemReportFolderExistTest() {

        if (!seller.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.mkdir();
        }

        seller.writeAvailableItemReport();

        File item_file = new File("src/test/resources/reports" +
                "/available_items.txt");
        boolean exists = item_file.exists();

        assertTrue(exists, "Error: File should exists.");

    }

    @Test
    void writeAvailableItemReportFolderDoesNotExistTest() {

        if (seller.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.delete();
        }

        seller.writeAvailableItemReport();

        File item_file = new File("src/test/resources/reports" +
                "/available_items.txt");
        boolean exists = item_file.exists();

        assertTrue(exists, "Error: File should exists.");

    }

    @Test
    void writeSoldItemReportFolderExistTest() {

        if (!seller.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.mkdir();
        }

        seller.writeSoldItemReport();

        File item_file = new File("src/test/resources/reports" +
                "/items_sold.txt");
        boolean exists = item_file.exists();

        assertTrue(exists, "Error: File should exists.");

    }

    @Test
    void writeSoldItemReportFolderDoesNotExistTest() {

        if (seller.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.delete();
        }

        seller.writeSoldItemReport();

        File item_file = new File("src/test/resources/reports" +
                "/items_sold.txt");
        boolean exists = item_file.exists();

        assertTrue(exists, "Error: File should exists.");

    }

    @Test
    void deleteAvailableItemReportReportExistTest() {

        seller.writeAvailableItemReport();

        boolean succeed = seller.deleteAvailableItemReport();

        assertTrue(succeed, "Error: File should be deleted.");

    }

    @Test
    void deleteAvailableItemReportReportDoesNotExistTest() {

        File f = new File("src/test/resources/reports/available_items.txt");

        if (f.exists())
            f.delete();

        boolean succeed = seller.deleteAvailableItemReport();

        assertFalse(succeed, "Error: File should not exists to begin with" +
                " so there's nothing to be deleted.");

    }

    @Test
    void deleteSoldItemReportReportExistTest() {

        seller.writeSoldItemReport();

        boolean succeed = seller.deleteSoldItemReport();

        assertTrue(succeed, "Error: File should be deleted.");

    }

    @Test
    void deleteSoldItemReportReportDoesNotExistTest() {

        File f = new File("src/test/resources/reports/items_sold.txt");

        if (f.exists())
            f.delete();

        boolean succeed = seller.deleteSoldItemReport();

        assertFalse(succeed, "Error: File should not exists to begin with" +
                " so there's nothing to be deleted.");

    }

    @Test
    void printAvailableItemTest() {

        String expected = seller.constructAvailableItem();
        String text = null;

        try{
            text = tapSystemOut(() -> {
                seller.printAvailableItem();
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(expected, text, "printAvailableItemTest failed");
    }

    @Test
    void printSoldItemTest() {

        String expected = seller.constructSoldItem();
        String text = null;

        try{
            text = tapSystemOut(() -> {
                seller.printSoldItem();
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(expected, text, "printSoldItemTest failed");
    }

}
