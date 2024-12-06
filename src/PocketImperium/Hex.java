package PocketImperium;

public class Hex{
	private int sectorID;
	private int systemLevel;
	private int fleet;
	
	public Hex(int sectorID, int systemLevel) {
		this.sectorID = sectorID;
		this.systemLevel = systemLevel;
	}
	
	public int getSystemLevel() {
		return this.systemLevel;
	}
	
	// Sends to the game the number of fleet present on the territory
	public int getFleet() {
		return this.fleet;
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
		// Set the fleet number to be at max the systemLevel
		if(this.fleet + fleetEntering <= systemLevel + 1) {
			this.fleet += fleetEntering;
		}
		
		else {
			this.fleet += systemLevel + 1;
		}
	}
}
