package org.example;

// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.*;
import java.util.Random;

// Server class
public class Server
{

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();

    // counter for clients
    static int i = 0;

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);

        //Initialize the output file
        FileWriter writer = new FileWriter("output.txt");
        writer.write("");
        writer.flush();

        Socket s;

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("New client request received : " + s);

            // obtain input and output streams
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            //get name&id
            System.out.println("Creating a new handler for this client...");
            String name = br.readLine();
            Random rand = new Random();
            int id = 10000+rand.nextInt(90000);
            bw.write(id + "");
            bw.newLine();
            bw.flush();

            // flush the user list to the new client
            for (ClientHandler mc : Server.ar)
            {
                bw.write("- " + mc.id + "-" + mc.name);
                bw.newLine();
                bw.flush();
            }
            bw.write("end");
            bw.newLine();
            bw.flush();

            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,name,id, br, bw);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            System.out.println("Adding this client to active client list");

            // add this client to active clients list
            ar.add(mtch);

            // start the thread.
            t.start();


        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    String name;
    final BufferedReader br;
    final BufferedWriter bw;
    Socket s;
    int id;
    Vector<ClientHandler> user = new Vector<>();

    // constructor
    public ClientHandler(Socket s, String name, int id,
                         BufferedReader br, BufferedWriter bw) {
        this.br = br;
        this.bw = bw;
        this.id = id;
        this.name = name;
        this.s = s;
    }

    @Override
    public void run() {
        //read the output file and send to the client
        try {
            BufferedReader reader = new BufferedReader(new FileReader("output.txt"));
            String line = reader.readLine();
            while (line != null) {
                this.bw.write(line);
                this.bw.newLine();
                this.bw.flush();
                line = reader.readLine();
            }
            this.bw.write("end");
            this.bw.newLine();
            this.bw.flush();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String received;
        while (true)
        {
            try
            {
                // receive the string
                received = br.readLine();

                //logout
                if(received.equals("logout")) {
                    this.bw.write("You have logged out successfully!");
                    this.bw.newLine();
                    this.bw.flush();
                    Server.ar.remove(this);
                    System.out.println("The client " + this.id + "-" + this.name + " has logged out.");
                    for (ClientHandler mc : Server.ar)
                    {
                        try {
                            mc.bw.write("[Server: " + this.id + "-" + this.name + " has left the chat!]");
                            mc.bw.newLine();
                            mc.bw.flush();
                        } catch (IOException ex) {

                        }
                    }
                    break;
                }
                //Receiver Display
                if(received.equals("[print-receiver]:the-message")) {
                    this.bw.write("[print-receiver]:the-message");
                    this.bw.newLine();
                    this.bw.flush();
                    for(ClientHandler mc : this.user) {
                        if(mc.id != this.id) {
                            this.bw.write(mc.id + "-" + mc.name);
                            this.bw.newLine();
                            this.bw.flush();
                        }
                    }
                    this.bw.write("end");
                    this.bw.newLine();
                    this.bw.flush();
                    continue;
                }

                // Get the current date and time
                LocalDateTime currentTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedCurrentTime = currentTime.format(formatter);

                received = received+ "   ("+formattedCurrentTime+")";
                System.out.println(this.id + "-"+ this.name + ": " + received);

                //store the message to the file
                BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true));
                writer.write(this.id + "-"+ this.name + ": " + received);
                writer.newLine();
                writer.flush();

                //broadcast message to other clients
                this.user = (Vector<ClientHandler>) Server.ar.clone();
                for (ClientHandler mc : Server.ar)
                {
                    if (mc.id != this.id)
                    {
                        mc.bw.write(this.id + "-" + this.name + ": " + received);
                        mc.bw.newLine();
                        mc.bw.flush();
                    }
                }
            } catch (IOException e) {
                // display message if client exits
                Server.ar.remove(this);
                System.out.println("The client " + this.id + "-" + this.name + " has logged out.");
                for (ClientHandler mc : Server.ar)
                {
                    try {
                        mc.bw.write("[Server: " + this.id + "-" + this.name + " has left the chat!]");
                        mc.bw.newLine();
                        mc.bw.flush();
                    } catch (IOException ex) {

                    }
                }
                break;
            }
        }
        try
        {
            // closing resources
            this.br.close();
            this.bw.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
