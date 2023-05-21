import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
public class Cashier {
    private JSONObject cashier;
    private String folder;
    private DataManager data;


    public Cashier(DataManager data, String folder) {
        this.folder = folder;
        this.data = data;
    }

    public void updateCashStatus(Scanner scan, User user) {
        Boolean validUser = false;
        // check whether user is valid
        for (Object obj : this.data.getUsersJSON()) {
            this.cashier = (JSONObject) obj;
            if (this.cashier.has("role")) {
                if (this.cashier.getString("name").equals(user.getName())) {
                    if (this.cashier.getString("role").equals("owner") || this.cashier.getString("role").equals("cashier")) {
                        validUser = true;
                        break;
                    }
                }
            }
        }

        if (validUser) {
            this.askForUpdateCash(scan);

        } else {
            System.out.println("Sorry, only owner and cashier have permission to update cash status!");
        }
    }

    public String displayNeededCash() {
        String[] notes = {"50", "20", "10", "5", "2", "1", "0.5", "0.2", "0.1", "0.05"};
        JSONObject cash = this.data.getCashJSON();
        String cashStatus = "";

        for (int i = 0; i < notes.length; i++) {
            String noteValue = notes[i];
            Double value = Double.parseDouble(noteValue);
            int amount = cash.getInt(noteValue);
            String msg = String.format("$%.02f: %s", value, amount);
            if (amount < 5) {
                msg = msg + " (*)";
            } else if (amount > 10) {
                msg = msg + " (^)";
            }
            cashStatus = cashStatus + msg + "\n";
        }

        cashStatus = cashStatus + "\n* - less than 5\n^ - greater than 10";
        return cashStatus;
    }

    public void askForUpdateCash(Scanner scan) {
        String[] notes = {"50", "20", "10", "5", "2", "1", "0.5", "0.2", "0.1", "0.05"};
        System.out.println("Please input all the note/coin values that you want to update separated by comma: ");
        String response = scan.nextLine();
        String updatedResponse = response.replace(" ", "");
        String[] dollars = updatedResponse.split(",");

        Payment pay = new Payment(this.data, this.folder);
        String invalid = pay.checkValidCash("", dollars, "cashier");
        ArrayList<String> cashToUpdate = pay.getCashVal();

        while (invalid.length() > 0 || updatedResponse.equals("")) {
            System.out.println(invalid + " are invalid notes/coins, please only enter the following: ");
            System.out.println(Arrays.toString(notes));
            System.out.println("Please input all the note/coin values that you want to update separated by comma: ");
            response = scan.nextLine();
            updatedResponse = response.replace(" ", "");
            dollars = updatedResponse.split(",");
            invalid = pay.checkValidCash("", dollars, "cashier");
            cashToUpdate = pay.getCashVal();
        }

        JSONObject cash = this.data.getCashJSON();
        DataManager newData = new DataManager(this.folder);
        newData.readData();
        JSONObject copied = newData.getCashJSON();

        for (int i = 0; i < cashToUpdate.size(); i++) {
            String noteVal = String.format("%.02f", Double.parseDouble(cashToUpdate.get(i)));
            int available = copied.getInt(cashToUpdate.get(i));
            System.out.println("\nCurrently there are x" + available +  " $" + noteVal + " in the vending machine.");
            System.out.print("Do you want to increase $" + noteVal + "? (yes/no):");
            String increDecre = scan.nextLine();
            String yesNo = increDecre.toLowerCase();
            while (yesNo.equals("yes") == false && yesNo.equals("no") == false) {
                System.out.print("Invalid input please only type yes or no:");
                increDecre = scan.nextLine();
                yesNo = increDecre.toLowerCase();
            }

            String msg = "How many $" + noteVal + " do you want to ";
            Boolean isIncrease = false;
            String quantity;
            Boolean systemInvalid = false;

            if (increDecre.toLowerCase().equals("yes")) {
                msg = msg + "increase by?";
                isIncrease = true;
                if (available >= 100) {
                    System.out.println("The system already have more than 100 $" + noteVal + ", unable to increase anymore");
                    System.out.println("Cannot process this request, next request will be processed");
                    systemInvalid = true;
                }
            } else {
                msg = msg + "decrease by?";
                if (available <= 0) {
                    System.out.println("The system already have 0 $" + noteVal + ", unable to decrease anymore");
                    System.out.println("Cannot process this request, next request will be processed");
                    systemInvalid = true;
                }
            }

            if (systemInvalid == false) {
                System.out.println(msg);
                quantity = scan.nextLine();

                while (this.checkValidQuantity(quantity, isIncrease, available, noteVal) == false) {
                    System.out.println("\n" + msg);
                    quantity = scan.nextLine();
                }

                copied.remove(cashToUpdate.get(i));
                int updatedCash = 0;
                if (isIncrease) {
                    updatedCash = available+Integer.parseInt(quantity);
                } else {
                    updatedCash = available-Integer.parseInt(quantity);
                }
                copied.put(cashToUpdate.get(i), updatedCash);
            }
        }

        this.data.setCashJSON(copied);
        this.data.writeJSON("cash");

    }

    public boolean checkValidQuantity(String num, Boolean isIncrease, int available, String noteVal) {
        try {
            int number = Integer.parseInt(num);
            if (isIncrease) {
                if (number + available > 100) {
                    System.out.println("Cannot increase the number of $" + noteVal + " to be over 100, please try a smaller quantity!");
                    return false;
                }
            } else {
                if (available - number < 0) {
                    System.out.println("The number of $" + noteVal + " to be removed is greater than currently available quantity, please try a smaller quantity!");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(num + " is not a valid integer, please try input the quantity again!");
            return false;
        }
        return true;
    }

    public void writeCashStatus() {
        File f = new File(String.format("src/%s/resources/reports", folder));

        if (!f.exists()) {
            f.mkdir();
        }

        String msg = this.displayNeededCash();
        String msg2 = this.transactionHistory();
        String fileName = String.format("src/%s/resources/reports/available_change.txt", this.folder);
        String fileName2 = String.format("src/%s/resources/reports/transaction_history.txt", this.folder);

        PrintWriter out = null;
        PrintWriter out2 = null;
        try {
            out = new PrintWriter(new FileOutputStream(fileName));
            out.write(msg);
            out2 = new PrintWriter(new FileOutputStream(fileName2));
            out2.write(msg2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.close();
        out2.close();
    }

    public String transactionHistory() {
        String header = "";
        String[] headings = {"date", "time", "paid", "change", "method", "user", "item", "code", "amount"};

        for (int i = 0; i < headings.length; i++) {
            header = header + String.format("%-20s", headings[i]);
        }

        JSONArray history = this.data.getHistJSON();
        for (int i = 0; i < history.length(); i++) {
            String element = "";
            JSONObject transaction = history.getJSONObject(i);
            if (transaction.getString("status").equals("success")) {
                element = element + "\n";
                for (int j = 0; j < 6; j++) {
                    String ans = "";
                    if (j == 2 || j == 3) {
                        ans = Double.toString(transaction.getDouble(headings[j]));
                    } else {
                        ans = transaction.getString(headings[j]);
                    }
                    element = element + String.format("%-20s", ans);
                }
                JSONArray items = transaction.getJSONArray("items");

                for (int x = 0; x < items.length(); x++) {
                    JSONObject product = items.getJSONObject(x);
                    String name = product.getString("name");
                    String code = product.getString("code");
                    String amount = Integer.toString(product.getInt("amount"));

                    if (x == 0) {
                        element = element + String.format("%-20s%-20s%-20s", name, code, amount);
                    } else {
                        int num = 120 + name.length();
                        element = element + String.format("%120s%-20s%-20s%-20s", "", name, code, amount);
                    }

                    if (items.length() > x + 1) {
                        element = element + "\n";
                    }
                }
            }

            header = header + element;
        }

        return header;
    }

    public void deleteCashierReports() {
        File fileName = new File(String.format("src/%s/resources/reports/available_change.txt", this.folder));
        File fileName2 = new File(String.format("src/%s/resources/reports/transaction_history.txt", this.folder));

        // Checking if the specified file exists or not
        if (fileName.exists()) {
            fileName.delete();
        }

        if (fileName2.exists()) {
            fileName2.delete();
        }
    }
}
