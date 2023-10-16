package org.example;
public class Client {

    public static String ipAddr = "localhost";
    public static int port = 8079;

    public static void main(String[] args) {
        new ClientSomthing(ipAddr, port);
    }
}