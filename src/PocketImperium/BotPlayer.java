package PocketImperium;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotPlayer extends Player implements Serializable {
    public enum Strategy {
        AGGRESSIVE, DEFENSIVE, RANDOM
    }

    private Strategy strategy;

    public BotPlayer(String name, String color, Strategy strategy) {
        super(name, color);
        this.strategy = strategy;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<CommandCard> generateStrategy() {
        // Implémentation de la stratégie du bot selon son type
        // Par exemple, une stratégie RANDOM pourrait choisir des CommandCards aléatoires
        return List.of(new CommandCard(CommandCard.Command.EXPAND)); // Exemple basique
    }
    
    @Override
    public void plan() {
        System.out.println(getName() + " (bot) is planning actions using " + strategy + " strategy...");

        planList.clear(); // Clear previous plans
        switch (strategy) {
            case AGGRESSIVE -> {
                // Exemple : Priorité à EXTERMINATE
                planList.add(new CommandCard(CommandCard.Command.EXTERMINATE));
                planList.add(new CommandCard(CommandCard.Command.EXPAND));
                planList.add(new CommandCard(CommandCard.Command.EXPLORE));
                }
            case DEFENSIVE -> {
                // Exemple : Priorité à EXPAND et DEFENSE
                planList.add(new CommandCard(CommandCard.Command.EXPAND));
                planList.add(new CommandCard(CommandCard.Command.EXPLORE));
                planList.add(new CommandCard(CommandCard.Command.EXTERMINATE));
                }
            case RANDOM -> {
                // Exemple : Commandes choisies aléatoirement
                CommandCard.Command[] commands = CommandCard.Command.values();
                for (int i = 0; i < 3; i++) {
                    int randomIndex = (int) (Math.random() * commands.length);
                    planList.add(new CommandCard(commands[randomIndex]));
                }
            }
        }
        System.out.println(getName() + " has planned: " + getPlanList());
    }

    @Override
    public void expand(int shipNumber) {
        // Sélectionner un secteur que le bot possède
        List<Sector> ownedSectors = getOwnedSector();
        if (ownedSectors.isEmpty()) {
            System.out.println(getName() + " has no sectors to expand.");
            return;
        }

        // Choisir un secteur aléatoirement parmi ceux possédés
        Sector selectedSector = ownedSectors.get(new Random().nextInt(ownedSectors.size()));

        // Trouver les hexagones disponibles dans ce secteur
        List<Hex> availableHexes = new ArrayList<>();
        for (int i = 0; i < selectedSector.getSection().size(); i++) {
            Hex hex = selectedSector.getSection().get(i);
            if (hex.fleetAvailablity() > 0) { // Si l'hexagone a de la place
                availableHexes.add(hex);
            }
        }

        if (availableHexes.isEmpty()) {
            System.out.println(getName() + " has no available hexes to expand in sector " + selectedSector.getSectorId());
            return;
        }

        // Choisir un hexagone parmi ceux disponibles
        Hex chosenHex = availableHexes.get(new Random().nextInt(availableHexes.size()));
        int hexIndex = selectedSector.getSection().indexOf(chosenHex);
        
        // Effectuer l'expansion sur l'hexagone choisi
        System.out.println(getName() + " (bot) is expanding in sector " + selectedSector.getSectorId() + " at hex " + hexIndex);
        selectedSector.expand(hexIndex, shipNumber);
        System.out.println("Bot placed " + shipNumber + " ships on hex " + hexIndex + " in sector " + selectedSector.getSectorId());
    }
}