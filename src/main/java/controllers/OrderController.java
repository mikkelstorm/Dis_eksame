package controllers;

import model.*;
import utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

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
   * Get all orders in database
   *
   * @return
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

  public static Order createOrder(Order order) {

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



//  Connection dbConTest = null;
//  PreparedStatement preparedStatement = null;
//  dbConTest = DatabaseController.getConnection();
//  dbConTest.setAutoCommit(false);

  public static Order createOrderTest(Order order) {

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

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.   FIX

    Connection dbConTest = null;
    dbConTest = DatabaseController.getConnection();
    int orderID;


    // https://stackoverflow.com/questions/40761905/best-practices-for-sql-transactions-in-java
    try {
      dbConTest.setAutoCommit(false);

      orderID = dbCon.insert(
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

      dbConTest.commit();


    }catch (SQLException e){
      e.printStackTrace();
      try{
        System.out.print("Transaction is being rolled back");
        dbConTest.rollback();
      }catch (SQLException er){
        er.printStackTrace();
      }

    }finally {
      if (dbConTest != null){
        try {
          dbConTest.close();
        }catch (SQLException ec){
          ec.printStackTrace();
        }
      }
      try {
        dbConTest.setAutoCommit(true);
      }catch (SQLException esac){
        esac.printStackTrace();
      }
    }

    if (order.getId() != 0){
      return order;
    }else {
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