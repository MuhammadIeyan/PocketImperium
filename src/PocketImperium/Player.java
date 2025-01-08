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
    private List<CommandCard> planList;
    private List<Sector> ownedSector;

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.points = 0;
        this.ships = 15;
        this.planList = new ArrayList<>();
        this.ownedSector = new ArrayList<>();
        this.fleetList = new HashMap<>();
    }
    

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Map<Hex, Integer> getFleetList() {
        return fleetList;
    }

    public int getPoints() {
        return points;
    }

	public void placeShips(int shipsPlaced) {
		this.ships = this.ships - shipsPlaced;
	}
	
	public int getRemainingShips() {
		return this.ships;
	}

    public void addFleet(Hex hex, int fleetSize) {
        // Ajoute ou met à jour une flotte sur un Hex
        fleetList.put(hex, fleetList.getOrDefault(hex, 0) + fleetSize);
        
    }

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
    
    public void setOwner(Sector freeSector) {
    	ownedSector.add(freeSector);
    }

    // Actions
    public void plan() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(name + " planifie ses actions.");
        
        // Afficher dynamiquement toutes les commandes disponibles dans CommandCard.Command
        CommandCard.Command[] availableCommands = CommandCard.Command.values();
        System.out.println("Veuillez choisir l'ordre des 3 CommandCards à jouer parmi les suivantes :");
        for (int i = 0; i < availableCommands.length; i++) {
            System.out.println((i + 1) + ". " + availableCommands[i]);
        }
    
        // Demander au joueur de choisir 3 commandes dans un ordre
        for (int i = 1; i <= 3; i++) {
            CommandCard.Command chosenCommand = null;
            do {
                System.out.print("Choisissez la carte #" + i + " (1 à " + availableCommands.length + ") : ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consommer la ligne restante après nextInt()
    
                if (choice >= 1 && choice <= availableCommands.length) {
                    chosenCommand = availableCommands[choice - 1]; // Convertit l'entrée utilisateur en Command
                } else {
                    System.out.println("Choix invalide. Veuillez entrer un chiffre entre 1 et " + availableCommands.length + ".");
                }
            } while (chosenCommand == null); // Continue jusqu'à ce qu'un choix valide soit effectué
    
            // Créer un nouvel objet CommandCard avec la commande choisie
            CommandCard card = new CommandCard(chosenCommand);
            planList.add(card); // Ajoute la carte choisie à la liste
        }
    
        System.out.println("Planification terminée pour " + name + ".");
        System.out.println("Ordre choisi : " + planList);
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
       System.out.println("Hex number " + selectedHex + " has " + 
    		   				sector.getSection().get(selectedHex).getFleet() + " ships \n");
    }

    public void exterminate(List<Hex> attackHexes, List<Integer> shipNumbers) {
        // Logique pour attaquer les Hex ennemis
        System.out.println(name + " attaque les Hex spécifiés.");
    }

    public void explore(int shipNumber, Hex newHex) {
        // Explorer de nouveaux Hex et y déplacer une flotte
        addFleet(newHex, shipNumber);
        System.out.println(name + " explore l'hexagone " + newHex);
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
}