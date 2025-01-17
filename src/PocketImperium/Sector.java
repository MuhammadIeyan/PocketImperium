package PocketImperium;

import java.io.Serializable;
import java.util.*;

public class Sector implements Serializable {
	private int sectorID;
	private List<Hex> section;
	private Player owner;
	
	/**
	 * This class represents a sector in the map of Pocket Imperium
	 * <p>
	 * Section is represented threw its specific sector ID, the hexes that compose it, 
	 * as well as the Player who owns it (if there is one).
	 * <p>
	 * @param sectorID An integer representing the Sectors ID
	 * @param section A list of hexes to represent the hexes that it composes
	 */
	public Sector(int sectorID, List<Hex> section) {
		this.sectorID = sectorID;
		this.section = section;
		this.owner = null; // Initially the sector doesn't have an owner
	}
	
	/**
	 * Returns the Sectors' ID
	 * @return an integer representing the sectors' ID
	 */
	public int getSectorID() {
		return this.sectorID;
	}
	
	/**
	 * Returns all the hexes that compose the Sector
	 * @return a list of hex that compose the Sector
	 */
	public List<Hex> getSection() {
		return this.section;
	}
	
	/**
	 * Returns a specific Hexes system level as stated in the Game (level 1, 2, and 3)
	 * @param hexesID the specific hexes ID to refer to it
	 * @return an integer that represents the system level
	 */
	public int getSystemLevel(int hexesID) {
		return this.section.get(hexesID).getSystemLevel();
	}
	
	/**
	 * Display all the available hexes that are free in the Sector
	 */
	public void availableSection() {
		for(int i = 0; i < section.size(); i++) {
			if(this.section.get(i).fleetAvailablity() > 0) {
				System.out.println("No." + i + " section is a system Level of " + 
						this.section.get(i).getSystemLevel() + ", and it has space");
			}
		}
	}
	
	/**
	 * Allows the game to inform the Sector that a Player is expanding here
	 * @param systemID: the target Hex that the Player has selected
	 * @param fleet: the number of ships the Player has decided to use
	 */
	public void expand(int systemID, int fleet) {
		this.section.get(systemID).setFleet(fleet);
	}
	
	/**
	 * Returns whether or not the Sector has an owner or not
	 * @return a boolean representing if the sector has an owner
	 */
	public boolean hasOwner() {
		if(this.owner == null) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Displays to the screen that this sector is free, as well as its sector ID
	 */
	public void displayFreeSector() {
		if(!hasOwner()) {
			System.out.println(this.getSectorID() + " sector is free");
		}
	}
	
	/**
	 * Returns the sectors' ID if it's free
	 * @return
	 */
	public int getFreeSectorID() {
		if(!hasOwner()) {
			return this.sectorID;
		}
		return -1; // We will treat -1 as our false value
	}
	
	/**
	 * Calculates the total number of points a Player will earn from this sector
	 * @return an integer representing the sector score of the PLayer
	 */
	public int sectorPoints() {
		int points = 0;
		Iterator<Hex> iterator = section.iterator();
		while(iterator.hasNext()) {
			points = iterator.next().getFleet();
		}
		return points;
	}
	
	/**
	 * Sets a player as the owner of the sector
	 */
	public void setOwner(Player player) {
		this.owner = player;
	}

	/**
	 * Returns the owners' name
	 * @return a string representing the player owners' name
	 */
	public String getOwnerName() {
		if (this.owner != null) {
			return this.owner.getName();
		}
		return null; // Si le secteur n'a pas de propriétaire
	}
	
	/**
	 * Returns a Hex based on the specified hex ID
	 * @param hexesID: integer representing the hex ID in the sector
	 */
    public Hex getHex(int hexesID) {
		if (hexesID < 0 || hexesID >= section.size()) {
			throw new IllegalArgumentException("Invalid hexesID: " + hexesID);
		}
		return section.get(hexesID);
	}

    /**
     * Returns the owner of the sector
     * @return a player who is the owner of this sector
     */
	public Player getOwner() {
        return this.owner;
    }

	/**
	 * Returns a list of hexes that compose the sector
	 */
    public List<Hex> getHexes() {
        return new ArrayList<>(this.section);
    }
	
    /**
     * Returns any random hex with a specified level from the Sector
     * @param level an integer representing the system level we want
     * @return a hex 
     */
	public int getRandomHexWithLevel(int level) {
		List<Hex> availableHexes = new ArrayList<>();
	
		// Filtrer les hexagones disponibles avec le niveau spécifié
		for (Hex hex : this.section) {
			if (hex.getSystemLevel() == level && !hex.getAvailability()) {
				availableHexes.add(hex);
			}
		}
	
		// Choisir un hexagone aléatoire parmi les hexagones disponibles
		if (!availableHexes.isEmpty()) {
			Random random = new Random();
			Hex chosenHex = availableHexes.get(random.nextInt(availableHexes.size()));
			return this.section.indexOf(chosenHex); // Retourne l'indice de l'hexagone choisi
		}
	
		return -1; // Si aucun hexagone n'est disponible avec le niveau spécifié
	}
	
}
