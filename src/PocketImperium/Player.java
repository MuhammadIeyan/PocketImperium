package PocketImperium;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Player implements Serializable {
    private String name;
    private String color;
    private Map<Hex, Integer> fleetList = new HashMap<>(); // Associe un Hex à un nombre de vaisseaux
	private int ships;
    private int points;
    protected List<CommandCard> planList;
    private List<Sector> ownedSector;
    
    /**
     * Class that represents a Player. A player is characterized by his name, his color,
     * the number of ships he has left, and his score.
     * 
     * */

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.points = 0;
        this.ships = 15;
        this.planList = new ArrayList<>();
        this.ownedSector = new ArrayList<>();
        this.fleetList = new HashMap<>();
    }
    
    /**
     * This method returns the Players name.
     * @return a string: players name
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the number of ships or fleets the Player has placed as well hexes where the ships are.
     * @return a Map with the owned hex as the key and the number of ships present on it as the value.
     */
    public Map<Hex, Integer> getFleetList() {
        return fleetList;
    }

    /**
     * This method returns the players score in the turn.
     * @return an integer: the Players' score for the turn.
     */
    public int getPoints() {
        return points;
    }
    
	/**
	 * This method returns the number of ships remaining at disposal for the Player to place.
	 * @return an integer: the number of ships in the hand of the Player
	 */
	public int getRemainingShips() {
		return this.ships;
	}

	/**
	 * This method allows the player add a select number of ships from a target Hex of his choice.
	 * 
	 * @param hex: the target Hex that the player wants to add his ships to during any move set.
	 * @param fleetSize: the number of ships the player wants to place on the specified hex.
	 */
    public void addFleet(Hex hex, int fleetSize) {
        // Ajoute ou met à jour une flotte sur un Hex
        fleetList.put(hex, fleetList.getOrDefault(hex, 0) + fleetSize);
        this.ships = this.ships - fleetSize;
    }
    /**
     * This method allows the player remove a select number of ships from a target Hex of his choice.
     * <p>
     * The player selects how many ships he wants to remove from one of his owned Hexes. If the selected 
     * Hex has less ships than the number requested by the player, than the Hexes ship number becomes 0.
     * <p>
     * @param hex: The target Hex the Player wants to remove ships from
     * @param fleetSize: The number of ships the Player want to remove 
     */
    public void removeFleet(Hex hex, int fleetSize) {
        // Retire une flotte d'un Hex, ou supprime l'entrée si la flotte tombe à 0
        if (fleetList.containsKey(hex)) {
            int currentFleet = fleetList.get(hex);
            int newFleetSize = currentFleet - fleetSize;
            if (newFleetSize <= 0) {
                fleetList.remove(hex);
            }
            else {
                fleetList.put(hex, newFleetSize);
            }
        }
    }
    
    /**
     * This method sets the Player as the owner of the selected Sector
     * @param freeSector: The sector that the Player has taken control of.
     */
    public void setOwner(Sector freeSector) {
    	ownedSector.add(freeSector);
    }

    // Actions
    public void plan() {
        Scanner scanner = new Scanner(System.in);
        
        // Afficher dynamiquement toutes les commandes disponibles dans CommandCard.Command
        CommandCard.Command[] availableCommands = CommandCard.Command.values();
        System.out.println("Please select the order of your Command cards from the following: ");
        for (int i = 0; i < availableCommands.length; i++) {
            System.out.println((i + 1) + ". " + availableCommands[i]);
        }
    
        // Demander au joueur de choisir 3 commandes dans un ordre
        for (int i = 1; i <= 3; i++) {
            CommandCard.Command chosenCommand = null;
            do {
                System.out.print("Your " + i + "th card is: (1 until " + availableCommands.length + ") : ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consommer la ligne restante après nextInt()
    
                if (choice >= 1 && choice <= availableCommands.length) {
                    chosenCommand = availableCommands[choice - 1]; // Convertit l'entrée utilisateur en Command
                } else {
                    System.out.println("Wrong choice. Please select a number between 1 and " + availableCommands.length + ".");
                }
            } while (chosenCommand == null); // Continue jusqu'à ce qu'un choix valide soit effectué
    
            // Créer un nouvel objet CommandCard avec la commande choisie
            CommandCard card = new CommandCard(chosenCommand);
            planList.add(card); // Ajoute la carte choisie à la liste
        }
    }

    public void expand(int shipNumber) {
    	// Display all the owned sectors
    	List<Integer> sectorID = new ArrayList<Integer>();
       for(int i = 0; i <  this.ownedSector.size(); i++) {
    	   System.out.println(this.name + " owns the " + this.ownedSector.get(i).getSectorId() + " sector");
    	   sectorID.add(this.ownedSector.get(i).getSectorId());
       }
       
       // Ask the user for the hex, he wants to put the ships on
       Scanner scan = new Scanner(System.in);
       System.out.println("Please select your sector to place " + shipNumber + " ships");
       int selectedSector = -1;
       int selectedSectorIndex = -1;
       while(sectorID.contains(selectedSector) == false) {
    	   selectedSector = scan.nextInt();
    	   if (sectorID.contains(selectedSector)) {
    		   selectedSectorIndex = sectorID.indexOf(selectedSector);
    	   }
       }
       
       // We have the sector now, the user will select the hex next
       Sector sector = this.ownedSector.get(selectedSectorIndex);
       List<Integer> ownedHex = new ArrayList<Integer>();
       System.out.println("Hexs available :");
       for(int i = 0; i < sector.getSection().size(); i++) {
    	    if(sector.getSection().get(i).getAvailability() == true) {
    		   System.out.println("Hex " + i);
    		   ownedHex.add(i);
    	   }
       }
       
       int selectedHex = -1;
       int count = 0;
       while(ownedHex.contains(selectedHex) == false) {
            if (count != 0) {
                System.out.println("Wrong entry, please take another Hex");
            }
    	    selectedHex = scan.nextInt();
            count = count + 1;
       }
       
       // Place the ships on the Hex
       sector.getSection().get(selectedHex).setFleet(shipNumber);
       this.ships = this.ships - shipNumber;
       
       System.out.println("Hex number " + selectedHex + " has " + 
                               sector.getSection().get(selectedHex).getFleet() + " ships \n");
       System.out.println("You have " + this.ships + " remaining.");
    }

    public void exterminate(List<Hex> attackHexes, List<Integer> shipNumbers) {
        // Logique pour attaquer les Hex ennemis
        System.out.println(name + " attaque les Hex spécifiés.");
    }

    public void explore(int fromSectorID, int fromHexID, int toSectorID, int toHexID, int shipNumber, Sector[][] map) {
        // Secteur et hex d'origine
        Sector fromSector = findOwnedSectorById(fromSectorID);
        if (fromSector == null) {
            System.out.println("Invalid source sector. Aborting.");
            return;
        }
    
        Hex fromHex = fromSector.getHex(fromHexID);
        if (fromHex == null || fromHex.getFleet() < shipNumber) {
            System.out.println("Invalid source hex or insufficient ships. Aborting.");
            return;
        }
    
        // Secteur et hex cible
        int targetRow = (toSectorID - 1) / 3;
        int targetCol = (toSectorID - 1) % 3;
    
        Sector toSector = map[targetRow][targetCol];
        if (toSector == null) {
            System.out.println("Invalid target sector. Aborting.");
            return;
        }
    
        Hex toHex = toSector.getHex(toHexID);
        if (toHex == null || toHex.getAvailability()) {
            System.out.println("Target hex is unavailable. Aborting.");
            return;
        }
    
        // Déplacer les vaisseaux
        fromHex.setFleet(fromHex.getFleet() - shipNumber);
        toHex.setFleet(toHex.getFleet() + shipNumber);
        toHex.setOwner(this);
    
        // Ajouter le secteur cible s'il n'est pas déjà contrôlé
        if (!this.ownedSector.contains(toSector)) {
            this.ownedSector.add(toSector);
        }
    
        System.out.println("Successfully moved " + shipNumber + " ships from Sector " + fromSectorID + " Hex " + fromHexID +
                " to Sector " + toSectorID + " Hex " + toHexID + ".");
    }

    private Sector findOwnedSectorById(int sectorId) {
        for (Sector sector : ownedSector) {
            if (sector.getSectorId() == sectorId) {
                return sector;
            }
        }
        return null;
    }

    public void exploit(int idSector) {
        // Exploiter un secteur pour récolter des points
        System.out.println(name + " exploite le secteur " + idSector);
    }

    public List<CommandCard> getPlanList() {
        return planList;
    }

    public void performActions() {
        // Exploiter un secteur pour récolter des points
        System.out.println(name + " execute ses actions ");
    }

    public int getFleetSize() {
        return fleetList.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getCurrentScore() {
        return points;
    }

    public List<Sector> getOwnedSector() {
        return this.ownedSector;
    }
    
    /**
     * This method returns the number of ships that the Player will use for a given command during a phase
     * <p>
     * The Player will be displayed the maximum numbers of ships he can use for the following 
     * phase, and then asked how many ships he wishes to play for the phase.
     * <p>
     * @param command: String representing the command type (Expand - Explore - Exterminate).
     * @param maxNumberShips: Integer representing the Max number of ships.
     * @return integer: the number of ships the player will use for a given command.
     */
    public int commandFleetNumber(String command, int maxNumberShips) {
		int shipNumber = 4;
		Scanner scan = new Scanner(System.in);
		switch(command) {
		case "EXPAND":
			while(shipNumber > maxNumberShips) {
				System.out.println("Please select the number of ships you want to place: ");
				shipNumber = scan.nextInt();
			}
			break;
		case "EXPLORE":
			while(shipNumber > maxNumberShips) {
				System.out.println("Please select the number of ships you want to move: ");
				shipNumber = scan.nextInt();
			}
			break;
		case "EXTERMINATE":
			while(shipNumber > maxNumberShips) {
				System.out.println("Please select the number of ships you want to attack with: ");
				shipNumber = scan.nextInt();
			}
			break;
		}
		return shipNumber;
	}
    
    
}