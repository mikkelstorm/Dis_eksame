package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import model.User;
import utils.Encryption;
import utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("user")
public class UserEndpoints {

    static UserCache userCache = new UserCache();

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

        // TODO: Add Encryption to JSON         :FIX
        //Kryptere vores json text gennem en XOR
        json = Encryption.encryptDecryptXOR(json);


        // TODO: What should happen if something breaks down?     :FIX
        // Returnere data til brugeren, der enten er bruger data eller tekst der siger brugeren ikke kunne findes
        if (user != null) {
            //Returnere et svar med status kode 200 for "OK" og 400 for client error ved request
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            return Response.status(400).entity("Could not find user").build();
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

        //Kalder vores cache metode for brugere, for at hente listen over brugere hurtigere
        ArrayList<User> users = userCache.getUsers(false);

        // Transfer users to json in order to return it to the user
        String json = new Gson().toJson(users);

        // TODO: Add Encryption to JSON           :FIX
        //Kryptere vores json text gennem en XOR
        json = Encryption.encryptDecryptXOR(json);

        // Return the users with the status code 200
        return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }

    /**
     * Klassen CreateUser har til formål at oprette en bruger gennem et POST call. Dette gøres ved at brugeren
     * sender en POST call med følgende variabler.
     * - "firstname": "fornavn",
     * - "lastname": "efternavn",
     * - "email": "email@abc.dk",
     * - "password": "kode123"
     * Alt dette bliver ændret fra Json til Gson, og derefter kørt igennem UserControllerens createUser metode
     * Hvis det er succesfuld bliver der returneret en bruger og ellers bliver der returneret en statuskode 400.
     *
     * @param body Json bruger
     * @return bruger eller statuskode
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response CreateUser(String body) {

        //læser Json fra POST body og gemmer det som en User i Gson format
        User newUser = new Gson().fromJson(body, User.class);

        //Kører UserControllerens metode createUser med den nye bruger
        User createUser = UserController.createUser(newUser);

        //For brugeren igen med et ID og createdTime hvis oprettelsen har været succesfuld
        String json = new Gson().toJson(createUser);

        // Returnere data til brugeren, som enten er brugerens information eller statuskode 400 hvis det er gået galt.
        if (createUser != null) {
            //Returnere en svar med statuskode 200 og brugeren i JSON text.
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            //Returnere statuskode 400 hvis det ikke var muligt at lave en bruger
            return Response.status(400).entity("Could not create user").build();
        }
    }

    /**
     * Klassen LoginUser har til formål at logge ind som en bruger, for at modtage en Token. Dette gøres ved at brugeren
     * sender en POST call med følgende variabler.
     * - "email": "email@abc.dk",
     * - "password": "kode123"
     * Alt dette bliver ændret fra Json til Gson, og derefter kørt igennem UserControllerens loginUser metode
     * Hvis det er succesfuld bliver der returneret en bruger med en oprettet Token
     * og ellers bliver der returneret en statuskode 400.
     *
     * @param body Json
     * @return Bruger med Token eller statuskode 400
     */
    // TODO: Make the system able to login users and assign them a token to use throughout the system.    :FIX
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response LoginUser(String body) {

        // læser json body og laver det om til en ny User/bruger
        User loginUser = new Gson().fromJson(body, User.class);

        // Kører brugeren loginUser og returnere en Token hvis succesfuld
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

    /**
     * Klassen DeleteUser har til formål at slette en bruger ved at skrive en email og token for brugeren.
     * Dette gøres ved at brugeren sender en POST call med følgende variabler.
     * - "email": "email@abc.dk",
     * - "token": "token.token"
     * Alt dette bliver ændret fra Json til Gson, og derefter kørt igennem UserControllerens DeleteUser metode
     * Hvis det er succesfuld bliver der returneret en tekst om at brugeren er slettet
     * og ellers bliver der returneret en statuskode 400.
     *
     * @param body Json
     * @return statuskode 200 at bruger er slettet eller statuskode 400 noget gik galt
     */
    // TODO: Make the system able to delete users     :FIX
    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response DeleteUser(String body) {

        // læser json body og laver det om til en ny User/bruger i Gson format
        User chosenUser = new Gson().fromJson(body, User.class);

        //putter brugeren ind i deleteUser metode
        User deleteUser = UserController.deleteUser(chosenUser);

        // Returnere data til brugeren
        if (deleteUser != null) {
            // Returnere et response med status kode 200 ved succes og status kode 400 ved fejl
            return Response.status(200).entity("Du slettede brugeren: " + deleteUser.getEmail()).build();
        } else {
            return Response.status(400).entity("Something went wrong").build();
        }

    }

    /**
     * Klassen UpdateUser har til formål at updatere en bruger ved brug af token.
     * Dette gøres ved at brugeren sender en POST call med følgende variabler.
     * - "firstname": "fornavn",
     * - "lastname": "efternavn",
     * - "password": "kode123",
     * - "email": "email@abc.dk",
     * - "newEmail": "nyemail@abc.dk",
     * - "token": "token.token"
     * Alt dette bliver ændret fra Json til Gson, og derefter kørt igennem UserControllerens UpdateUser metode
     * Hvis det er succesfuld bliver der returneret en tekst om at brugeren er slettet
     * og ellers bliver der returneret en statuskode 400.
     *
     * @param body Json
     * @return opdateret bruger eller statuskode 400 ved fejl
     */
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
