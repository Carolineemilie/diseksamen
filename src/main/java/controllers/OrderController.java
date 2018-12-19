package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.cbsexam.OrderEndpoints;
import model.Address;
import model.LineItem;
import model.Order;
import model.User;
import utils.Log;
import cache.OrderCache;

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
                String sql = "SELECT\n" +
                        "orders.id as order_id,\n" +
                        "orders.order_total,\n" +
                        "orders.created_at as order_created_at,\n" +
                        "orders.updated_at as order_updated_at,\n" +
                        "user.id as user_id,\n" +
                        "user.email as user_email,\n" +
                        "user.password as user_password,\n" +
                        "user.last_name as user_lastname,\n" +
                        "user.first_name as user_firstname,\n" +
                        "line_item.id as line_item_id, \n" +
                        "line_item.quantity as line_item_quantity,\n" +
                        "line_item.price as line_item_price,\n" +
                        "product.id as product_id,\n" +
                        "product.stock as product_stock,\n" +
                        "product.description as product_description,\n" +
                        "product.price as product_price,\n" +
                        "product.sku as product_sku,\n" +
                        "product.product_name, \n" +
                        "billing.id as billing_id,\n" +
                        "billing.name as billing_name,\n" +
                        "billing.street_address as billing_street_adress,\n" +
                        "billing.city as billing_city,\n" +
                        "billing.zipcode as billing_zipcode,\n" +
                        "shipping.id as shipping_id,\n" +
                        "shipping.name as shipping_name,\n" +
                        "shipping.street_address as shipping_street_adress,\n" +
                        "shipping.city as shipping_city,\n" +
                        "shipping.zipcode as shipping_zipcode \n" +
                        "FROM orders \n" +
                        "LEFT JOIN user ON orders.user_id = user.id\n" +
                        "LEFT JOIN line_item ON orders.id = line_item.order_id\n" +
                        "LEFT JOIN product ON line_item.product_id = product.id\n" +
                        "LEFT JOIN address as billing ON orders.billing_address_id = billing.id\n" +
                        "LEFT JOIN address as shipping ON orders.shipping_address_id = shipping.id\n" +
                        "WHERE orders.id = " + id;

        OrderCache orderCache = new OrderCache();
        orderCache.getOrder(true);

        // Do the query in the database and create an empty object for the results
        ResultSet rs = dbCon.query(sql);

        try {
            if (rs.next()) {

                // Perhaps we could optimize things a bit here and get rid of nested queries.
                //User user = UserController.getUser(rs.getInt("user_id"));
                //ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
                //Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
                //Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));
                User user = UserController.getUserNoneNested(rs);
                ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrderNoneNested(rs);
                Address billingAddress = AddressController.getAddressNoneNested(rs,"billing");
                Address shippingAddress = AddressController.getAddressNoneNested(rs,"shipping");

                // Create an object instance of order from the database data
                Order order =
                        new Order(
                                rs.getInt("order_id"),
                                user,
                                lineItems,
                                billingAddress,
                                shippingAddress,
                                rs.getFloat("order_total"),
                                rs.getLong("order_created_at"),
                                rs.getLong("order_updated_at"));

                // Returns the built order
                System.out.println("test");
                return order;
            } else {
                System.out.println("No order found");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

        // Returns null
        return null;
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
        dbCon = new DatabaseController();

        String sql = "SELECT * FROM orders";

        ResultSet rs = dbCon.query(sql);
        ArrayList<Order> orders = new ArrayList<Order>();

        try {
            while (rs.next()) {

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

        // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.:FIX

        Connection connection = DatabaseController.getConnection();

        try {
            connection.setAutoCommit(false);

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
            for (LineItem item : order.getLineItems()) {
                item = LineItemController.createLineItem(item, order.getId());
                items.add(item);
            }

            order.setLineItems(items);

            connection.commit();

        } catch (SQLException et) {
            System.out.println(et.getMessage());

            try {
                connection.rollback();
                System.out.println("Roll back transactions");
            } catch (SQLException R1) {
                System.out.println("Transactions were not rolled back successfully");
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException R2) {
                    et.printStackTrace();
                }
            }

        }

        OrderEndpoints.orderCache.getOrder(true);


        // Return order
        return order;

    }

}