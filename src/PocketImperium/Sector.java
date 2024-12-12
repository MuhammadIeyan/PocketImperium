package PocketImperium;

import java.io.Serializable;
import java.util.*;

public class Sector implements Serializable {
	private int sectorID;
	private List<Hex> section;
	private Player owner;
	
	public Sector(int sectorID, List<Hex> section) {
		this.sectorID = sectorID;
		this.section = section;
		this.owner = null; // Initially the sector doesn't have an owner
	}
	
	public int getSectorId() {
		return this.sectorID;
	}
	
	public List<Hex> getSection() {
		return this.section;
	}
	
	public int getSystemLevel(int hexesID) {
		return this.section.get(hexesID).getSystemLevel();
	}
	
	// Display all the available sections, or hexes that are free in this sector
	public void availableSection() {
		for(int i = 0; i < section.size(); i++) {
			if(this.section.get(i).fleetAvailablity() > 0) {
				System.out.println("No." + i + " section has space. It is a system Level of " + 
						this.section.get(i).getSystemLevel() + ", and it has space for " + 
						this.section.get(i).fleetAvailablity() + " ships.");
			}
		}
	}
	
	// Expand on the sector that you possess
	public void expand(int systemID, int fleet) {
		this.section.get(systemID).setFleet(fleet);
	}
	
	// Check if the sector has an owner
	public boolean hasOwner() {
		if(this.owner == null) {
			return false;
		}
		
		return true;
	}
	
	public void displayFreeSector() {
		if(!hasOwner()) {
			System.out.println(this.getSectorId() + " sector is free");
		}
	}
	
	public int getFreeSectorID() {
		if(!hasOwner()) {
			return this.sectorID;
		}
		return -1; // We will treat -1 as our false value
	}
	
	public int sectorPoints() {
		int points = 0;
		Iterator<Hex> iterator = section.iterator();
		while(iterator.hasNext()) {
			points = iterator.next().getFleet();
		}
		return points;
	}
	
	public void setOwner(Player player) {
		this.owner = player;
	}
	
	
}
