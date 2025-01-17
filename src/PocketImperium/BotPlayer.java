package PocketImperium;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

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


    @Override
    public void expand(int shipNumber) {
        // Récupérer les secteurs possédés par le bot
        List<Sector> ownedSectors = getOwnedSector();
        if (ownedSectors.isEmpty()) {
            System.out.println(getName() + " has no sectors to expand.");
            return;
        }

        // Trouver les hexagones possédés dans les secteurs contrôlés
        Map<Sector, List<Hex>> ownedHexesInSectors = new HashMap<>();
        for (Sector sector : ownedSectors) {
            List<Hex> ownedHexes = new ArrayList<>();
            for (Hex hex : sector.getSection()) {
                if (hex.getOwner() == this) {
                    ownedHexes.add(hex); // Ajouter uniquement les hexagones possédés par le bot avec de la place
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

        // Choisir un secteur parmi ceux avec des hexagones possédés disponibles
        List<Sector> availableSectors = new ArrayList<>(ownedHexesInSectors.keySet());
        Sector selectedSector = availableSectors.get(new Random().nextInt(availableSectors.size()));

        // Choisir un hexagone parmi les hexagones possédés dans ce secteur
        List<Hex> availableHexes = ownedHexesInSectors.get(selectedSector);
        Hex chosenHex = availableHexes.get(new Random().nextInt(availableHexes.size()));
        int hexIndex = selectedSector.getSection().indexOf(chosenHex);

        // Ajouter les vaisseaux au hexagone choisi
        System.out.println(getName() + " (bot) is expanding in sector " + selectedSector.getSectorId() + " at hex " + hexIndex);
        selectedSector.expand(hexIndex, shipNumber);
        System.out.println("Bot placed " + shipNumber + " ships on hex " + hexIndex + " in sector " + selectedSector.getSectorId());
    }

    @Override
    public void explore(int fromSectorID, int fromHexID, int toSectorID, int toHexID, int shipNumber, Sector[][] map) {
        // Sélectionner un secteur d'origine parmi ceux possédés
        Random random = new Random();
        Sector fromSector = null;
        
        // Chercher le secteur d'origine dans ownedSector
        for (Sector sector : this.getOwnedSector()) {
            if (sector.getSectorId() == fromSectorID) {
                fromSector = sector;
                break;
            }
        }

        if (fromSector == null) {
            System.out.println("Invalid source sector. Aborting.");
            return;
        }

        // Sélectionner un hex dans ce secteur d'origine
        List<Hex> availableHexes = new ArrayList<>();
        for (int i = 0; i < fromSector.getSection().size(); i++) {
            if (fromSector.getSection().get(i).getAvailability()) {
                availableHexes.add(fromSector.getSection().get(i));
            }
        }

        // Choisir un hex au hasard parmi ceux disponibles
        if (availableHexes.isEmpty()) {
            System.out.println("No available hexes in the source sector. Aborting.");
            return;
        }

        // Choisir un hex et un nombre de vaisseaux à déplacer
        Hex fromHex = availableHexes.get(random.nextInt(availableHexes.size()));
        int fleetToMove = Math.min(fromHex.getFleet(), shipNumber); // Ne pas dépasser le nombre de vaisseaux disponibles

        // Chercher un secteur de destination qui n'est pas contrôlé
        Sector toSector = null;
        for (Sector sector : this.getOwnedSector()) {
            if (sector.getOwner() == null) {  // Si le secteur n'a pas de propriétaire, c'est un secteur libre
                toSector = sector;
                break;
            }
        }

        if (toSector == null) {
            System.out.println("No available target sector. Aborting.");
            return;
        }

        // Choisir un hex de destination
        List<Hex> availableTargetHexes = new ArrayList<>();
        for (int i = 0; i < toSector.getSection().size(); i++) {
            if (!toSector.getSection().get(i).getAvailability()) {
                availableTargetHexes.add(toSector.getSection().get(i));
            }
        }

        // Choisir un hex de destination au hasard
        if (availableTargetHexes.isEmpty()) {
            System.out.println("No available hexes in the target sector. Aborting.");
            return;
        }

        Hex toHex = availableTargetHexes.get(random.nextInt(availableTargetHexes.size()));

        // Lancer l'exploration
        System.out.println("Bot is exploring...");
        super.explore(fromSector.getSectorId(), fromHex.getSectorID(), toSector.getSectorId(), toHex.getSectorID(), fleetToMove, map);
        System.out.println("Bot successfully explored and moved " + fleetToMove + " ships.");
    }
    
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