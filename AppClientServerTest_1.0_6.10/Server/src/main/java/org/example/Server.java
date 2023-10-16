package org.example;
import entities.Topic;
import entities.Vote;
import entities.VoteController;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.*;

class ServerSomthing extends Thread {

    private final Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private final BufferedReader in; // поток чтения из сокета
    private final BufferedWriter out; // поток завписи в сокет
    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию исключения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        new readFromClient().start(); // вызываем run()
    }
    public class readFromClient extends Thread{
        @Override
        public void run() {
            try {
                String clientName = null;
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        break;
                    }

                    String[] parts = input.split(" ");
                    String command = parts[0];

                    if (command.equals("login")) {
                        if (parts.length == 2 && parts[1].startsWith("-u=") && clientName == null) {
                            clientName = parts[1].substring(3);
                            if(!Server.loggedInClients.contains(clientName)){
                                Server.loggedInClients.add(clientName);
                                send("Logged in as " + clientName);
                                Server.logger.info("Client with name {} logged in",clientName);
                            } else{
                                send("This username is already used");
                            }
                        } else {
                            send("Invalid login command.");
                        }
                    }
                    else if (clientName == null) {
                        send("Please log in first.");
                    }
                    else if (command.equals("create")) {
                        if (parts[1].equals("topic") && parts.length == 3 && parts[2].startsWith("-n=")) {
                            String topicName = parts[2].substring(3);
                            if (Server.voteController.findTopicByName(topicName) == null) {
                                Server.voteController.addTopic(new Topic(topicName));
                                send("Created topic: " + topicName);
                                Server.logger.info("Topic with name {} has been created", topicName);
                            } else {
                                send("Topic " + topicName + " already exists.");
                            }
                        }
                        else if (parts[1].equals("vote") && parts.length == 3 && parts[2].startsWith("-t=")) {
                            String topicName = parts[2].substring(3);
                            if (Server.voteController.findTopicByName(topicName) != null) {
                                String voteName = null;
                                send("Enter a unique name of vote");
                                try{
                                    while(true){
                                        voteName = in.readLine();
                                        if(Server.voteController.findTopicByName(topicName).findVoteByName(voteName) == null){
                                            break;
                                        }
                                        else{
                                            send("Vote with name "
                                                    + voteName + " in topic "
                                                    + topicName + " is already exist.");
                                        }
                                    }
                                }catch (NullPointerException ex){
                                    Server.logger.error(ex);
                                }
                                send("Enter the voting topic:");
                                String description = in.readLine();
                                send("Enter the number of answer options:");
                                int numOptions = Integer.parseInt(in.readLine());
                                Map<String, Integer> option = new HashMap<>();
                                for (int i = 0; i < numOptions; i++) {
                                    send("Enter option " + (i + 1) + ":");
                                    option.put(in.readLine(), 0);
                                }
                                Server.voteController.findTopicByName(topicName).addVote(
                                        new Vote(voteName, clientName, description, option));
                                send("Created vote " + voteName + " in topic " + topicName);
                                Server.logger.info("Vote {} has been created in topic {}", voteName, topicName);
                            }
                            else{
                                send("Topic " + topicName + " does not exist.");
                            }
                        }
                        else {
                            send("Invalid create command.");
                        }
                    }
                    else if (command.equals("view")) {
                        if(parts.length == 1){
                            StringBuilder stringBuilder = new StringBuilder();
                            Server.voteController.getTopics().forEach(
                                    topic -> stringBuilder.append(topic.getNameOfTopic()).append(" (votes in topic=").append(topic.getVotesInTopic().size()).append(")").append("\n")
                            );
                            send(String.valueOf(stringBuilder));
                        }
                        else if(parts.length == 2 && parts[1].startsWith("-t=")){
                            StringBuilder stringBuilder1 = new StringBuilder();
                            String topicName = parts[1].substring(3);
                            Topic topic = Server.voteController.findTopicByName(topicName);
                            if(topic == null){
                                send("There is no topic called " + topicName + ".");
                            }
                            else if(topic.getVotesInTopic().size() > 0){
                                stringBuilder1.append("List of votes in topic ").append(topicName).append(":\n");
                                topic.getVotesInTopic().forEach(
                                        topic1 -> stringBuilder1.append(topic1.getVoteName() + "\n")
                                );
                                send(String.valueOf(stringBuilder1));
                            }
                            else{
                                send("There is no votes in topic " + topicName + ".");
                            }
                        }
                        else if(parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")){
                            String topicName = parts[1].substring(3);
                            String voteName = parts[2].substring(3);
                            Topic topic = Server.voteController.findTopicByName(topicName);
                            Vote vote = topic.findVoteByName(voteName);
                            if(topic != null){
                                if(vote != null){
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder
                                            .append("Voting topic: ").append(vote.getDescription()).append("\n");
                                    vote.getOption().forEach((key, value) -> stringBuilder
                                            .append("Option - ").append(key).append(";")
                                            .append(" votes - ").append(value).append("\n"));
                                    send(String.valueOf(stringBuilder));
                                }
                                else{
                                    send("Vote with name " + voteName
                                            + " in topic " + topicName + " does not exist.");
                                }
                            }
                            else{
                                send("Topic with name " +  topicName + "does not exist.");
                            }
                        }
                        else{
                            send("Wrong view command");
                        }
                    }
                    else if (command.equals("vote")){
                        if(parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")){
                            String topicName = parts[1].substring(3);
                            String voteName = parts[2].substring(3);
                            Topic topic = Server.voteController.findTopicByName(topicName);
                            Vote vote = topic.findVoteByName(voteName);
                            if(topic != null){
                                if(vote != null){
                                    if(!vote.getVoters().contains(clientName)){
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder
                                                .append("Please, write option what you want to choice.\n")
                                                .append("Answer options:\n");
                                        vote.getOption().forEach((key, value) -> stringBuilder
                                                .append(key).append("\n"));
                                        send(String.valueOf(stringBuilder));
                                        try{
                                            while(true){
                                                String choice = in.readLine();
                                                if(vote.getOption().containsKey(choice)){
                                                    vote.addVote(choice);
                                                    vote.addVoter(clientName);
                                                    send("Very good!");
                                                    break;
                                                }
                                                else{
                                                    send("Incorrect choice!");
                                                }
                                            }
                                        }
                                        catch (NullPointerException ex){
                                            Server.logger.error(ex);
                                        }
                                    }
                                    else{
                                        send("You have already voted in this vote!");
                                    }
                                }
                                else{
                                    send("Vote with name " + voteName
                                            + " in topic " + topicName + " does not exist.");
                                }
                            }
                            else{
                                send("Topic with name " + topicName + " does not exist.");
                            }
                        }
                        else{
                            send("Wrong vote command!");
                        }
                    }
                    else if (command.equals("delete")){
                        if(parts.length == 3 && parts[1].startsWith("-t=") && parts[2].startsWith("-v=")){
                            String topicName = parts[1].substring(3);
                            String voteName = parts[2].substring(3);
                            Topic topic = Server.voteController.findTopicByName(topicName);
                            Vote vote = topic.findVoteByName(voteName);
                            if(topic != null){
                                if(vote != null){
                                    if(vote.getCreator().equals(clientName)){
                                        topic.deleteVote(voteName, clientName);
                                        send("Vote " + voteName + " has been deleted!");
                                    }
                                    else{
                                        send("You do not have permission to do this action");
                                    }
                                }
                                else{
                                    send("Vote with name " + voteName
                                            + " in topic " + topicName + " does not exist.");
                                }
                            }
                            else{
                                send("Topic with name " + topicName + " does not exist.");
                            }
                        }
                        else{
                            send("Wrong delete command!");
                        }
                    }
                    else if (command.equals("exit")) {
                        Server.loggedInClients.remove(clientName);
                        ServerSomthing.this.downService();
                        Server.logger.info("Client with name {} has logged out", clientName);
                        break;
                    }
                    else {
                        send("Unknown command: " + command);
                    }
                }

                socket.close();
            } catch (IOException e) {
                Server.logger.error(e);
            }
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}

    }

    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerSomthing vr : Server.serverList) {
                    if(vr.equals(this)) vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }
}

class ServerConsole extends Thread{
    private final BufferedReader inputFromConsole; // поток чтения из сокета

    public ServerConsole() throws IOException {
        inputFromConsole = new BufferedReader(new InputStreamReader(System.in));
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String input = inputFromConsole.readLine();
                if(input == null){
                    break;
                }
                String[] parts = input.split(" ", 2);
                String command = parts[0];
                switch (command) {
                    case "exit" -> {
                        Server.serverList.clear();
                        Server.loggedInClients.clear();
                        Server.logger.info("Server is turned off");
                        System.exit(0);
                    }
                    case "save" -> {
                        VoteController voteController = Server.voteController;
                        String filename = parts[1];
                        // создадим список объектов, которые будем записывать

                        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                            oos.writeObject(voteController);
                            System.out.println("File has been written");
                            Server.logger.info("File {} has been written",filename);
                        } catch (Exception ex) {
                            Server.logger.error(ex);
                        }
                    }
                    case "load" -> {
                        String filename = parts[1];
                        // десериализация в новый список
                        VoteController newVoteController;
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                            newVoteController = (VoteController) ois.readObject();
                            Server.voteController.setTopics(newVoteController.getTopics());
                            System.out.println("File has been rad");
                            Server.logger.info("File {} has been rad", filename);
                        } catch (Exception ex) {
                            Server.logger.error(ex);
                        }
                    }
                    default -> System.out.println("Wrong command!");
                }
            }
        }
        catch (IOException ex){
            Server.logger.error(ex);
        }
    }
}


public class Server {
    public static Logger logger;

    public static final int PORT = 8079;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента
    public static HashSet<String> loggedInClients = new HashSet<>();

    public static VoteController voteController = new VoteController();

    public static void main(String[] args) throws IOException {
        logger = LogManager.getRootLogger();

        try (ServerSocket server = new ServerSocket(PORT)) {
            BufferedReader inputServer = new BufferedReader(new InputStreamReader(System.in));
            new ServerConsole();
            logger.log(Level.INFO, "Server started");
            while (true){
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomthing(socket)); // добавить новое соединенние в список
                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex);
        }

    }
}