package PocketImperium;

import java.io.Serializable;
import java.util.List;

public class BotPlayer extends Player implements Serializable {
    public enum Strategy {
        AGGRESSIVE, DEFENSIVE, RANDOM
    }

    private Strategy strategy;

    public BotPlayer(String name, String color, Strategy strategy) {
        super(name, color);
        this.strategy = strategy;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<CommandCard> generateStrategy() {
        // Implémentation de la stratégie du bot selon son type
        // Par exemple, une stratégie RANDOM pourrait choisir des CommandCards aléatoires
        return List.of(new CommandCard(CommandCard.Command.EXPAND)); // Exemple basique
    }
    
    @Override
    public void plan() {
        System.out.println(getName() + " planifie ses actions avec une stratégie " + strategy + "...");
        // Implémente la logique de chaque stratégie ici
    }
}