package org.jboss.ee.tutorial.jaxrs.android.data;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface LibraryClient {

    @GET
    @Path("/books")
    public List<Book> getBooks();

    @GET
    @Path("/book/{isbn}")
    public Book getBook(@PathParam("isbn") String id);

    @PUT
    @Path("/book/{isbn}")
    public Book addBook(@PathParam("isbn") String id, @QueryParam("title") String title);

    @POST
    @Path("/book/{isbn}")
    public Book updateBook(@PathParam("isbn") String id, String title);

    @DELETE
    @Path("/book/{isbn}")
    public Book removeBook(@PathParam("isbn") String id);
}