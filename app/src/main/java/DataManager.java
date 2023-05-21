import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class DataManager {

    String folder;
    JSONObject cashJSONobj;
    JSONArray itemsJSONarr;
    JSONArray cardsJSONarr;
    JSONArray histJSONarr;
    JSONArray usersJSONarr;

    public DataManager(String folder) {
        this.folder = folder;
    }

    public void readData() {

        cashJSONobj = readJSONObject(String.format("src/%s/resources/available_cash.json", folder));
        itemsJSONarr = readJSONArray(String.format("src/%s/resources/available_items.json", folder));
        cardsJSONarr = readJSONArray(String.format("src/%s/resources/credit_cards.json", folder));
        histJSONarr = readJSONArray(String.format("src/%s/resources/transaction_hist.json", folder));
        usersJSONarr = readJSONArray(String.format("src/%s/resources/user_data.json", folder));

    }

    public JSONObject readJSONObject(String path) {

        File f = new File(path);

        try {
            InputStream is = new FileInputStream(f);
            JSONTokener tokener = new JSONTokener(is);
            JSONObject obj = new JSONObject(tokener);
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray readJSONArray(String path) {

        File f = new File(path);

        try {
            InputStream is = new FileInputStream(f);
            JSONTokener tokener = new JSONTokener(is);
            JSONArray obj = new JSONArray(tokener);
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // getting the data
    public JSONObject getCashJSON() {return cashJSONobj;}
    public JSONArray getItemsJSON() {return itemsJSONarr;}
    public JSONArray getCardsJSON() {return cardsJSONarr;}
    public JSONArray getHistJSON() {return histJSONarr;}
    public JSONArray getUsersJSON() {return usersJSONarr;}

    // setting the data
    public void setCashJSON(JSONObject cashJSONobj) {this.cashJSONobj = cashJSONobj;}
    public void setItemsJSON(JSONArray itemsJSONarr) {this.itemsJSONarr = itemsJSONarr;}
    public void setHistJSON(JSONArray histJSONarr) {this.histJSONarr = histJSONarr;}
    public void setUsersJSON(JSONArray usersJSONarr) {this.usersJSONarr = usersJSONarr;}

    public void writeJSON(String JSONFile) {

        String fileName = "";
        String toWrite = "";

        // If other files needs to be written, conditions can be added
        if (JSONFile.equals("cash")) {
            fileName = "available_cash.json";
            toWrite = this.cashJSONobj.toString(4);
        } else if (JSONFile.equals("items")) {
            fileName = "available_items.json";
            if (this.itemsJSONarr != null) {
                toWrite = this.itemsJSONarr.toString(4);
            }
        } else if (JSONFile.equals("user")) {
            fileName = "user_data.json";
            toWrite = this.usersJSONarr.toString(4);
        } else if (JSONFile.equals("transaction")){
            fileName = "transaction_hist.json";
            toWrite = this.histJSONarr.toString(4);
        }

        String path = String.format("src/%s/resources/%s", this.folder, fileName);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(path));
            out.write(toWrite);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();
    }

}
