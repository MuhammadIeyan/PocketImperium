package PocketImperium;

import java.io.Serializable;

public class Hex implements Serializable {
	private int sectorID;
	private int systemLevel;
	private int fleet;
	private Player fleetOwner;
	private boolean isOccupied;
		
	public Hex(int sectorID, int systemLevel) {
		this.sectorID = sectorID;
		this.systemLevel = systemLevel;
		isOccupied = false;
		this.fleetOwner = null;
	}
	
	public int getSystemLevel() {
		return this.systemLevel;
	}

	public int getSectorID() {
		return this.sectorID;
	}
	
	// Sends to the game the number of fleet present on the territory
	public int getFleet() {
		return this.fleet;
	}

	public Player getOwner() {
		return this.fleetOwner;
	}
	public void setOwner(Player owner) {
		this.fleetOwner = owner;
    }
	
	public boolean getAvailability() {
		if (this.fleet != 0) {
			this.isOccupied = true;
		}
		else {
			this.isOccupied = false;
		}
		return this.isOccupied;
	}

	public int fleetAvailablity() {
		if (this.fleet < this.systemLevel + 1) {
			return this.systemLevel + 1 - this.fleet;
		}
		else {
			return 0;
		}
		
	}
	
	public void setFleet(int fleetEntering) {
		this.fleet += fleetEntering;
		if (this.fleet > 0) {
			this.isOccupied = true;
		}
	}
	
	// Will return the extra number of fleets so that it can go back to the player
	public int extraFleet() {
		// Set the fleet number to be at max the systemLevel
		if (this.fleet > systemLevel + 1) {
			int extraFleet = this.fleet - (systemLevel + 1);
			this.fleet = systemLevel + 1;
			return extraFleet;
		}
		return 0;
	}
}
