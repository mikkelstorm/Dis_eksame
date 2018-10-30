package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it.       :FIX
    //Hashing af password sker ved at gemme password igennem metoden hashWithSaltMd5
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + new Hashing().hashWithSaltMd5(user.getPassword())
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }


  //Selv lavet metode
  public static User loginUser(User user){


    // Skriver i log at vi er noget til dette step
    Log.writeLog(UserController.class.getName(), user, "Login as an user", 0);

    // Tjekker om der er forbindelse til databasen
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //SQL statement
    String sql = "SELECT * FROM user WHERE email=\'" + user.getEmail() + "\' AND password=\'" + user.getPassword() + "\'";

      //Kører en query med sql statement over brugernavn og kode. Returnere en tom liste, hvis der ikke findes
      //en bruger med samme brugernavn og kode i databasen
      ResultSet rs = dbCon.query(sql);

    try {
      // tager kun en user, da der ikke er flere brugere med samme brugernavn og kode
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
        user.setToken(new Hashing().sha("TestShaToken")
                + "." + new Hashing().sha(Integer.toString(user.getId())));

        // returnere fundet bruger
        return user;
      } else {
        System.out.println("Wrong email or password");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    //Returnere null user, hvis der ikke er fundet en bruger med pågældende brugernavn og kode
    return user;
  }


  public static User deleteUser(User user){

    // Skriver i log at vi er noget til dette step
    Log.writeLog(UserController.class.getName(), user, "Deleting an User", 0);

    // Tjekker om der er forbindelse til databasen
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //SQL statement
    String sql = "SELECT * FROM user WHERE email=\'" + user.getEmail() + "\'";
    String token = user.getToken();

    ResultSet rs = dbCon.query(sql);

    try {
      // tager kun en user, da der ikke er flere brugere med samme brugernavn og kode
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
        user.setToken(new Hashing().sha("TestShaToken")
                + "." + new Hashing().sha(Integer.toString(user.getId())));

        // returnere fundet bruger

      } else {
        System.out.println("Something went wrong");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    if(token.equals(user.getToken())){
      System.out.println("Det virker\n userToken" + user.getToken() + "\n tokenBody" + token);
      String deleteSQL = "DELETE FROM user WHERE email= \'" + user.getEmail() + "\'";
      dbCon.insert(deleteSQL);

      return user;

    }else{
      System.out.println("Du fucked op");
      return null;
    }

  }


  public static User UpdateUser(User user){

    // Skriver i log at vi er noget til dette step
    Log.writeLog(UserController.class.getName(), user, "Update an User", 0);

    // Tjekker om der er forbindelse til databasen
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //SQL statement
    String sql = "SELECT * FROM user WHERE id=" + user.id;
    String token = user.getToken();

    ResultSet rs = dbCon.query(sql);
    User tempUser = user;

    try {
      // tager kun en user, da der ikke er flere brugere med samme brugernavn og kode
      if (rs.next()) {
        tempUser =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
        tempUser.setToken(new Hashing().sha("TestShaToken")
                + "." + new Hashing().sha(Integer.toString(tempUser.getId())));

      } else {
        System.out.println("Could not find User");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }


    if(token.equals(tempUser.getToken())){
      System.out.println("Det virker\n userToken" + user.getToken() + "\n tokenBody" + token);
      //SQL statement
      String updateSql = "UPDATE user SET first_name= \'" + user.firstname + "\', " +
                                          "last_name= \'" + user.lastname + "\', " +
                                          "email= \'" + user.email + "\' " +
                          "WHERE id=" + user.id;

      dbCon.insert(updateSql);

      return user;
    }else{
      System.out.println("Du fucked op");
      return null;
    }









  }






}
