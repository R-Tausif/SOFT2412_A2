import org.json.JSONObject;
import org.json.JSONArray;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;

public class Payment {
    private DataManager data;
    private String[] notes;
    private ArrayList<Double> changeVal;
    private ArrayList<String> productCode;
    private String folder;
    private JSONObject userObj;
    private HashMap<Double, Integer> validNotes;
    private ArrayList<Double> providedCash;
    private int totalCash;
    private ArrayList<String> neatCash;

    public Payment(DataManager data, String folder) {
        this.data = data;
        this.notes = new String[]{"50", "20", "10", "5", "2", "1", "0.5", "0.2", "0.1", "0.05"};
        this.folder = folder;
    }

    public void askProduct(User user, Scanner scan) {
        boolean cancel = false;

        JSONArray products = this.data.getItemsJSON();
        HashMap<String, JSONObject> availableProduct = new HashMap<>();
        for (int i = 0; i < products.length(); i++) {
            JSONObject obj = products.getJSONObject(i);
            String code = Integer.toString(obj.getInt("code"));
            availableProduct.put(code, obj);
        }

        Boolean validInput = false;
        Boolean validUser = false;

        // check whether user is valid
        validUser = this.setUserObj(user);

        while (validInput == false) {
            if (cancel) {
                break;
            }

            // the user is valid
            if (validUser) {
                System.out.println("\nPlease specify the product code that you want to purchase\n" +
                        "(You can type CANCEL to cancel the transaction in any process of purchasing)");
                System.out.println("If want to specify multiple products, please separate the product code by comma eg {1002, 1003}");
                String response = scan.nextLine();

                // User wants to cancel the transaction
                cancel = cancelTransaction(response);
                if (!cancel) {
                    String changedResponse = response.replace(" ", "");
                    String[] purchase = changedResponse.split(",");

                    String invalid = this.checkValidProduct(availableProduct, purchase);

                    if (invalid.length() > 0) {
                        System.out.println("\nProduct codes: " + invalid + " does not exists!");
                        System.out.println("Please select from the following product codes");
                        System.out.println(availableProduct.keySet());
                    } else {
                        ArrayList<String> neatPurchase = this.productCode;
                        System.out.println("\nPlease specify the corresponding quantities for each product");
                        double totalPrice = 0.0;
                        HashMap<String, Integer> productQuantity = new HashMap<>();

                        for (int i = neatPurchase.size()-1 ; i >= 0; i--) {
                            if (cancel) {
                                break;
                            }

                            String currentProduct = neatPurchase.get(i);
                            String name = availableProduct.get(currentProduct).getString("name");
                            if (availableProduct.get(currentProduct).getInt("amount") >= 1) {
                                System.out.print("Product: " + name + " (" + currentProduct + "), " + "Quantity: ");
                                String quantity = scan.nextLine();

                                cancel = cancelTransaction(quantity);
                                if (!cancel) {
                                    Boolean valid = this.checkValidQuantity(quantity, currentProduct, availableProduct);

                                    while (valid == false) {
                                        if (cancel) {break;}

                                        System.out.println("Invalid product quantity, please try again!");
                                        System.out.print("Product: " + name + " (" + currentProduct + "), " + "Quantity: ");
                                        quantity = scan.nextLine();

                                        cancel = cancelTransaction(quantity);
                                        if (!cancel) {
                                            valid = this.checkValidQuantity(quantity, currentProduct, availableProduct);
                                        }
                                    }
                                    if (cancel) { break; }

                                    productQuantity.put(currentProduct, Integer.parseInt(quantity));
                                    double currentPrice = availableProduct.get(currentProduct).getDouble("price");
                                    totalPrice = totalPrice + currentPrice * Integer.parseInt(quantity);
                                }
                            } else {
                                System.out.println("Sorry the product: " + name + " is not available now, next order will be processed");
                                neatPurchase.remove(i);
                            }

                        }

                        if (cancel) {break;}

                        if (neatPurchase.size() > 0) {
                            validInput = true;
                            System.out.println("Total price that needs to be paid: $" + String.format("%.02f", totalPrice));
                            System.out.print("Do you want to pay by cash or card? (cash/card):");
                            String paymentType = scan.nextLine();

                            cancel = cancelTransaction(paymentType);
                            if (!cancel) {
                                Boolean validPayment = false;

                                while (validPayment == false) {
                                    if (cancel) { break; }

                                    if (paymentType.toLowerCase().equals("cash")) {
                                        this.askCashInput(totalPrice, productQuantity, availableProduct, neatPurchase, scan);
                                        validPayment = true;

                                        // Credit Card payment
                                    } else if (paymentType.toLowerCase().equals("card")) {
                                        boolean paySuccess = true;

                                        try {
                                            // check whether there is a stored card
                                            JSONObject card_details = (JSONObject) userObj.get("card");

                                            // ask the user if he/she wants to pay by the stored/new card?
                                            while (true) {
                                                if (cancel) { break; }

                                                System.out.print("Do you want to pay by the stored card? (yes/no) ");
                                                String answer = scan.nextLine();

                                                cancel = cancelTransaction(answer);
                                                if (!cancel) {
                                                    if (answer.toLowerCase().equals("yes")) {
                                                        System.out.println("You have successfully paid by your stored card:)");
                                                        break;
                                                    }else if (answer.toLowerCase().equals("no")){
                                                        System.out.print("Please enter your new card's holder name: ");
                                                        String new_holder = scan.nextLine();

                                                        cancel = cancelTransaction(new_holder);
                                                        if (!cancel) {
                                                            String new_card_number;
                                                            if (this.folder.equals("main")) {
                                                                MaskWithAsterisk passField = new MaskWithAsterisk();
                                                                new_card_number = passField.readPassword("Please enter your new card's number: ");
                                                                System.out.println("\n");
                                                            } else {
                                                                System.out.print("Please enter your new card's number: ");
                                                                new_card_number = scan.nextLine();
                                                            }

                                                            cancel = cancelTransaction(new_card_number);
                                                            if (!cancel) {
                                                                Card new_card = new Card();
                                                                new_card.setHolder(new_holder);
                                                                new_card.setNumber(new_card_number);
                                                                paySuccess = new_card.checkCard(data.getCardsJSON());
                                                                // ask whether to store card details
                                                                if (paySuccess) {
                                                                    new_card.storeCard(this.data, user, scan);
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    }else {
                                                        System.out.println("Invalid input:(");
                                                    }
                                                }
                                            }

                                            // no stored credit card
                                        }catch (Exception e) {
                                            String user_name = user.getName();
                                            System.out.println("You don't have a valid card in the machine.");
                                            System.out.print("Please enter your card's holder name: ");
                                            String holder = scan.nextLine();

                                            cancel = cancelTransaction(holder);
                                            if (!cancel) {
                                                String card_number;
                                                if (this.folder.equals("main")) {
                                                    MaskWithAsterisk passField = new MaskWithAsterisk();
                                                    card_number = passField.readPassword("Please enter your card number: ");
                                                    System.out.println("\n");
                                                } else {
                                                    System.out.print("Please enter your card number: ");
                                                    card_number = scan.nextLine();
                                                }

                                                cancel = cancelTransaction(card_number);
                                                if (!cancel) {
                                                    Card new_card = new Card();
                                                    new_card.setHolder(holder);
                                                    new_card.setNumber(card_number);
                                                    paySuccess = new_card.checkCard(data.getCardsJSON());

                                                    // ask whether to store card details
                                                    if (paySuccess && !user_name.equals("anonymous")) {
                                                        new_card.storeCard(this.data, user, scan);
                                                    }
                                                }
                                            }
                                        }

                                        if (cancel) { break; }

                                        if (paySuccess) {
                                            System.out.println("\n********************** Receipt **********************");
                                            this.printPriceList(productQuantity, availableProduct, neatPurchase, totalPrice);
                                            System.out.println("********************** Receipt **********************\n");
                                            this.decreaseStock(productQuantity, neatPurchase);
                                            this.data.writeJSON("items");
                                            validPayment = true;
                                            this.updateLastFiveItems(neatPurchase, availableProduct);
                                            this.data.writeJSON("user");
                                            this.updateTransaction(totalPrice, 0.0, "card", productQuantity, availableProduct);

                                        }
                                        break;

                                    } else {
                                        System.out.print("Invalid input, please only select cash or card: ");
                                        paymentType = scan.nextLine();
                                        cancel = cancelTransaction(paymentType);
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                System.out.println("You are not yet a valid user for the system");
            }
        }

        if (cancel == true) {
            this.recordCancelledTransaction("user cancelled");
        }
    }
    public void recordCancelledTransaction(String reason) {
        if (userObj != null) {
            String name = userObj.getString("name");
            JSONArray transac = this.data.getHistJSON();
            JSONObject cancelTransac = new JSONObject();
            LocalDate currDate = LocalDate.now();
            cancelTransac.put("date", currDate.toString());
            cancelTransac.put("reason", reason);
            cancelTransac.put("user", name);
            cancelTransac.put("status", "fails");
            transac.put(cancelTransac);
            data.setHistJSON(transac);
            data.writeJSON("transaction");
        }
    }

    public String checkValidProduct(HashMap<String, JSONObject> products, String[] purchases) {
        String invalid = "";
        ArrayList<String> validCode = new ArrayList<String>();
        for (int i = 0; i < purchases.length; i++) {
            if (products.containsKey(purchases[i]) == false) {
                if (invalid.length() == 0) {
                    invalid = purchases[i];
                } else {
                    invalid = invalid + ", " + purchases[i];
                }
            } else {
                if (validCode.contains(purchases[i]) == false) {
                    validCode.add(0, purchases[i]);
                }
            }
        }
        if (invalid.length() == 0) {
            this.productCode = validCode;
        }
        return invalid;
    }

    public Boolean checkValidQuantity(String response, String code, HashMap<String, JSONObject> products) {
        try {
            int quantity = Integer.parseInt(response);
            int availableQuantity = products.get(code).getInt("amount");
            if (quantity <= 0) {
                System.out.println("\nQuantities should be positive integers, please try again!");
                return false;
            } else if (availableQuantity < quantity) {
                String name = products.get(code).getString("name");
                System.out.println("\nThere is currently only " + availableQuantity + " " + name + " available, please try again!");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("\nQuantities needs to be in whole numbers, please try again!");
            return false;
        }
        return true;
    }

    public void printPriceList(HashMap<String, Integer> quantity, HashMap<String, JSONObject> product, ArrayList<String> purchase, Double total) {
        System.out.println("\nYou have purchased the following items");
        for (int i = purchase.size() - 1; i >= 0; i--) {
            String code = purchase.get(i);
            String name = product.get(code).getString("name");
            double price = product.get(code).getDouble("price");
            int number = quantity.get(code);
            double subTotal = price * number;
            System.out.println(name + " x" + number + " @ $" + String.format("%.02f", price) + " each"+ ": $" + String.format("%.02f", subTotal));
        }
        System.out.println("\nTotal price: $" + String.format("%.02f", total));
    }


    public void askCashInput(Double price, HashMap<String, Integer> productQuantity, HashMap<String, JSONObject> availableProduct, ArrayList<String> neatPurchase, Scanner scan) {
        double[] changes = {50, 20, 10, 5, 2, 1, 0.5, 0.2, 0.1, 0.05};
        boolean cancel = false;

        System.out.println("Please input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
        String response = scan.nextLine();

        cancel = cancelTransaction(response);
        if (!cancel) {
            Boolean inputValid = false;

            // white spaces are ignored
            String changedResponse = response.replace(" ", "");
            String[] dollars = changedResponse.split(",");
            validNotes = new HashMap<>();

            JSONObject existingCash = this.data.getCashJSON();

            DataManager newData = new DataManager(this.folder);
            newData.readData();
            JSONObject copied = newData.getCashJSON();

            totalCash = 0;
            double price2 = Math.round(price*20.0)/20.0;
            int priceCents = (int) (price2 * 100.0);

            while (inputValid == false) {
                if (cancel) {break;}

                validNotes = new HashMap<>();
                providedCash = new ArrayList<>();
                totalCash = 0;

                String invalid = this.checkValidCash("", dollars, "payment");

                // If invalid input is given, request for input again
                if (invalid.length() > 0) {
                    System.out.println();
                    System.out.println(invalid + " are invalid input, only the following note values will be accepted:");
                    System.out.println(Arrays.toString(changes));
                    System.out.println("Please try again!");

                    System.out.println("\nPlease input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
                    response = scan.nextLine();

                    cancel = cancelTransaction(response);
                    if (!cancel) {
                        changedResponse = response.replace(" ", "");
                        dollars = changedResponse.split(",");
                    }

                    // If not enough cash given, refund what's given and request for input again
                } else if (totalCash < priceCents) {
                    System.out.println("\nNot enough cash is provided, please try again!");
                    System.out.println("*********************** Refund ***********************");
                    System.out.println("Following cash are returned: note value x quantity");
                    this.printCash(changes, validNotes, totalCash, "provided");
                    System.out.println("*********************** Refund ***********************\n");

                    System.out.println("\nPlease input your cash payment below by specifying each note and coin in dollar terms separated by comma {eg 5,5,10,0.1}");
                    response = scan.nextLine();

                    cancel = cancelTransaction(response);
                    if (!cancel) {
                        changedResponse = response.replace(" ", "");
                        dollars = changedResponse.split(",");
                    }
                } else {
                    inputValid = true;
                }
            }

            if (!cancel) {
                this.increDecreCash(copied, validNotes, providedCash, true);

                // Check if any change needs to be given
                if (totalCash - priceCents == 0) {
                    System.out.println("\n********************** Receipt **********************");
                    this.printPriceList(productQuantity, availableProduct, neatPurchase, price);
                    System.out.println("\nYou have input the following note value x quantity");
                    this.printCash(changes, validNotes, totalCash, "provided");
                    System.out.println("\nYour change is: $0.00");
                    System.out.println("********************** Receipt **********************\n");
                    this.data.setCashJSON(copied);
                    this.data.writeJSON("cash");
                    this.decreaseStock(productQuantity, neatPurchase);
                    this.data.writeJSON("items");
                    this.updateLastFiveItems(neatPurchase, availableProduct);
                    this.data.writeJSON("user");

                    double paid = (double)totalCash/100.0;
                    this.updateTransaction(paid, 0.0, "cash", productQuantity, availableProduct);

                } else {
                    HashMap<Double, Integer> giveback = this.cashChange(priceCents, totalCash, copied);
                    // If not enough change in the vending machine, needs to cancel transaction and
                    // print the refund statement
                    if (giveback == null) {
                        System.out.println("\nThere is not enough change can be given for this payment.");
                        System.out.println("*********************** Refund ***********************");
                        System.out.println("Following cash are returned: note value x quantity");
                        this.printCash(changes, validNotes, totalCash, "provided");
                        System.out.println("*********************** Refund ***********************\n");
                        this.recordCancelledTransaction("not enough change");

                        // If enough change in the system, print the receipt
                    } else {
                        System.out.println("\n********************** Receipt **********************");
                        this.printPriceList(productQuantity, availableProduct, neatPurchase, price);
                        System.out.println("\nYou have input the following note value x quantity");
                        this.printCash(changes, validNotes, totalCash, "provided");
                        System.out.println("\nYour change is: ");
                        this.printCash(changes, giveback, totalCash - priceCents, "given back");
                        System.out.println("********************** Receipt **********************\n");

                        this.increDecreCash(copied, giveback, this.changeVal, false);
                        this.changeVal = null;
                        this.data.setCashJSON(copied);
                        this.data.writeJSON("cash");
                        this.decreaseStock(productQuantity, neatPurchase);
                        this.data.writeJSON("items");
                        this.updateLastFiveItems(neatPurchase, availableProduct);
                        this.data.writeJSON("user");

                        double paid = (double) totalCash / 100.0;
                        double change1 = (double) (totalCash - priceCents) / 100.0;
                        this.updateTransaction(paid, change1, "cash", productQuantity, availableProduct);
                    }
                }
            }
        }
        if (cancel == true) {
            this.recordCancelledTransaction("user cancelled");
        }
    }

    public String checkValidCash(String invalid, String[] dollars, String function) {
        double[] changes = {50, 20, 10, 5, 2, 1, 0.5, 0.2, 0.1, 0.05};
        String[] notes = {"50", "20", "10", "5", "2", "1", "0.5", "0.2", "0.1", "0.05"};
        ArrayList<String> cashStore = new ArrayList<>();

        // Looping through each note to check if valid
        for (int i = 0; i < dollars.length; i++) {
            Double noteValue;
            try {
                noteValue = Double.parseDouble(dollars[i]);
                Boolean isValid = false;

                // check if the input is valid Australian dollar
                for (int z = 0; z < changes.length; z++) {
                    // Incrementing the count for the same note
                    if (changes[z] == noteValue) {
                        if (function.equals("payment")) {
                            if (validNotes.containsKey(noteValue)) {
                                int oldValue = validNotes.get(noteValue);
                                validNotes.put(noteValue, oldValue+1);
                            } else {
                                validNotes.put(noteValue, 1);
                                providedCash.add(noteValue);
                            }
                            totalCash = totalCash + (int) (noteValue*100);
                        } else if (function.equals("cashier")) {
                            if (cashStore.contains(notes[z]) == false) {
                                cashStore.add(notes[z]);
                            }
                        }

                        isValid = true;
                        break;
                    }
                }

                // Concatenating all invalid notes into one string
                if (isValid == false) {
                    if (invalid.length() == 0) {
                        invalid = invalid + dollars[i];
                    } else {
                        invalid = invalid + ", " + dollars[i];
                    }
                }

                // Check if the input is an actual number
            } catch (NumberFormatException e) {
                if (invalid.length() == 0) {
                    invalid = invalid + dollars[i];
                } else {
                    invalid = invalid + ", " + dollars[i];
                }
            }

            if (invalid.length() == 0) {
                this.neatCash = cashStore;
            }
        }
        return invalid;
    }
    public void printCash(double[] changes, HashMap<Double, Integer> validNotes, int totalCash, String msg) {
        for (int j = 0; j < changes.length; j++) {
            if (validNotes.containsKey(changes[j])) {
                System.out.println("$" + String.format("%.02f", changes[j]) + " x" + validNotes.get(changes[j]));
            }
        }
        double dollar = (double)totalCash/100.0;
        System.out.println("Total cash " + msg + ": $" + String.format("%.02f", dollar));
    }

    public void increDecreCash(JSONObject cashCopied, HashMap<Double, Integer> validNotes, ArrayList<Double> keyCash, Boolean isIncre) {
        String note;
        for (int j = 0; j < keyCash.size(); j++) {
            if (keyCash.get(j) >= 1.0) {
                note = Integer.toString((keyCash.get(j).intValue()));
            } else {
                note = Double.toString(keyCash.get(j));
            }
            int numCash = cashCopied.getInt(note);
            cashCopied.remove(note);
            if (isIncre == true) {
                cashCopied.put(note, numCash + validNotes.get(keyCash.get(j)));
            } else {
                cashCopied.put(note, numCash - validNotes.get(keyCash.get(j)));
            }
        }
    }

    public HashMap<Double, Integer> cashChange(int price, int customer, JSONObject available) {
        HashMap<Double, Integer> changes = new HashMap<>();
        ArrayList<Double> notesVal = new ArrayList<>();
        int giveback = customer - price;

        for (int i = 0; i < this.notes.length; i++) {
            int currentNote = (int) (Double.parseDouble(this.notes[i])*100.0);

            if (giveback == 0) {
                break;
            } else if (currentNote <= giveback){
                int availableNote = (int) available.get(this.notes[i]);

                if (availableNote > 0) {
                    int noteAmount = Math.floorDiv(giveback, currentNote);
                    notesVal.add(Double.parseDouble(this.notes[i]));
                    if (noteAmount <= availableNote) {
                        giveback = giveback % currentNote;
                        changes.put(Double.parseDouble(this.notes[i]), noteAmount);
                    } else {
                        giveback = giveback - currentNote * availableNote;
                        changes.put(Double.parseDouble(this.notes[i]), availableNote);
                    }
                }
            }
        }

        if (giveback > 0) {
            return null;
        }
        this.changeVal = notesVal;

        return changes;
    }

    public void decreaseStock(HashMap<String, Integer> productQuantity, ArrayList<String> neatPurchase) {
        JSONArray items = this.data.getItemsJSON();
        int totalType = neatPurchase.size();

        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject itemProduct = items.getJSONObject(i);
                String itemCode = String.valueOf(itemProduct.getInt("code"));
                if (totalType == 0) {
                    break;
                }

                if (neatPurchase.contains(itemCode)) {
                    totalType = totalType - 1;
                    int quantity = itemProduct.getInt("amount");
                    itemProduct.remove("amount");
                    int decrease = productQuantity.get(itemCode);
                    itemProduct.put("amount", quantity - decrease);
                }
            }

        }
    }
    public ArrayList<String> getCashVal() {
        return this.neatCash;
    }

    public ArrayList<String> getProductCode() {
        return this.productCode;
    }

    public JSONObject getUserObj() {
        return this.userObj;
    }

    public boolean setUserObj(User user) {
        for (Object obj : this.data.getUsersJSON()) {
            userObj = (JSONObject) obj;
            if ((userObj.getString("name")).equals(user.getName())) {
                return true;
            }
        }
        return false;
    }

    public void updateLastFiveItems(ArrayList<String> purchases, HashMap<String, JSONObject> availableProduct) {
        if (userObj != null) {
            if (userObj.has("history")) {
                JSONArray arr = userObj.getJSONArray("history");
                this.updateElementPopular(arr, purchases, availableProduct);
            } else {
                JSONArray newArray = new JSONArray();
                userObj.put("history", newArray);
                this.updateElementPopular(newArray, purchases, availableProduct);
            }
        }
    }

    public void updateElementPopular(JSONArray arr, ArrayList<String> purchases, HashMap<String, JSONObject> availableProduct) {
        for (int i = 0; i < purchases.size(); i++) {
            String itemName = availableProduct.get(purchases.get(i)).getString("name");
            int exists = this.checkExistingPopular(arr, itemName);
            if (arr.length() == 5) {
                if (exists >= 0) {
                    arr.remove(exists);
                } else {
                    arr.remove(0);
                }
                arr.put(itemName);
            } else {
                arr.put(itemName);
            }
        }
    }

    public int checkExistingPopular(JSONArray arr, String item) {
        for (int i = 0; i < arr.length(); i++) {
            if (arr.getString(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public void updateTransaction(Double paid, Double change, String paymentMethod, HashMap<String, Integer> productQuantity, HashMap<String, JSONObject> availableProduct) {
        JSONArray itemsBought = new JSONArray();

        if (this.productCode != null) {
            for (int i = 0; i < this.productCode.size(); i++) {
                JSONObject item = new JSONObject();
                String code = this.productCode.get(i);
                String name = availableProduct.get(code).getString("name");
                int amount = productQuantity.get(code);
                item.put("code", code);
                item.put("name", name);
                item.put("amount", amount);
                itemsBought.put(item);
            }

            JSONObject transaction = new JSONObject();
            String userName = this.userObj.getString("name");
            LocalTime currTime = LocalTime.now();
            LocalDate currDate = LocalDate.now();
            transaction.put("date", currDate.toString());
            transaction.put("time", currTime.toString());
            transaction.put("user", userName);
            transaction.put("status", "success");
            transaction.put("method", paymentMethod);
            transaction.put("paid", paid);
            transaction.put("change", change);
            transaction.put("items", itemsBought);

            JSONArray history = this.data.getHistJSON();
            history.put(transaction);
            this.data.writeJSON("transaction");
        }
    }

    public boolean cancelTransaction(String input) {
        if (input.toUpperCase().equals("CANCEL")) {
            System.out.println("\nYou have successfully cancelled the transaction.");
            return true;
        }
        return false;
    }

}
