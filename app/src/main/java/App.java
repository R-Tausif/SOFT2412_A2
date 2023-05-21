/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.text.*;
import java.util.*;

import org.json.*;

//import org.json.JSONArray;

import java.io.Console;

import static java.lang.Thread.sleep;


public class App {
//    private DataManager data;
    private final int MAX_WORD = 20; // can be altered
    public static void main(String[] args) {

        App machine = new App();
        machine.start("main");

    }

    public void start(String folder) {

        /* start the code here to make it easier for
         * testing.
         */

        /* the dataManager functions as the holder of the data
         * if you make any changes to the data please set update
         * the data inside the dataManager to make it up to date
         */
        DataManager dataManager = new DataManager(folder);

        // set the default user as anonymous
        User user1 = new User();
        user1.setName("anonymous");
        user1.setRole("customer");

        dataManager.readData();
//        this.data = dataManager;

        // create the required instance of classes
        Display disp = new Display(MAX_WORD);
        MaskWithAsterisk passField = new MaskWithAsterisk();

        Seller seller = new Seller(folder, MAX_WORD, dataManager);

        Cashier cash = new Cashier(dataManager, folder);

        Scanner scan = new Scanner(System.in);

        System.out.println("Welcome to the Vending Machine!");

        Owner owner = new Owner(dataManager, folder);

        while (true) {
            seller = new Seller(folder, MAX_WORD, dataManager);
            // Display the available items
            // and the last purchases for customer
            if (user1.getRole().equals("customer")) {
                disp.display(dataManager.getItemsJSON(),
                        dataManager.getUsersJSON(), user1.getName());
            }

            // write the reports accordingly
            writeReports(user1, cash, seller, owner, dataManager);

            // prints the reports notification
            printReportsNotif(user1);

            // prints the appropriate priviledge for each role
            printPrivileges(user1);

            System.out.print("\nEnter: ");

            String input = scan.nextLine();
            input = input.toUpperCase();

            if (input.equals("EXIT")) {
                break;
            } else if (input.equals("HORIZONTAL")) {
                if (!user1.getRole().equals("customer")) {
                    System.out.print(String.format("Error: %s does not have " +
                                    "the access to HORIZONTAL",
                            user1.getRole()));
                    continue;
                }

                disp.setHorizontal(true);
            } else if (input.equals("VERTICAL")) {
                if (!user1.getRole().equals("customer")) {
                    System.out.print(String.format("Error: %s does not have " +
                                    "the access to VERTICAL",
                            user1.getRole()));
                    continue;
                }

                disp.setHorizontal(false);

            } else if (input.equals("BUY")) {

                // only customer is allowed to buy things.
                if (!user1.getRole().equals("customer")) {
                    System.out.print(String.format("Error: %s does not have the access to BUY",
                            user1.getRole()));
                    continue;
                }

                Payment p = new Payment(dataManager, folder);
                p.askProduct(user1, scan);
                // log out automatically
                user1 = new User();
                user1.setName("anonymous");
                user1.setPassword(null);
                user1.setRole("customer");
                System.out.println("You have successfully logged out:)");

            } else if (input.equals("LOGOUT")) {

                if ((user1.getName()).equals("anonymous")) {
                    System.out.println("Error: You have not logged in yet.");
                    continue;
                }

                // delete the reports here
                deleteReports(seller, cash, owner);

                System.out.println("You have successfully logged out:)");

                user1.setName("anonymous");
                user1.setRole("customer");


            } else if(input.equals("LOGIN")){

                // addressing that a user must be logged out first before logging in to another account.
                if (!user1.getName().equals("anonymous")) {
                    System.out.println("Error: Please LOGOUT from your current account first.");
                    continue;
                }

                // comment
                System.out.println("PLEASE ENTER YOUR USERNAME: ");
                String userNameInput = scan.nextLine();
                String passwordInput = "";

                if (folder.equals("main")) {
                    passwordInput = passField.readPassword("Enter your password: ");
                } else {
                    System.out.print("Enter your password: ");
                    passwordInput = scan.nextLine();
                }

                boolean stringNotEmpty = user1.inputNotEmpty(userNameInput, passwordInput);
                if (stringNotEmpty){
                    boolean userResult = user1.matchUserData(userNameInput, passwordInput, dataManager.getUsersJSON());
                    try {
                        sleep(1);
                    }catch (Exception e){

                    }
                    System.out.println("");
                    if (userResult){
                        System.out.println("YOU HAVE LOGGED IN SUCCESSFULLY!");

                        // set user information
                        user1.setName(userNameInput);
                        user1.setPassword(passwordInput);
                        for (Object obj: dataManager.getUsersJSON()) {
                            JSONObject userObj = (JSONObject) obj;
                            if ((userObj.getString("name")).equals(user1.getName())) {
                                user1.setRole(userObj.getString("role"));
                                break;
                            }
                        }

                    }else{
                        System.out.println("SORRY THIS COMBINATION OF USERNAME AND PASSWORD DOESN'T EXIST ");
                    }
                }else{
                    System.out.println("\nSORRY USERNAME OR PASSWORD CANNOT BE EMPTY");
                }

            } else if (input.equals("CREATE AN ACCOUNT")){

                /* this is the consequences of have to logout before login to another account.
                * because after creating a new account, the user is automatically logged in
                * to the new account
                 */
                if (!user1.getName().equals("anonymous")) {
                    System.out.println("Error: you must be an anonymous user" +
                            " to create a new customer account.");
                    continue;
                }
                String usernameInput;
                String passwordInput;
                System.out.println("YOU WANT TO CREATE AN ACCOUNT.");
                System.out.println("PLEASE ENTER YOUR USERNAME: ");
                usernameInput = scan.nextLine();

                if (folder.equals("main")) {
                    passwordInput = passField.readPassword("Enter your password: ");
                } else {
                    System.out.print("Enter your password: ");
                    passwordInput = scan.nextLine();
                }
                boolean stringNotEmpty = user1.inputNotEmpty(usernameInput, passwordInput);
                if (stringNotEmpty){
                    System.out.println("\n");
                    if (user1.userNameExists(usernameInput,dataManager.getUsersJSON())){
                        System.out.println("SORRY THIS USERNAME ALREADY EXISTS. PLEASE TRY AGAIN.");
                    }else{
                        user1.createAccount(usernameInput,passwordInput, "customer", dataManager.getUsersJSON(),
                                dataManager);
                        user1.setName(usernameInput);
                        user1.setRole("customer");
                        user1.setPassword(passwordInput);
                        System.out.println("YOUR ACCOUNT HAS BEEN CREATED SUCCESSFULLY!");
                    }
                }else{
                    System.out.println("\nSORRY USERNAME OR PASSWORD CANNOT BE EMPTY");
                }



            } else if (input.equals("MODIFY ITEMS")) {

                if (!(user1.getRole().equals("seller") ||
                        user1.getRole().equals("owner"))) {
                    System.out.print(String.format("Error: %s does not have the access to MODIFY ITEMS",
                            user1.getRole()));
                    continue;
                }

                seller.modifyItems(scan);

            } else if (input.equals("MODIFY CASH")) {

                cash.updateCashStatus(scan, user1);

            } else if (input.equals("REPORTS ITEMS")) {
                if (!(user1.getRole().equals("seller") ||
                        user1.getRole().equals("owner"))) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS ITEMS",
                            user1.getRole()));
                    continue;
                }

                seller.printAvailableItem();

            } else if (input.equals("REPORTS ITEMS SOLD")) {
                if (!(user1.getRole().equals("seller") ||
                        user1.getRole().equals("owner"))) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS ITEMS SOLD",
                            user1.getRole()));
                    continue;
                }

                seller.printSoldItem();

            } else if (input.equals("REPORTS CHANGE")) {
                if (!(user1.getRole().equals("cashier") ||
                        user1.getRole().equals("owner"))) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS CHANGE", user1.getRole()));
                    continue;
                }
                System.out.println("\nThe current cash availability is:");
                System.out.println(cash.displayNeededCash());
            } else if (input.equals("REPORTS TRANSACTION")) {
                if (!(user1.getRole().equals("cashier") ||
                        user1.getRole().equals("owner"))) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS TRANSACTION", user1.getRole()));
                    continue;
                }
                System.out.println("\nThe summary of transaction is:");
                System.out.println(cash.transactionHistory());
            } else if (input.equals("REPORTS USERS")) {
                if (!user1.getRole().equals("owner")) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS USERS", user1.getRole()));
                    continue;
                }
                System.out.println("\nThe list of all username is (USERNAME: ROLE):");
                System.out.println(owner.usernamesRoles());
            } else if (input.equals("REPORTS CANCELLED")) {
                if (!user1.getRole().equals("owner")) {
                    System.out.print(String.format("Error: %s does not have the access to REPORTS CANCELLED", user1.getRole()));
                    continue;
                }

                System.out.println(owner.constructFailedTransactions());

            } else if (input.equals("CREATE A NEW ACCOUNT")){
                if (!user1.getRole().equals("owner")){
                    System.out.print(String.format("Error: %s does not have the access to CREATE A NEW ACCOUNT", user1.getRole()));
                }else{
                    String usernameInput;
                    String passwordInput;
                    System.out.println("YOU WANT TO CREATE AN ACCOUNT.");
                    System.out.println("PLEASE ENTER THE USERNAME: ");
                    usernameInput = scan.nextLine();
                    if (folder.equals("main")) {
                        passwordInput = passField.readPassword("PLEASE ENTER THE PASSWORD: ");
                    } else {
                        System.out.print("PLEASE ENTER THE PASSWORD: ");
                        passwordInput = scan.nextLine();
                    }
                    boolean stringNotEmpty = user1.inputNotEmpty(usernameInput, passwordInput);
                    if (stringNotEmpty){
                        System.out.println("\n");
                        if (user1.userNameExists(usernameInput,dataManager.getUsersJSON())){
                            System.out.println("SORRY THIS USERNAME ALREADY EXISTS. PLEASE TRY AGAIN.");
                        }else{
                            String role;
                            System.out.println("PLEASE ENTER THE ROLE OF THIS USER");
                            role = scan.nextLine().toLowerCase();
                            if (role.equals("owner") || role.equals("seller")||role.equals("cashier")) {
                                user1.createAccount(usernameInput,passwordInput, role, dataManager.getUsersJSON(),
                                        dataManager);
                                System.out.println(String.format("%s ACCOUNT HAS BEEN CREATED SUCCESSFULLY!", role));
                            } else {
                                System.out.println("\nSORRY YOU CAN ONLY CREATE ACCOUNTS OF TYPE SELLER, CASHIER OR OWNER");
                            }
                        }
                    }else{
                        System.out.println("\nSORRY USERNAME OR PASSWORD CANNOT BE EMPTY");
                    }
                }
            }else if (input.equals("REMOVE AN ACCOUNT")){
                if (!user1.getRole().equals("owner")){
                    System.out.print(String.format("Error: %s does not have the access to REMOVE AN ACCOUNT", user1.getRole()));
                }else{
                    String usernameInput;
                    System.out.println("YOU WANT TO REMOVE AN ACCOUNT");
                    System.out.println("PLEASE ENTER THE USERNAME OF THE ACCOUNT: ");
                    usernameInput = scan.nextLine();

                    boolean stringNotEmpty = user1.inputNotEmpty(usernameInput, "00");
                    if (stringNotEmpty){
                        System.out.println("\n");
                        if (!user1.userNameExists(usernameInput,dataManager.getUsersJSON())){
                            System.out.println("SORRY THIS USERNAME DOESN'T EXIST. PLEASE TRY AGAIN.");
                        } else {
                            if (user1.isCustomer(usernameInput, dataManager.getUsersJSON())) {
                                System.out.println("SORRY YOU CANNOT REMOVE CUSTOMER ACCOUNTS.");
                            } else {
                                user1.removeAccount(usernameInput, dataManager.getUsersJSON(), dataManager);
                                System.out.println("ACCOUNT HAS BEEN REMOVED SUCCESSFULLY!");
                            }
                        }
                    }else{
                        System.out.println("\nSORRY USERNAME CANNOT BE EMPTY");
                    }

                }
            }
            else {
                System.out.println(String.format("Error: There is no command called %s\n", input));
            }
        }

        // deleting the reports if the user has not logged out.
        deleteReports(seller, cash, owner);

        scan.close();
        System.out.println("\nThank you for your patronage!");
        System.out.println("See you next time!");

    }

    public void deleteReports(Seller seller, Cashier cash, Owner owner) {

        seller.deleteAvailableItemReport();
        seller.deleteSoldItemReport();
        cash.deleteCashierReports();
        owner.deleteUserReports();
        owner.deleteFailedTransactions();
    }

    public void printPrivileges(User user1) {
        System.out.print(String.format("%s Options: EXIT",
                user1.getRole().toUpperCase()));

        if (user1.getName().equals("anonymous")) {
            System.out.print(", CREATE AN ACCOUNT, LOGIN");
        } else {
            System.out.print(", LOGOUT");
        }

        if (user1.getRole().equals("customer")) {
            System.out.print(", BUY, HORIZONTAL/VERTICAL");
        }

        if (user1.getRole().equals("seller")) {
            System.out.print(", MODIFY ITEMS");
        }

        if (user1.getRole().equals("owner")) {
            System.out.print(", MODIFY CASH, MODIFY ITEMS, CREATE A NEW ACCOUNT, REMOVE AN ACCOUNT");
        }

        if (user1.getRole().equals("cashier")) {
            System.out.print(", MODIFY CASH");
        }
    }

    public void writeReports(User user1, Cashier cash, Seller seller, Owner owner,
                              DataManager dataManager) {
        if (user1.getRole().equals("seller") ||
                user1.getRole().equals("owner")) {
            seller.writeAvailableItemReport();
            seller.writeSoldItemReport();
        }

        if (user1.getRole().equals("cashier") ||
                user1.getRole().equals("owner")) {
            cash.writeCashStatus();
        }

        if (user1.getRole().equals("owner")) {
            owner.writeUserReports();
            owner.writeFailedTransactions();
        }
    }

    public void printReportsNotif(User user1) {

        if (user1.getRole().equals("customer"))
            return;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\nNOTICE! The following reports have been" +
                " generated for %s:\n", user1.getRole().toUpperCase()));
        sb.append("--> ENTER \"REPORTS <KEY WORDS>\" to print the "+
                "corresponding report onto the inteface \n");
        sb.append("--> OR GO TO \"src/main/"+
                "resources/\" to look into the reports file directly.\n");

        int max_left = 26;
        int max_center = 15;
        int max_right = 50;

        String thickBorder = String.format("|%s|\n",
                StringUtils.repeat("=", max_right+max_left+max_center+2));
        String thinBorder = String.format("|%s|\n",
                StringUtils.repeat("_", max_right+max_left+max_center+2));

        sb.append("\n");
        sb.append(thickBorder);

        String left_title = StringUtils.center("Reports", max_left);
        String center_title = StringUtils.center("<KEY WORDS>", max_center);
        String right_title = StringUtils.center("Brief Explanation", max_right);
        sb.append(String.format("|%s|%s|%s|\n", left_title, center_title,
                right_title));

        sb.append(thickBorder);

        if (user1.getRole().equals("seller") ||
            user1.getRole().equals("owner")) {

            String left_side = StringUtils.center("available_items.csv", max_left);
            String center_side = StringUtils.center("ITEMS", max_center);
            String right_side = StringUtils.center("details of items in " +
                    "the vending machine", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

            sb.append(thinBorder);

            left_side = StringUtils.center("items_sold.csv", max_left);
            center_side = StringUtils.center("ITEMS SOLD", max_center);
            right_side = StringUtils.center("summary of sold items", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

        }

        if (user1.getRole().equals("cashier") ||
                user1.getRole().equals("owner")) {

            sb.append(thinBorder);

            String left_side = StringUtils.center("available_change.txt", max_left);
            String center_side = StringUtils.center("CHANGE", max_center);
            String right_side = StringUtils.center("details of available cash in " +
                    "the vending machine", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

            sb.append(thinBorder);

            left_side = StringUtils.center("transaction_history.csv", max_left);
            center_side = StringUtils.center("TRANSACTION", max_center);
            right_side = StringUtils.center("summary of transactions", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

        }

        if (user1.getRole().equals("owner")) {

            sb.append(thinBorder);

            String left_side = StringUtils.center("users.txt", max_left);
            String center_side = StringUtils.center("USERS", max_center);
            String right_side = StringUtils.center("details of usernames and "+
                    "its roles", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

            sb.append(thinBorder);

            left_side = StringUtils.center("cancelled_transactions.csv", max_left);
            center_side = StringUtils.center("CANCELLED", max_center);
            right_side = StringUtils.center("summary of failed transactions", max_right);
            sb.append(String.format("|%s|%s|%s|\n", left_side, center_side,
                    right_side));

        }

        sb.append(thickBorder);

        System.out.println(sb.toString());

    }
}