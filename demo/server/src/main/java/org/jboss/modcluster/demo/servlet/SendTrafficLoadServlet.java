/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.modcluster.demo.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author Paul Ferraro
 */
public class SendTrafficLoadServlet extends LoadServlet {

    private static final long serialVersionUID = -8586013739155819909L;
    private static final String SIZE = "size";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int size = Integer.parseInt(request.getParameter(SIZE)) * 1024;

        this.log("Writing " + (size / 1024) + "KB blob to response for: " + request.getRequestURL().toString());

        response.getOutputStream().write(new byte[size]);
        response.flushBuffer();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int duration = Integer.parseInt(this.getParameter(request, DURATION, DEFAULT_DURATION));

        String size = this.getParameter(request, SIZE, "100");
        URI uri = this.createLocalURI(request, Collections.singletonMap(SIZE, size));

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            for (int i = 0; i < duration; ++i) {
                this.log("Sending send traffic load request to: " + uri);

                long start = System.currentTimeMillis();

                HttpClientUtils.closeQuietly(client.execute(new HttpPost(uri)));

                long ms = 1000 - (System.currentTimeMillis() - start);

                if (ms > 0) {
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        this.writeLocalName(request, response);
    }
}
