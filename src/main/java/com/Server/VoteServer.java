package com.Server;

import com.journaldev.jsf.util.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.clients.producer.KafkaProducer;

public class VoteServer {
    private static final String KAFKA_TOPIC = "vote-events-topic";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        try {
            startSocketServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Producer<String, String> createKafkaProducer() {

        java.util.Properties kafkaProps = new java.util.Properties();
        kafkaProps.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<String, String>(kafkaProps);
    }

    public static void processVote(String voterID, String voteOption, Producer<String, String> kafkaProducer) throws Exception{
    	
        sendKafkaEvent("Vote enregistré pour " + voteOption + " par l'électeur " + voterID, kafkaProducer);
    }

    private static void sendKafkaEvent(String message, Producer<String, String> kafkaProducer) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(KAFKA_TOPIC, message);

        try {
            kafkaProducer.send(record);
            System.out.println("Message envoyé avec succès à " + record.key());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message à " + record.key() + ": " + e.getMessage());

        }
    }

    private static void startSocketServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server listening on port 8000...");        
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Client client = new Client();
                System.out.println("New client with ID : " + client.getuserId() + " is connected: " + socket.getInetAddress());
               
                Runnable clientHundler = new handleClient(socket, client);

                // Create a new thread for each client
                Thread thread = new Thread(clientHundler);
                thread.start();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }

    static class handleClient implements Runnable {
        private Socket socket;
        public Client client;

        public handleClient(Socket socket, Client client) {
            this.socket = socket;
            this.client = client;
        }

        public void run() {
            while (client.getisConnected()) {}
            try {
            	socket.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    }
}

