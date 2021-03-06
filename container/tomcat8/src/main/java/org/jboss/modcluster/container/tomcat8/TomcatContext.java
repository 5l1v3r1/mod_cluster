/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.modcluster.container.tomcat8;

import javax.servlet.ServletException;
import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.Valve;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.modcluster.container.Host;
import org.jboss.modcluster.container.listeners.ServletRequestListener;
import org.jboss.modcluster.container.tomcat.RequestListenerValveFactory;

/**
 * @author Paul Ferraro
 * @author Radoslav Husar
 */
public class TomcatContext extends org.jboss.modcluster.container.tomcat.TomcatContext {

    public TomcatContext(Context context, Host host) {
        super(context, host, new RequestListenerValveFactory() {
            @Override
            public Valve createValve(ServletRequestListener listener) {
                return new RequestListenerValve(listener);
            }
        });
    }

    private static class RequestListenerValve extends ValveBase {
        private final ServletRequestListener listener;

        RequestListenerValve(ServletRequestListener listener) {
            this.listener = listener;
        }

        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            this.event(request, response, null);
        }

        @Override
        public void event(Request request, Response response, CometEvent event) throws IOException, ServletException {
            this.listener.requestInitialized();

            Valve valve = this.getNext();

            try {
                if (event != null) {
                    valve.event(request, response, event);
                } else {
                    valve.invoke(request, response);
                }
            } finally {
                this.listener.requestDestroyed();
            }
        }

        @Override
        public int hashCode() {
            return this.listener.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof RequestListenerValve)) return false;

            RequestListenerValve valve = (RequestListenerValve) object;

            return this.listener == valve.listener;
        }
    }
}
