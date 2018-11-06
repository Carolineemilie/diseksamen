package com.cbsexam;

import com.google.gson.Gson;
import controllers.UserController;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

    /**
     * @param idUser
     * @return Responses
     */
    @GET
    @Path("/{idUser}")
    public Response getUser(@PathParam("idUser") int idUser) {

        // Use the ID to get the user from the controller.
        User user = UserController.getUser(idUser);

        try {
            // TODO: Add Encryption to JSON: FIX
            // Convert the user object to json in order to return the object
            String json = new Gson().toJson(user);

            json = Encryption.encryptDecryptXOR(json);

            // TODO: What should happen if something breaks down? FIX 
            if (user != null) {
                // Return the user with the status code 200 if the user is found
                return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
            } else {
                //Returns status code 404 if the user could not be found
                return Response.status(404).entity("Could not find user").build();
            }

        } catch (Exception error) {
            error.getStackTrace();
            //Returns status code if an internal error occurs
            return Response.status(500).entity("Internal server error").build();
        }
    }


    /**
     * @return Responses
     */
    @GET
    @Path("/")
    public Response getUsers() {

        // Write to log that we are here
        Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

        // Get a list of users
        ArrayList<User> users = UserController.getUsers();

        // TODO: Add Encryption to JSON: FIX
        // Transfer users to json in order to return it to the user
        String json = new Gson().toJson(users);

        json = Encryption.encryptDecryptXOR(json);

        // Return the users with the status code 200
        return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(String body) {

        // Read the json from body and transfer it to a user class
        User newUser = new Gson().fromJson(body, User.class);

        // Use the controller to add the user
        User createUser = UserController.createUser(newUser);

        // Get the user back with the added ID and return it to the user
        String json = new Gson().toJson(createUser);

        // Return the data to the user
        if (createUser != null) {
            // Return a response with status 200 and JSON as type
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            return Response.status(400).entity("Could not create user").build();
        }
    }

    // TODO: Make the system able to login users and assign them a token to use throughout the system.
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginUser(String x) {

        // Return a response with status 200 and JSON as type
        return Response.status(400).entity("Endpoint not implemented yet").build();
    }

    // TODO: Make the system able to delete users: FIX
    @DELETE
    @Path("/delete/{idUser}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("idUser") int idUser) {

        User deleteUser1 = UserController.getUser(idUser);
        User deleteUser2 = UserController.deleteUser(deleteUser1);
        String json = new Gson().toJson(deleteUser2);


        if (deleteUser2 != null) {
            // Return a response with status 200 and JSON as type
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            // Return a response with status code 404 - not found
            return Response.status(404).entity("Could not find user").build();

        }

    }

    // TODO: Make the system able to update users: FIX
    @PUT
    @Path("/update/{idUser}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(String body) {

        User UserUpdate = new Gson().fromJson(body, User.class);
        User updateUser = UserController.updateUser(UserUpdate);

        String json = new Gson().toJson(updateUser);

        if (updateUser != null) {

            // Return a response with status 200 and JSON as type
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            // Return a response with status code 404 - not found
            return Response.status(404).entity("Could not find user").build();

        }
    }
}