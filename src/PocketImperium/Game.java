package PocketImperium;

import java.util.*;

public class Game {
	private List<Player> playerList;
	private int turnNumber;
	private boolean isFinished;
	private String[] color = {"Red", "Green", "Yellow", "Blue"};
	private Sector[][] map; // Makes the 9 Sector that will be used for the game
	
	public Game() {
		this.playerList = new ArrayList<>();
	}
	
	public void startGame() {
		System.out.println("Welcome to Pocket Imperium");
		
		// Ask how many players will play in the game
		int numberPlayer = -1;
		System.out.println("How many players are in the game? (4 Player Maximum)");
		Scanner scan = new Scanner(System.in); // Use a single Scanner instance
	    
	    while (numberPlayer < 1 || numberPlayer > 4) {
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
		
		// Start the 1rst turn
		isFinished = false;
		turnNumber = 1;
		buildMap();
		displayMap();
		while (turnNumber < 10 && isFinished == false) {
			turnNumber++;
			startTurn();
		}
		
	}
	
	public void startTurn() {
		System.out.println("Turn starting");
		Iterator<Player> playerIterator = playerList.iterator();
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
	
	public void perform() {
		
	}
	
	public void buildMap() {
		map = new Sector[3][3]; // Makes the 9 Sector that will be used for the game
		
		int sectorID = 1;
		// Build one sector at a time
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 3; column++) {
				List<Hex> hexes = new ArrayList<>();
				// Sector with 3 hexes
				for(int i = 0; i < 3; i++) {
					Hex hex = new Hex(sectorID, i+1);
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
	
	
	// Main function, i.e the entry of our game
	public static void main(String[] args) {
		Game PocketImperium = new Game();
		PocketImperium.startGame();
		
	}

}