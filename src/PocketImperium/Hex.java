package PocketImperium;

import java.io.Serializable;

/**
 * This class helps to define the hex in the Pocket Imperium Game. A hex is a sector with a
 * specific system level, fleet count, and an owner. Its information is maintained whether
 * it's occupied or not
 */
public class Hex implements Serializable {
	private int sectorID;
	private int systemLevel;
	private int fleet;
	private Player fleetOwner;
	private boolean isOccupied;
	
	/**
	 * Builds a Hex object with the sector ID and its system level
	 * @param sectorID The unique ID of the sector.
	 * @param systemLevel The level of the system in the sector.
	 */
	public Hex(int sectorID, int systemLevel) {
		this.sectorID = sectorID;
		this.systemLevel = systemLevel;
		isOccupied = false;
		this.fleetOwner = null;
	}
	
	/**
	 * Gets the system level of the hex.
	 * 
	 * @return The system level of the hex.
	 */
	public int getSystemLevel() {
		return this.systemLevel;
	}

	/**
	 * Gets the sector ID of the hex.
	 * 
	 * @return The sector ID of the hex.
	 */
	public int getSectorID() {
		return this.sectorID;
	}
	
	/**
	 * Gets the number of ships on the hex.
	 * @return An integer representing the number of ships on the hex.
	 */
	public int getFleet() {
		return this.fleet;
	}

	/**
	 * Gets the Player who owns the hex.
	 * @return The Player who owns the hex.
	 */
	public Player getOwner() {
		return this.fleetOwner;
	}
	
	/**
	 * Sets the owner of the Hex.
	 * @param owner The Player who is the owner of the Hex.
	 */
	public void setOwner(Player owner) {
		this.fleetOwner = owner;
    }
	
	/**
	 * Checks whether or not the Hex is free.
	 * @return True if the hex is occupied, else False.
	 */
	public boolean getAvailability() {
		if (this.fleet != 0) {
			this.isOccupied = true;
		}
		else {
			this.isOccupied = false;
		}
		return this.isOccupied;
	}

	/**
	 * Calculates the number of fleets that can still be added to the hex.
	 * 
	 * @return The number of ships that the hex can store in extra.
	 */
	public int fleetAvailablity() {
		if (this.fleet < this.systemLevel + 1) {
			return this.systemLevel + 1 - this.fleet;
		}
		else {
			return 0;
		}
		
	}
	
	/**
	 * Adds the number of ships to the hex.
	 * @param fleetEntering The number of ships entering the hex.
	 */
	public void setFleet(int fleetEntering) {
		this.fleet += fleetEntering;
		if (this.fleet > 0) {
			this.isOccupied = true;
		}
	}
	
	/**
	 * Calculates the number of ships that are in extra on the Hex and returns them to the Player.
	 * @return The number of extra fleets that exceed the capacity.
	 */
	public int extraFleet() {
		// Set the fleet number to be at max the systemLevel
		if (this.fleet > systemLevel + 1) {
			int extraFleet = this.fleet - (systemLevel + 1);
			this.fleet = systemLevel + 1;
			return extraFleet;
		}
		return 0;
	}
	
	/**
	 * Updates the Hex status based on an attack made by a Player. It will update who is the owner 
	 * and check how many ships are left after the attack.
	 * @param fleetEntering The number of ships the opponent is attacking with.
	 * @param attacker The player who is attacking.
	 */
	public void isAttached(int fleetEntering, Player attacker) {
		if (this.fleet < fleetEntering) {
			this.fleet = fleetEntering - this.fleet;
			this.fleetOwner = attacker;
		}
		else {
			this.fleet = this.fleet - fleetEntering;
			this.updateOwner();
		}
	}
	
	/**
	 * Updates the owner of the Hex.
	 */
	public void updateOwner() {
		if(this.fleet == 0) {
			this.fleetOwner = null;
		}
	}
}


