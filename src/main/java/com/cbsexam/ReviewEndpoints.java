package com.cbsexam;

import com.google.gson.Gson;
import controllers.ReviewController;
import model.Review;
import utils.Encryption;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("search")
public class ReviewEndpoints {

    /**
     * @param reviewTitle
     * @return Responses
     */
    @GET
    @Path("/title/{title}")
    public Response search(@PathParam("title") String reviewTitle) {

        // Call our controller-layer in order to get the order from the DB
        ArrayList<Review> reviews = ReviewController.searchByTitle(reviewTitle);

        // We convert the java object to json with GSON library imported in Maven
        String json = new Gson().toJson(reviews);

        // TODO: Add Encryption to JSON           :FIX
        json = Encryption.encryptDecryptXOR(json);

        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }


}
