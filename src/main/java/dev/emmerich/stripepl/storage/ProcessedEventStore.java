package dev.emmerich.stripepl.storage;

public interface ProcessedEventStore {
    boolean hasProcessed(String eventId);
    void markProcessed(String eventId);
    default void close() {}
}