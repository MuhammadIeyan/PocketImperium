package PocketImperium;

import java.util.*;

public class Sector {
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
	
	// Check if the sector has an owner
	public boolean hasOwner() {
		if(this.owner == null) {
			return false;
		}
		
		return true;
	}
	
	public int sectorPoints() {
		int points = 0;
		Iterator<Hex> iterator = section.iterator();
		while(iterator.hasNext()) {
			points = iterator.next().getFleet();
		}
		return points;
	}
	
	
}
