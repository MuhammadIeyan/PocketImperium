package PocketImperium;

import java.io.Serializable;

/**
 * Represents a command card used by a player or bot to issue commands in the game.
 * Each CommandCard holds a specific command that defines the action to be performed.
 */
public class CommandCard implements Serializable {
    
    /**
     * Enum representing the different types of commands a CommandCard can issue.
     */
    public enum Command {
        EXPAND, EXPLORE, EXTERMINATE
    }

    private Command command;

    /**
     * Constructs a new CommandCard with the specified command.
     *
     * @param command The command to be assigned to the card.
     */
    public CommandCard(Command command) {
        this.command = command;
    }

    /**
     * Gets the command associated with this CommandCard.
     *
     * @return The command associated with this CommandCard.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Sets a new command for this CommandCard.
     *
     * @param command The new command to be set for the CommandCard.
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * Returns a string representation of the command associated with this CommandCard.
     *
     * @return A string representing the command of the CommandCard.
     */
    @Override
    public String toString() {
        return command.toString();  // Returns the command name as a string
    }
}
