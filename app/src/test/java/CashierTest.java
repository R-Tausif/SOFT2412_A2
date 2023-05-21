import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.Scanner;

public class CashierTest {
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;
    private DataManager data;
    private JSONObject cash;
    private Cashier cashier;

    @BeforeEach
    public void setUp() {
        this.data = new DataManager("test");
        this.data.readData();
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
        this.cashier = new Cashier(this.data, "test");
        this.cash = this.data.getCashJSON();
    }

    @AfterEach
    public void cleanUp() {
        String cashFile = "src/test/resources/available_cash.json";
        String copyCash = "src/test/resources/check_cash.json";

        // Rewrite the available_cash.json and available_items.json back to original state
        PrintWriter output = null;
        Scanner scan = null;
        try {
            scan = new Scanner(new File(copyCash));
            String json = scan.useDelimiter("\\Z").next();

            output = new PrintWriter(new FileOutputStream(cashFile));
            output.write(json);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        output.close();
        scan.close();

        cashier.deleteCashierReports();

        // Set the console back to normal
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void testDisplayNeededCash() {
        String ans = cashier.displayNeededCash();
        String expected = "$50.00: 1 (*)\n$20.00: 0 (*)\n$10.00: 0 (*)\n$5.00: 2 (*)\n" +
                "$2.00: 2 (*)\n$1.00: 0 (*)\n$0.50: 0 (*)\n$0.20: 5\n$0.10: 1 (*)\n$0.05: 0 (*)\n" +
                "\n* - less than 5\n^ - greater than 10";
        assertEquals(ans, expected);
    }

    @Test
    public void testCheckValidQuantity() {
        ByteArrayOutputStream baos = this.writeInputOutput("");
        this.cashier.checkValidQuantity("10", true, 5, "5.00");
        this.cashier.checkValidQuantity("5", false, 5, "5.00");
        assertEquals(baos.toString().trim(), "");
    }

    @Test
    public void testCheckValidQuantityInvalidOver() {
        ByteArrayOutputStream baos = this.writeInputOutput("");
        this.cashier.checkValidQuantity("98", true, 5, "5.00");
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Cannot increase the number of $5.00 to be over 100, please try a smaller quantity!");
    }

    @Test
    public void testCheckValidQuantityInvalidUnder() {
        ByteArrayOutputStream baos = this.writeInputOutput("");
        this.cashier.checkValidQuantity("6", false, 5, "5.00");
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "The number of $5.00 to be removed is greater than currently available quantity, please try a smaller quantity!");
    }

    @Test
    public void testCheckValidQuantityInvalidNotNumber() {
        ByteArrayOutputStream baos = this.writeInputOutput("");
        this.cashier.checkValidQuantity("abc", false, 5, "5.00");
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "abc is not a valid integer, please try input the quantity again!");
    }

    @Test
    public void testTransactionHistory() {
        String ans = cashier.transactionHistory();

        String[] response = ans.split("\n");
        assertEquals(response.length, 11);
        assertEquals(response[0].replace(" ", ""), "datetimepaidchangemethoduseritemcodeamount");
        assertEquals(response[1].replace(" ", ""), "2022-10-1020:58:19.031100.050.0cashuserMineralWater10011");
        assertEquals(response[2].replace(" ", ""), "Bounty20031");
        assertEquals(response[3].replace(" ", ""), "2022-10-1015:37:23.33250.00.0carduserMineralWater10011");
        assertEquals(response[4].replace(" ", ""), "Bounty20031");
        assertEquals(response[5].replace(" ", ""), "2022-10-2420:44:49.78026.02.0cashanonymousPringles30025");
        assertEquals(response[6].replace(" ", ""), "Smiths30013");
        assertEquals(response[7].replace(" ", ""), "2022-10-2420:47:27.43913.00.0cardamazingUserCocacola10031");
        assertEquals(response[8].replace(" ", ""), "Smiths30012");
        assertEquals(response[9].replace(" ", ""), "MineralWater10013");
        assertEquals(response[10].replace(" ", ""), "2022-10-2421:47:29.4550952002.00.0carduserMineralWater10011");
    }

    @Test
    public void testWriteCashStatus() {
        cashier.writeCashStatus();
        File fileName = new File("src/test/resources/reports/available_change.txt");
        File fileName2 = new File("src/test/resources/reports/transaction_history.txt");
        assertTrue(fileName.exists());
        assertTrue(fileName2.exists());
    }

    @Test
    public void testAskForUpdateCash() {
        String userInput = "0.20, 10, 10, 5, 5\nno\n3\nyes\n6\nno\n2\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);
        cashier.askForUpdateCash(new Scanner(System.in));
        this.data.readData();

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input all the note/coin values that you want to update separated by comma:");
        assertEquals(lines[2].trim(), "Currently there are x5 $0.20 in the vending machine.");
        assertEquals(lines[3].trim(), "Do you want to increase $0.20? (yes/no):How many $0.20 do you want to decrease by?");
        assertEquals(lines[5].trim(), "Currently there are x0 $10.00 in the vending machine.");
        assertEquals(lines[6].trim(), "Do you want to increase $10.00? (yes/no):How many $10.00 do you want to increase by?");
        assertEquals(lines[8].trim(), "Currently there are x2 $5.00 in the vending machine.");
        assertEquals(lines[9].trim(), "Do you want to increase $5.00? (yes/no):How many $5.00 do you want to decrease by?");

        assertEquals(this.data.getCashJSON().getInt("0.2"), 2);
        assertEquals(this.data.getCashJSON().getInt("10"), 6);
        assertEquals(this.data.getCashJSON().getInt("5"), 0);
    }

    @Test
    public void testAskForUpdateCashInvalidNotes() {
        String userInput = "0, 2, abc, 0.7, 5\n10, 0.2, 5\nno\nna\nyes\n98\n95\nno\n5\n2\n";
        ByteArrayOutputStream baos = writeInputOutput(userInput);
        cashier.askForUpdateCash(new Scanner(System.in));
        this.data.readData();

        String userInput2 = "0.2\nyes\n";
        ByteArrayOutputStream baos2 = writeInputOutput(userInput);
        cashier.askForUpdateCash(new Scanner(System.in));
        this.data.readData();

        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Please input all the note/coin values that you want to update separated by comma:");
        assertEquals(lines[1].trim(), "0, abc, 0.7 are invalid notes/coins, please only enter the following:");
        assertEquals(lines[3].trim(), "Please input all the note/coin values that you want to update separated by comma:");
        assertEquals(lines[5].trim(), "Currently there are x0 $10.00 in the vending machine.");
        assertEquals(lines[6].trim(), "Do you want to increase $10.00? (yes/no):The system already have 0 $10.00, unable to decrease anymore");
        assertEquals(lines[7].trim(), "Cannot process this request, next request will be processed");
        assertEquals(lines[9].trim(), "Currently there are x5 $0.20 in the vending machine.");
        assertEquals(lines[10].trim(), "Do you want to increase $0.20? (yes/no):Invalid input please only type yes or no:How many $0.20 do you want to increase by?");
        assertEquals(lines[11].trim(), "Cannot increase the number of $0.20 to be over 100, please try a smaller quantity!");
        assertEquals(lines[13].trim(), "How many $0.20 do you want to increase by?");
        assertEquals(lines[15].trim(), "Currently there are x2 $5.00 in the vending machine.");
        assertEquals(lines[16].trim(), "Do you want to increase $5.00? (yes/no):How many $5.00 do you want to decrease by?");
        assertEquals(lines[17].trim(), "The number of $5.00 to be removed is greater than currently available quantity, please try a smaller quantity!");

        assertEquals(this.data.getCashJSON().getInt("10"), 0);
        assertEquals(this.data.getCashJSON().getInt("0.2"), 100);
        assertEquals(this.data.getCashJSON().getInt("5"), 0);
    }

    @Test
    public void testUpdateCashStatusInvalidUser() {
        User customer = new User();
        customer.setName("user");
        customer.setRole("customer");
        ByteArrayOutputStream baos = this.writeInputOutput("");
        cashier.updateCashStatus(new Scanner(System.in), customer);
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Sorry, only owner and cashier have permission to update cash status!");
    }

    @Test
    public void testUpdateCashStatusInvalidSeller() {
        User customer = new User();
        customer.setName("user2");
        customer.setRole("seller");
        ByteArrayOutputStream baos = this.writeInputOutput("");
        cashier.updateCashStatus(new Scanner(System.in), customer);
        String[] lines = baos.toString().split("\n");
        assertEquals(lines[0].trim(), "Sorry, only owner and cashier have permission to update cash status!");
    }

    @Test
    public void testUpdateCashStatusValidCashier() {
        String userInput = "10, 0.2, 5\nyes\n5\nyes\n30\nno\n2\n";
        User cashier1 = new User();
        cashier1.setName("money");
        cashier1.setRole("111111");
        ByteArrayOutputStream baos = this.writeInputOutput(userInput);
        String expected = "$50.00: 1 (*)\n$20.00: 0 (*)\n$10.00: 5\n$5.00: 0 (*)\n" +
                "$2.00: 2 (*)\n$1.00: 0 (*)\n$0.50: 0 (*)\n$0.20: 35 (^)\n$0.10: 1 (*)\n$0.05: 0 (*)\n" +
                "\n* - less than 5\n^ - greater than 10";

        cashier.updateCashStatus(new Scanner(System.in), cashier1);
        String[] lines = baos.toString().split("\n");

        assertEquals(lines[0].trim(), "Please input all the note/coin values that you want to update separated by comma:");
        assertEquals(this.data.getCashJSON().getInt("10"), 5);
        assertEquals(this.data.getCashJSON().getInt("0.2"), 35);
        assertEquals(this.data.getCashJSON().getInt("5"), 0);
        assertEquals(this.cashier.displayNeededCash(), expected);
    }

    @Test
    public void testDeleteCashierReports() {
        cashier.writeCashStatus();
        File fileName = new File("src/test/resources/reports/available_change.txt");
        File fileName2 = new File("src/test/resources/reports/transaction_history.txt");
        assertTrue(fileName.exists());
        assertTrue(fileName2.exists());

        cashier.deleteCashierReports();
        assertFalse(fileName.exists());
        assertFalse(fileName2.exists());
    }

    public ByteArrayOutputStream writeInputOutput(String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        System.setIn(bais);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);
        return baos;
    }
}
