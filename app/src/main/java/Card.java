
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

public class Card {
    private String holder_name;
    private String card_number;

//    public Card(String holder_name, String card_number) {
//        this.holder_name = holder_name;
//        this.card_number = card_number;
//    }

    public void setHolder(String holder) { this.holder_name = holder; }
    public void setNumber(String n) { this.card_number = n; }
    public String getHolder() { return this.holder_name; }
    public String getNumber() { return this.card_number; }

    public boolean checkCard(JSONArray cards) {
        // check whether the card is valid
        for (Object a_card : cards) {
            JSONObject one_card = (JSONObject) a_card;

            if (one_card.get("name").equals(this.holder_name) && one_card.get("number").equals(this.card_number)) {
                System.out.println("The card is valid:)");
                return true;
            }
        }
        System.out.println("The card is invalid:(");
        return false;
    }

    public void storeCard(DataManager data, User user, Scanner scan) {
        System.out.println("Do you want to save the card details? (yes/no)");
        String input = scan.nextLine();

        JSONObject new_card = new JSONObject();
        new_card.put("name", this.holder_name);
        new_card.put("number", this.card_number);

        if (input.equals("yes")) {
            if (user.getName().equals("anonymous")) {
                System.out.println("Anonymous user is not allowed to store card in the machine.");

            }else {
                System.out.println("The card is saved in your account.");

                // change user's data
                JSONArray user_data = data.getUsersJSON();

                for (Object each_user : user_data) {
                    JSONObject matched_user = (JSONObject) each_user;

                    // assume username is unique
                    if (matched_user.get("name").equals(user.getName())) {

                        // check whether the user already stores a card
//                    if (matched_user.has("card")) {
                        matched_user.put("card", new_card);
                        break;
                    }
                }
                data.setUsersJSON(user_data);
                data.writeJSON("user");
            }
        }else {
            System.out.println("The card is not saved in the machine.");
        }
    }
}
