import org.json.JSONObject;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.Scanner;

public class OwnerTest {
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;
    private DataManager data;
    private Owner owner;

    @BeforeEach
    public void setUp() {
        this.data = new DataManager("test");
        this.data.readData();
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
        this.owner = new Owner(this.data, "test");
    }

    @AfterEach
    public void cleanUp() {
        String transacFile = "src/test/resources/transaction_hist.json";
        String copyTransac = "src/test/resources/check_transac.json";

        // Rewrite the available_cash.json and available_items.json back to original state
        PrintWriter output = null;
        Scanner scan = null;
        try {
            scan = new Scanner(new File(copyTransac));
            String json = scan.useDelimiter("\\Z").next();

            output = new PrintWriter(new FileOutputStream(transacFile));
            output.write(json);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        output.close();
        scan.close();

        owner.deleteUserReports();
        owner.deleteFailedTransactions();

        // Set the console back to normal
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void testUsernamesRoles() {
        String[] ans = owner.usernamesRoles().split("\n");
        String[] expected = {"John: customer","Owner1: owner","money: cashier","user: customer", "user2: seller"};

        assertEquals(ans[0], expected[0]);
        assertEquals(ans[1], expected[1]);
        assertEquals(ans[2], expected[2]);
        assertEquals(ans[3], expected[3]);
        assertEquals(ans[4], expected[4]);
    }

    @Test
    public void testWriteReport() {
        owner.writeUserReports();
        File fileName = new File("src/test/resources/reports/users.txt");
        assertTrue(fileName.exists());
    }

    @Test
    public void testDeleteReport() {
        owner.writeUserReports();
        File fileName = new File("src/test/resources/reports/users.txt");
        assertTrue(fileName.exists());
        owner.deleteUserReports();
        assertFalse(fileName.exists());
    }

    @Test
    void writeFailedTransactionFolderNotExistsTest() {


        if (owner.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.delete();

        }

        owner.writeFailedTransactions();

        File file = new File("src/test/resources/reports/failed_transactions.txt");
        boolean exists = file.exists();

        assertTrue(exists, "Error: file should exists");

    }

    @Test
    void writeFailedTransactionFolderExistsTest() {


        if (!owner.isFolderExists()) {
            File file = new File("src/test/resources/reports");
            file.mkdir();

        }

        owner.writeFailedTransactions();

        File file = new File("src/test/resources/reports/failed_transactions.txt");
        boolean exists = file.exists();

        assertTrue(exists, "Error: file should exists");

    }

    @Test
    void deleteFailedTransactionFileExistsTest() {

        owner.writeFailedTransactions();

        boolean succeed = owner.deleteFailedTransactions();

        assertTrue(succeed, "Error: file should be deleted");

    }

    @Test
    void deleteFailedTransactionFileNotExistsTest() {

        File file = new File("src/test/resources/reports/failed_transactions.txt");

        if (file.exists())
            file.delete();

        boolean succeed = owner.deleteFailedTransactions();

        assertFalse(succeed, "Error: File should not exists to begin with" +
                " so there's nothing to be deleted");

    }
}
