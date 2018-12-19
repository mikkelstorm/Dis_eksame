package controllers;

import model.User;
import utils.Config;
import utils.Hashing;
import utils.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Klassen UserController har til formål at håndtere alle metoder med brugeren. Dette er metoder
 * som at hente data på en bruger fra databasen, oprette en ny bruger i databasen, login, slette og opdatere en bruger
 */
public class UserController {

    private static DatabaseController dbCon;

    public UserController() {
        dbCon = new DatabaseController();
    }

    /**
     * Metode der henter en specifik bruger på baggrund af brugerens ID i databasen.
     *
     * @param id UserID i databasen
     * @return bruger
     */
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

    /**
     * Opretter en bruger i databasen
     *
     * @param user data fra post call
     * @return brugeren
     */
    public static User createUser(User user) {

        //Skriver i loggen, man er noget til denne metode
        Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

        //Sætter tiden for hvornår brugeren er oprettet
        user.setCreatedTime(System.currentTimeMillis() / 1000L);

        //Tjekker om der er forbindelse til databasen
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        //TODO: tjek om der findes flere med den email?     :FIX - egen todo
        //SQL statement der henter en bruger på baggrund af deres email.
        String sql = "SELECT * FROM user WHERE email=\'" + user.getEmail() + "\'";
        ResultSet rs = dbCon.query(sql);
        Boolean check = false;

        //prøver at tjekke om der findes en bruger med denne email i databasen.
        //Dette gøres for at sikre at der ikke er flere med samme email, da flere metode er bygget på at finde en email
        //og hvis der er flere med samme email, så ændrer man måske den forkerte bruger
        try {
            // tager kun en user, da der ikke er flere brugere med samme email og kode
            if (rs.next()) {

                check = true;
                System.out.println("Brugeren findes i databasen");
                return null;

            } else {
                check = false;
                System.out.println("Brugeren findes ikke i databasen");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // Insert the user in the DB
        // TODO: Hash the user password before saving it.       :FIX
        //Hashing af password sker ved at gemme password igennem metoden hashWithSaltMd5WithTimestamp
        //Dette gør at koden bliver gemt ved at køre stingen "password" + "tiden når oprettelsen sker" og hasher det
        //saltet vil være created_time
//    if(!check) {
        int userID = dbCon.insert(
                "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                        + user.getFirstname()
                        + "', '"
                        + user.getLastname()
                        + "', '"
                        + new Hashing().HashWithSaltMd5WithTimestamp(user.getPassword(), user.getCreatedTime())
                        + "', '"
                        + user.getEmail()
                        + "', "
                        + user.getCreatedTime()
                        + ")");

        if (userID != 0) {
            //Update the userid of the user before returning
            user.setId(userID);
            return user;
        } else {
            // Return null if user has not been inserted into database
            return null;
        }
//    }
        // Return user
//    return null;
    }


    /**
     * Metoden LoginUser gør at hvis en bruger logger ind med en rigtigt kode og email, så får brugeren returneret en token
     *
     * @param user
     * @return
     */
    public static User loginUser(User user) {

        User tempUser = user;

        // Skriver i log at vi er noget til dette step
        Log.writeLog(UserController.class.getName(), user, "Login as an user", 0);

        // Tjekker om der er forbindelse til databasen
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        //SQL statement
        String sql = "SELECT * FROM user WHERE email=\'" + user.getEmail() + "\'";

        //Kører en query med sql statement over brugernavn og kode. Returnere en tom liste, hvis der ikke findes
        //en bruger med samme brugernavn og kode i databasen
        ResultSet rs = dbCon.query(sql);

        try {
            // tager kun en user, da der ikke er flere brugere med samme email og kode
            if (rs.next()) {
                user =
                        new User(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("password"),
                                rs.getString("email"),
                                rs.getLong("created_at"));

            } else {
                System.out.println("Forkert email eller kodeord");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        if (user.getPassword().equals(new Hashing().HashWithSaltMd5WithTimestamp(tempUser.getPassword(), user.getCreatedTime()))) {
            //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
            user.setToken(new Hashing().sha(Config.getTokenSalt())
                    + "." + new Hashing().sha(Integer.toString(user.getId())));
            user.setPassword(tempUser.getPassword());
            // returnere fundet bruger med valid password
            return user;
        } else {
            // returnere null, da bruger ikke fundet
            System.out.println("Token matcher ikke");
            return null;
        }

    }

    /**
     * Metoden DeleteUser sletter en bruger, hvis både email og token matcher
     *
     * @param user
     * @return
     */
    public static User deleteUser(User user) {

        // Skriver i log at vi er nået til dette step
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
            // tager kun en user, da der ikke er flere brugere med samme email
            if (rs.next()) {
                user =
                        new User(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("password"),
                                rs.getString("email"));

                //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
                user.setToken(new Hashing().sha(Config.getTokenSalt())
                        + "." + new Hashing().sha(Integer.toString(user.getId())));

            } else {
                System.out.println("Noget gik galt");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        //validere token mellem brugerens token skabt at databasen og token fra body i post request
        //Hvis valid, slettes brugeren
        if (token.equals(user.getToken())) {
            System.out.println("Det virker\n userToken" + user.getToken() + "\n tokenBody" + token);
            String deleteSQL = "DELETE FROM user WHERE email= \'" + user.getEmail() + "\'";
            dbCon.insert(deleteSQL);

            return user;

        } else {
            System.out.println("Token matcher ikke");
            return null;
        }
    }

    /**
     * UpdateUser metoden opdatere en brugers navn, kode(som resultere i nyt token) og email
     *
     * @param user
     * @return brugers nye informationer
     */
    public static User UpdateUser(User user) {

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
            // tager kun en user, da der ikke er flere brugere med samme id
            if (rs.next()) {
                tempUser =
                        new User(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("password"),
                                rs.getString("email"));

                //tilknytter en token til brugeren på baggrund af en fælles token (payload) og privat token (verify signature)
                tempUser.setToken(new Hashing().sha(Config.getTokenSalt())
                        + "." + new Hashing().sha(Integer.toString(tempUser.getId())));

            } else {
                System.out.println("Could not find User");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        //validere token mellem brugerens token skabt at databasen og token fra body i post request
        //Hvis valid, opdateres brugeren
        if (token.equals(tempUser.getToken())) {
            System.out.println("Det virker\n userToken" + user.getToken() + "\n tokenBody" + token);
            //SQL statement
            String updateSql = "UPDATE user SET first_name= \'" + user.getFirstname() + "\', " +
                    "last_name= \'" + user.getLastname() + "\', " +
                    "password= \'" + new Hashing().HashWithSaltMd5WithTimestamp(user.getPassword(), tempUser.getCreatedTime()) + "\', " +
                    "email= \'" + user.getNewEmail() + "\' " +
                    "WHERE email= \'" + user.getEmail() + "\'";

            dbCon.insert(updateSql);

            return user;
        } else {
            System.out.println("Du fucked op");
            return null;
        }
    }


}
