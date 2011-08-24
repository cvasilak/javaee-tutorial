/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ee.tutorial.jaxrs.server;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.logging.Logger;

/**
 * A simple JAX-RS endpoint
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
@Path("/library")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class Library {

    private static final Logger log = Logger.getLogger(Library.class);

    private static Map<String, Book> books = new LinkedHashMap<String, Book>();
    static {
        Book[] bookarr = new Book[] { 
                new Book("001", "The Judgment"), 
                new Book("002", "The Stoker"), 
                new Book("003", "Jackals and Arabs"), 
                new Book("004", "The Refusal") 
        };
        for (Book book : bookarr) {
            books.put(book.getIsbn(), book);
        }
    }

    @GET
    @Path("/books")
    public Collection<Book> getBooks() {
        Collection<Book> result = books.values();
        log.infof("getBooks: %s", result);
        return result;
    }

    @GET
    @Path("/book/{isbn}")
    public Book getBook(@PathParam("isbn") String id) {
        Book book = books.get(id);
        log.infof("getBook: %s", book);
        return book;
    }

    @PUT
    @Path("/book/{isbn}")
    public Book addBook(@PathParam("isbn") String id, @QueryParam("title") String title) {
        Book book = new Book(id, title);
        log.infof("addBook: %s", book);
        books.put(id, book);
        return book;
    }

    @POST
    @Path("/book/{isbn}")
    public Book updateBook(@PathParam("isbn") String id, String title) {
        Book book = books.get(id);
        if (book != null) {
            book.setTitle(title);
        }
        log.infof("updateBook: %s", book);
        return book;
    }

    @DELETE
    @Path("/book/{isbn}")
    public Book removeBook(@PathParam("isbn") String id) {
        Book book = books.remove(id);
        log.infof("removeBook: %s", book);
        return book;
    }
}