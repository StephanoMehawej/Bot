package utilities.abstracts;

import utilities.interfaces.Commands;
import utilities.interfaces.Resources;
import vendor.abstracts.AbstractTextCommand;

/**
 * Simple class that implements the logic of
 * {@link vendor.abstracts.AbstractTextCommand AbstractTextCommand} so that a
 * simple message can be sent via the private channel of the user that used this
 * command.
 *
 * @version 1.0
 * @since v0.4.0
 * @author V-ed (Guillaume Marcoux)
 */
public abstract class SimplePrivateTextCommand extends AbstractTextCommand
		implements Commands, Resources {
	
	@Override
	protected void sendMessageMethod(String textToSend, TextType textType){
		
		switch(textType){
		case SIMPLE:
			sendPrivateMessage(textToSend);
			break;
		case INFO_LINE:
			sendInfoPrivateMessage(textToSend);
			break;
		case INFO_BLOCK:
			sendInfoPrivateMessage(textToSend, false);
			break;
		}
		
	}
	
}
