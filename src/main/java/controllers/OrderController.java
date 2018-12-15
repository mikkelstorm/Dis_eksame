package controllers;

import model.*;
import utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Klassen OrderController har til formål at håndtere at hente og oprette ordrer
 */
public class OrderController {

  private static DatabaseController dbCon;
  private static Connection dbConTest = null;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  /**
   * Metoden getOrder henter en specifik order på baggrund af dens ID, hvor der bliver hentet alle informationer
   * - Bruger
   * - Indkøbskurv
   * - Adresser for både levering og betaler
   * - order information
   * @param id orderID
   * @return order
   */
  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders where id=" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Metoden getOrder henter alle ordrer i databasen, hvor der bliver hentet alle informationer
   * - Bruger
   * - Indkøbskurv
   * - Adresser for både levering og betaler
   * - order information
   * @return alle ordrer i databasen
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT * FROM orders";

    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        //TODO: Skal optimeres ved en enkel SQL linje med inner joints mm
        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an order from the database data
        Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }



  private static boolean Validate(int id, String metode) {

    boolean valid = false;
    if (id > 0) {
      valid = true;
    } else {
      //TODO: skal laves så den returnere hvilke fejl i oprettelsen bliver printet i postman mm
      System.out.println(metode);
    }
    return valid;
  }

  /**
   * Metoden createOrder har til formål at oprette ordrer, hvilket bliver gjort gennem en masse SQL inserts og
   * der bliver benytter mange af de andre controller klasser.
   * Der er blevet benyttet transaktion i denne metode, da man gerne vil sikre at alt bliver overført eller intet.
   * @param order body fra post
   * @return order hvis succesfuld ellers null
   */
  public static Order createOrder(Order order) {

    //skriver i loggen, at vi har nået denne metode
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    //sætter tiden for hvornår ordren blev oprettet og opdateret
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    //Tjekker om der er forbindelse til databasen
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }


    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.   FIX
    // https://stackoverflow.com/questions/40761905/best-practices-for-sql-transactions-in-java
    //Ovenståede link er brugt til inspiration og for at finde syntaxen for at lave en transaction for databasen

    //ved SQL kald skal laves i en try catch, da der blev smidt SQLexception
    try {
      //opretter forbinde til databasen
    Connection dbConTransaction = null;
    dbConTransaction = DatabaseController.getConnection();


    //Sætter autoCommit til false, for at sikre at databasen ikke overfører statement automatisk
      dbConTransaction.setAutoCommit(false);

      //Gør klar til at gemme addresser i databasen, når der bliver Committet til databasen senere.
      //samt gemmer adresserne i ordren
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      //Gør klar til at gemme brugeren i databasen, når der bliver comittet til databasen senere.
      //Samt gemmer brugeren i ordrer
    order.setCustomer(UserController.createUser(order.getCustomer()));


    //Indsætter den fulde ordrer i databasen, gemmem databasen metoden insert ved et SQL statement
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");



      if (orderID != 0) {
        //Opdatere Ordrens ID, for at kunne gemme alle produkter i indkøbskurven med samme ID for den returnere.
        order.setId(orderID);
      }
      //Opretter en tom liste i ordrer, som bruges til at gå gennem LineItem og give dem samme ID.
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      //gemmer LineItems (indkøbskurven) i databasen
      for(LineItem item : order.getLineItems()){
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);

      //Comitter alt det der blevet prøvet at blive gemt i databasen.
      dbConTransaction.commit();


      //Catch der fanger SQLException og printer den ud
    }catch (SQLException e){
      e.printStackTrace();
      //Nyt try catch der har til formål at lave rollback, hvis noget går galt
      try{
        System.out.print("Transaction is being rolled back");
        dbConTest.rollback();
      }catch (SQLException er){
        er.printStackTrace();
      }

      //Finally statement, som i slutningen af alle transaktion sætter autoCommit til true igen, for at metoder uden
      //transaktion kan køre
    }finally {
      try {
        dbConTest.setAutoCommit(true);
      }catch (SQLException e){
        e.printStackTrace();
      }
      //Lukker databasen, hvis der er oprettet forbindelse til en database
      if (dbConTest != null){
        try {
          dbConTest.close();
        }catch (SQLException ec){
          ec.printStackTrace();
        }
      }
    }
    //returner ordren, hvis der bliver fundet en og ellers bliver der returneret null
    if (order.getId() != 0){
      return order;
    }else {
      return null;
    }

  }





















  public static Order createOrderudentransaktion(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    order.setCustomer(UserController.createUser(order.getCustomer()));

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.


    if(Validate(order.getCustomer().id, "Customer") & Validate(order.getBillingAddress().getId(), "BillingAddress") & Validate(order.getShippingAddress().getId(), "ShippingAddress")){


      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");



      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }
      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for(LineItem item : order.getLineItems()){
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);

      // Return order
      return order;
    }else {
      System.out.println("fejl i oprettelsen af order");
      return null;
    }

  }

  public static ArrayList<Order> getOrdersTest() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT * FROM orders";

    String sqlTest = "SELECT * FROM orders\n" +
            "INNER JOIN user ON orders.user_id = user.id\n" +
//            "INNER JOIN line_item ON orders.id = line_item.order_id\n" +
//            "INNER JOIN product ON line_item.product_id = product.id\n" +
            "INNER JOIN address ON orders.billing_address_id = address.id";

    ResultSet rs = dbCon.query(sqlTest);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        //TODO: Skal optimeres ved en enkel SQL linje med inner joints mm
        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));

//        Product product =
//                new Product(
//                        rs.getInt("id"),
//                        rs.getString("product_name"),
//                        rs.getString("sku"),
//                        rs.getFloat("price"),
//                        rs.getString("description"),
//                        rs.getInt("stock"));
//
//        ArrayList<LineItem> items = new ArrayList<>();
//
//        LineItem lineItems =
//                new LineItem(
//                        rs.getInt("id"),
//                        product,
//                        rs.getInt("quantity"),
//                        rs.getFloat("price"));
//
//        items.add(lineItems);


        Address billingAddress =
                new Address(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rs.getString("zipcode"));

          Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));
//        Address shippingAddress =
//                new Address(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("street_address"),
//                        rs.getString("city"),
//                        rs.getString("zipcode"));


        // Create an order from the database data
        Order order =
                new Order(
                        rs.getInt("id"),
                        user,
                        lineItems,
                        billingAddress,
                        shippingAddress,
                        rs.getFloat("order_total"),
                        rs.getLong("created_at"),
                        rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }



}