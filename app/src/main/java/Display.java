import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

public class Display {

    private boolean horizontal;
    private final String categories[] = {"Drinks",
            "Chocolates", "Chips", "Candies"};
    private int max_word;

    private JSONArray drinks;
    private JSONArray chocs;
    private JSONArray chips;
    private JSONArray candies;

    private JSONArray items[];
    private int max;

    private int maxFiller;

    public Display(int max_word) {

        this.horizontal = true;
        this.max_word = max_word;
    }

    public void display(JSONArray itemsJSON, JSONArray usersJSON,
                        String user) {

        displayItem(itemsJSON);
        displayPurchases(usersJSON, user);
    }

    public void displayItem(JSONArray itemsJSON) {

        groupItem(itemsJSON);

        String ret;
        if (horizontal)
            ret = constructHorizontal();
        else
            ret = constructVertical();

        System.out.print(ret);
    }

    public void displayPurchases(JSONArray usersJSON,
                                  String user) {

        String ret = constructPurchases(usersJSON, user);

        System.out.print(ret);
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public boolean groupItem(JSONArray itemsJSON) {
        drinks = new JSONArray();
        chocs = new JSONArray();
        chips = new JSONArray();
        candies = new JSONArray();

        try {
            for (Object obj : itemsJSON) {

                JSONObject item = (JSONObject) obj;
                String category = item.getString("category");

                if (category.equals(categories[0]))
                    drinks.put(item);
                else if (category.equals(categories[1]))
                    chocs.put(item);
                else if (category.equals(categories[2]))
                    chips.put(item);
                else if (category.equals(categories[3]))
                    candies.put(item);
            }
        } catch (JSONException e) {
            return false;
        }

        JSONArray items[] = {drinks, chocs, chips, candies};

        // get the size of the largest category.
        int max = items[0].length();
        for (int i = 0; i < items.length; i++) {
            if (items[i].length() > max)
                max = items[i].length();
        }

        this.max = max;
        this.items = items;
        return true;
    }

    public String constructVertical() {

        StringBuilder sb = new StringBuilder();

        int filler = max_word*4+4+max_word;
        maxFiller = filler + 2;

        sb.append("\n|");
        sb.append(StringUtils.repeat('=', filler));
        sb.append("|\n|");

        sb.append(StringUtils.repeat(' ', max_word));
        for (String s : categories) {
            sb.append(String.format("|%s", StringUtils.center(s, max_word)));
        }

        sb.append("|\n|");
        sb.append(StringUtils.repeat('=', filler));
        sb.append("|\n");

        for (int i = 0; i < max; i++) {

            JSONObject drink = null;
            JSONObject choc = null;
            JSONObject chip = null;
            JSONObject candy = null;

            if (i < drinks.length())
                drink = drinks.getJSONObject(i);

            if (i < chocs.length())
                choc = chocs.getJSONObject(i);

            if (i < chips.length())
                chip = chips.getJSONObject(i);

            if (i < candies.length())
                candy = candies.getJSONObject(i);

            JSONObject items[] = {drink, choc, chip, candy};

            // add the names
            sb.append(String.format("||%s|",
                    StringUtils.center("name", max_word-2)));

            for (int j = 0; j < items.length; j++) {

                JSONObject item = items[j];

                if (item != null) {
                    sb.append(String.format("|%s",
                            StringUtils.center(item.getString("name"), max_word)));
                }
                else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }
            }
            sb.append("|\n||");
            sb.append(StringUtils.repeat('_', filler-1));
            sb.append("|\n");

            // add the prices
            sb.append(String.format("||%s|",
                    StringUtils.center("price", max_word-2)));

            for (int j = 0; j < items.length; j++) {

                JSONObject item = items[j];

                if (item != null) {
                    float price = item.getFloat("price");
                    String priceStr = String.format("$%.2f", price);
                    sb.append(String.format("|%s", StringUtils.center(priceStr, max_word)));
                }
                else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }
            }
            sb.append("|\n||");
            sb.append(StringUtils.repeat('_', filler-1));
            sb.append("|\n");

            // add the item codes
            sb.append(String.format("||%s|",
                    StringUtils.center("code", max_word-2)));

            for (int j = 0; j < items.length; j++) {

                JSONObject item = items[j];

                if (item != null) {
                    int code = item.getInt("code");
                    String codeStr = Integer.toString(code);
                    sb.append(String.format("|%s", StringUtils.center(codeStr, max_word)));
                }
                else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }
            }

            sb.append("|\n||");
            sb.append(StringUtils.repeat('_', filler-1));
            sb.append("|\n");

            // add the available amount
            sb.append(String.format("||%s|",
                    StringUtils.center("available", max_word-2)));

            for (int j = 0; j < items.length; j++) {

                JSONObject item = items[j];

                if (item != null) {
                    int amount = item.getInt("amount");
                    String amountStr = Integer.toString(amount);
                    sb.append(String.format("|%s", StringUtils.center(amountStr, max_word)));
                }
                else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }
            }

            sb.append("|\n|");
            sb.append(StringUtils.repeat('=', filler));
            sb.append("|\n");

        }
        sb.append("\n");

        return sb.toString();
    }

    public String constructHorizontal() {

        StringBuilder sb = new StringBuilder();

        int filler = (max_word * (max+1)) + max+1;
        maxFiller = filler + max_word + 2;

        sb.append("\n|");
        sb.append(StringUtils.repeat('=', filler+max_word));
        sb.append("|\n");

        for (int j = 0; j < items.length; j ++) {
            JSONArray products = items[j];

            // add name
            sb.append("|");
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(String.format("||%s|",
                    StringUtils.center("name", max_word-2)));

            for (int i = 0; i < max; i++) {
                if (i < products.length()) {
                    JSONObject product = products.getJSONObject(i);
                    sb.append(String.format("|%s",
                            StringUtils.center(product.getString("name"), max_word)));
                } else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }

            }

            sb.append("|\n|");
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(StringUtils.repeat('_', filler));
            sb.append("|\n");


            // add category
            sb.append(String.format("|%s",
                    StringUtils.center(categories[j], max_word)));

            // add price
            sb.append(String.format("||%s|",
                    StringUtils.center("price", max_word-2)));

            for (int i = 0; i < max; i++) {
                if (i < products.length()) {
                    JSONObject product = products.getJSONObject(i);
                    float price = product.getFloat("price");
                    String priceStr = String.format("$%.2f", price);
                    sb.append(String.format("|%s", StringUtils.center(priceStr, max_word)));
                } else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }

            }

            sb.append("|\n|");
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(StringUtils.repeat('_', filler));
            sb.append("|\n|");

            // add code
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(String.format("||%s|",
                    StringUtils.center("code", max_word-2)));

            for (int i = 0; i < max; i++) {
                if (i < products.length()) {
                    JSONObject product = products.getJSONObject(i);
                    int code = product.getInt("code");
                    String codeStr = Integer.toString(code);
                    sb.append(String.format("|%s", StringUtils.center(codeStr, max_word)));
                } else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }

            }

            sb.append("|\n|");
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(StringUtils.repeat('_', filler));
            sb.append("|\n|");

            // add available amount
            sb.append(StringUtils.repeat(' ', max_word));
            sb.append(String.format("||%s|",
                    StringUtils.center("available", max_word-2)));

            for (int i = 0; i < max; i++) {
                if (i < products.length()) {
                    JSONObject product = products.getJSONObject(i);
                    int code = product.getInt("amount");
                    String codeStr = Integer.toString(code);
                    sb.append(String.format("|%s", StringUtils.center(codeStr, max_word)));
                } else {
                    sb.append(String.format("|%s",
                            StringUtils.center("", max_word)));
                }
            }

            sb.append("|\n|");
            sb.append(StringUtils.repeat('=', filler+max_word));
            sb.append("|\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    public String constructPurchases(JSONArray usersJSON,
                      String user) {

        JSONArray history = null;

        try {
            for (Object obj: usersJSON) {

                JSONObject item = (JSONObject) obj;
                String name = item.getString("name");

                if (name.equals(user)) {
                    history = item.getJSONArray("history");
                }
            }
        } catch (JSONException e) {
            return "Error: There is a key error";
        }

        StringBuilder sb = new StringBuilder();

        String firstLine = null;

        if (user.equals("anonymous")) {
            firstLine = "|Last Purchases by anonymous users|";
        } else {
            firstLine = String.format("|Last Purchases by User: %s|", user);
        }

        int filler = firstLine.length()-2;

        // making the equals ('=') line
        String temp = String.format("|%s|",
                StringUtils.repeat('=', filler));
        String fillerStr = StringUtils.center(temp, maxFiller);

        sb.append(fillerStr);
        sb.append("\n");

        sb.append(StringUtils.center(firstLine, maxFiller));
        sb.append("\n");

        sb.append(fillerStr);
        sb.append("\n");

        if (history != null && history.length() > 0) {

            for (Object obj : history) {

                String item = (String) obj;
                String item2 = StringUtils.center(
                        String.format("- %s", item), filler);
                sb.append(StringUtils.center(
                        String.format("|%s|", item2), maxFiller
                ));
                sb.append("\n");
            }
        } else {
            String item = StringUtils.center(
                    "|- No purchases|", filler);
            sb.append(StringUtils.center(
                    item, maxFiller));
            sb.append("\n");
        }

        sb.append(fillerStr);
        sb.append("\n");
        sb.append("\n");

        return sb.toString();
    }
}
