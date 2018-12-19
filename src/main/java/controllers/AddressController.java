package controllers;

import model.Address;
import utils.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AdresseControlleren har alle metoderne der har til formål enten at hente adresser eller oprette addresser
 */
public class AddressController {

    private static DatabaseController dbCon;

    public AddressController() {
        dbCon = new DatabaseController();
    }

    /**
     * Metoden getAddress henter adressen med en bestemt id
     *
     * @param id
     * @return adresse
     */
    public static Address getAddress(int id) {

        // Check for DB Connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Our SQL string
        String sql = "SELECT * FROM address where id=" + id;

        // Do the query and set the initial value to null
        ResultSet rs = dbCon.query(sql);
        Address address = null;

        try {
            // Get the first row and build an address object
            if (rs.next()) {
                address =
                        new Address(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("street_address"),
                                rs.getString("city"),
                                rs.getString("zipcode"));

                // Return our newly added object
                return address;
            } else {
                System.out.println("No address found");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // Returns null if we can't find anything.
        return address;
    }

    /**
     * metoden createAddress laver en adresse
     *
     * @param address
     * @return null, hvis addresse ikke er blevet gemt og ellers returneret den addressen
     */
    public static Address createAddress(Address address) {

        // Write in log that we've reach this step
        Log.writeLog(ProductController.class.getName(), address, "Actually creating a line item in DB", 0);

        // Check for DB Connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Insert the product in the DB
        int addressID = dbCon.insert(
                "INSERT INTO address(name, city, zipcode, street_address) VALUES('"
                        + address.getName()
                        + "', '"
                        + address.getCity()
                        + "', '"
                        + address.getZipCode()
                        + "', '"
                        + address.getStreetAddress()
                        + "')");

        if (addressID != 0) {
            //Update the productid of the product before returning
            address.setId(addressID);
        } else {
            // Return null if product has not been inserted into database
            return null;
        }

        // Return product, will be null at this point
        return address;
    }

}
