package dev.emmerich.stripepl.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.InsertOneOptions;
import org.bson.Document;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class MongoProcessedEventStore implements ProcessedEventStore {

    private final MongoClient client;
    private final MongoCollection<Document> collection;
    private final Plugin plugin;

    public MongoProcessedEventStore(Plugin plugin, String connectionUri, String databaseName, String collectionName, long ttlDays) {
        this.plugin = plugin;
        ConnectionString conn = new ConnectionString(connectionUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(conn)
                .build();
        this.client = MongoClients.create(settings);
        MongoDatabase db = client.getDatabase(databaseName);
        this.collection = db.getCollection(collectionName);

        collection.createIndex(Indexes.ascending("eventId"), new IndexOptions().unique(true));
        if (ttlDays > 0) {
            collection.createIndex(Indexes.ascending("createdAt"), new IndexOptions().expireAfter(ttlDays, TimeUnit.DAYS));
        }
    }

    @Override
    public boolean hasProcessed(String eventId) {
        if (eventId == null || eventId.isEmpty()) return false;
        return collection.find(Filters.eq("eventId", eventId)).limit(1).first() != null;
    }

    @Override
    public void markProcessed(String eventId) {
        if (eventId == null || eventId.isEmpty()) return;
        try {
            Document doc = new Document("eventId", eventId)
                    .append("createdAt", new java.util.Date());
            collection.insertOne(doc, new InsertOneOptions());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "";
            if (!message.contains("duplicate key")) {
                plugin.getLogger().warning("Failed to insert processed event '" + eventId + "': " + message);
            }
        }
    }

    @Override
    public void close() {
        try { client.close(); } catch (Exception ignored) {}
    }
}


