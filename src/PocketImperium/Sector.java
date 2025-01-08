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
				System.out.println("No." + i + " section is a system Level of " + 
						this.section.get(i).getSystemLevel() + ", and it has space");
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

	public String getOwnerName() {
		if (this.owner != null) {
			return this.owner.getName();
		}
		return null; // Si le secteur n'a pas de propriétaire
	}
	

    public Hex getHex(int hexesID) {
		if (hexesID < 0 || hexesID >= section.size()) {
			throw new IllegalArgumentException("Invalid hexesID: " + hexesID);
		}
		return section.get(hexesID);
	}

	public Player getOwner() {
        return this.owner;
    }

    public List<Hex> getHexes() {
        return new ArrayList<>(this.section);
    }
	
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
