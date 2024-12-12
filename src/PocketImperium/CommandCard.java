package PocketImperium;

import java.io.Serializable;

public class CommandCard implements Serializable {
    public enum Command {
        EXPAND, EXPLORE, EXTERMINATE
    }

    private Command command;

    public CommandCard(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command.toString();  // Retourne le nom de la commande sous forme de chaîne
    }
}