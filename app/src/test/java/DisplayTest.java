import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

public class DisplayTest {

    private Display disp;
    private DataManager dataManager;
    private JSONArray userJSON;

    @BeforeEach
    void setup() {

        disp = new Display(20);
        dataManager = new DataManager("test");
        dataManager.readData();
        userJSON = new JSONArray(dataManager.getUsersJSON().toString());
    }

    @AfterEach
    void cleanUp() {

        String path = String.format("src/test/resources/user_data.json");
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(path));
            out.write(userJSON.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();
    }

    @Test
    void printProductsVerticalTest() {

        disp.groupItem(dataManager.getItemsJSON());
        String items = disp.constructVertical();

        String text = null;

        try{
            text = tapSystemOut(() -> {
                disp.setHorizontal(false);
                disp.displayItem(dataManager.getItemsJSON());
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(items, text, "printProductsVerticalTest failed");

    }

    @Test
    void printProductsHorizontalTest() {

        disp.groupItem(dataManager.getItemsJSON());
        String items = disp.constructHorizontal();

        String text = null;

        try{
            text = tapSystemOut(() -> {
                disp.setHorizontal(true);
                disp.displayItem(dataManager.getItemsJSON());
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(items, text, "printProductsHorizontalTest failed");

    }

    @Test
    void printPurchasesAnonymousTest() {

        String purchases = disp.constructPurchases(dataManager.getUsersJSON(), "anonymous");

        String text = null;
        disp.setHorizontal(true);
        try{
            text = tapSystemOut(() -> {

                disp.displayPurchases(dataManager.getUsersJSON(), "anonymous");
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(purchases, text, "printPurchasesTest failed");

    }

    @Test
    void printPurchasesCustomerLoggedInTest() {

        JSONArray usersJSON = dataManager.getUsersJSON();
        JSONObject user = new JSONObject();
        String name = "display";
        user.put("password", "123");
        user.put("role", "customer");
        user.put("name", name);
        JSONArray hist = new JSONArray();
        hist.put("Mineral Water");
        hist.put("Pepsi");
        hist.put("Sprite");
        hist.put("Bounty");
        hist.put("Mars");
        user.put("history", hist);
        usersJSON.put(user);

        String purchases = disp.constructPurchases(usersJSON, name);

        String text = null;
        disp.setHorizontal(true);
        try{
            text = tapSystemOut(() -> {

                disp.displayPurchases(usersJSON, name);
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(purchases, text, "printPurchasesTest failed");

    }

    @Test
    void printPurchasesEmptyHistoryTest() {

        JSONArray usersJSON = dataManager.getUsersJSON();
        JSONObject user = new JSONObject();
        String name = "display";
        user.put("password", "123");
        user.put("role", "customer");
        user.put("name", name);
        JSONArray hist = new JSONArray();
        user.put("history", hist);
        usersJSON.put(user);

        String purchases = disp.constructPurchases(usersJSON, name);

        String text = null;
        disp.setHorizontal(true);
        try{
            text = tapSystemOut(() -> {

                disp.displayPurchases(usersJSON, name);
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(purchases, text, "printPurchasesTest failed");

    }

    @Test
    void mainDisplayTest() {

        JSONArray usersJSON = dataManager.getUsersJSON();
        JSONObject user = new JSONObject();
        String name = "display";
        user.put("password", "123");
        user.put("role", "customer");
        user.put("name", name);
        JSONArray hist = new JSONArray();
        hist.put("Mineral Water");
        hist.put("Pepsi");
        hist.put("Sprite");
        hist.put("Bounty");
        hist.put("Mars");
        user.put("history", hist);
        usersJSON.put(user);

        disp.groupItem(dataManager.getItemsJSON());
        String items = disp.constructHorizontal();

        String purchases = disp.constructPurchases(usersJSON, name);

        StringBuilder sb = new StringBuilder();
        sb.append(items);
        sb.append(purchases);

        String expected = sb.toString();

        String text = null;
        disp.setHorizontal(true);
        try{
            text = tapSystemOut(() -> {

                disp.display(dataManager.getItemsJSON(), usersJSON, name);
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        assertEquals(expected, text, "mainDisplayTest failed");
    }
}


