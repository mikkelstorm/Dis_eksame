package com.cbsexam;

import cache.ProductCache;
import com.google.gson.Gson;
import controllers.ProductController;
import model.Product;
import utils.Config;
import utils.Encryption;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("product")
public class ProductEndpoints {

    ProductCache productCache = new ProductCache();

    /**
     * @param idProduct
     * @return Responses
     */
    @GET
    @Path("/{idProduct}")
    public Response getProduct(@PathParam("idProduct") String idProduct) {

        // TODO: Add Encryption to JSON       :FIX plus ekstra
        //Denne metode tjekker om der er skrevet krypterings koden ind i URL efter talet, hvor efter
        //den hvis krypteringskoden er korrekt returnere uden kryptering.
        //hvis krypteringskoden ikke er korrekt returnere et krypteret produkt

        String tal = "";
        String key = "";

        //Et for loop der dele URL'ens idProduct op i to dele.
        //første del er selve product nummeret, da det skal findes i databasen
        //næste del er krypteringskoden
        for (int i = 0; i < idProduct.length(); i++) {
            final char c = idProduct.charAt(i);

            //Køre endpoint URL igennem for hvert bogstav og hvis det er et tal, gemmer i tal variablen
            //og hvis det er et bogstav i key variablen.
            if (c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0') {
                tal = tal + c;
            } else {
                key = key + c;
            }
        }
        //Der er en fejl ved at man ikke kan have til i krypteringskoden. Jeg havde løst dette problem ved at man skulle
        //skrive et "?" og så ville den gemme alle tal efter. Eksemplet er skrevet nedeunder. Men dette er måden
        //postman normalt gør tingene på og kunne derfor ikke sende det som en del af URL'en. Den ville kun sende alt før "?"
//        if (c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0' && !tjek) {tal = tal + c;}
//        if(tjek) {key = key + c;}
//        if (c == '?') {tjek = true;}


        //laver Strengen tal om til en int, da databasen skal bruge en int
        int idProductSoeg = Integer.parseInt(tal);
        // Call our controller-layer in order to get the order from the DB
        Product product = ProductController.getProduct(idProductSoeg);

        // We convert the java object to json with GSON library imported in Maven
        String json = new Gson().toJson(product);

        //Kryptere vores json text gennem en XOR hvis krypteringskoden ikke matcher
        if (!key.equals(Config.getEncryptionCode())) {
            json = Encryption.encryptDecryptXOR(json);
        }


        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    }

    /**
     * @return Responses
     */
    @GET
    @Path("/")
    public Response getProducts() {

        //Kalder vores cache metode for produkter, for at hente listen over produkter hurtigere
        ArrayList<Product> products = productCache.getProducts(false);

        // We convert the java object to json with GSON library imported in Maven
        String json = new Gson().toJson(products);

        // TODO: Add Encryption to JSON         :FIX
        //Kryptere vores json text gennem en XOR
        json = Encryption.encryptDecryptXOR(json);

        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(String body) {

        // Read the json from body and transfer it to a product class
        Product newProduct = new Gson().fromJson(body, Product.class);

        // Use the controller to add the user
        Product createdProduct = ProductController.createProduct(newProduct);

        // Get the user back with the added ID and return it to the user
        String json = new Gson().toJson(createdProduct);

        // Return the data to the user
        if (createdProduct != null) {
            // Return a response with status 200 and JSON as type
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            return Response.status(400).entity("Could not create user").build();
        }
    }
}
