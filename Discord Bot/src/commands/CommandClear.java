package commands;

import java.util.List;

import framework.CommandConfirmed;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.exceptions.PermissionException;

/**
 * 
 * @author Stephano
 *
 *         Classe qui permet d'�ffacer tout les messages dans le chat ou on
 *         l'invoque
 *
 */
public class CommandClear extends CommandConfirmed {
	
	public CommandClear(){
		super("Are you sure you want to clear all messages?");
	}
	
	@Override
	public void confirmed(){
		
		try{
			
			fullClean();
			
		}
		catch(PermissionException e){
			sendMessage("I do not have the permissions for that!");
		}
		
	}
	
	private void fullClean() throws PermissionException{
		
		boolean vide = false;
		MessageHistory history = getTextContext().getHistory();
		do{
			
			List<Message> messages = history.retrievePast(100).complete();
			
			if(!messages.isEmpty()){
				
				getTextContext().deleteMessages(messages).complete();
				
			}
			else{
				vide = true;
			}
			
		}while(!vide);
		
	}
	
}
