import org.json.JSONArray;
import org.json.JSONObject;

public class User {
    private String name;
    private String password;
    private String role;

    DataManager dataManager;
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return this.name; }
    public String getPassword() { return this.password; }
    public String getRole() { return this.role; }

    public boolean matchUserData(String name, String password, JSONArray userData){
       for (Object obj: userData){
           JSONObject userObj = (JSONObject) obj;
           String userName = userObj.getString("name");

           if (userName.equals(name)){
               String userPassword = userObj.getString("password");
               if (userPassword.equals(password)){
                   return true;
               }return false;
           }
       }
       return false;
    }

    public boolean isCustomer(String name, JSONArray userData) {
        for (Object obj: userData){
            JSONObject userObj = (JSONObject) obj;
            String userName = userObj.getString("name");

            if (userName.equals(name)){
                if (name != "anonymous") {
                    if (userObj.getString("role").equals("customer")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean userNameExists(String name, JSONArray userData){
        for (Object obj: userData){
            JSONObject userObj = (JSONObject) obj;
            String userName = userObj.getString("name");

            if (userName.equals(name)){
                return true;
            }
        }
        return false;
    }


    public void createAccount(String name, String password, String role, JSONArray userData,
                              DataManager dataManager){

        JSONObject newUserObj = new JSONObject();
        newUserObj.put("name", name);
        newUserObj.put("password", password);
        newUserObj.put("role", role);
        JSONArray hist = new JSONArray();
        newUserObj.put("history", hist);
        userData.put(newUserObj);
        dataManager.writeJSON("user");
    }

    public void removeAccount(String name, JSONArray userData, DataManager dataManager){
        int i = 0;
        boolean found = false;
        for (Object obj: userData){
            JSONObject userObj = (JSONObject) obj;
            String userName = userObj.getString("name");

            if (userName.equals(name)){
                found = true;
                break;
            }else{
                i++;
            }
        }
        if (found){
            userData.remove(i--);
            dataManager.writeJSON("user");
        }
    }

    public boolean inputNotEmpty(String name, String password){
        if (name != null && password != null) {
            if (name.length() > 0 & password.length() > 0){
                return true;
            }
        }
        return false;
    }

}
