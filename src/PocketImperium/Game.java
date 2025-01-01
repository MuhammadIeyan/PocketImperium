package PocketImperium;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class Game implements Serializable{
	private List<Player> playerList;
	private int turnNumber;
	private boolean isFinished;
	private String[] color = {"Red", "Green", "Blue"};
	private Sector[][] map; // Makes the 9 Sector that will be used for the game
	
	public Game() {
		this.playerList = new ArrayList<>();
	}

	public List<Sector> getMap() {
        List<Sector> sectorList = new ArrayList<>();
        
        // Parcourir le tableau bidimensionnel pour ajouter chaque secteur à la liste
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                sectorList.add(map[i][j]); // Ajoute chaque secteur à la liste
            }
        }
        
        return sectorList;
    }
	
	
	public void startGame() {
		System.out.println("Welcome to Pocket Imperium");
		
		int menu = 0;
		System.out.println("1 : New Game");
		System.out.println("2 : Load Game");
		Scanner scanMenu = new Scanner(System.in);
		
		while (menu != 1 && menu != 2) {
			System.out.print(">>> ");
			if (scanMenu.hasNextInt()) {
				menu = scanMenu.nextInt();
			} else {
				System.out.println("Invalid input. Please enter 1 or 2.");
				scanMenu.next();
			}
		}
	
		if (menu == 2) {
			// load game
			System.out.print("Enter the filename to load the game: ");
			String filename = scanMenu.next();
			try {
				Game loadedGame = loadFromObject(filename);
				this.playerList = loadedGame.playerList;
				this.turnNumber = loadedGame.turnNumber;
				this.isFinished = loadedGame.isFinished;
				this.map = loadedGame.map;
				displayMap();
				startTurn();
				return;
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Failed to load game: " + e.getMessage());
				return; // Sortir si le chargement échoue
			}
		}


		// Ask how many players will play in the game
		int numberPlayer = -1;
		System.out.println("How many players are in the game? (3 Player Maximum)");
		Scanner scan = new Scanner(System.in); // Use a single Scanner instance
	    
	    while (numberPlayer < 1 || numberPlayer > 3) {
	        System.out.print("Enter a valid number of players: ");
	        if (scan.hasNextInt()) {
	            numberPlayer = scan.nextInt();
	            scan.nextLine(); // Consume the leftover newline
	        }
	        else {
	            System.out.println("Invalid input. Please enter an integer.");
	            scan.nextLine(); // Clear invalid input
	        }
	    }
			
		// Setup all the players
		String name;
		for(int i = 0; i < numberPlayer; i++) {
			System.out.println("What is the name of the player?");
			name = scan.nextLine();
			Player player = new Player(name, color[i]);
			playerList.add(player);
		}
		
		// Setup the game
		turnNumber = 0;
		buildMap();
		displayMap();
		setupGame();
		
		// Start the 1rst turn
		isFinished = false;
		turnNumber = 1;
		while (turnNumber < 10 && isFinished == false) {
			turnNumber++;
			startTurn();
		}
		
	}
	
	public void setupGame() {
		Player currentPlayer;
		Set<Integer> freeSectorID = availableSectors(); // Will keep track of all the free Sectors
		
		// Loop through the players in clockwise direction
		for(int i = 0; i < playerList.size(); i++) {
			currentPlayer = playerList.get(i);
			
			// Display all the free Sectors
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					map[row][col].displayFreeSector();
				}
			}
			
			// Ask the current player to place the initial 2 ships to start the game
			int sectorID = -1;
			while(!freeSectorID.contains(sectorID)) {
				System.out.println(currentPlayer.getName() + " you have " + currentPlayer.getRemainingShips() + 
						" ships, please place 2 ships on an unoccupied level 1 system in an unoccupied Sector");
				Scanner scan = new Scanner(System.in);
				while(sectorID == -1 || sectorID == 5 || !freeSectorID.contains(sectorID)) {
					System.out.println("Please select the sector you want");
					sectorID = scan.nextInt();
				}
			}
			
			// Check which sector it corresponds to the map and set the player as the owner
			int row = (sectorID-1)/3; // will get a number from 0 to 2
			int col = (sectorID-1)%3; // will get a number from 0 to 2
			map[row][col].setOwner(currentPlayer);// Sets the player as the owner of the sector
			currentPlayer.setOwner(map[row][col]); // To keep track of all the sectors owned by the player
			
			freeSectorID.remove(sectorID); // will delete the sector from the map
			
			// This will display the sections available in the sector
			map[row][col].availableSection();
			
			// This will force the user to only select a level 1 system
			int systemLevel = 0;
			int hexesID = -1;
			while(systemLevel != 1) {
				Scanner scan = new Scanner(System.in);
				System.out.println("Please select one sector with a level 1 system");
				hexesID = scan.nextInt();
				systemLevel = map[row][col].getSystemLevel(hexesID);
			}
			map[row][col].expand(hexesID, 2);
			Hex chosenHex = map[row][col].getHex(hexesID); // Récupère l'hexagone sélectionné
			chosenHex.setOwner(currentPlayer); // Attribue le propriétaire à l'hexagone
			currentPlayer.addFleet(chosenHex, 2); // Ajoute la flotte du joueur sur cet hexagone

			currentPlayer.placeShips(2);
		}
		
		// Loop through the players in anti clockwise direction
		for(int i = playerList.size() - 1; i >= 0; i--) {
			currentPlayer = playerList.get(i);
			
			// Display all the free Sectors
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					map[row][col].displayFreeSector();
				}
			}
			
			// Ask the current player to place the initial 2 ships to start the game
			int sectorID = -1;
			while(!freeSectorID.contains(sectorID)) {
				System.out.println(currentPlayer.getName() + " you have " + currentPlayer.getRemainingShips() + 
						" ships, please place 2 ships on an unoccupied level 1 system in an unoccupied Sector");
				System.out.println("Please select the sector you want");
				Scanner scan = new Scanner(System.in);
				while(sectorID == -1 || sectorID == 5) {
					sectorID = scan.nextInt();
				}
				
			}
			
			// Check which sector it corresponds to the map and set the player as the owner
			int row = (sectorID-1)/3; // will get a number from 0 to 2
			int col = (sectorID-1)%3; // will get a number from 0 to 2
			map[row][col].setOwner(currentPlayer); // Sets the player as the owner of the sector
			currentPlayer.setOwner(map[row][col]); // To keep track of all the sectors owned by the player
			
			freeSectorID.remove(sectorID); // will delete the sector from the map
			
			// This will display the sections available in the sector
			map[row][col].availableSection();
			
			// This will force the user to only select a level 1 system
			int systemLevel = 0;
			int hexesID = -1;
			while(systemLevel != 1) {
				Scanner scan = new Scanner(System.in);
				System.out.println("Please select one sector with a level 1 system");
				hexesID = scan.nextInt();
				systemLevel = map[row][col].getSystemLevel(hexesID);
			}
			map[row][col].expand(hexesID, 2);
			Hex chosenHex = map[row][col].getHex(hexesID); // Récupère l'hexagone sélectionné
			chosenHex.setOwner(currentPlayer); // Attribue le propriétaire à l'hexagone
			currentPlayer.addFleet(chosenHex, 2);
			currentPlayer.placeShips(2);
		}
	}
	
	public void startTurn() {
		System.out.println("Turn starting");
		
		// Offrir la possibilité de sauvegarder au début du tour
		System.out.println("Press 'q' to save the game or any other key to start the turn.");
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		
		if (input.equalsIgnoreCase("q")) {
			System.out.print("Do you want to save the game? (yes/no): ");
			String response = scan.nextLine();
			if (response.equalsIgnoreCase("yes")) {
				System.out.print("Enter the filename to save the game: ");
				String filename = scan.nextLine();
				try {
					saveToObject(this, filename);
					System.out.println("Game saved successfully.");
				} catch (IOException e) {
					System.out.println("Failed to save game: " + e.getMessage());
				}
			}
		}
	
		// Initialiser l'itérateur des joueurs et lancer le tour
		Iterator<Player> playerIterator = playerList.iterator();
	
		while (playerIterator.hasNext()) {
			Player currentPlayer = playerIterator.next();
			currentPlayer.getPlanList().clear();
			System.out.println(currentPlayer.getName() + ", it's your turn. Preparing actions...");
			
			// Appeler la méthode plan() pour que le joueur prépare ses actions
			currentPlayer.plan();
			System.out.println("\n"); // Pour sauter une ligne (mieux pour la visibilite en terminale)
		}
	
		// Vérification des cartes choisies par les joueurs
		int[] expandRepeat = this.commandRepeats().get(0);
		int[] exploreRepeat = this.commandRepeats().get(1);
		int[] exterminateRepeat = this.commandRepeats().get(2);
		System.out.println("Expand array: " + Arrays.toString(expandRepeat));
		System.out.println("Explore array: " + Arrays.toString(exploreRepeat));
		System.out.println("Exterminate array: " + Arrays.toString(exterminateRepeat));
		System.out.println();
		
		// Sets the order of the turn, i.e in what order the players will play their commands
		List<Integer> playerOrder = this.setTurnOrder(expandRepeat, exploreRepeat, exterminateRepeat); // Keeps track of Player order
		List<Integer> commandOrder = this.setCommandOrder(expandRepeat, exploreRepeat, exterminateRepeat); // Keeps track of Command order
		System.out.println(playerOrder);
		System.out.println(commandOrder);
		
		for (int i = 0; i < playerOrder.size(); i++) {
			int order = playerOrder.get(i);
			int command = commandOrder.get(i);
			
			Player currentPlayer = playerList.get(order);
			System.out.println(currentPlayer.getName() + " it is your turn now.....");
			
			// 0 represents EXPAND, 1 represents EXPLORE & 2 represents EXTERMINATE
			switch(command) {
			case 0:
				System.out.println(currentPlayer.getName() + " will play the command EXPAND.....");
				currentPlayer.expand(2);
				break;
			case 1:
				System.out.println(currentPlayer.getName() + " will play the command EXPLORE.....");
				break;
			case 2:
				System.out.println(currentPlayer.getName() + " will play the command EXTERMINATE.....");
				break;
			}
			// Make a small pause in between
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public List<Integer> setTurnOrder(int[] expandRepeat, int[] exploreRepeat, int[] exterminateRepeat) {
		List<Integer> playerTurnOrder = new ArrayList<Integer>();
		// We need to make a local copy of the arrays to keep them intact
		int[] expandCopy = expandRepeat.clone();
		int[] exploreCopy = exploreRepeat.clone();
		int[] exterminateCopy = exterminateRepeat.clone();
		
		// Because we have 3 command cards, we will loop through the players 3 times
		for (int i = 0; i < 3; i++) {
			while (expandCopy[i] > 0) {
				for (int j = 0; j < playerList.size(); j++) {
					String command = playerList.get(j).getPlanList().get(i).toString();
					if(command.equals("EXPAND")) {
						playerTurnOrder.add(j);
					}
					expandCopy[i]--;
				}
			}
			
			while (exploreCopy[i] > 0) {
				for (int j = 0; j < playerList.size(); j++) {
					String command = playerList.get(j).getPlanList().get(i).toString();
					if(command.equals("EXPLORE")) {
						playerTurnOrder.add(j);
					}
					exploreCopy[i]--;
				}
			}
			
			while (exterminateCopy[i] > 0) {
				for (int j = 0; j < playerList.size(); j++) {
					String command = playerList.get(j).getPlanList().get(i).toString();
					if(command.equals("EXTERMINATE")) {
						playerTurnOrder.add(j);
					}
					exterminateCopy[i]--;
				}
			}
		}
		return playerTurnOrder;
	}
	
	public List<Integer> setCommandOrder(int[] expandRepeat, int[] exploreRepeat, int[] exterminateRepeat) {
		List<Integer> commandOrder = new ArrayList<Integer>();
		// We need to make a local copy of the arrays to keep them intact
		int[] expandCopy = expandRepeat.clone();
		int[] exploreCopy = exploreRepeat.clone();
		int[] exterminateCopy = exterminateRepeat.clone();
		
		// Because we have 3 command cards, we will loop through the players 3 times
		for (int i = 0; i < 3; i++) {
			// 0 represents "EXPAND", 1 represents "EXPLORE", 2 represents "EXTERMINATE"
			while (expandCopy[i] > 0) {
				commandOrder.add(0);
				expandCopy[i]--;
			}
			
			while (exploreCopy[i] > 0) {
				commandOrder.add(1);
				exploreCopy[i]--;
			}
			
			while (exterminateCopy[i] > 0) {
				commandOrder.add(2);
				exterminateCopy[i]--;
			}
		}
		return commandOrder;
	}
	
	public void buildMap() {
		map = new Sector[3][3]; // Makes the 9 Sector that will be used for the game
		
		int sectorID = 1;
		// Build one sector at a time
		for(int row = 0; row < map.length; row++) {
			for(int column = 0; column < map[row].length; column++) {
				List<Hex> hexes = new ArrayList<>();
				List<Integer> hexLevel = new ArrayList<Integer>();
				if (row == map.length /2 && column == map[row].length /2){
					Hex hex = new Hex(sectorID, 3);
					hexes.add(hex);
				}
				else {
					// Randomly assign the map different sectors
					Collections.addAll(hexLevel, 1, 1, 2, 0, 0);
					Collections.shuffle(hexLevel);
					// Sector with 5 hexes
					for(int i = 0; i < 5; i++) {
						int systemLevel = hexLevel.get(i);
						Hex hex = new Hex(sectorID, systemLevel); // will assign a random system level
						hexes.add(hex);
					}
				}
				// Assign the sector to one part of the map
				map[row][column] = new Sector(sectorID, hexes);
				sectorID++;
			}
		}
		
	}
	
	public void displayMap() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                Sector sector = map[row][col];
                System.out.println("Sector ID: " + sector.getSectorId());
                System.out.println("Hexes:");
                for (Hex hex : sector.getSection()) {
					// Affiche les informations de base du hexagone
					System.out.print("  Hex Level: " + hex.getSystemLevel() + ", Fleet: " + hex.getFleet());
				
					// Vérifie si un propriétaire existe et l'affiche
					if (hex.getOwner() != null) {
						System.out.print(", Owner: " + hex.getOwner().getName());
					}
				
					// Passe à la ligne suivante après l'affichage
					System.out.println();
				}
				
            }
        }
    }
	
	public Set<Integer> availableSectors() {
		Set<Integer> Sectors = new HashSet<Integer>();
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				map[row][col].displayFreeSector();
				int sectorID = map[row][col].getFreeSectorID();
				Sectors.add(sectorID);
			}
		}
		return Sectors;
	}
	
	public List<int[]> commandRepeats() {
		// Check how many times they are being repeated in a turn
		int[] expandRepeat = new int[3];
		int[] exploreRepeat = new int[3];
		int[] exterminateRepeat = new int[3];
		List<int[]> cardRepeat = new ArrayList<int[]>();
		
		// Check the moves selected in order for each player
		for (int i = 0; i < 3; i++) {
			Iterator<Player> playerIterator = playerList.iterator();
			int expandRepeating = 0;
			int exploreRepeating = 0;
			int exterminateRepeating = 0;
			while(playerIterator.hasNext()) {
				Player currentPlayer = playerIterator.next();
				String card = currentPlayer.getPlanList().get(i).toString(); // This will get the command chosen by the player
				switch (card) {
				case "EXPAND":
					expandRepeating++;
					break;
				case "EXPLORE":
					exploreRepeating++;
					break;
				case "EXTERMINATE":
					exterminateRepeating++;	
					break;
				}
			}
			
			// Add repetitions in an array so we can get the move
			expandRepeat[i] = expandRepeating;
			exploreRepeat[i] = exploreRepeating;
			exterminateRepeat[i] = exterminateRepeating;
		}
		
		// Add all the values in 1 single List
		cardRepeat.add(expandRepeat);
		cardRepeat.add(exploreRepeat);
		cardRepeat.add(exterminateRepeat);
		return cardRepeat;
	}

	public static void saveToObject(Game game, String filename) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
			oos.writeObject(game);
		}
	}
	
	public Player getPlayerByName(String playerName) {
		for (Player player : playerList) { // Assume playerList is the list of all players in the game
			if (player.getName().equals(playerName)) {
				return player;
			}
		}
		return null; // If no player is found with the given name
	}

	public static Game loadFromObject(String filename) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
			return (Game) ois.readObject();
		}
	}	
	
	
	// Main function, i.e the entry of our game
	public static void main(String[] args) {
		Game PocketImperium = new Game();
		PocketImperium.startGame();
	}

}