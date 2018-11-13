package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cbsexam.UserEndpoints;
import com.auth0.jwt.algorithms.Algorithm;
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

    UserCache userCache = new UserCache();
    userCache.getUsers(true);

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
    // TODO: Hash the user password before saving it.
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + user.getPassword()
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    UserEndpoints.userCache.getUsers(true);

    // Return user
    return user;
  }

  public static String getLogin(User user) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Making the query for the database
    String sql = "SELECT * FROM user WHERE email=" + user.getEmail() + "AND password" + Hashing.shaWithSalt(user.getPassword());

    //Doing the query for the database
    ResultSet rs = dbCon.query(sql);
    User userLogin;
    String token = null;

    try {
      //Command "next" will start from the top of the result set and therefore get our object, as it only contains a single object
      if (rs.next()) {
        userLogin = new User(

                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        if (userLogin != null) {

          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create()
                    .withClaim("userId", user.getId())
                    .withIssuer("auth0")
                    .sign(algorithm);
          } catch (JWTCreationException exception) {
            //Invalid signing configuration/ could not convert claims
          } finally {
            return token;
          }
        }

      } else {
        System.out.println("Was unable to find the user");
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    //Return null
    return "";
  }

  public static User deleteUser(User user) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    try {
      PreparedStatement deleteUser = dbCon.getConnection().prepareStatement("DELETE FROM user WHERE id = ?");
      deleteUser.setInt(1, user.getId());

      deleteUser.executeUpdate();

    } catch (SQLException sql) {
      sql.getStackTrace();
    }
    return user;

  }


  public static User updateUser(User user) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    try {
      PreparedStatement updateUser = DatabaseController.getConnection().prepareStatement("UPDATE user SET firstname =? SET lastname =? SET password =? SET email =? WHERE id =?");

      updateUser.setString(1, user.getFirstname());
      updateUser.setString(2, user.getLastname());
      updateUser.setString(3, user.getPassword());
      updateUser.setString(4, user.getEmail());
      updateUser.setInt(5, user.getId());

      updateUser.executeUpdate();

    } catch (SQLException sql) {
      sql.printStackTrace();
    }
    return user;

  }

  public static String getTokenVerifier(User user) {
    //Checking if there is a connection to the database
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Building the actual query for the database

    String sql = "SELECT * FROM user WHERE id=" + user.getId();

    //The query is executed
    ResultSet rs = dbCon.query(sql);
    User sessionToken;
    String token = user.getToken();

    try {
      //will get the first object of the user table, since we only have one
      if (rs.next()) {
        sessionToken =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        if (sessionToken != null) {
          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            Claim claim = jwt.getClaim("userID");

            if (user.getId() == claim.asInt()) {
              return token;
            }

          } catch (JWTVerificationException e) {
            System.out.println(e.getMessage());

          }

        }

      } else {
        System.out.println("The user could not be found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    //Return null
    return "";
  }

}
