import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.*;

public class Seller {

    private String dir;
    private int max_word;
    private final int MAX_CAT = 12; //from chocolates + 2
    private final int MAX_DIGITS = 6; //arbitrary

    private final String categories[] = {"Drinks",
            "Chocolates", "Chips", "Candies"};

    private DataManager dataManager;

    public Seller(String dir, int max_word, DataManager dataManager) {
        this.dir = dir;
        this.max_word = max_word;
        this.dataManager = dataManager;
    }


    public boolean modifyItems(Scanner scan) {

        JSONArray itemsJSON = dataManager.getItemsJSON();

        while (true) {
            System.out.print("\nPlease input the code of the item you want to modify," +
                    " enter DONE when you've finished\nCode: ");
            String input = scan.nextLine();
            input = input.toUpperCase();

            if (input.equals("DONE"))
                break;

            int code = 0;
            try {
                code = new BigInteger(input).intValueExact();

                // check if the input is valid.
            } catch (NumberFormatException e) {
                System.out.println("Error: The code is not valid," +
                        " please try again.");
                continue;
            } catch (ArithmeticException e) {
                System.out.println("Error: The input is too big or too" +
                        " small for an integer.");
                continue;
            }

            int itemIdx = getItem(code, itemsJSON);

            // check if the item exist.
            if (itemIdx == -1) {
                System.out.println("Error: The item does not exist, please try again.");
                continue;
            }

            JSONObject item = itemsJSON.getJSONObject(itemIdx);
            System.out.println(String.format("\nYou chose to modify %s",
                    item.getString("name")));

            while (true) {

                System.out.println("\nPlease enter the attribute name you want to modify, " +
                        "enter DONE when you've finished:");
                System.out.print("attributes: CATEGORY, NAME, PRICE, AMOUNT\nEnter: ");
                input = scan.nextLine();
                input = input.toUpperCase();

                if (input.equals("DONE")) {
                    break;
                } else if (input.equals("CATEGORY")) {
                    String category = null;
                    while (true) {
                        System.out.print("Please enter the new category name, " +
                                "Enter CANCEL to exit modifying category:\nEnter: ");
                        category = scan.nextLine();

                        if (category.toUpperCase().equals("CANCEL"))
                            break;
                        else if (!isValidCategory(category))
                            continue;

                        item.put("category", category);
                        break;
                    }

                } else if (input.equals("NAME")) {
                    String name = null;
                    while (true) {
                        System.out.print("Please enter the new product name" +
                                "Enter CANCEL to exit modifying name:\nEnter: ");
                        name = scan.nextLine();

                        if (name.toUpperCase().equals("CANCEL"))
                            break;
                        else if (!isValidName(name))
                            continue;

                        item.put("name", name);
                        break;
                    }

                } else if (input.equals("PRICE")) {
                    String priceStr = null;
                    float price = -1;
                    while (true) {
                        System.out.print("Please enter the new price" +
                                "Enter CANCEL to exit modifying price:\nEnter: ");
                        priceStr = scan.nextLine();

                        price = isValidPrice(priceStr);

                        if (priceStr.toUpperCase().equals("CANCEL"))
                            break;
                        else if (price == -1)
                            continue;

                        item.put("price", price);
                        break;
                    }

                } else if (input.equals("AMOUNT")) {
                    String amountStr = null;
                    int amount = -1;
                    while (true) {
                        System.out.print("Please enter the new amount" +
                                "Enter CANCEL to exit modifying amount:\nEnter: ");
                        amountStr = scan.nextLine();

                        amount = isValidAmount(amountStr);

                        if (amountStr.toUpperCase().equals("CANCEL"))
                            break;
                        else if (amount == -1)
                            continue;

                        item.put("amount", amount);
                        break;
                    }

                } else {
                    System.out.println(String.format("Error: There is no attribute called %s", input));
                }
            }
            itemsJSON.put(itemIdx, item);

        }

        dataManager.writeJSON("items");

        return true;
    }

    public int getItem(int code, JSONArray itemsJSON) {

        for (int i = 0; i < itemsJSON.length(); i++) {

            JSONObject item = itemsJSON.getJSONObject(i);
            int itemCode = item.getInt("code");

            if (itemCode == code)
                return i;
        }

        return -1;
    }

    public boolean isValidCategory(String category) {
        if (category.length() == 0) {
            System.out.println("Error: The input is empty.");
            return false;
        }

        boolean matched = false;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category))
                matched = true;
        }
        if (!matched) {
            System.out.println("Error: The input does not match any category name.");
            return false;
        }

        return true;
    }

    public boolean isValidName(String name) {
        if (name.length() == 0) {
            System.out.println("Error: The input is empty.");
            return false;
        }

        if (name.length() > max_word) {
            System.out.println(String.format(
                    "Error: The input length exceeded the allowed range of %d",
                    max_word));
            return false;
        }

        return true;
    }

    public float isValidPrice(String priceStr) {
        if (priceStr.length() == 0) {
            System.out.println("Error: The input is empty.");
            return -1;
        }

        float price = 0;
        try {

            price = Float.parseFloat(priceStr);
            if (String.valueOf(price).contains("E")) {
                System.out.println("Error: Value is too large or " +
                        "too small for a float.");
                return -1;
            }

        } catch(NumberFormatException e) {
            System.out.println("Error: The input is not a float.");
            return -1;
        }

        if (price < 0) {
            System.out.println("Error: Price cannot be negative.");
            return -1;
        }

        return price;
    }

    public int isValidAmount(String amountStr) {
        if (amountStr.length() == 0) {
            System.out.println("Error: The input is empty.");
            return -1;
        }

        int amount = 0;
        try {

            amount = new BigInteger(amountStr).intValueExact();

        } catch(NumberFormatException e) {
            System.out.println("Error: The input is not an integer.");
            return -1;
        } catch(ArithmeticException e) {
            System.out.println("Error: Input is too large or too small " +
                    "for an integer.");
            return -1;
        }

        if (amount < 0) {
            System.out.println("Error: Amount cannot be negative.");
            return -1;
        }

        if (amount > 15) {
            System.out.println("Error: Amount cannot exceed 15.");
            return -1;
        }

        return amount;
    }

    public String constructAvailableItem() {

        JSONArray itemsJSON = dataManager.getItemsJSON();
        StringBuilder sb = new StringBuilder();

        int fillerLen = max_word+4+MAX_CAT+(MAX_DIGITS*2)+4;
        String thickBorder = String.format("|%s|\n",
                StringUtils.repeat('=', fillerLen));

        sb.append("\n");
        sb.append(thickBorder);

        String nameT = StringUtils.center("name", max_word);
        String categoryT = StringUtils.center("category", MAX_CAT);
        String priceT = StringUtils.center("price", MAX_DIGITS);
        String amountT = StringUtils.center("amount", MAX_DIGITS);

        sb.append(String.format("|code|%s|%s|%s|%s|\n",nameT, categoryT,
                priceT, amountT));

        sb.append(thickBorder);

        for (Object obj : itemsJSON) {
            JSONObject item = (JSONObject) obj;

            String name = StringUtils.center(
                    item.getString("name"), max_word);
            String category = StringUtils.center(
                    item.getString("category"), MAX_CAT);

            String price = String.valueOf(item.getFloat("price"));
            String priceCen = StringUtils.center(price, MAX_DIGITS);

            String amount = String.valueOf(item.getInt("amount"));
            String amountCen = StringUtils.center(amount, MAX_DIGITS);

            String code = String.valueOf(item.getInt("code"));

            sb.append(String.format("|%s|%s|%s|%s|%s|\n",
                    code, name, category, priceCen, amountCen));

        }
        sb.append(thickBorder);

        return sb.toString();
    }

    public JSONObject getSummarySoldItem() {

        JSONArray itemsJSON = dataManager.getItemsJSON();
        JSONArray histJSON = dataManager.getHistJSON();

        JSONObject summary = new JSONObject();

        for (Object obj : itemsJSON) {
            JSONObject item = (JSONObject) obj;

            JSONObject details = new JSONObject();
            details.put("code", item.getInt("code"));
            details.put("sold", 0);

            summary.put(item.getString("name"), details);
        }

        // get the items sold by iterating through histJSON
        // and put the sold amount to summary.
        for (Object obj : histJSON) {
            JSONObject transaction = (JSONObject) obj;

            if (transaction.getString("status").equals("fails"))
                continue;

            JSONArray items = transaction.getJSONArray("items");

            for (Object o : items) {
                JSONObject item = (JSONObject) o;

                // get the item details from hist.
                String name = item.getString("name");
                int sold = item.getInt("amount");
                JSONObject details = null;
                int totalSold = 0;

                if (summary.has(name)) {
                    // get the item details from summary.
                    details = summary.getJSONObject(name);
                    totalSold = details.getInt("sold");
                    totalSold += sold;
                    details.put("sold", totalSold);
                    summary.put(name, details);
                }
            }

        }
        return summary;
    }

    public String constructSoldItem() {

        JSONObject summary = getSummarySoldItem();

        StringBuilder sb = new StringBuilder();

        int fillerLen = max_word+MAX_DIGITS+4+2;
        String thickBorder = String.format("|%s|\n",
                StringUtils.repeat('=', fillerLen));

        sb.append("\n");
        sb.append(thickBorder);

        String nameT = StringUtils.center("name", max_word);
        String soldT = StringUtils.center("sold", MAX_DIGITS);

        sb.append(String.format("|code|%s|%s|\n",nameT, soldT));

        sb.append(thickBorder);

        for (Object obj : summary.names()) {

            String name = (String) obj;
            JSONObject details = summary.getJSONObject(name);

            String nameCentered = StringUtils.center(name,
                    max_word);
            String sold = String.valueOf(details.getInt("sold"));
            String soldCentered = StringUtils.center(sold,
                    MAX_DIGITS);

            sb.append(String.format("|%d|%s|%s|\n",
                    details.getInt("code"), nameCentered, soldCentered));

        }

        sb.append(thickBorder);

        return sb.toString();
    }

    public boolean writeSoldItemReport() {

        // make the summary of item sold first.
        JSONObject summary = getSummarySoldItem();

        // create folder if it doesn't exist.
        if (!isFolderExists())
            createFolder();

        try {
            File f = new File(String.format("src/%s/resources/reports/items_sold.txt", dir));
            PrintWriter writer = new PrintWriter(f);

            writer.print(constructSoldItem());

            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean writeAvailableItemReport() {

        // create folder if it doesn't exist.
        if (!isFolderExists())
            createFolder();

        try {
            File f = new File(String.format("src/%s/resources/reports/available_items.txt", dir));
            PrintWriter writer = new PrintWriter(f);

            writer.print(constructAvailableItem());

            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public void printAvailableItem() {
        System.out.print(constructAvailableItem());
    }

    public void printSoldItem() {
        System.out.print(constructSoldItem());
    }

    public boolean deleteAvailableItemReport() {

        File f = new File(String.format("src/%s/resources/reports/available_items.txt", dir));

        // return if the file does not exist.
        if (!f.exists())
            return false;

        f.delete();

        return true;
    }

    public boolean deleteSoldItemReport() {

        File f = new File(String.format("src/%s/resources/reports/items_sold.txt", dir));

        // return if the file does not exist.
        if (!f.exists())
            return false;

        f.delete();

        return true;
    }

    public boolean isFolderExists() {
        File folder = new File(String.format("src/%s/resources/reports", dir));
        if (!folder.exists()) {
            return false;
        }
        return true;
    }

    public void createFolder() {
        File file = new File(String.format("src/%s/resources/reports", dir));
        file.mkdir();
    }

    public String[] getCategories() {return this.categories;}
    public int getMax_word() {return this.max_word;}
}
