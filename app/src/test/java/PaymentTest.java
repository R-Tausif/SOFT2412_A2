import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.*;
public class PaymentTest {
    private Payment pay;
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;
    private DataManager data;
    private JSONObject cash;
    private JSONArray items;

    @BeforeEach
    public void setUp() {
        this.data = new DataManager("test");
        this.data.readData();
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
        this.pay = new Payment(data, "test");
        this.cash = this.data.getCashJSON();
        this.items = this.data.getItemsJSON();
    }

    @AfterEach
    public void cleanUp() {
        String cashFile = "src/test/resources/available_cash.json";
        String itemFile = "src/test/resources/available_items.json";
        String histFile = "src/test/resources/transaction_hist.json";
        String userFile = "src/test/resources/user_data.json";

        String copyCash = "src/test/resources/check_cash.json";
        String copyItems = "src/test/resources/check_items.json";
        String copyHist = "src/test/resources/check_transac.json";
        String copyUser = "src/test/resources/check_user.json";

        // Rewrite the available_cash.json and available_items.json back to original state
        PrintWriter output = null;
        PrintWriter output2 = null;
        PrintWriter output3 = null;
        PrintWriter output4 = null;

        Scanner scan = null;
        Scanner scan2 = null;
        Scanner scan3 = null;
        Scanner scan4 = null;
        try {
            scan = new Scanner(new File(copyCash));
            String json = scan.useDelimiter("\\Z").next();

            output = new PrintWriter(new FileOutputStream(cashFile));
            output.write(json);

            scan2 = new Scanner(new File(copyItems));
            String json2 = scan2.useDelimiter("\\Z").next();

            output2 = new PrintWriter(new FileOutputStream(itemFile));
            output2.write(json2);

            scan3 = new Scanner(new File(copyHist));
            String json3 = scan3.useDelimiter("\\Z").next();

            output3 = new PrintWriter(new FileOutputStream(histFile));
            output3.write(json3);

            scan4 = new Scanner(new File(copyUser));
            String json4 = scan4.useDelimiter("\\Z").next();

            output4 = new PrintWriter(new FileOutputStream(userFile));
            output4.write(json4);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        output.close();
        output2.close();
        output3.close();
        output4.close();
        scan.close();
        scan2.close();
        scan3.close();
        scan4.close();

        // Set the console back to normal
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void testCheckValidProductAllValid() {
        HashMap<String, JSONObject> products = this.arrangeItems();

        String[] responses = {"1001", "1002", "1003"};
        String answer = this.pay.checkValidProduct(products, responses);
        assertEquals(answer, "");
    }

    @Test
    public void testCheckValidProductValidRepeated() {
        HashMap<String, JSONObject> products = this.arrangeItems();

        String[] responses = {"1001", "1002", "1001", "1003", "1002"};
        String answer = this.pay.checkValidProduct(products, responses);
        assertEquals(answer, "");
        assertEquals(String.join(", ", this.pay.getProductCode()), "1003, 1002, 1001");
    }

    @Test
    public void testCheckValidProductInvalidCode() {
        HashMap<String, JSONObject> products = this.arrangeItems();

        String[] responses = {"01001", "1002", "a", "1003", "hello"};
        String answer = this.pay.checkValidProduct(products, responses);
        assertEquals(answer, "01001, a, hello");
    }

    @Test
    public void testCheckValidQuantityAllValid() {
        HashMap<String, JSONObject> products = this.arrangeItems();

        String code = "1001";
        String response = "7";
        String response2 = "6";
        String response3 = "1";
        assertTrue(this.pay.checkValidQuantity(response, code, products));
        assertTrue(this.pay.checkValidQuantity(response2, code, products));
        assertTrue(this.pay.checkValidQuantity(response3, code, products));
    }

    @Test
    public void testCheckValidQuantityNotPositiveWhole() {
        HashMap<String, JSONObject> products = this.arrangeItems();
        ByteArrayOutputStream baos = this.writeInputOutput("");

        String code = "1001";
        String response = "a";
        String response2 = "1.01";
        String response3 = "-5";
        Boolean answer = this.pay.checkValidQuantity(response, code, products);
        Boolean answer2 = this.pay.checkValidQuantity(response2, code, products);
        Boolean answer3 = this.pay.checkValidQuantity(response3, code, products);
        String[] lines = baos.toString().split("\n");

        assertFalse(answer);
        assertFalse(answer2);
        assertFalse(answer3);
        assertEquals(lines[1].trim(), "Quantities needs to be in whole numbers, please try again!");
        assertEquals(lines[3].trim(), "Quantities needs to be in whole numbers, please try again!");
        assertEquals(lines[5].trim(), "Quantities should be positive integers, please try again!");
    }

    @Test
    public void testCheckValidQuantityTooLargeTooSmall() {
        HashMap<String, JSONObject> products = this.arrangeItems();
        ByteArrayOutputStream baos = this.writeInputOutput("");

        String code = "1001";
        String response = "15";
        String response2 = "8";
        String response3 = "0";
        Boolean answer = this.pay.checkValidQuantity(response, code, products);
        Boolean answer2 = this.pay.checkValidQuantity(response2, code, products);
        Boolean answer3 = this.pay.checkValidQuantity(response3, code, products);
        String[] lines = baos.toString().split("\n");

        assertFalse(answer);
        assertFalse(answer2);
        assertFalse(answer3);
        assertEquals(lines[1].trim(), "There is currently only 7 Mineral Water available, please try again!");
        assertEquals(lines[3].trim(), "There is currently only 7 Mineral Water available, please try again!");
        assertEquals(lines[5].trim(), "Quantities should be positive integers, please try again!");
    }

    @Test
    public void testPrintPriceList() {
        HashMap<String, JSONObject> products = this.arrangeItems();
        ByteArrayOutputStream baos = this.writeInputOutput("");

        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1003", 1);
        quantity.put("1002", 2);
        quantity.put("1001", 3);
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1003");
        purchase.add("1002");
        purchase.add("1001");
        Double total = 10.0;

        this.pay.printPriceList(quantity, products, purchase, total);
        String[] lines = baos.toString().split("\n");

        assertEquals(lines[1].trim(), "You have purchased the following items");
        assertEquals(lines[2].trim(), "Mineral Water x3 @ $2.00 each: $6.00");
        assertEquals(lines[3].trim(), "Sprite x2 @ $1.50 each: $3.00");
        assertEquals(lines[4].trim(), "Coca cola x1 @ $1.00 each: $1.00");
        assertEquals(lines[6].trim(), "Total price: $10.00");
    }

    @Test
    public void testPrintCash() {
        HashMap<String, JSONObject> products = this.arrangeItems();
        ByteArrayOutputStream baos = this.writeInputOutput("");

        double[] changes = {5.0, 2.0, 1.0};
        HashMap<Double, Integer> validNotes = new HashMap<>();
        validNotes.put(5.0, 1);
        validNotes.put(2.0, 2);
        validNotes.put(1.0, 1);
        int totalCash = 1000;
        String msg = "provided";

        this.pay.printCash(changes, validNotes, totalCash, msg);
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "$5.00 x1");
        assertEquals(lines[1].trim(), "$2.00 x2");
        assertEquals(lines[2].trim(), "$1.00 x1");
        assertEquals(lines[3].trim(), "Total cash provided: $10.00");
    }

    @Test
    public void testIncreDecreCashIncrease() {
        JSONObject cashCopied = new JSONObject(this.cash.toString());
        HashMap<Double, Integer> validNotes = new HashMap<>();
        validNotes.put(5.0, 1);
        validNotes.put(2.0, 2);
        validNotes.put(0.5, 2);

        ArrayList<Double> keyCash = new ArrayList<>();
        keyCash.add(5.0);
        keyCash.add(2.0);
        keyCash.add(0.5);

        this.pay.increDecreCash(cashCopied, validNotes, keyCash, true);
        assertEquals(cashCopied.getInt("5"), 3);
        assertEquals(cashCopied.getInt("2"), 4);
        assertEquals(cashCopied.getInt("0.5"), 2);
    }

    @Test
    public void testIncreDecreCashDecrease() {
        JSONObject cashCopied = new JSONObject(this.cash.toString());
        HashMap<Double, Integer> validNotes = new HashMap<>();
        validNotes.put(5.0, 1);
        validNotes.put(2.0, 2);
        validNotes.put(0.2, 5);

        ArrayList<Double> keyCash = new ArrayList<>();
        keyCash.add(5.0);
        keyCash.add(2.0);
        keyCash.add(0.2);

        this.pay.increDecreCash(cashCopied, validNotes, keyCash, false);
        assertEquals(cashCopied.getInt("5"), 1);
        assertEquals(cashCopied.getInt("2"), 0);
        assertEquals(cashCopied.getInt("0.2"), 0);
    }

    @Test
    public void testCashChangeValid() {
        HashMap<Double, Integer> changes = this.pay.cashChange(1200, 2000, this.cash);
        assertTrue(changes.containsKey(5.0));
        assertTrue(changes.containsKey(2.0));
        assertTrue(changes.containsKey(0.2));
        assertEquals(changes.get(5.0), 1);
        assertEquals(changes.get(2.0), 1);
        assertEquals(changes.get(0.2), 5);
    }

    @Test
    public void testCashChangeValid2() {
        HashMap<Double, Integer> changes = this.pay.cashChange(1500, 3000, this.cash);
        assertTrue(changes.containsKey(5.0));
        assertTrue(changes.containsKey(2.0));
        assertTrue(changes.containsKey(0.2));
        assertEquals(changes.get(5.0), 2);
        assertEquals(changes.get(2.0), 2);
        assertEquals(changes.get(0.2), 5);
    }

    @Test
    public void testCashChangeInvalid() {
        HashMap<Double, Integer> changes = this.pay.cashChange(1400, 3000, this.cash);
        assertNull(changes);
    }

    @Test
    public void testDecreaseStock() {
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1001", 3);
        quantity.put("1002", 2);
        quantity.put("1003", 1);
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1001");
        purchase.add("1002");
        purchase.add("1003");

        this.pay.decreaseStock(quantity, purchase);
        HashMap<String, JSONObject> products = this.arrangeItems();
        assertEquals(products.get("1001").getInt("amount"), 4);
        assertEquals(products.get("1002").getInt("amount"), 5);
        assertEquals(products.get("1003").getInt("amount"), 6);
    }

    @Test
    public void testAskCashInputValidExact() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");
        double price = 6.0;
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1001", 3);

        HashMap<String, JSONObject> availableProduct = this.arrangeItems();
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1001");

        String userInput = "5.0,1.0\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        payment.askCashInput(price, quantity, availableProduct, purchase, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[2].trim(), "********************** Receipt **********************");
        assertEquals(lines[4].trim(), "You have purchased the following items");
        assertEquals(lines[5].trim(), "Mineral Water x3 @ $2.00 each: $6.00");
        assertEquals(lines[7].trim(), "Total price: $6.00");
        assertEquals(lines[9].trim(), "You have input the following note value x quantity");
        assertEquals(lines[10].trim(), "$5.00 x1");
        assertEquals(lines[11].trim(), "$1.00 x1");
        assertEquals(lines[12].trim(), "Total cash provided: $6.00");
        assertEquals(lines[14].trim(), "Your change is: $0.00");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(0).getInt("amount"), 4);
        assertEquals(obj.getInt("5"), 3);
        assertEquals(obj.getInt("1"), 1);
    }

    @Test
    public void testAskCashInputValidMore() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");
        double price = 9.0;
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1002", 2);
        quantity.put("1001", 3);

        HashMap<String, JSONObject> availableProduct = this.arrangeItems();
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1002");
        purchase.add("1001");

        String userInput = "5.0,2.0,2.0,1.0,1.0\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        payment.askCashInput(price, quantity, availableProduct, purchase, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[2].trim(), "********************** Receipt **********************");
        assertEquals(lines[5].trim(), "Mineral Water x3 @ $2.00 each: $6.00");
        assertEquals(lines[6].trim(), "Sprite x2 @ $1.50 each: $3.00");
        assertEquals(lines[8].trim(), "Total price: $9.00");
        assertEquals(lines[10].trim(), "You have input the following note value x quantity");
        assertEquals(lines[11].trim(), "$5.00 x1");
        assertEquals(lines[12].trim(), "$2.00 x2");
        assertEquals(lines[13].trim(), "$1.00 x2");
        assertEquals(lines[14].trim(), "Total cash provided: $11.00");
        assertEquals(lines[16].trim(), "Your change is:");
        assertEquals(lines[17].trim(), "$2.00 x1");
        assertEquals(lines[18].trim(), "Total cash given back: $2.00");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(1).getInt("amount"), 5);
        assertEquals(obj.getInt("5"), 3);
        assertEquals(obj.getInt("2"), 3);
        assertEquals(obj.getInt("1"), 2);
    }

    @Test
    public void testAskCashInputNotEnoughChange() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");
        double price = 1.5;
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1002", 1);

        HashMap<String, JSONObject> availableProduct = this.arrangeItems();
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1002");

        String userInput = "20,5\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        payment.askCashInput(price, quantity, availableProduct, purchase, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[2].trim(), "There is not enough change can be given for this payment.");
        assertEquals(lines[3].trim(), "*********************** Refund ***********************");
        assertEquals(lines[4].trim(), "Following cash are returned: note value x quantity");
        assertEquals(lines[5].trim(), "$20.00 x1");
        assertEquals(lines[6].trim(), "$5.00 x1");
        assertEquals(lines[7].trim(), "Total cash provided: $25.00");
        assertEquals(lines[8].trim(), "*********************** Refund ***********************");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(1).getInt("amount"), 7);
        assertEquals(obj.getInt("20"), 0);
        assertEquals(obj.getInt("5"), 2);
    }

    @Test
    public void testAskCashInputInvalidDollar() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");
        double price = 1.5;
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1002", 1);

        HashMap<String, JSONObject> availableProduct = this.arrangeItems();
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1002");

        String userInput = "a,5,c,0.13,3\n5,   2\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        payment.askCashInput(price, quantity, availableProduct, purchase, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[2].trim(), "a, c, 0.13, 3 are invalid input, only the following note values will be accepted:");
        assertEquals(lines[4].trim(), "Please try again!");
        assertEquals(lines[6].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[8].trim(), "********************** Receipt **********************");
        assertEquals(lines[15].trim(), "You have input the following note value x quantity");
        assertEquals(lines[16].trim(), "$5.00 x1");
        assertEquals(lines[17].trim(), "$2.00 x1");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(1).getInt("amount"), 6);
        assertEquals(obj.getInt("5"), 2);
        assertEquals(obj.getInt("2"), 3);
        assertEquals(obj.getInt("0.2"),3);
        assertEquals(obj.getInt("0.1"), 0);
    }

    @Test
    public void testAskCashInputNotEnoughCash() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");
        double price = 6.0;
        HashMap<String, Integer> quantity = new HashMap<>();
        quantity.put("1001", 3);

        HashMap<String, JSONObject> availableProduct = this.arrangeItems();
        ArrayList<String> purchase = new ArrayList<>();
        purchase.add("1001");

        String userInput = "5\n5,  1\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);

        payment.askCashInput(price, quantity, availableProduct, purchase, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        assertEquals(lines[2].trim(), "Not enough cash is provided, please try again!");
        assertEquals(lines[3].trim(), "*********************** Refund ***********************");
        assertEquals(lines[4].trim(), "Following cash are returned: note value x quantity");
        assertEquals(lines[5].trim(), "$5.00 x1");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(0).getInt("amount"), 4);
        assertEquals(obj.getInt("5"), 3);
        assertEquals(obj.getInt("1"), 1);
    }

    @Test
    public void testAskProductValid() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");

        String userInput = "1001,1002,1003\n3\n2\n1\ncash\n5,5\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);
        User user = new User();
        user.setName("anonymous");
        user.setRole("customer");

        payment.askProduct(user, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[1].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[3].trim(), "If want to specify multiple products, please separate the product code by comma eg {1002, 1003}");
        assertEquals(lines[5].trim(), "Please specify the corresponding quantities for each product");

        assertEquals(lines[6].trim(), "Product: Mineral Water (1001), Quantity: Product: Sprite (1002), Quantity: Product: Coca cola (1003), Quantity: Total price that needs to be paid: $10.00");
        assertEquals(lines[7].trim(), "Do you want to pay by cash or card? (cash/card):Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(0).getInt("amount"), 4);
        assertEquals(arr.getJSONObject(1).getInt("amount"), 5);
        assertEquals(arr.getJSONObject(2).getInt("amount"), 6);
        assertEquals(obj.getInt("5"), 4);
    }

    @Test
    public void testAskProductInvalidCodeQuantity() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");

        String userInput = "1001,10002,1003\n1001, 1002, 1003, 1001\na\n3\n9\n2\n-1\n1\neftpos\ncash\n5,5\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);

        Scanner scan = new Scanner(System.in);
        User user = new User();
        user.setName("anonymous");
        user.setRole("customer");

        payment.askProduct(user, scan);

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[1].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[5].trim(), "Product codes: 10002 does not exists!");
        assertEquals(lines[6].trim(), "Please select from the following product codes");
        assertEquals(lines[9].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[13].trim(), "Please specify the corresponding quantities for each product");
        assertEquals(lines[14].trim(), "Product: Mineral Water (1001), Quantity:");
        assertEquals(lines[15].trim(), "Quantities needs to be in whole numbers, please try again!");
        assertEquals(lines[17].trim(), "Product: Mineral Water (1001), Quantity: Product: Sprite (1002), Quantity:");
        assertEquals(lines[18].trim(), "There is currently only 7 Sprite available, please try again!");
        assertEquals(lines[20].trim(), "Product: Sprite (1002), Quantity: Product: Coca cola (1003), Quantity:");
        assertEquals(lines[21].trim(), "Quantities should be positive integers, please try again!");
        assertEquals(lines[23].trim(), "Product: Coca cola (1003), Quantity: Total price that needs to be paid: $10.00");
        assertTrue(lines[24].trim().startsWith("Do you want to pay by cash or card? (cash/card):Invalid input, please only select cash or card:"));

        JSONArray arr = datamanager.getItemsJSON();
        JSONObject obj = datamanager.getCashJSON();
        assertEquals(arr.getJSONObject(0).getInt("amount"), 4);
        assertEquals(arr.getJSONObject(1).getInt("amount"), 5);
        assertEquals(arr.getJSONObject(2).getInt("amount"), 6);
        assertEquals(obj.getInt("5"), 4);
    }

    @Test
    public void testCheckValidCash() {
        String[] dollars = {"a", "15", "5", "10", "00.2","1.7", "2", "5"};
        String ans = pay.checkValidCash("", dollars, "cashier");
        assertEquals(ans, "a, 15, 1.7");
    }

    @Test
    public void testUpdateLastFiveItems() {
        HashMap<String, JSONObject> availableItems = this.arrangeItems();
        ArrayList<String> purchases = new ArrayList<>();
        purchases.add("3001");
        purchases.add("3002");
        User customer = new User();
        customer.setName("John");
        customer.setRole("customer");

        pay.setUserObj(customer);
        pay.updateLastFiveItems(purchases, availableItems);
        JSONObject obj = pay.getUserObj();
        assertTrue(obj.has("history"));
        JSONArray history = obj.getJSONArray("history");
        assertEquals(history.length(), 2);
        assertEquals(history.get(0), "Smiths");
        assertEquals(history.get(1), "Pringles");
    }

    @Test
    public void testUpdateLastFiveItemsRepeated() {
        HashMap<String, JSONObject> availableItems = this.arrangeItems();
        ArrayList<String> purchases = new ArrayList<>();
        purchases.add("1004");
        purchases.add("3001");
        purchases.add("3002");
        User customer = new User();
        customer.setName("user");
        customer.setRole("customer");

        pay.setUserObj(customer);
        pay.updateLastFiveItems(purchases, availableItems);
        JSONObject obj = pay.getUserObj();
        assertTrue(obj.has("history"));
        JSONArray history = obj.getJSONArray("history");
        assertEquals(history.length(), 5);
        assertEquals(history.get(0), "Mars");
        assertEquals(history.get(1), "Bounty");
        assertEquals(history.get(2), "Pepsi");
        assertEquals(history.get(3), "Smiths");
        assertEquals(history.get(4), "Pringles");
    }

    @Test
    public void testPayByCreditSavedCard() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");

        String userInput = "1001,1002,1001, 1002,  1003\n3\n3\n3\ncard\nyes\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);
        User user = new User();
        user.setName("user");
        user.setRole("customer");

        payment.askProduct(user, new Scanner(System.in));
        String[] lines = baos.toString().split("\n");

        assertEquals(lines[1].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[2].trim(), "(You can type CANCEL to cancel the transaction in any process of purchasing)");
        assertEquals(lines[7].trim(), "Do you want to pay by cash or card? (cash/card):Do you want to pay by the stored card? (yes/no) " +
                "You have successfully paid by your stored card:)");
    }

    @Test
    public void testPayByCreditNotSavedCard() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");

        String userInput = "1001,1002,1001, 1002,  1003\n3\n3\n3\ncard\nna\nno\nKasey\n60146\nno\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);
        User user = new User();
        user.setName("user");
        user.setRole("customer");

        payment.askProduct(user, new Scanner(System.in));
        String[] lines = baos.toString().split("\n");

        assertEquals(lines[1].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[7].trim(), "Do you want to pay by cash or card? (cash/card):Do you want to pay by the stored card? (yes/no) Invalid input:(");
        assertEquals(lines[8].trim(), "Do you want to pay by the stored card? (yes/no) Please enter your new card's holder name: " +
                "Please enter your new card's number: The card is valid:)");
        assertEquals(lines[9].trim(), "Do you want to save the card details? (yes/no)");
        assertEquals(lines[10].trim(), "The card is not saved in the machine.");
    }

    /*
     * Message should input The card is valid, not invalid
     */
    @Test
    public void testPayByCreditNoCard() {
        DataManager datamanager = new DataManager("test");
        datamanager.readData();
        Payment payment = new Payment(datamanager, "test");

        String userInput = "1001,1002,1001, 1002,  1003\n3\n3\n3\ncard\nKasey\n60146\nyes\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);
        User user = new User();
        user.setName("John");
        user.setRole("customer");

        payment.askProduct(user, new Scanner(System.in));
        String[] lines = baos.toString().split("\n");

        assertEquals(lines[1].trim(), "Please specify the product code that you want to purchase");
        assertEquals(lines[7].trim(), "Do you want to pay by cash or card? (cash/card):You don't have a valid card in the machine.");
        assertEquals(lines[8].trim(), "Please enter your card's holder name: Please enter your card number: The card is valid:)");
        assertEquals(lines[9].trim(), "Do you want to save the card details? (yes/no)");
        assertEquals(lines[10].trim(), "The card is saved in your account.");
        JSONObject check = datamanager.getUsersJSON().getJSONObject(5).getJSONObject("card");
        assertEquals(check.getString("number"), "60146");
        assertEquals(check.getString("name"), "Kasey");
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

    /*
     * Helper method to make item code the key for accessing the JSONObject
     * of an item's information
     */
    public HashMap<String, JSONObject> arrangeItems() {
        HashMap<String, JSONObject> products = new HashMap<>();
        if (this.items != null) {
            for (int i = 0; i < this.items.length(); i++) {
                JSONObject obj = this.items.getJSONObject(i);
                String code = Integer.toString(obj.getInt("code"));
                products.put(code, obj);
            }
        }

        return products;
    }
}
