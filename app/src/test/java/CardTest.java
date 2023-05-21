import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    private Card card;
    private JSONArray card_list;
    private JSONArray user_list;
    private DataManager dm;
    private User user;
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;

    @BeforeEach
    public void setUp() {
        this.card = new Card();
        this.dm = new DataManager("test");
        dm.readData();
        this.card_list = dm.getCardsJSON();
        this.user_list = dm.getUsersJSON();

        this.user = new User();
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
    }

    @AfterEach
    public void setBack() {
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void checkValidCard() {
        this.card.setHolder("Charles");
        this.card.setNumber("40691");

        assertEquals("Charles", this.card.getHolder());
        assertEquals("40691", this.card.getNumber());

        assertNotNull(this.card_list);
        assertTrue(this.card.checkCard(this.card_list));
    }

    @Test
    public void checkInvalidCard() {
        this.card.setHolder("May");
        this.card.setNumber("123");

        assertEquals("May", this.card.getHolder());
        assertEquals("123", this.card.getNumber());

        assertFalse(this.card.checkCard(this.card_list));
    }

    @Test
    public void testStoreCard() {
        // anonymous user
        this.user.setName("anonymous");
        this.user.setRole("customer");

        this.card.setHolder("Sergio");
        this.card.setNumber("42689");

        // input "yes"
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("yes");
        Scanner scan = new Scanner(System.in);

        card.storeCard(this.dm, this.user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("Anonymous user is not allowed to store card in the machine.", lines[1].trim());

        // input "no"
        baos = new PaymentTest().writeInputOutput("no");
        scan = new Scanner(System.in);

        card.storeCard(this.dm, this.user, scan);
        lines = baos.toString().split("\n");
        assertEquals("The card is not saved in the machine.", lines[1].trim());

        // check the user_data.json is not modified
        for (Object user : this.user_list) {
            JSONObject anon_user = (JSONObject) user;
            if (anon_user.get("name").equals("anonymous")) {
                assertFalse(anon_user.has("card"));
                break;
            }
        }
    }

    @Test
    public void testStoreCard2() {
        // User "user"
        JSONObject old_card = new JSONObject();
        old_card.put("name", "Charles");
        old_card.put("number", "40691");

        this.user.setName("user");
        this.user.setRole("customer");
        this.card.setHolder("Sergio");
        this.card.setNumber("42689");

        // input "no"
        ByteArrayOutputStream baos = new PaymentTest().writeInputOutput("no");
        Scanner scan = new Scanner(System.in);

        card.storeCard(this.dm, this.user, scan);
        String[] lines = baos.toString().split("\n");
        assertEquals("The card is not saved in the machine.", lines[1].trim());

        // check the user_data.json is not modified
        for (Object user : this.user_list) {
            JSONObject certain_user = (JSONObject) user;
            if (certain_user.get("name").equals(this.user.getName())) {
                assertTrue(certain_user.has("card"));
                JSONObject user_card = (JSONObject) certain_user.get("card");
                assertEquals("Charles", user_card.get("name"));
                assertEquals("40691", user_card.get("number"));
                break;
            }
        }

        // input "yes"
        baos = new PaymentTest().writeInputOutput("yes");
        scan = new Scanner(System.in);

        card.storeCard(this.dm, this.user, scan);
        lines = baos.toString().split("\n");
        assertEquals("The card is saved in your account.", lines[1].trim());

        // check the user_data.json is successfully modified
        for (Object user : this.user_list) {
            JSONObject certain_user = (JSONObject) user;
            if (certain_user.get("name").equals(this.user.getName())) {
                assertTrue(certain_user.has("card"));
                JSONObject user_card = (JSONObject) certain_user.get("card");
                assertEquals("Sergio", user_card.get("name"));
                assertEquals("42689", user_card.get("number"));

                // change user_data back
                certain_user.put("card", old_card);
                this.dm.setUsersJSON(this.user_list);
                this.dm.writeJSON("user");
                break;
            }
        }
    }

}