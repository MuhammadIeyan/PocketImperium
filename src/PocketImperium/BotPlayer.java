package PocketImperium;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a computer-controlled player (bot) with a specific strategy.
 * Inherits from the Player class and implements various actions based on the bot's strategy.
 */
public class BotPlayer extends Player implements Serializable {
    
    /**
     * Enum representing the possible strategies for a bot player.
     */
    public enum Strategy {
        AGGRESSIVE, DEFENSIVE, RANDOM
    }

    private Strategy strategy;

    /**
     * Constructs a new BotPlayer with the specified name, color, and strategy.
     *
     * @param name     The name of the bot.
     * @param color    The color of the bot.
     * @param strategy The strategy that the bot will follow.
     */
    public BotPlayer(String name, String color, Strategy strategy) {
        super(name, color);
        this.strategy = strategy;
    }

    /**
     * Gets the current strategy of the bot.
     *
     * @return The strategy of the bot.
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * Sets a new strategy for the bot.
     *
     * @param strategy The new strategy to apply.
     */
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Generates a list of command cards based on the bot's current strategy.
     * For example, a RANDOM strategy could generate random command cards.
     *
     * @return A list of command cards (CommandCard) generated according to the bot's strategy.
     */
    public List<CommandCard> generateStrategy() {
        // Implementation of the bot's strategy based on its type.
        // Example: returns an EXPAND command.
        return List.of(new CommandCard(CommandCard.Command.EXPAND)); // Basic example
    }

    /**
     * Plans actions based on the bot's strategy.
     * For example, aggressive bots will prioritize extermination, while defensive bots will expand first.
     */
    @Override
    public void plan() {
        System.out.println(getName() + " (bot) is planning actions using " + strategy + " strategy...");

        planList.clear(); // Clear previous plans
        switch (strategy) {
            case AGGRESSIVE:
                planList.add(new CommandCard(CommandCard.Command.EXTERMINATE));
                planList.add(new CommandCard(CommandCard.Command.EXPAND));
                planList.add(new CommandCard(CommandCard.Command.EXPLORE));
                break;
            case DEFENSIVE:
                planList.add(new CommandCard(CommandCard.Command.EXPAND));
                planList.add(new CommandCard(CommandCard.Command.EXPLORE));
                planList.add(new CommandCard(CommandCard.Command.EXTERMINATE));
                break;
            case RANDOM:
                CommandCard.Command[] commands = CommandCard.Command.values();
                for (int i = 0; i < 3; i++) {
                    int randomIndex = (int) (Math.random() * commands.length);
                    planList.add(new CommandCard(commands[randomIndex]));
                }
                break;
        }        
    }

    /**
     * Expands the bot's control by adding ships to its owned hexes.
     *
     * @param shipNumber The number of ships to add during the expansion.
     */
    @Override
    public void expand(int shipNumber) {
        // Retrieve the sectors owned by the bot
        List<Sector> ownedSectors = getOwnedSector();
        if (ownedSectors.isEmpty()) {
            System.out.println(getName() + " has no sectors to expand.");
            return;
        }

        // Find the hexes owned in controlled sectors
        Map<Sector, List<Hex>> ownedHexesInSectors = new HashMap<>();
        for (Sector sector : ownedSectors) {
            List<Hex> ownedHexes = new ArrayList<>();
            for (Hex hex : sector.getSection()) {
                if (hex.getOwner() == this) {
                    ownedHexes.add(hex); // Add only owned hexes with available space
                }
            }
            if (!ownedHexes.isEmpty()) {
                ownedHexesInSectors.put(sector, ownedHexes);
            }
        }

        if (ownedHexesInSectors.isEmpty()) {
            System.out.println(getName() + " has no owned hexes with available space to expand.");
            return;
        }

        // Choose a sector from those with available owned hexes
        List<Sector> availableSectors = new ArrayList<>(ownedHexesInSectors.keySet());
        Sector selectedSector = availableSectors.get(new Random().nextInt(availableSectors.size()));

        // Choose a hex among the owned hexes in the selected sector
        List<Hex> availableHexes = ownedHexesInSectors.get(selectedSector);
        Hex chosenHex = availableHexes.get(new Random().nextInt(availableHexes.size()));
        int hexIndex = selectedSector.getSection().indexOf(chosenHex);

        // Add ships to the chosen hex
        System.out.println(getName() + " (bot) is expanding in sector " + selectedSector.getSectorID() + " at hex " + hexIndex);
        selectedSector.expand(hexIndex, shipNumber);
        System.out.println("Bot placed " + shipNumber + " ships on hex " + hexIndex + " in sector " + selectedSector.getSectorID());
    }

    /**
     * Makes the bot explore a new sector and move ships from one hex to another.
     *
     * @param fromSectorID The ID of the sector from which ships are being moved.
     * @param fromHexID    The ID of the hex from which ships are being moved.
     * @param toSectorID   The ID of the destination sector.
     * @param toHexID      The ID of the destination hex.
     * @param shipNumber   The number of ships to move.
     * @param map          The map representing the sectors and hexes in the game.
     */
    @Override
    public void explore(int fromSectorID, int fromHexID, int toSectorID, int toHexID, int shipNumber, Sector[][] map) {
        // Select an origin sector from the ones owned
        Random random = new Random();
        Sector fromSector = null;

        // Search for the origin sector in owned sectors
        for (Sector sector : this.getOwnedSector()) {
            if (sector.getSectorID() == fromSectorID) {
                fromSector = sector;
                break;
            }
        }

        if (fromSector == null) {
            System.out.println("Invalid source sector. Aborting.");
            return;
        }

        // Select a hex in the origin sector
        List<Hex> availableHexes = new ArrayList<>();
        for (int i = 0; i < fromSector.getSection().size(); i++) {
            if (fromSector.getSection().get(i).getAvailability()) {
                availableHexes.add(fromSector.getSection().get(i));
            }
        }

        // Choose a hex randomly from the available ones
        if (availableHexes.isEmpty()) {
            System.out.println("No available hexes in the source sector. Aborting.");
            return;
        }

        // Choose a hex and number of ships to move
        Hex fromHex = availableHexes.get(random.nextInt(availableHexes.size()));
        int fleetToMove = Math.min(fromHex.getFleet(), shipNumber); // Do not exceed the number of available ships

        // Find a target sector that is not owned
        Sector toSector = null;
        for (Sector sector : this.getOwnedSector()) {
            if (sector.getOwner() == null) {  // If the sector has no owner, it's a free sector
                toSector = sector;
                break;
            }
        }

        if (toSector == null) {
            System.out.println("No available target sector. Aborting.");
            return;
        }

        // Choose a destination hex
        List<Hex> availableTargetHexes = new ArrayList<>();
        for (int i = 0; i < toSector.getSection().size(); i++) {
            if (!toSector.getSection().get(i).getAvailability()) {
                availableTargetHexes.add(toSector.getSection().get(i));
            }
        }

        // Choose a destination hex randomly
        if (availableTargetHexes.isEmpty()) {
            System.out.println("No available hexes in the target sector. Aborting.");
            return;
        }

        Hex toHex = availableTargetHexes.get(random.nextInt(availableTargetHexes.size()));

        // Start exploration
        System.out.println("Bot is exploring...");
        super.explore(fromSector.getSectorID(), fromHex.getSectorID(), toSector.getSectorID(), toHex.getSectorID(), fleetToMove, map);
        System.out.println("Bot successfully explored and moved " + fleetToMove + " ships.");
    }

    /**
     * Returns the number of ships the bot can command to move.
     *
     * @param command       The command for which the bot needs to determine the number of ships.
     * @param maxNumberShips The maximum number of ships the bot can command.
     * @return The number of ships to move.
     */
    @Override
    public int commandFleetNumber(String command, int maxNumberShips) {
        int shipNumber = 4;
        Random rand = new Random();
        while(shipNumber > maxNumberShips) {
            shipNumber = rand.nextInt(maxNumberShips);
        }
        return shipNumber;
    }
}
