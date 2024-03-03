package de.timongcraft.eventforge.events;

/**
 * Indicates if an action should not be executed.
 */
@SuppressWarnings("unused")
public interface Cancellable {

    /**
     * Gets the cancellation state of the event.
     *
     * @return The state as boolean
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancel The state as boolean
     */
    void setCancelled(boolean cancel);

}