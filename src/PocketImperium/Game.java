package PocketImperium;

import java.util.*;

public class Game {
	private List<Player> playerList;
	private int turnNumber;
	private boolean isFinished;
	private String[] color = {"Red", "Green", "Blue"};
	private Sector[][] map; // Makes the 9 Sector that will be used for the game
	
	public Game() {
		this.playerList = new ArrayList<>();
	}
	
	public void startGame() {
		System.out.println("Welcome to Pocket Imperium");
		
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
				System.out.println("Please select the sector you want");
				Scanner scan = new Scanner(System.in);
				sectorID = scan.nextInt();
			}
			
			// Check which sector it corresponds to the map and set the player as the owner
			int row = (sectorID-1)/3; // will get a number from 0 to 2
			int col = (sectorID-1)%3; // will get a number from 0 to 2
			map[row][col].setOwner(currentPlayer); // Sets the player as the owner of the sector
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
				sectorID = scan.nextInt();
			}
			
			// Check which sector it corresponds to the map and set the player as the owner
			int row = (sectorID-1)/3; // will get a number from 0 to 2
			int col = (sectorID-1)%3; // will get a number from 0 to 2
			map[row][col].setOwner(currentPlayer); // Sets the player as the owner of the sector
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
			currentPlayer.placeShips(2);
		}
	}
	
	public void startTurn() {
		System.out.println("Turn starting");
		Iterator<Player> playerIterator = playerList.iterator();
		// Set move set
		while(playerIterator.hasNext()) {
			Player currentPlayer = playerIterator.next();
			for(int i = 0; i < 3; i++) {
				System.out.println(currentPlayer.getName() + " please select your move order");
				System.out.println("1 for Explore, 2 for Expand, 3 for Exterminate");
				Scanner scan = new Scanner(System.in);
				currentPlayer.setCommand(scan.nextInt());
			}
			System.out.println(currentPlayer.getCommand());
			
			// Remove the command sets of the player
			currentPlayer.resetCommand();
		}
	}
	
	
	public void buildMap() {
		map = new Sector[3][3]; // Makes the 9 Sector that will be used for the game
		
		int sectorID = 1;
		// Build one sector at a time
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 3; column++) {
				List<Hex> hexes = new ArrayList<>();
				List<Integer> hexLevel = new ArrayList<Integer>();
				
				// Randomly assign the map different sectors
				Collections.addAll(hexLevel, 1, 1, 2, 0, 0);
				Collections.shuffle(hexLevel);
				
				// Sector with 5 hexes
				for(int i = 0; i < 5; i++) {
					int systemLevel = hexLevel.get(i);
					Hex hex = new Hex(sectorID, systemLevel); // will assign a random system level
					hexes.add(hex);
				}
				// Assign the sector to one part of the map
				map[row][column] = new Sector(sectorID, hexes);
				sectorID++;
			}
		}
		
	}
	
	public void displayMap() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Sector sector = map[row][col];
                System.out.println("Sector ID: " + sector.getSectorId());
                System.out.println("Hexes:");
                for (Hex hex : sector.getSection()) {
                    System.out.println("  Hex Level: " + hex.getSystemLevel() + ", Fleet: " + hex.getFleet());
                }
            }
        }
    }
	
	public Set<Integer> availableSectors() {
		Set<Integer> Sectors = new HashSet<Integer>();
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				map[row][col].displayFreeSector();
				int sectorID = map[row][col].getFreeSectorID();
				Sectors.add(sectorID);
			}
		}
		return Sectors;
	}
	
	// Main function, i.e the entry of our game
	public static void main(String[] args) {
		Game PocketImperium = new Game();
		PocketImperium.startGame();
	}

}