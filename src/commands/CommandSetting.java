package commands;

import utilities.BotCommand;
import errorHandling.BotError;
import vendor.exceptions.NoContentException;
import vendor.objects.ParametersHelp;

import java.util.function.Consumer;

public class CommandSetting extends BotCommand {
	
	@Override
	public void action(){
		
		tryAndChangeSetting("prefix", "prefix", (value) -> {
			sendMessage("You switched the prefix to `" + value + "`!");
		});
		
		tryAndChangeSetting("nickname", "nickname", (value) -> {
			setSelfNickname(value.toString());
			
			sendMessage("The nickname of the bot is now set to `" + value
					+ "`!");
		});
		
		tryAndChangeSetting(
				"confirm_stop",
				"confirm_stop",
				(value) -> {
					boolean isConfirming = (boolean)value;
					
					if(isConfirming){
						sendMessage("Stopping the most recent running command will now ask for a confirmation.");
					}
					else{
						sendMessage("Stopping the most recent running command will not ask for a confirmation anymore.");
					}
				});
		
	}
	
	public void tryAndChangeSetting(String settingName, String parameterName,
			Consumer<Object> onSuccess){
		
		if(hasParameter(parameterName)){
			
			String parameterContent = null;
			
			try{
				parameterContent = getParameter(parameterName)
						.getParameterContent();
			}
			catch(NoContentException e){}
			
			if(parameterContent == null){
				
				Object defaultSettingValue = getSettings()
						.getField(settingName).getDefaultValue();
				Object currentSettingValue = getSettings()
						.getField(settingName).getDefaultValue();
				
				sendMessage("The default value for the setting "
						+ code(settingName) + " is : "
						+ ital(code(defaultSettingValue))
						+ ". Current value : " + code(currentSettingValue)
						+ ".");
				
			}
			else{
				
				try{
					setSetting(settingName, parameterContent, onSuccess);
				}
				catch(IllegalArgumentException e){
					new BotError(this, e.getMessage());
				}
				
			}
			
		}
		
	}
	
	@Override
	public Object getCalls(){
		return new String[]
		{
			"setting", "settings"
		};
	}
	
	@Override
	public String getCommandDescription(){
		return "This command changes settings for the bot. Use the parameters below to change what you want to change!";
	}
	
	@Override
	public ParametersHelp[] getParametersDescriptions(){
		return new ParametersHelp[]
		{
			new ParametersHelp(
					"Changes the prefix used for each command. Default is "
							+ code(getSettings().getField("prefix")
									.getDefaultValue()) + ".", "prefix"),
			new ParametersHelp(
					"Changes the bot's nickname. His default name is "
							+ code(getSettings().getField("nickname")
									.getDefaultValue()) + ".", "nickname"),
			new ParametersHelp(
					"Determine the behavior of stopping the most recent running command. "
							+ code("true")
							+ " to ask for a confirmation, "
							+ code("false")
							+ " to stop the most recent command without confirming. Default is set to "
							+ code(getSettings().getField("nickname")
									.getDefaultValue()) + ".", "confirm_stop"),
		};
	}
	
}
