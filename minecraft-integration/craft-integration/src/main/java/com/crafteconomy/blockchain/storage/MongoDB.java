package com.crafteconomy.blockchain.storage;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoDB {
    private MongoClient client;
    private final String database;
    private final String URI;

    /**
     * Create a new instance of MongoDB
     *
     * @param host The host name for MongoDB
     * @param port The port for MongoDB
     * @param database The database for MongoDB
     */
    public MongoDB(String host, int port, String database) {
        this(host, port, database, null, null);
    }

    /**
     * Create a new instance of MongoDB
     *
     * @param host Hostname for MongoDB
     * @param port Port for MongoDB
     * @param username Username for MongoDB
     * @param password Password for MongoDB
     */
    public MongoDB(String host, int port, String database, String username, String password) {
        this(host, port, database, username, password, null);
    }

    /**
     * Create a new instance of MongoDB
     *
     * @param host Hostname for MongoDB
     * @param port Port for MongoDB
     * @param username Username for MongoDB
     * @param password Password for MongoDB
     * @param options  MongoClientOptions
     */
    public MongoDB(String host, int port, String database, String username, String password, String options) {
        this.database = database;
        URI = URICreator(host, port, username, password, options);
        this.connect();
    }

    public MongoDB(String uri, String database) {
        this.URI = uri;
        this.database = database;
        this.connect();
    }


    /**
     * Connects to the database
     */
    public void connect() {
        client = new MongoClient(new MongoClientURI(URI));        
    }

    /**
     * @return the Client
     */
    public MongoClient getClient() {
        return client;
    }

    /**
     * @return The Database
     */
    public MongoDatabase getDatabase() {
        return client.getDatabase(database);
    }

    /**
     * Close the client connection
     */
    public void disconnect() {
        client.close();
    }

    /**
     * Creates a MongoClientURI from the given parameters
     * @param host Database host name
     * @param port Database port
     * @param user Database user
     * @param password Database password
     * @param optionalOptions Optional MongoClientURI options
     * @return MongoClientURI
     */
    private String URICreator(String host, int port, String user, String password, String optionalOptions){
        // mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[defaultauthdb][?options]]
        // example: "mongodb://myDBReader:D1fficultP%40ssw0rd@mongodb0.example.com:27017/?authSource=admin";
        String _uri = "mongodb://";
        if(user != null) {
            if(password == null) { password = ""; }
            _uri += user + ":" + password + "@";
        }
        _uri += host + ":" + port + "/";
        if(optionalOptions != null){
            _uri += optionalOptions;
        }
        return _uri;
    }
}
