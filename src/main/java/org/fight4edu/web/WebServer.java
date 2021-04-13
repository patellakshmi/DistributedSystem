package org.fight4edu.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import sun.net.httpserver.HttpServerImpl;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class WebServer {

    private int port;
    private HttpServer httpServer;

    public WebServer(int port) throws IOException {
        this.port = port;
        InetAddress inetAddress = new InetSocketAddress("127.0.0.1",this.port).getAddress();
        String add = inetAddress.getHostAddress();
        String hostName = inetAddress.getCanonicalHostName();
        httpServer = HttpServer.create(new InetSocketAddress(this.port), 10);
        HttpContext statusContext = httpServer.createContext("/status");
        HttpContext taskContext = httpServer.createContext("/task");

        statusContext.setHandler(this::statusHandler);
        taskContext.setHandler(this::taskHandler);
    }

    public void start() throws IOException {
        httpServer.start();
    }

    private void statusHandler(HttpExchange httpExchange) throws IOException {
        if( !httpExchange.getRequestMethod().equalsIgnoreCase("get")){
            httpExchange.close();
            return;
        }

        System.out.println("Request........");
        String message = "Server is live";
        httpExchange.sendResponseHeaders(200, message.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(message.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private void taskHandler(HttpExchange httpExchange){

    }

}
