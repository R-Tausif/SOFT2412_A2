import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {


    User userTest = new User();

    @AfterEach
    public void cleanUp() {
        String userFile = "src/test/resources/user_data.json";
        String copyUser = "src/test/resources/check_user.json";

        // Rewrite the available_cash.json and available_items.json back to original state
        PrintWriter output = null;
        Scanner scan = null;
        try {
            scan = new Scanner(new File(copyUser));
            String json = scan.useDelimiter("\\Z").next();

            output = new PrintWriter(new FileOutputStream(userFile));
            output.write(json);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        output.close();
        scan.close();

    }



    // when user exists
    @Test
    void matchUserDataCheck() {
        JSONArray userArray = new JSONArray();
        JSONObject newObject = new JSONObject();
        newObject.put("name", "user");
        newObject.put("password", "123");
        userArray.put(newObject);


        boolean result = userTest.matchUserData("user", "123", userArray);
        assertTrue(result);
    }

    // when user doesn't exists
    @Test
    void matchUserDataCheck_2() {

        JSONArray userArray = new JSONArray();
        JSONObject newObject = new JSONObject();
        newObject.put("name", "user");
        newObject.put("password", "123");
        userArray.put(newObject);

        boolean result = userTest.matchUserData("user2", "123", userArray);
        assertFalse(result);
    }

    @Test
    void matchUserDataCheck_3() {

        JSONArray userArray = new JSONArray();
        JSONObject newObject = new JSONObject();
        newObject.put("name", "user2");
        newObject.put("password", "123");
        userArray.put(newObject);

        boolean result = userTest.matchUserData("user2", "1233", userArray);
        assertFalse(result);
    }

    // success
    @Test
    void createAccountCheck() {
        JSONArray userArray = new JSONArray();
        DataManager dataManager = new DataManager("test");
        dataManager.readData();
        userTest.createAccount("Sam", "Green", "customer", userArray, dataManager);

        JSONArray testArray = new JSONArray();
        JSONObject testObject = new JSONObject();
        JSONArray histArr = new JSONArray();
        testObject.put("name", "Sam");
        testObject.put("password", "Green");
        testObject.put("role","customer");
        testObject.put("history", histArr);
        testArray.put(testObject);

        assertEquals(userArray.toString(), testArray.toString());

    }

    // user already exists
    @Test
    void userNameCheck() {
        JSONArray userArray = new JSONArray();
        JSONObject userObject = new JSONObject();
        userObject.put("name", "newuser");
        userObject.put("password", "1233333");
        userArray.put(userObject);
        boolean result = userTest.userNameExists("newuser", userArray);
        assertTrue(result);
    }

     // user doesn't exist
    @Test
    void userNameCheck_2() {
        JSONArray userArray = new JSONArray();
        JSONObject userObject = new JSONObject();
        userObject.put("name", "newuser");
        userObject.put("password", "1233333");
        userArray.put(userObject);
        boolean result = userTest.userNameExists("usernotfound", userArray);
        assertFalse(result);
    }

// check for empty inputs
    @Test
    void userNameCheck_3() {
    boolean result_1 = userTest.inputNotEmpty("username", "password");
    boolean result_2 = userTest.inputNotEmpty("", "password");
    boolean result_3 = userTest.inputNotEmpty("username", "");
    boolean result_4 = userTest.inputNotEmpty("", "");

    assertTrue(result_1);
    assertFalse(result_2);
    assertFalse(result_3);
    assertFalse(result_4);
}
    @Test
    void setPasswordTest() {
        userTest.setPassword("123");
        assertEquals(userTest.getPassword(), "123");
    }

    @Test
    void getRoleTest(){
        userTest.setRole("seller");

        assertEquals(userTest.getRole(), "seller");
    }

    @Test
    void removeAccountTest(){
        JSONArray userArray = new JSONArray();
        DataManager dataManager = new DataManager("test");
        dataManager.readData();
        userTest.createAccount("Sam", "Green", "customer", userArray, dataManager);
        userTest.removeAccount("Sam", userArray, dataManager);

        // when the user account to be removed exists
        assertEquals(userArray.length(), 0);

        userTest.createAccount("Sam", "Green", "customer", userArray, dataManager);
        userTest.removeAccount("Ben", userArray, dataManager);

        // when the user account to be removed does not exist
        assertEquals(userArray.length(), 1);
    }
}
