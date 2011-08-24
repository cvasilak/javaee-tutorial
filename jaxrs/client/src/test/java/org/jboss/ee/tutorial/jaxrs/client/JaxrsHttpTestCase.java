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
package org.jboss.ee.tutorial.jaxrs.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The JAX-RS HTTP request test
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
@RunAsClient
@RunWith(Arquillian.class)
public class JaxrsHttpTestCase {
    
    private static final String REQUEST_PATH = "http://localhost:8080/jaxrs-sample/library";
    
    @Deployment(testable = false)
    public static Archive<?> deploy() {
        File serverTargetDir = new File("../server/target");
        String[] list = serverTargetDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".war");
            }
        });
        File warFile = new File(serverTargetDir + "/" + list[0]);
        JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, warFile);
        return archive;
    }

    @Test
    public void testHttpGetList() throws Exception {
        String content = httpGet("/books");
        Book[] books = new ObjectMapper().readValue(content, Book[].class);
        assertNotNull("Books not null", books);
        assertEquals(4, books.length);
    }

    @Test
    public void testHttpGet() throws Exception {
        String content = httpGet("/book/001");
        Book book = new ObjectMapper().readValue(content, Book.class);
        assertNotNull("Book not null", book);
        assertEquals("The Judgment", book.getTitle());
        assertEquals("001", book.getIsbn());
    }

    @Test
    public void testHttpPut() throws Exception {
        String content = httpPut("/book/1234?title=Android", null);
        Book book = new ObjectMapper().readValue(content, Book.class);
        assertNotNull("Book not null", book);
        assertEquals("Android", book.getTitle());
        assertEquals("1234", book.getIsbn());
        content = httpGet("/books");
        Book[] books = new ObjectMapper().readValue(content, Book[].class);
        assertNotNull("Books not null", books);
        assertEquals(5, books.length);
    }

    @Test
    public void testHttpUpdate() throws Exception {
        String content = httpPost("/book/1234", "Android for Dummies");
        Book book = new ObjectMapper().readValue(content, Book.class);
        assertNotNull("Book not null", book);
        assertEquals("Android for Dummies", book.getTitle());
        assertEquals("1234", book.getIsbn());
        content = httpGet("/books");
        Book[] books = new ObjectMapper().readValue(content, Book[].class);
        assertNotNull("Books not null", books);
        assertEquals(5, books.length);
    }

    @Test
    public void testHttpDelete() throws Exception {
        String content = httpDelete("/book/1234");
        Book book = new ObjectMapper().readValue(content, Book.class);
        assertNotNull("Book not null", book);
        assertEquals("Android for Dummies", book.getTitle());
        assertEquals("1234", book.getIsbn());
        content = httpGet("/books");
        Book[] books = new ObjectMapper().readValue(content, Book[].class);
        assertNotNull("Books not null", books);
        assertEquals(4, books.length);
    }

    private String httpGet(String uriPath) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.get(REQUEST_PATH + uriPath, 5, TimeUnit.SECONDS);
    }

    private String httpPut(String uriPath, String message) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.put(REQUEST_PATH + uriPath, message, 5, TimeUnit.SECONDS);
    }

    private String httpPost(String uriPath, String message) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.post(REQUEST_PATH + uriPath, message, 5, TimeUnit.SECONDS);
    }

    private String httpDelete(String uriPath) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.delete(REQUEST_PATH + uriPath, 5, TimeUnit.SECONDS);
    }
}
