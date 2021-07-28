package com.example.demo4Cat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/hello-world")
public class HelloResource {
    @GET
    @Produces("text/plain")
    @Path("/hello")
    public String hello() {
        return "Hello, World!";
    }
    @GET
    @Produces("text/plain")
    @Path("/another")
    public String another()
    {
        return  "another" ;
    }
}