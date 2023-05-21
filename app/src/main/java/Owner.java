import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Owner {
    private String folder;
    private DataManager data;

    public Owner(DataManager data, String folder) {
        this.folder = folder;
        this.data = data;
    }

    public void writeUserReports() {
        if (!isFolderExists())
            createFolder();
        String msg = this.usernamesRoles();
        String path = String.format("src/%s/resources/reports/users.txt", this.folder);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(path));
            out.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();
    }

    public String usernamesRoles() {
        JSONArray allUsers = this.data.getUsersJSON();
        String ans = "";
        HashMap<String, String> userRoles = new HashMap<>();
        int num = allUsers.length()-1;
        String[] names = new String[num];

        for (int i = 1; i < allUsers.length(); i++) {
            JSONObject user = allUsers.getJSONObject(i);
            String username = user.getString("name");
            String role = user.getString("role");
            userRoles.put(username, role);
            names[i-1] = username;
        }

        Arrays.sort(names);

        for (String s: names) {
            ans = ans + s + ": " + userRoles.get(s) + "\n";
        }

        return ans;
    }

    public void deleteUserReports() {
        File fileName = new File(String.format("src/%s/resources/reports/users.txt", this.folder));

        // Checking if the specified file exists or not
        if (fileName.exists()) {
            fileName.delete();
        }
    }

    public String constructFailedTransactions() {

        JSONArray histJSON = data.getHistJSON();

        StringBuilder sb = new StringBuilder();

        //date,time,user,reason = 10,20,20,50
        int maxDate = 10;
        int maxTime = 20;
        int maxUser = 20;
        int maxReason = 50;

        //int fillerLen = maxDate+maxTime+maxUser+maxReason+3;
        int fillerLen = maxDate+maxUser+maxReason+2;
        String thickBorder = String.format("|%s|\n",
                StringUtils.repeat('=', fillerLen));

        sb.append("\n");
        sb.append(thickBorder);

        String dateT = StringUtils.center("date", maxDate);
        //String timeT = StringUtils.center("time", maxTime);
        String userT = StringUtils.center("user", maxUser);
        String reasonT = StringUtils.center("reason", maxReason);

//        sb.append(String.format("|%s|%s|%s|%s|",
//                dateT, timeT, userT, reasonT));
        sb.append(String.format("|%s|%s|%s|\n",
                dateT, userT, reasonT));

        sb.append(thickBorder);

        int count = 0;

        for (Object obj : histJSON) {
            JSONObject transaction = (JSONObject) obj;

            if (transaction.getString("status").equals("success"))
                continue;

            count++;

            String user = StringUtils.center(
                    transaction.getString("user"), maxUser);
            String date = StringUtils.center(
                    transaction.getString("date"), maxDate);
//            String time = StringUtils.center(
//                    transaction.getString("time"), maxTime);
            String reason = StringUtils.center(
                    transaction.getString("reason"), maxReason);

            sb.append(String.format("|%s|%s|%s|\n", date, user, reason));


        }

        sb.append(thickBorder);

        return sb.toString();
    }

    public boolean writeFailedTransactions() {

        // create folder if it doesn't exist.
        if (!isFolderExists())
            createFolder();

        try {
            File f = new File(String.format(
                    "src/%s/resources/reports/failed_transactions.txt", folder));
            PrintWriter writer = new PrintWriter(f);

            writer.print(constructFailedTransactions());

            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean deleteFailedTransactions() {

        File f = new File(String.format(
                "src/%s/resources/reports/failed_transactions.txt", folder));

        // return if the file does not exist.
        if (!f.exists())
            return false;

        f.delete();

        return true;
    }

    public boolean isFolderExists() {

        File f = new File(String.format("src/%s/resources/reports", folder));

        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public void createFolder() {
        File file = new File(String.format("src/%s/resources/reports", folder));
        file.mkdir();
    }

}
