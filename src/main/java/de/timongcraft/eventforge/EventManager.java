package de.timongcraft.eventforge;

import de.timongcraft.eventforge.annotations.Subscribe;
import de.timongcraft.eventforge.events.PostOrder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Manages event dispatching and handling.
 * Allows registration and un-registration of event listeners.
 */
@SuppressWarnings("unused")
public class EventManager {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Class<?>, List<EventHandler>> eventHandlers = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Logger logger;

    /**
     * Constructs an {@link EventManager} with a specified {@link Logger}.
     *
     * @param logger The logger for logging event processing errors
     */
    public EventManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * Registers an event listener to be called for subscribed events.
     *
     * @param listener The listener to register
     */
    public void registerListener(Object listener) {
        requireNonNull(listener, "listener cannot be null");
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(Subscribe.class)) {
                EventHandler eventHandler = new EventHandler(listener, method, method.getAnnotation(Subscribe.class).priority());
                rwLock.writeLock().lock();
                try {
                    eventHandlers.computeIfAbsent(method.getParameterTypes()[0], unused -> new ArrayList<>()).add(eventHandler);
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Unregisters a previously registered event listener.
     * Removes all event handlers associated with the specified listener.
     *
     * @param listener The listener to unregister
     */
    public void unregisterListener(Object listener) {
        requireNonNull(listener, "listener cannot be null");
        rwLock.writeLock().lock();
        try {
            for (List<EventHandler> methods : eventHandlers.values())
                methods.removeIf(method -> method.listener == listener);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Fires an event asynchronously, returning a CompletableFuture.
     *
     * @param event The event to fire
     * @return A {@link CompletableFuture} representing the event's completion
     */
    public <E> CompletableFuture<E> fire(E event) {
        requireNonNull(event, "event cannot be null");
        final CompletableFuture<E> future = new CompletableFuture<>();
        fire(future, event);
        return future;
    }

    /**
     * Fires an event asynchronously without waiting for its completion.
     *
     * @param event The event to fire
     */
    public void fireAndForget(Object event) {
        requireNonNull(event, "event cannot be null");
        fire(null, event);
    }

    /**
     * Shuts down the executor service, waiting for tasks to complete.
     *
     * @return Returns if the executor shutdown normally or needed a termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean shutdown() throws InterruptedException {
        executor.shutdown();
        return executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Fires an event and manages its handlers asynchronously.
     *
     * @param future The {@link CompletableFuture} to complete after handling the event
     * @param event  The event to fire, must not be null.
     * @param <E>    The event class
     */
    private <E> void fire(CompletableFuture<E> future, E event) {
        rwLock.readLock().lock();
        try {
            List<CompletableFuture<Void>> futures = eventHandlers.getOrDefault(event.getClass(), Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparingInt(eventHandler -> eventHandler.postOrder.ordinal()))
                    .map(eventHandler -> CompletableFuture.runAsync(() -> {
                        try {
                            eventHandler.invoke(event);
                        } catch (ReflectiveOperationException e) {
                            logger.log(Level.SEVERE, "Error while processing event", e);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            if (future != null) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> future.complete(event))
                        .exceptionally(throwable -> {
                            future.completeExceptionally(throwable);
                            return null;
                        });
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Represents a handler for an event.
     * Containing the listener class, event handler {@link Method} and {@link PostOrder}.
     */
    private static class EventHandler {

        private final Object listener;
        private final Method method;
        private final PostOrder postOrder;

        private EventHandler(Object listener, Method method, PostOrder postOrder) {
            this.listener = listener;
            this.method = method;
            this.postOrder = postOrder;
        }

        public void invoke(Object event) throws ReflectiveOperationException {
            method.invoke(listener, event);
        }

        public Object getListener() {
            return listener;
        }

        public Method getMethod() {
            return method;
        }

        public PostOrder getPostOrder() {
            return postOrder;
        }

    }

}