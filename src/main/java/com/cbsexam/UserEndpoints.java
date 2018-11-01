package com.cbsexam;

import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Hashing;
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

    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    // TODO: Add Encryption to JSON         :FIX check turn on/off
//    json = Encryption.encryptDecryptXOR(json);


    // TODO: What should happen if something breaks down?     :FIX
    // Returnere data til brugeren, der enten er bruger data eller tekst der siger brugeren ikke kunne findes
    if (user != null) {
      //Returnere et svar med status kode 200 for "OK" og 400 for client error ved request
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not find user").build();
    }

  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    // TODO: Add Encryption to JSON           :FIX   check turn on/off
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

  // TODO: Make the system able to login users and assign them a token to use throughout the system.    :FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // læser json body og laver det om til en ny User/bruger
    User loginUser = new Gson().fromJson(body, User.class);

    // loginUser returnere en token
    User userToken = UserController.loginUser(loginUser);

    //Gemmer bruger data som Json format
    String json = new Gson().toJson(userToken);


    // Returnere data til brugeren
    if (userToken != null) {
      // Returnere et response med status kode 200 ved succes og status kode 400 ved fejl
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Wrong username or password").build();
    }
  }


  // TODO: Make the system able to delete users     :FIX
  @POST
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String body) {

    // læser json body og laver det om til en ny User/bruger i Gson format
    User chosenUser = new Gson().fromJson(body, User.class);

    //putter brugeren ind i slet bruger metode
    User deleteUser = UserController.deleteUser(chosenUser);

    // Returnere data til brugeren
    if (deleteUser != null) {
      // Returnere et response med status kode 200 ved succes og status kode 400 ved fejl
      return Response.status(200).entity("Du slettede brugeren: " + deleteUser.getEmail()).build();
    } else {
      return Response.status(400).entity("Something went wrong").build();
    }

  }





  // TODO: Make the system able to update users         :FIX
  @POST
  @Path("/update")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String body) {

    // læser json body og laver det om til en ny User/bruger i Gson format
    User newUserData = new Gson().fromJson(body, User.class);

    //putter brugeren ind i opdatere bruger metode
    User updateUser = UserController.UpdateUser(newUserData);

    //Gemmer bruger data som Json format
    String json = new Gson().toJson(updateUser);

    // Return the data to the user
    if (updateUser != null) {
      // Returnere et response med status kode 200 ved succes og status kode 400 ved fejl
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }

  }
}
