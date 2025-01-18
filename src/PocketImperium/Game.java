package PocketImperium;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;


/**
 * Represents the main game logic and state for the Pocket Imperium game.
 * <p>
 * This class manages the game by handling the list of players, the game map, and the turn sequence.
 * It also tracks whether the game is finished and provides mechanisms for managing player actions,
 * including movement, exploration, and sector management.
 * </p>
 * <p>
 * The game uses a two-dimensional array of sectors to represent the map, with each sector containing 
 * multiple hexagons. Players interact with these sectors by selecting and moving ships or attacking 
 * enemy sectors. The game also includes functionality to save and load game data to and from files.
 * </p>
 *
 * @see Player
 * @see Sector
 */
public class Game implements Serializable{
	private List<Player> playerList;
	private int turnNumber;
	private boolean isFinished;
	private String[] color = {"Red", "Green", "Blue"};
	private Sector[][] map; // Makes the 9 Sector that will be used for the game
	
	public Game() {
		this.playerList = new ArrayList<>();
	}
	
	/**
	* Starts the Pocket Imperium game, allowing the user to set up and play.
	* 
	* <p>This method initializes the game by offering the user a choice between starting
	* a new game or loading a saved game. If a new game is chosen, it prompts the user
	* to configure the players (either human or bots), their names, and the strategies
	* for bot players. It then builds the game map, displays it, and starts the first turn.
	* 
	* <p>The game loop runs for a maximum of 10 turns or until the game is marked as finished.
	* 
	* <p>Steps:
	* <ol>
	*   <li>Display a menu for the user to choose between "New Game" and "Load Game".</li>
	*   <li>If "Load Game" is chosen, load the game state from a file and resume gameplay.</li>
	*   <li>If "New Game" is chosen, prompt the user to specify the number of players (maximum 3).</li>
	*   <li>For each player, ask if it is a bot or a human:
	*       <ul>
	*         <li>For bots, request the name and strategy (AGGRESSIVE, DEFENSIVE, or RANDOM).</li>
	*         <li>For humans, request the player's name.</li>
	*       </ul>
	*   </li>
	*   <li>Build the game map, initialize the game state, and start the first turn.</li>
	*   <li>Run the game for up to 10 turns or until the game is marked as finished.</li>
	* </ol>
	* 
	*/
	public void startGame() throws InterruptedException {

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
		for (int i = 0; i < numberPlayer; i++) {
			System.out.println("Is this player a bot? (yes/no)");
			String isBot = scan.nextLine().trim().toLowerCase();

			if (isBot.equals("yes")) {
				System.out.println("Enter the bot's name:");
				name = scan.nextLine();
				System.out.println("Choose the bot's strategy: (AGGRESSIVE, DEFENSIVE, RANDOM)");
				String strategyInput = scan.nextLine().trim().toUpperCase();

				BotPlayer.Strategy strategy;
				try {
					strategy = BotPlayer.Strategy.valueOf(strategyInput);
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid strategy. Defaulting to RANDOM.");
					strategy = BotPlayer.Strategy.RANDOM;
				}

				BotPlayer botPlayer = new BotPlayer(name, color[i], strategy);
				playerList.add(botPlayer);
				System.out.println("Bot player " + name + " with " + strategy + " strategy added.");
			} else {
				System.out.println("Enter the player's name:");
				name = scan.nextLine();
				Player player = new Player(name, color[i]);
				playerList.add(player);
				System.out.println("Human player " + name + " added.");
			}
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
	




	/**
	* Sets up the initial game state by allowing players (human or bot) to place their ships
	* in two phases: clockwise and counterclockwise. Each player chooses a sector and a hex
	* with a level 1 system to place their ships.
	*
	* <p><b>Process:</b></p>
	* <ul>
	*   <li>Iterates through the player list in clockwise order and then counterclockwise.</li>
	*   <li>For each player:
	*     <ul>
	*       <li>Displays all free sectors on the map.</li>
	*       <li>If the player is a bot:
	*         <ul>
	*           <li>Selects a random free sector and hex with level 1 system.</li>
	*         </ul>
	*       </li>
	*       <li>If the player is human:
	*         <ul>
	*           <li>Prompts the player to select a valid sector and hex with level 1 system.</li>
	*         </ul>
	*       </li>
	*       <li>Sets the selected sector and hex as owned by the player.</li>
	*       <li>Places 2 ships in the selected hex and updates the player's fleet and remaining ships.</li>
	*     </ul>
	*   </li>
	* </ul>
	* 
	* <p><b>Key Operations:</b></p>
	* <ul>
	*   <li>Ensures the selected sector and hex are valid and unoccupied.</li>
	*   <li>Updates the map and player's state to reflect ownership and fleet allocation.</li>
	*   <li>Handles bot and human players differently for sector and hex selection.</li>
	* </ul>
	*/
	public void setupGame() throws InterruptedException {
		Player currentPlayer;
	
		Set<Integer> freeSectorID = availableSectors(); // Keep track of all free Sectors
		freeSectorID.remove(5); // Exclut le secteur 5 des secteurs disponibles

	
		// Loop through the players in clockwise direction
		for (int i = 0; i < playerList.size(); i++) {
			currentPlayer = playerList.get(i);
	
			// Display all the free Sectors
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					map[row][col].displayFreeSector();
				}
			}
	
			int sectorID = -1;
			if (currentPlayer instanceof BotPlayer) {
				// Logique pour le bot : choisir un secteur libre aléatoire
				System.out.println(currentPlayer.getName() + " is a bot. He is thinking....");
				Thread.sleep(1000);
				
				sectorID = freeSectorID.stream().findAny().orElse(-1);
				System.out.println(currentPlayer.getName() + " (bot) chose sector " + sectorID + ".");
			} else {
				// Logique pour un joueur humain
				Scanner scan = new Scanner(System.in);
				while (!freeSectorID.contains(sectorID)) {
					System.out.println(currentPlayer.getName() + " you have " + currentPlayer.getRemainingShips() +
							" ships, please place 2 ships on an unoccupied level 1 system in an unoccupied Sector");
					System.out.println("Please select the sector you want");
					sectorID = scan.nextInt();
				}
			}
	
			// Check which sector it corresponds to the map and set the player as the owner
			int row = (sectorID - 1) / 3; // will get a number from 0 to 2
			int col = (sectorID - 1) % 3; // will get a number from 0 to 2
			map[row][col].setOwner(currentPlayer); // Sets the player as the owner of the sector
			currentPlayer.setOwner(map[row][col]); // To keep track of all the sectors owned by the player
	
			freeSectorID.remove(sectorID); // Remove the sector from the map
	
			// This will display the sections available in the sector
			map[row][col].availableSection();
	
			int systemLevel = 0;
			int hexesID = -1;
			if (currentPlayer instanceof BotPlayer) {
				// Logique pour le bot : choisir un système de niveau 1 aléatoire
				System.out.println(currentPlayer.getName() + " is thinking....");
				Thread.sleep(1000);
				
				hexesID = map[row][col].getRandomHexWithLevel(1);
				System.out.println(currentPlayer.getName() + " (bot) chose hex " + hexesID + ".");
			} else {
				// Logique pour un joueur humain
				Scanner scan = new Scanner(System.in);
				while (systemLevel != 1) {
					System.out.println("Please select one sector with a level 1 system");
					hexesID = scan.nextInt();
					systemLevel = map[row][col].getSystemLevel(hexesID);
				}
			}
	
			map[row][col].expand(hexesID, 2);
			Hex chosenHex = map[row][col].getHex(hexesID); // Get the selected hex
			chosenHex.setOwner(currentPlayer); // Assign ownership to the hex
			currentPlayer.addFleet(chosenHex, 2); // Add the player's fleet to this hex
	
		}
	
		// Repeat the same logic for the anti-clockwise phase
		for (int i = playerList.size() - 1; i >= 0; i--) {
			currentPlayer = playerList.get(i);
	
			// Display all the free Sectors
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					map[row][col].displayFreeSector();
				}
			}
	
			int sectorID = -1;
			if (currentPlayer instanceof BotPlayer) {
				System.out.println(currentPlayer.getName() + " is thinking....");
				Thread.sleep(1000);
				
				sectorID = freeSectorID.stream().findAny().orElse(-1);
				System.out.println(currentPlayer.getName() + " (bot) chose sector " + sectorID + ".");
			} else {
				Scanner scan = new Scanner(System.in);
				while (!freeSectorID.contains(sectorID)) {
					System.out.println(currentPlayer.getName() + " you have " + currentPlayer.getRemainingShips() +
							" ships, please place 2 ships on an unoccupied level 1 system in an unoccupied Sector");
					System.out.println("Please select the sector you want");
					sectorID = scan.nextInt();
				}
			}
	
			int row = (sectorID - 1) / 3;
			int col = (sectorID - 1) % 3;
			map[row][col].setOwner(currentPlayer);
			currentPlayer.setOwner(map[row][col]);
	
			freeSectorID.remove(sectorID);
	
			map[row][col].availableSection();
	
			int systemLevel = 0;
			int hexesID = -1;
			if (currentPlayer instanceof BotPlayer) {
				System.out.println(currentPlayer.getName() + " is thinking....");
				Thread.sleep(1000);
				hexesID = map[row][col].getRandomHexWithLevel(1);
				System.out.println(currentPlayer.getName() + " (bot) chose hex " + hexesID + ".");
			} else {
				Scanner scan = new Scanner(System.in);
				while (systemLevel != 1) {
					System.out.println("Please select one sector with a level 1 system");
					hexesID = scan.nextInt();
					systemLevel = map[row][col].getSystemLevel(hexesID);
				}
			}
	
			map[row][col].expand(hexesID, 2);
			Hex chosenHex = map[row][col].getHex(hexesID);
			chosenHex.setOwner(currentPlayer);
			currentPlayer.addFleet(chosenHex, 2);
		}
	}
	
	/**
	 * This method starts a turn of the Game.
	 * <p>
	 * The method starts a new turn of the Game. Players are first prompted if they want to 
	 * save their game so far, then the turn begins by asking each player in what order he 
	 * will play his moves. Based on the move set selected, the player turn order will be 
	 * arranged to follow the rules of the Game where Expand goes first, followed by Explore 
	 * and then Exterminate. Players will then play their selected moves for each phase.
	 * <p>
	 * 
	 * @throws InterruptedException
	 */
	public void startTurn() throws InterruptedException {
		System.out.println("Turn starting");
		
		// Offer the option to save at the start of the turn
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
	
		// Initialize the player iterator and start the turn
		Iterator<Player> playerIterator = playerList.iterator();
	
		while (playerIterator.hasNext()) {
			Player currentPlayer = playerIterator.next();
			currentPlayer.getPlanList().clear();
			
			System.out.println(currentPlayer.getName() + ", it's your turn. Preparing actions...");
			
			if (currentPlayer instanceof BotPlayer) {
				// Bot planning
				System.out.println(currentPlayer.getName() + " is thinking....");
				Thread.sleep(2000);
				currentPlayer.plan();
				
			} else {
				// Human player planning
				System.out.println("Please select your actions.");
				currentPlayer.plan();
			}
			System.out.println("\n");
		}
		
	
		// Check the cards chosen by the players
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
			
			if (currentPlayer instanceof BotPlayer) {
				System.out.println(currentPlayer.getName() + " is a bot. He is thinking....");
				Thread.sleep(2000);
			}
			int numberOfRep;
			int shipNumber;
			int maxShipNumber;
			// 0 represents EXPAND, 1 represents EXPLORE & 2 represents EXTERMINATE
			switch(command) {
			case 0:
				System.out.println(currentPlayer.getName() + " will play the command EXPAND.....");
				numberOfRep = expandRepeat[i/3];
				maxShipNumber = this.commandPower(numberOfRep);
				shipNumber = currentPlayer.commandFleetNumber("EXPAND", maxShipNumber);
				
				this.executeExpand(currentPlayer, shipNumber);
				this.displayMap();
				break;
				
			case 1:
				System.out.println(currentPlayer.getName() + " will play the command EXPLORE.....");
				numberOfRep = exploreRepeat[i/3];
				maxShipNumber = this.commandPower(numberOfRep);
				shipNumber = currentPlayer.commandFleetNumber("EXPLORE", maxShipNumber);
				
				this.executeExplore(currentPlayer, shipNumber);
				this.displayMap();
				break;
				
			case 2:
				System.out.println(currentPlayer.getName() + " will play the command EXTERMINATE.....");
				numberOfRep = exterminateRepeat[i/3];
				maxShipNumber = this.commandPower(numberOfRep);
				shipNumber = currentPlayer.commandFleetNumber("EXTERMINATE", maxShipNumber);
				
				this.executeExterminate(currentPlayer, shipNumber);
				this.displayMap();
				break;
			}
			// Make a small pause in between
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		endTurn();
		verifyEnd();
	}
	/**
	 * Calculates the scores of each player to determine who is the Winner at the end.
	 */
	public void endTurn() {
		// Calculate the scores
		Map<Player, Integer> Scores = new HashMap<>();
		for (Player player : playerList) {
			int score = player.getCurrentScore();

			for (Sector[] row : map) {
				for (Sector sector : row) {
					if (sector.getOwner() == player) {
						for (Hex hex : sector.getHexes()) {
							score += hex.getSystemLevel() * 2; // Double the value of the systems
						}
					}
				}
			}
			Scores.put(player, score);
			System.out.println(player.getName() + " final score: " + score);
		}
	}
	
	/**
	 * Checks if the Game has ended or not.
	 * <p>
	 * This method will check if number of turns is more than 9 or if any player doesn't 
	 * have any ships left, and if he's been eliminated from the game.
	 * <p>
	 */
	public void verifyEnd() {

		// Check if the game has reached 9 turns or if a player has been eliminated
		if (turnNumber >= 9 || playerList.stream().anyMatch(player -> player.getFleetSize() == 0)) {
			isFinished = true; // Mark the game as finished
			System.out.println("The game has ended!");
	
			// Calculate the final scores
			Map<Player, Integer> finalScores = new HashMap<>();
			for (Player player : playerList) {
				int score = player.getCurrentScore();
	
				for (Sector[] row : map) {
					for (Sector sector : row) {
						if (sector.getOwner() == player) {
							for (Hex hex : sector.getHexes()) {
								score += hex.getSystemLevel() * 2; // Double the value of the systems
							}
						}
					}
				}
				finalScores.put(player, score);
				System.out.println(player.getName() + " final score: " + score);
			}
	
			// Determine the winner
			Player winner = Collections.max(finalScores.entrySet(), Map.Entry.comparingByValue()).getKey();
			System.out.println("The winner is " + winner.getName() + " with " + finalScores.get(winner) + " points!");
	
			// Mark the game as finished
			return;
		}
	
		// If no end condition is met, the game continues
		System.out.println("The game continues. Turn " + turnNumber + " is in progress.");
	}
	

	/**
	* Determines the number of ships a player can use based on the number of identical cards revealed.
	* 
	* <p>The number of ships is determined as follows:</p>
	* <ul>
	*   <li>If {@code numberOfRep == 1}, the player can use 3 ships.</li>
	*   <li>If {@code numberOfRep == 2}, the player can use 2 ships.</li>
	*   <li>Otherwise, the player can use only 1 ship.</li>
	* </ul>
	* 
	* <p>A message displaying the result is shown to the player.</p>
	*/
	public int commandPower(int numberOfRep) {
		if (numberOfRep == 1) {
			System.out.println("You can use 3 ships");
			return 3;
		}
		if (numberOfRep == 2) {
			System.out.println("You can use 2 ships");
			return 2;
		}
		System.out.println("You can use 1 ship only");
		return 1;
	}
	

	/**
	* Calculates the number of occurrences of each command card (EXPAND, EXPLORE, EXTERMINATE) 
	* selected by players during a turn.
	* 
	* <p>This method iterates over the players and counts how many times each command card is chosen 
	* in each phase of the turn (three phases in total). The results are stored as follows:</p>
	* <ul>
	*   <li>An array for the "EXPAND" command counts per phase.</li>
	*   <li>An array for the "EXPLORE" command counts per phase.</li>
	*   <li>An array for the "EXTERMINATE" command counts per phase.</li>
	* </ul>
	* 
	* <p>Each array contains three integers, where each index corresponds to a phase of the turn. 
	* These arrays are then added to a list that represents the total counts for all commands.</p>
	* 
	* @return a list of three integer arrays, each representing the count of a specific command card:
	*         <ul>
	*           <li>The first array contains the counts for the "EXPAND" command for each phase.</li>
	*           <li>The second array contains the counts for the "EXPLORE" command for each phase.</li>
	*           <li>The third array contains the counts for the "EXTERMINATE" command for each phase.</li>
	*         </ul>
	*/
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
	


	/**
	* Determines the turn order of players based on their chosen command cards (EXPAND, EXPLORE, EXTERMINATE) 
	* and the number of times each card was selected during a turn.
	* 
	* <p>The method creates a turn order list by iterating over the command card counts for each phase of the turn 
	* (three phases in total). Players who chose a specific command are added to the turn order based on the card 
	* type and its occurrence.</p>
	* 
	* @param expandRepeat an array containing the count of "EXPAND" commands chosen in each phase of the turn.
	* @param exploreRepeat an array containing the count of "EXPLORE" commands chosen in each phase of the turn.
	* @param exterminateRepeat an array containing the count of "EXTERMINATE" commands chosen in each phase of the turn.
	* @return a list of integers representing the turn order of players. Each integer corresponds to a player's index 
	*         in the {@code playerList}. The order is determined by the players' command card selections.
	*/
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
	

	/**
	* Generates the execution order of commands (EXPAND, EXPLORE, EXTERMINATE) for a turn, 
	* based on the number of times each command was selected in each phase of the turn.
	* 
	* <p>The method processes the counts of command card selections and returns a list 
	* where each element represents a specific command. The commands are encoded as follows:</p>
	* <ul>
	*   <li>{@code 0} - Represents the "EXPAND" command.</li>
	*   <li>{@code 1} - Represents the "EXPLORE" command.</li>
	*   <li>{@code 2} - Represents the "EXTERMINATE" command.</li>
	* </ul>
	* 
	* @param expandRepeat an array containing the count of "EXPAND" commands chosen in each phase of the turn.
	* @param exploreRepeat an array containing the count of "EXPLORE" commands chosen in each phase of the turn.
	* @param exterminateRepeat an array containing the count of "EXTERMINATE" commands chosen in each phase of the turn.
	* @return a list of integers representing the execution order of commands for the turn. Each integer corresponds 
	*         to a command type, encoded as described above.
	*/
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
	

	/**
	* Finds and returns the neighboring hexes of a given hex within the same sector.
	* 
	* <p>This method retrieves all hexes in the sector where the provided hex is located.
	* It identifies the index of the given hex and displays the neighboring hexes within the sector.</p>
	* 
	* @param hex the {@code Hex} object for which neighboring hexes are to be found.
	*            This hex must belong to a sector on the game map.
	* @return a list of {@code Hex} objects representing all hexes in the same sector as the given hex.
	* 
	* @see Hex
	* @see Sector
	* @see Map
	*/
	public List<Hex> findNeighbours(Hex hex) {

        // Get the sector ID as well as the sector of the hex
        int sectorID = hex.getSectorID();
        Sector hexSector = this.getMap().get(sectorID - 1);

        // Get all the hexes in the same sector
        List<Hex> neighbours = hexSector.getHexes();
        int hexID = neighbours.indexOf(hex);

        System.out.println("You are moving from sector No. " + sectorID + " from the Hex + " + hexID);
        for (int i = 0; i < neighbours.size(); i++) {
            System.out.println("You can move on the sector No. " + sectorID + " Hex No. " + i);
        }

        return neighbours;
    }
	
	/**
	* Executes the "Explore" command for the given player.
	* The player selects a sector and hex to move ships from, and then selects a destination sector and hex to explore.
	* If the player is a bot, all selections are made randomly.
	*
	* @param currentPlayer the player executing the command, which can be a human or a bot.
	* @param shipNumber the number of ships to move for exploration.
	*
	* This method:
	* <ul>
	*   <li>Identifies all sectors and hexes owned by the player that can be used for exploration.</li>
	*   <li>Allows a human player to select a sector and hex or selects them randomly for a bot.</li>
	*   <li>Adjusts the number of ships moved if there are insufficient ships in the selected hex.</li>
	*   <li>Allows the player to select a target sector and hex for exploration, or selects them randomly for a bot.</li>
	*   <li>Validates the selected destination and performs the exploration by moving the ships.</li>
	* </ul>
	*
	* Notes:
	* <ul>
	*   <li>If the player does not own any sectors or hexes suitable for exploration, the method exits early.</li>
	*   <li>If the target destination is invalid, the exploration is aborted.</li>
	* </ul>
	*/
	public void executeExplore(Player currentPlayer, int shipNumber) {
		
		this.moveFrom(currentPlayer, shipNumber);
		Random random = new Random();
	
		// Select a destination
		int targetSectorID;
		int targetHexID;
	
		if (currentPlayer instanceof BotPlayer) {
			// Select a random sector and hexagon for the bot
			targetSectorID = random.nextInt(9) + 1; // Assume that sector IDs are between 1 and 9
			targetHexID = random.nextInt(6); // Assume there are 6 hexagons per sector
			System.out.println("Bot exploring to sector " + targetSectorID + " and hex " + targetHexID);
		} else {
			// Ask the user to choose a destination
			Scanner scan = new Scanner(System.in);
			System.out.println("Please select the sector you want to move to: ");
			targetSectorID = scan.nextInt();
			System.out.println("Please select the hex you want to move to: ");
			targetHexID = scan.nextInt();
		}
	
		// Verify and execute the exploration
		Sector targetSector = map[(targetSectorID - 1) / 3][(targetSectorID - 1) % 3];
		if (targetSector == null || targetHexID < 0 || targetHexID >= targetSector.getSection().size()) {
			System.out.println("Invalid destination. Exploration aborted.");
			return;
		}
	
		targetSector.expand(targetHexID, shipNumber);
		Hex targetHex = targetSector.getSection().get(targetHexID);
		targetHex.setOwner(currentPlayer);
		System.out.println(currentPlayer.getName() + " successfully explored to sector " + targetSectorID + " and hex " + targetHexID);
	}
	
	/**
	 * This method allows the current Player to use the Expand command.
	 * <p>
	 * This method ask the player the number of ships he want to place as well as the target hex. 
	 * In case the player doesn't have enough ships to use the Expand command, he will be asked to 
	 * place a lower number of ships.
	 * <p>
	 * 
	 * @param currentPlayer: The target Player who will use the Expand command of the game.
	 * @param shipNumber: The number of ships the player wishes to add on a Hex.
	 */
	public void executeExpand(Player currentPlayer, int shipNumber) {
		Scanner scan1 = new Scanner(System.in);
    	while (shipNumber > currentPlayer.getFleetSize()) {
    		System.out.println("You have " + currentPlayer.getFleetSize() + " left, you don't have enough ships to place.....");
    		System.out.println("Please select the number of ships that you want to place: ");
    		shipNumber = scan1.nextInt();
    	}
		currentPlayer.expand(shipNumber);
	}
	
	/**
	* Executes the "Exterminate" command for the given player.
	* The player selects an opponent's sector and hex to attack, or a bot makes the selection randomly.
	* The targeted hex is then attacked using the specified number of ships.
	*
	* @param currentPlayer the player executing the command, which can be a human or a bot.
	* @param shipNumber the number of ships to use for the attack.
	*
	* This method:
	* <ul>
	*   <li>Lists all opponent-controlled sectors available for attack.</li>
	*   <li>Allows a human player to select a sector and hex or randomly selects them for a bot.</li>
	*   <li>Validates the player's choices to ensure they are valid.</li>
	*   <li>Processes the attack on the selected hex using the specified number of ships.</li>
	* </ul>
	*
	* Notes:
	* <ul>
	*   <li>If no opponent sectors are available, the method exits without performing any action.</li>
	*   <li>If no hexes in the targeted sector are available, the method also exits.</li>
	* </ul>
	*/
	public void executeExterminate(Player currentPlayer, int shipNumber) {
		System.out.println(currentPlayer.getName() + " is about to use the Exterminate command...");
	
		this.moveFrom(currentPlayer, shipNumber);
		
		Random random = new Random();
		int indexOfPlayer = playerList.indexOf(currentPlayer);
	
		// List of sectors owned by opponents
		List<Sector> playerSectors = new ArrayList<>();
		List<Integer> playerSectorIDs = new ArrayList<>();
	
		for (int i = 0; i < playerList.size(); i++) {
			if (i != indexOfPlayer) {
				List<Sector> opponentSectors = playerList.get(i).getOwnedSector();
				for (Sector sector : opponentSectors) {
					System.out.println("The sector " + sector.getSectorID() + " is owned by " + playerList.get(i).getName());
					playerSectors.add(sector);
					playerSectorIDs.add(sector.getSectorID());
				}
			}
		}
	
		if (playerSectors.isEmpty()) {
			System.out.println("No sectors available to attack. Aborting.");
			return;
		}
	
		final int targetSectorID;
		final Sector targetSector;
	
		if (currentPlayer instanceof BotPlayer) {
			// Randomly select a sector for the bot
			targetSectorID = playerSectorIDs.get(random.nextInt(playerSectorIDs.size()));
			targetSector = playerSectors.stream()
					.filter(sector -> sector.getSectorID() == targetSectorID)
					.findFirst()
					.orElse(null);
			System.out.println("Bot selected sector " + targetSectorID + " to attack.");
		} else {
			// Ask the user to choose a sector
			Scanner scan = new Scanner(System.in);
			int userSelectedSectorID = -1;
			do {
				System.out.println("Please select the sector you want to attack: ");
				userSelectedSectorID = scan.nextInt();
			} while (!playerSectorIDs.contains(userSelectedSectorID));
	
			targetSectorID = userSelectedSectorID;
			targetSector = playerSectors.stream()
					.filter(sector -> sector.getSectorID() == targetSectorID)
					.findFirst()
					.orElse(null);
		}
	
		if (targetSector == null) {
			System.out.println("Invalid sector selected. Aborting.");
			return;
		}
	
		// Available hexagons in the target sector
		List<Hex> availableHexes = targetSector.getSection();
		List<Integer> hexIndexes = new ArrayList<>();
	
		for (int i = 0; i < availableHexes.size(); i++) {
			if (availableHexes.get(i).getAvailability()) {
				System.out.println("You can attack the " + i + "th Hex of level " + availableHexes.get(i).getSystemLevel());
				hexIndexes.add(i);
			}
		}
	
		if (hexIndexes.isEmpty()) {
			System.out.println("No hexes available to attack in the selected sector.");
			return;
		}
	
		final int targetHexIndex;
	
		if (currentPlayer instanceof BotPlayer) {
			// Randomly select a hex for the bot
			targetHexIndex = hexIndexes.get(random.nextInt(hexIndexes.size()));
			System.out.println("Bot selected hex " + targetHexIndex + " to attack.");
		} else {
			// Ask the user to choose a hex
			Scanner scan = new Scanner(System.in);
			int userSelectedHexIndex = -1;
			do {
				System.out.println("Please select the Hex you want to attack: ");
				userSelectedHexIndex = scan.nextInt();
			} while (!hexIndexes.contains(userSelectedHexIndex));
	
			targetHexIndex = userSelectedHexIndex;
		}
	
		// Attack the selected hex
		Hex targetHex = availableHexes.get(targetHexIndex);
		targetHex.isAttached(shipNumber, currentPlayer);
	
		System.out.println(currentPlayer.getName() + " successfully attacked Hex " + targetHexIndex + " in sector " + targetSectorID + ".");
	}
	
	/**
	 * Allows the player to move ships from a sector and hex, either through user input or randomly for a bot player.
	 * <p>
	 * The method first prompts the player (or selects randomly for a bot) to choose a sector they own. Then, it allows the 
	 * player (or bot) to choose an available hex within the selected sector. The method verifies if the selected sector and hex 
	 * are valid and ensures that the number of ships being moved is not greater than the available fleet. Finally, it updates 
	 * the fleet count of the selected hex accordingly.
	 * </p>
	 *
	 * @param currentPlayer The player whose ships are being moved. Can be a human player or a bot.
	 * @param shipNumber The number of ships the player wants to move.
	 * @throws IllegalArgumentException If the selected sector or hex is invalid.
	 */
	public void moveFrom(Player currentPlayer, int shipNumber) {
		
		Random random = new Random();
	
		// Attack the selected hex
		List<Integer> sectorIDs = new ArrayList<>();
		for (Sector sector : currentPlayer.getOwnedSector()) {
			sectorIDs.add(sector.getSectorID());
		}
	
		if (sectorIDs.isEmpty()) {
			System.out.println("No sectors available to explore from.");
			return;
		}
	
		Sector sector;
	
		if (currentPlayer instanceof BotPlayer) {
			// Randomly select a sector for the bot
			int selectedSector = sectorIDs.get(random.nextInt(sectorIDs.size()));
			// Loop through the sectors to find the one with the matching ID
			sector = null;
			for (Sector s : currentPlayer.getOwnedSector()) {
				if (s.getSectorID() == selectedSector) {
					sector = s;
					break; // Stop the loop as soon as the sector is found
				}
			}
			System.out.println("Bot selected sector " + selectedSector);
		} else {
			// Ask the user to choose a sector
			Scanner scan = new Scanner(System.in);
			int selectedSector;
			do {
				System.out.println("Please select a sector you want to move your ships from: ");
				selectedSector = scan.nextInt();
			} while (!sectorIDs.contains(selectedSector));
			
			// Make the selected variable final
			final int userSelectedSector = selectedSector;

			// Find the corresponding sector
			sector = null;
			for (Sector s : currentPlayer.getOwnedSector()) {
				if (s.getSectorID() == userSelectedSector) {
					sector = s;
					break; // Stop the search as soon as the sector is found
				}
			}
		}
	
		if (sector == null) {
			System.out.println("Invalid sector. Aborting.");
			return;
		}
	
		// List the available hexes in the sector
		List<Integer> availableHexes = new ArrayList<>();
		for (int i = 0; i < sector.getSection().size(); i++) {
			if (sector.getSection().get(i).getAvailability()) {
				availableHexes.add(i);
			}
		}
	
		if (availableHexes.isEmpty()) {
			System.out.println("No available hexes to explore from.");
			return;
		}
	
		int selectedHex;
	
		if (currentPlayer instanceof BotPlayer) {
			// Randomly select a hex for the bot
			selectedHex = availableHexes.get(random.nextInt(availableHexes.size()));
			System.out.println("Bot selected hex " + selectedHex);
		} else {
			// Ask the user to choose a hex
			Scanner scan = new Scanner(System.in);
			do {
				System.out.println("Please select the hex you want to move your ships from: ");
				selectedHex = scan.nextInt();
			} while (!availableHexes.contains(selectedHex));
		}
	
		Hex hex = sector.getSection().get(selectedHex);
		if (hex.getFleet() - shipNumber < 0) {
			shipNumber = hex.getFleet(); // Adjust the number of ships
			System.out.println("Not enough ships. Moving " + shipNumber + " ships instead.");
		}
		hex.setFleet(-shipNumber);
	}
	

	/**
	* Constructs the game map by initializing a 3x3 grid of {@code Sector} objects.
	* 
	* <p>This method creates a total of 9 sectors, each with a unique sector ID. 
	* The center sector contains a single level-3 system, while other sectors are populated 
	* with a specified number of hexes, each assigned a randomly shuffled system level 
	* according to predefined rules. The map is stored as a 2D array of {@code Sector} objects.</p>
	* 
	* <p>Details of the sector layout:</p>
	* <ul>
	*   <li>The center sector (middle of the grid) contains a single hex with system level 3.</li>
	*   <li>Sectors in the middle column of the grid contain 5 hexes with levels distributed as: 
	*       1, 1, 2, 0, 0.</li>
	*   <li>All other sectors contain 6 hexes with levels distributed as: 1, 1, 1, 2, 0, 0.</li>
	* </ul>
	* 
	* @see Sector
	* @see Hex
	*/
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
					// Determine if the sector is in the middle column
					boolean isMiddleColumn = (column == map[row].length / 2);
	
					// Add system levels based on the rules
					if (isMiddleColumn) {
						// Sectors with 5 hexagons
						Collections.addAll(hexLevel, 1, 1, 2, 0, 0);
					} else {
						// Sectors with 6 hexagons
						Collections.addAll(hexLevel, 1, 1, 1, 2, 0, 0);
					}
					Collections.shuffle(hexLevel);
	
					// Number of hexagons to create
					int hexCount = isMiddleColumn ? 5 : 6;
					for (int i = 0; i < hexCount; i++) {
						int systemLevel = hexLevel.get(i);
						Hex hex = new Hex(sectorID, systemLevel);
						hexes.add(hex);
					}
				}
				// Assign the sector to one part of the map
				map[row][column] = new Sector(sectorID, hexes);
				sectorID++;
			}
		}
	}
	

	/**
	* Displays the game map in a structured and readable format.
	* 
	* <p>This method prints the map details, including sector IDs, hex information, and 
	* ownership details for each hex. The map is displayed row by row, with each sector's 
	* hexagons and their respective details shown below the sector ID.</p>
	* 
	* <p>Details displayed include:</p>
	* <ul>
	*   <li><strong>Sector IDs:</strong> Displayed as a single row for all sectors.</li>
	*   <li><strong>Hex details:</strong> Each hexagon's system level and fleet information 
	*       are shown in rows below the sector ID.</li>
	*   <li><strong>Ownership:</strong> Displays the owner of each hex (if any) in a dedicated row.</li>
	* </ul>
	* 
	* <p>Empty spaces are added for alignment when sectors have different numbers of hexagons.</p>
	* 
	* <p>This method assumes the following structure for the map:</p>
	* <ul>
	*   <li>The map is a 2D array of {@code Sector} objects.</li>
	*   <li>Each sector contains a list of {@code Hex} objects, which store the system level, 
	*       fleet information, and ownership details.</li>
	* </ul>
	* 
	* <p><strong>Output Example:</strong></p>
	* <pre>
	* Map:
	* Sector ID: 1       Sector ID: 2       Sector ID: 3       
	*   Hex Level: 1, Fleet: 2              Hex Level: 0, Fleet: 0              Hex Level: 2, Fleet: 1   
	*     Owner: Player1                      Owner: None                          Owner: Player2         
	* ...
	* </pre>
	* 
	* @see Sector
	* @see Hex
	*/
	public void displayMap() {
		System.out.println("Map:");
	
		// Display the sector IDs in a single line
		for (Sector[] row : map) {
			for (Sector sector : row) {
				System.out.print("Sector ID: " + sector.getSectorID() + "   			");
			}
			System.out.println(); // Move to the next line after the sector IDs
	
			// Find the maximum number of hexagons in a sector in the row
			int maxHexCount = 0;
			for (Sector sector : row) {
				maxHexCount = Math.max(maxHexCount, sector.getSection().size());
			}
	
			// Display the hexagons row by row
			for (int hexIndex = 0; hexIndex < maxHexCount; hexIndex++) {
				for (Sector sector : row) {
					if (hexIndex < sector.getSection().size()) {
						Hex hex = sector.getSection().get(hexIndex);
						// Display hexagon information
						System.out.print("  Hex Level: " + hex.getSystemLevel() + ", Fleet: " + hex.getFleet());
					} else {
						// If the sector has fewer hexagons than the current row, display empty space
						System.out.print("                        ");
					}
					System.out.print("   "); // Space between sectors
				}
				System.out.println(); // Move to the next line after a row of hexagons
	
				// Display the owners of the hexagons in a dedicated line
				for (Sector sector : row) {
					if (hexIndex < sector.getSection().size()) {
						Hex hex = sector.getSection().get(hexIndex);
						if (hex.getOwner() != null) {
							System.out.print("    Owner: " + hex.getOwner().getName() + "     ");
						} else {
							System.out.print("                         "); // Spacing for alignment
						}
					} else {
						System.out.print("                         "); // Spacing for alignment
					}
					System.out.print("   "); // Space between sectors
				}
				System.out.println(); // Move to the next line after a row of owners
			}
	
			System.out.println(); // Empty line between sector groups
		}
	}
	
	/**
	 * Returns all the Sectors of the map as a list
	 * @return List of the sectors in the map
	 */
	public List<Sector> getMap() {
        List<Sector> sectorList = new ArrayList<>();
        
        // Parcourir le tableau bidimensionnel pour ajouter chaque secteur à la liste
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                sectorList.add(map[i][j]); // add each sector to the list
            }
        }
        
        return sectorList;
    }
	
	/**
	 * Returns all the unoccupied sectors in the Map of the Game.
	 * @return a set of integers representing all the free sector's ID
	 */
	public Set<Integer> availableSectors() {
		Set<Integer> Sectors = new HashSet<Integer>();
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				int sectorID = map[row][col].getFreeSectorID();
				Sectors.add(sectorID);
			}
		}
		return Sectors;
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
	public static void main(String[] args) throws InterruptedException {
		Game PocketImperium = new Game();
		PocketImperium.startGame();
	}

}