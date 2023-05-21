import java.io.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CancelTest {
    private User user;
    private Payment payment;
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;

    @BeforeEach
    public void setUp() {
        this.user = new User();
        user.setName("anonymous");
        user.setPassword(null);
        user.setRole("customer");

        DataManager dm = new DataManager("test");
        dm.readData();

        this.payment = new Payment(dm, "test");
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
    }

    @AfterEach
    public void setBack() {
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void cancelProductCode() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("cancel");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[5].trim());
    }

    @Test
    public void cancelQuantity() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[7].trim());
    }

    @Test
    public void cancelPayMethod() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\n1\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[8].trim());
    }

    @Test
    public void cancelCash() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\n1\ncash\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[9].trim());
    }

    @Test
    public void cancelInvalidCash() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\n1\ncash\n100\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[15].trim());
    }

    @Test
    public void cancelCard() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\n1\ncard\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[9].trim());
    }

    @Test
    public void cancelCardPwd() {
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("1001\n1\ncard\nCharles\ncancel\n");
        Scanner scan = new Scanner(System.in);

        payment.askProduct(user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("You have successfully cancelled the transaction.", lines[9].trim());
    }
}
