package PocketImperium;

import java.util.*;

public class Player {
	private String name;
	private String color;
	private int point;
	private int ships;
	private List<CommandCard> command;
	
	public Player(String name, String color, int ships) {
		this.name = name;
		this.color = color;
		this.point = 0;
		this.ships = ships;
		this.command = new ArrayList();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setCommand(int i) {
		if(i==1) {
			command.add(CommandCard.Expand);
		}
		else if(i==2) {
			command.add(CommandCard.Explore);
		}
		else {
			command.add(CommandCard.Exterminate);
		}
	}
	
	public void resetCommand() {
		command.clear();
	}
	
	public List<String> getCommand() {
		List<String> moveSet = new ArrayList();
		Iterator<CommandCard> commandIterator = command.iterator();
		while(commandIterator.hasNext()) {
			moveSet.add(commandIterator.next().toString());
		}
		return moveSet;
	}
	
	public void perform() {
		
	}
}
