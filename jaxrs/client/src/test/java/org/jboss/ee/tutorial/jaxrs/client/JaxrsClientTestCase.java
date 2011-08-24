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
import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The JAX-RS client test
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
@RunAsClient
@RunWith(Arquillian.class)
public class JaxrsClientTestCase {
    
    private static final String REQUEST_PATH = "http://localhost:8080/jaxrs-sample/library";
    static LibraryClient client;
    static {
        // This initialization only needs to be done once per VM
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
        client = ProxyFactory.create(LibraryClient.class, REQUEST_PATH);
    }

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
    public void testClientList() throws Exception {
        Collection<Book> books = client.getBooks();
        assertNotNull("Books not null", books);
        assertEquals(4, books.size());
    }

    @Test
    public void testClientGet() throws Exception {
        Book book = client.getBook("001");
        assertNotNull("Book not null", book);
        assertEquals("The Judgment", book.getTitle());
        assertEquals("001", book.getIsbn());
    }

    @Test
    public void testClientAdd() throws Exception {
        Book book = client.addBook("1234", "Android");
        assertNotNull("Book not null", book);
        assertEquals("Android", book.getTitle());
        assertEquals("1234", book.getIsbn());
        Collection<Book> books = client.getBooks();
        assertNotNull("Books not null", books);
        assertEquals(5, books.size());
    }

    @Test
    public void testClientUpdate() throws Exception {
        Book book = client.updateBook("1234", "Android for Dummies");
        assertNotNull("Book not null", book);
        assertEquals("Android for Dummies", book.getTitle());
        assertEquals("1234", book.getIsbn());
        Collection<Book> books = client.getBooks();
        assertNotNull("Books not null", books);
        assertEquals(5, books.size());
    }

    @Test
    public void testClientDelete() throws Exception {
        Book book = client.removeBook("1234");
        assertNotNull("Book not null", book);
        assertEquals("Android for Dummies", book.getTitle());
        assertEquals("1234", book.getIsbn());
        Collection<Book> books = client.getBooks();
        assertNotNull("Books not null", books);
        assertEquals(4, books.size());
    }
}
