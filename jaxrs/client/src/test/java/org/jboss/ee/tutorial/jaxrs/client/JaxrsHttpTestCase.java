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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
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
        String result = httpGet("/books");
        Assert.assertEquals("[{\"name\":\"Harry Potter\",\"isbn\":\"1234\"}]", result);
    }

    @Test
    public void testHttpGet() throws Exception {
        String result = httpGet("/book/1234");
        Assert.assertEquals("{\"name\":\"Harry Potter\",\"isbn\":\"1234\"}", result);
    }

    @Test
    public void testHttpPut() throws Exception {
        String result = httpPut("/book/5678?name=Android", null);
        Assert.assertEquals("{\"name\":\"Android\",\"isbn\":\"5678\"}", result);
    }

    @Test
    public void testHttpDelete() throws Exception {
        String result = httpDelete("/book/5678");
        Assert.assertEquals("{\"name\":\"Android\",\"isbn\":\"5678\"}", result);
    }

    private String httpGet(String uriPath) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.get(REQUEST_PATH + uriPath, 5, TimeUnit.SECONDS);
    }

    private String httpPut(String uriPath, String message) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.put(REQUEST_PATH + uriPath, message, 5, TimeUnit.SECONDS);
    }

    private String httpDelete(String uriPath) throws MalformedURLException, ExecutionException, TimeoutException {
        return HttpRequest.delete(REQUEST_PATH + uriPath, 5, TimeUnit.SECONDS);
    }
}
