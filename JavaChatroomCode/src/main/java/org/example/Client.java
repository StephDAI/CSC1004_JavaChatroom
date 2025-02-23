package org.example;

// Java implementation for multithreaded chat client
// Save file as Client.java

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client
{
    final static int ServerPort = 1234;

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        //get name
        System.out.println("Please enter your name for the group chat: ");
        String name = scn.nextLine();
        bw.write(name);
        bw.newLine();
        bw.flush();

        //get id and print welcome message&user list
        int id = Integer.parseInt(br.readLine());
        System.out.println("Welcome to the group chat, " + id + "-" + name + "!");
        System.out.println("Currently connected users:");
        while (true) {
            String userList = br.readLine();
            if (userList.equals("end")) {
                break;
            }
            System.out.println(userList);
        }
        System.out.println();

        //print the chat history
        System.out.println("History chat:");
        while (true) {
            String msg = br.readLine();
            if (msg.equals("end")) {
                break;
            }
            System.out.println(msg);
        }
        System.out.println();
        System.out.println("Now let's start chatting!");

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    while (true) {
                        // read the message to deliver.
                        String msg = scn.nextLine();

                        //logout
                        if(msg.equals("logout")) {
                            bw.write(msg);
                            bw.newLine();
                            bw.flush();
                            break;
                        }

                        //Receiver Display
                        if(msg.equals("[print-receiver]:the-message")) {
                            bw.write(msg);
                            bw.newLine();
                            bw.flush();
                            continue;
                        }

                        // Get,define and print the current date and time
                        LocalDateTime currentTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                        String formattedCurrentTime = currentTime.format(formatter);
                        System.out.println(formattedCurrentTime);

                        // write on the output stream
                        bw.write(msg);
                        bw.newLine(); // add a new line to indicate end of message
                        bw.flush(); // flush the stream to ensure the message is sent
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        String msg = br.readLine();

                        //logout
                        if(msg.equals("You have logged out successfully!")) {
                            System.out.println(msg);
                            break;
                        }
                        //receiver display
                        if(msg.equals("[print-receiver]:the-message")) {
                            System.out.println("The last message was sent to:");
                            while (true) {
                                String userList = br.readLine();
                                if (userList.equals("end")) {
                                    break;
                                }
                                System.out.println("-   "+userList);
                            }
                            System.out.println();
                            continue;
                        }

                        //print the message
                        System.out.println(msg);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}
