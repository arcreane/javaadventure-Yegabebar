package com.company;

import com.company.environment.Dungeon;
import com.company.environment.Room;
import com.company.miscellaneous.Events;
import com.company.miscellaneous.MonsterType;
import com.company.liveEntities.Player;
import com.company.miscellaneous.Stats;
import com.company.miscellaneous.WeaponType;

public class Game {

    public static void startGame(){
        //We instanciate the Player character and the dungeon
        Player hero = new Player(Stats.hpPlayer, Stats.atkPlayer);
        System.out.println("Welcome to Coding Dungeon");
        System.out.println("");
        Dungeon dungeon = new Dungeon(Stats.nbRooms);

        //Main loop, used to pass from room to room
        for(int i= 0; i<dungeon.room.length; i++){
            int flaskBonus=0;
            //Shortened notation of the current room when we iterate on the for loop
            Room room = dungeon.room[i];
            System.out.println("⍑⍑⍑⍑ ROOM "+(i+1)+" ⍑⍑⍑⍑");
            //Room generation inside the current dungeon slot, generates the monster as well
            room=new Room();
            //The combat happens here, while the monster is not dead
            while(room.monster.getHp()>0){
                int dmgPlayerAttack=0;
                int dmgMonsterAttack=0;

                System.out.println("==== NEW TURN ===");
                //If the monster is not Ko, (managed by a boolean in another class)
                if(Events.monsterKo==false){
                    //Set the damage value for the monster attack
                    dmgMonsterAttack = room.monster.attack();
                    //Substract the player health with the monster damage, then display the result
                    hero.setHp(hero.getHp()-dmgMonsterAttack);
                    if(Events.monsterKo==false){System.out.println("You have lost "+dmgMonsterAttack+" hp");}

                }else{
                    //Else if the monster is Ko, we reset the monsterKo state for the next turn
                    Events.monsterKo=false;
                    System.out.println("The Barbarian starts to recover");
                }

                //Check the monster type, in order to know which weapon the player will have to use
                if(room.monster.MType.MName.equals("Barbarian")){
                    hero.setWeaponType(WeaponType.SWORD);
                }else{
                    hero.setWeaponType(WeaponType.WATER_FLASK);
                    //If it's a sorcerer we try to get the player knocked out and store the result in a boolean for later use
                    Events.playerKo = Events.eventRandomizer(Stats.sorcererEventRate);
                }

                if(hero.getHp()<1){ //If the player health is down to zero the player looses and gets brought back to the main menu
                    System.out.println("");
                    System.out.println("=== GAME OVER ===");
                    System.out.println("You died with a lot of suffering");
                    return;
                }

                System.out.println("You have only "+hero.getHp()+" hp remaining"); //If the player is not dead we display the remaining HP

                if(Events.playerKo){ //Checks if the monster knocks down the player:if true, skip the rest of the turn
                    System.out.println("The monster knocked you down for one turn");
                    System.out.println("");
                    continue;
                }

                //If the player is not dead yet, the player is asked for input
                System.out.println("Type "+hero.WType.WName +" to fight back");
                String playerAction = Main.getPlayerInput();
                //Check if the input is a correct one+ if the weapon matches the monster type
                boolean hit = manageInput(playerAction, room.monster.MType);
                //If it's the case, the monster will be hit
                if(hit){
                    System.out.println("====== HIT ======");
                    //Set the damage value for the player attack, and then apply it on the monster's life
                    dmgPlayerAttack = hero.attack(hero.WType.WName, flaskBonus);
                    room.monster.setHp((room.monster.getHp()-dmgPlayerAttack));
                    //This function has two goals: initialize the flask bonus and attempt to get the knockout effect for the barbarian
                    flaskBonus = initializePlayerEvents(hero, flaskBonus);
                    //Displays how many HP the monster has lost, and then display the monster(s status accordingly
                    System.out.println("The "+room.monster.MType.MName +" has lost "+dmgPlayerAttack+" hp");
                    if(room.monster.getHp()<=0){
                        //If the monster is dead and the room is the last one, the player wins.
                        if(i==Stats.nbRooms-1){
                            System.out.println("Congratulations, the treasure is yours!");
                            System.out.println("");
                            return;
                        }
                        System.out.println("Congrats, the "+room.monster.MType.MName +" is dead, you can open the next door");
                        System.out.println("=================");
                    }else{
                        System.out.println("He has only "+room.monster.getHp()+" hp remaining");
                    }
                }else{System.out.println("You miss!");} //Else if not hit, the attacked missed.
                System.out.println(""); //Skip next line
            }
            //We reset the variable monsterKo as we don't want to set Ko the monster from the next room
            Events.monsterKo=false;
        }
    }

    private static int initializePlayerEvents(Player hero, int flaskBonus) {
        //If the weapon is the flask, we initiliaze the flask bonus
        if(hero.WType.WName.equals("Water_Flask")){
            //Displays text depending if whether the bonus has already been initialized or not
            if(flaskBonus ==0){
                System.out.println("Flask Bonus has been reseted");
            }else{
                System.out.println("Another flask hits the sorcerer adding water to the pool at his feet,"
                        +" your damage is now "+ (hero.getAtk()+flaskBonus));
            }
            //Then we add +2 to the bonus score for the next turn (next while loop)
            flaskBonus +=2;
        }else{ //else if the weapon is the sword, we try to knock out the monster
            //If the player event kicks in (monster knocked out)
            if(Events.eventRandomizer(Stats.playerEventRate)){
                Events.monsterKo=true; //We set the variable monsterKo to true for the next turn
                System.out.println("The Barbarian is knocked out for one turn after you hit it on its head");
            }
        }
        return flaskBonus;
    }

    private static boolean manageInput(String playerAction, MonsterType monsterType){
        boolean hit = false;
        //If the player input matches the type of the monster, the boolean "hit" is set to true
        if(playerAction.equals("Sword")&&monsterType.MName.equals("Barbarian")){
            hit = true;
        }else if(playerAction.equals("Water_Flask")&&monsterType.MName.equals("Sorcerer")){
            hit = true;
        }
        return hit;
    }
}
