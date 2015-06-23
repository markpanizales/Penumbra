package com.wormz.penumbraweb.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by markanthonypanizales on 4/13/15.
 */
@Path("hellos")
public class HelloResource {

    // The Java method will process HTTP GET requests
    @GET
    // The Java method will produce content identified by the MIME Media
    // type "text/plain"
    @Produces(MediaType.APPLICATION_JSON)
    public Hello getMessage(){
        Hello hello1 = new Hello("1", "ronan");
        return hello1;
    }

//    Map<String, Hello> database;
//
//    public HelloResource() {
//        database = new HashMap<String, Hello>();
//        Hello hello1 = new Hello("1", "ronan");
//        Hello hello2 = new Hello("2", "john");
//
//        database.put(hello1.getId(), hello1);
//        database.put(hello2.getId(), hello2);
//
//    }

//    @GET
//    @Produces("application/json")
//    public Collection<Hello> get(){
//        return database.values();
//    }

//    @GET
//    @Path("/{id}")
//    @Produces("application/json")
//    public Hello getHello(@PathParam("id") String id) {
//        return database.get(id);
//    }
}
