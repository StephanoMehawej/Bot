package io.github.vhoyon.bot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.github.ved.jrequester.Option;
import io.github.ved.jrequester.OptionData;
import io.github.ved.jsanitizers.exceptions.BadFormatException;
import io.github.vhoyon.bot.errorHandling.BotError;
import io.github.vhoyon.bot.utilities.BotCommand;
import io.github.vhoyon.bot.utilities.music.MusicManager;
import io.github.vhoyon.vramework.interfaces.BufferLevel;
import io.github.vhoyon.vramework.modules.Logger;
import io.github.vhoyon.vramework.util.settings.Setting;
import io.github.vhoyon.vramework.util.settings.SettingRepositoryRepository;

/**
 * Command to interact with the settings of this bot. There's quite a few things
 * you can do, here's a small list :
 * <ul>
 * <li>Don't add any content to the available parameters to see their current
 * and default values;</li>
 * <li>Add the flag {@code -d} (<i>or {@code default}</i>) to set back the
 * settings added as parameters (which has <b>no text afterward</b>) to their
 * default state. Any parameters that has content will not be set back to its
 * default;</li>
 * <li>Add content after the parameters that you want to change to set its
 * value. Appropriate error messages will be sent if validations fails.</li>
 * </ul>
 * <p>
 * Run the command {@code !!help setting} to see more about available parameters
 * (in other words, available settings)!
 * </p>
 * 
 * @version 1.0
 * @since v0.7.0
 * @author V-ed (Guillaume Marcoux)
 */
public class CommandSetting extends BotCommand {
	
	/**
	 * Class that facilitates the logic to change settings based on the code it
	 * gets given in its {@link #onSuccess(Object) onSuccess(T)} method.<br>
	 * It also allows to not execute the {@link #sendMessage(String)} (<i>and
	 * its variants</i>) methods by overriding those methods to execute only
	 * under the right state.
	 * 
	 * @param <T>
	 *            The type of the setting to deal with. This type will be used
	 *            when sending the object to the {@link #onSuccess(Object)
	 *            onSuccess(T)} method.
	 * @version 1.0
	 * @since v0.10.0
	 * @author V-ed (Guillaume Marcoux)
	 */
	protected abstract class SettingChanger<T> extends BotCommand {
		
		public boolean shouldSendMessages;
		
		private String settingName;
		private String parameterName;
		private boolean isPresent;
		
		public SettingChanger(String settingAndParamName){
			this(settingAndParamName, settingAndParamName);
		}
		
		public SettingChanger(String settingName, String parameterName){
			super(CommandSetting.this);
			
			this.shouldSendMessages = true;
			this.settingName = settingName;
			this.parameterName = parameterName;
			this.isPresent = this.hasOption(this.parameterName);
			
			CommandSetting.this.settings.add(this);
		}
		
		@Override
		public void actions(){
			
			@SuppressWarnings("unchecked")
			Consumer<Object> onSuccess = (value) -> {
				try{
					onSuccess((T)value);
				}
				catch(ClassCastException e){
					Logger.log(
							"One of your SettingChanger is not well typed : make sure that your settings configuration and your SettingChanger in CommandSetting are synchronized correctly!",
							Logger.LogType.ERROR);
				}
			};
			
			OptionData param = getOption(this.parameterName);
			
			String parameterContent = param.getContent();
			
			BufferLevel level = isForGuild() ? BufferLevel.GUILD : null;
			
			if(parameterContent == null){
				
				Setting<Object> settingField = getSettings(level).getSetting(
						this.settingName);
				
				if(CommandSetting.this.shouldSwitchToDefault){
					
					this.setSendable(false);
					
					settingField.setToDefaultValue(onSuccess);
					
					if(level == BufferLevel.GUILD && hasTextSettings()){
						
						SettingRepositoryRepository
								.getReposOfGuildTextChannels(getGuild())
								.forEach(
										repo -> repo.getSetting(
												this.settingName)
												.setToDefaultValue());
						
					}
					
					this.setSendable(true);
					
					sendMessage(lang("WasSetToDefault", code(settingName),
							ital(code(settingField.getDefaultValue()))));
					
				}
				else{
					
					Object defaultSettingValue = settingField.getDefaultValue();
					Object currentSettingValue = settingField.getValue();
					
					sendMessage(lang("TellDefaultValue", code(settingName),
							ital(code(defaultSettingValue)),
							code(currentSettingValue)));
					
				}
				
			}
			else{
				
				try{
					
					setSetting(settingName, parameterContent, level, onSuccess);
					
					if(level == BufferLevel.GUILD && hasTextSettings()){
						
						SettingRepositoryRepository
								.getReposOfGuildTextChannels(getGuild())
								.forEach(
										repo -> repo.save(settingName,
												parameterContent));
						
					}
					
				}
				catch(BadFormatException e){
					new BotError(this, e.getMessage());
				}
				
			}
			
		}
		
		public abstract void onSuccess(T value);
		
		public boolean isForGuild(){
			return isOnlyForGuild() || hasOption("g");
		}
		
		public boolean isOnlyForGuild(){
			return false;
		}
		
		public boolean hasTextSettings(){
			return !isOnlyForGuild();
		}
		
		public boolean isPresent(){
			return this.isPresent;
		}
		
		public void setSendable(boolean shouldSendMessages){
			this.shouldSendMessages = shouldSendMessages;
		}
		
		@Override
		public String getCall(){
			return null;
		}
		
		@Override
		public String sendMessage(String messageToSend){
			return this.shouldSendMessages ? super.sendMessage(messageToSend)
					: null;
		}
		
		@Override
		public String lang(String key){
			return this.shouldSendMessages ? CommandSetting.this.lang(key)
					: null;
		}
		
		@Override
		public String lang(String key, Object... replacements){
			return this.shouldSendMessages ? CommandSetting.this.lang(key,
					replacements) : null;
		}
		
	}
	
	private boolean shouldSwitchToDefault;
	private List<SettingChanger<?>> settings = new ArrayList<>();
	
	@Override
	public void actions(){
		
		this.shouldSwitchToDefault = hasOption("d");
		
		this.setupSettings();
		
		boolean hasAtLeastOneSetting = settings.stream()
				.filter(SettingChanger::isPresent).peek(SettingChanger::action)
				.count() > 0;
		
		if(!hasAtLeastOneSetting){
			new BotError(this, lang("ErrorNoSettingEntered", buildVCommand(HELP
					+ " " + "setting")));
		}
		
	}
	
	/**
	 * Method to setup all the available settings, confirming their types and
	 * defining the arbitrary code to run on success.<br>
	 * This setup requires the use of the SettingChanger class that handles all
	 * the storage and when-to-call logic. Simply create a new SettingChanger,
	 * define its {@code onSuccess} method and you'll be good to go.
	 * 
	 * @since v0.10.0
	 */
	protected void setupSettings(){
		
		new SettingChanger<String>("prefix"){
			@Override
			public void onSuccess(String newPrefix){
				sendMessage(lang("SuccessPrefix", code(newPrefix)));
			}
		};
		
		new SettingChanger<Character>("option_prefix"){
			@Override
			public void onSuccess(Character newOptionPrefix){
				sendMessage(lang(
						"SuccessOptionPrefix",
						code(newOptionPrefix),
						ital(lang("SuccessOptionPrefixSupp",
								code(newOptionPrefix + "" + newOptionPrefix)))));
			}
		};
		
		new SettingChanger<String>("nickname"){
			@Override
			public void onSuccess(String newNickname){
				setSelfNickname(newNickname);
				
				sendMessage(lang("SuccessNickname", code(newNickname)));
			}
		};
		
		new SettingChanger<Boolean>("confirm_stop"){
			@Override
			public void onSuccess(Boolean isConfirming){
				if(isConfirming){
					sendMessage(lang("SuccessConfirmStopNowConfirms"));
				}
				else{
					sendMessage(lang("SuccessConfirmStopNowDoesNotConfirm"));
				}
			}
		};
		
		new SettingChanger<Integer>("volume"){
			@Override
			public void onSuccess(Integer volume){
				MusicManager.get().onPlayerPresent(this.getGuild(),
						(player) -> player.setVolume(volume));
				
				sendMessage(lang("SuccessVolume", code(volume)));
			}
			
			@Override
			public boolean isOnlyForGuild(){
				return true;
			}
		};
		
		new SettingChanger<Integer>("empty_drop_delay"){
			@Override
			public void onSuccess(Integer delay){
				MusicManager.get().onPlayerPresent(this.getGuild(),
						(player) -> player.setEmptyDropDelay(delay));
				
				sendMessage(lang("SuccessEmptyDropDelay", code(delay)));
			}
			
			@Override
			public boolean isOnlyForGuild(){
				return true;
			}
		};
		
		new SettingChanger<Integer>("alone_drop_delay"){
			@Override
			public void onSuccess(Integer delay){
				sendMessage(lang("SuccessAloneDropDelay", code(delay)));
			}
			
			@Override
			public boolean isOnlyForGuild(){
				return true;
			}
		};
		
	}
	
	@Override
	public String getCall(){
		return "setting";
	}
	
	@Override
	public List<String> getAliases(){
		return Arrays.asList("settings");
	}
	
	@Override
	public String getCommandDescription(){
		return lang("Description");
	}
	
	@Override
	public Option[] getOptions(){
		return new Option[]
		{
			new Option(lang("OptionPrefix", code(getSetting("prefix")
					.getDefaultValue())), "prefix"),
			new Option(lang("OptionOptionPrefix",
					code(getSetting("option_prefix").getDefaultValue())),
					"option_prefix"),
			new Option(lang("OptionNickname", code(getSetting("nickname")
					.getDefaultValue())), "nickname"),
			new Option(lang("OptionConfirmStop", code("true"), code("false"),
					code(getSetting("confirm_stop").getDefaultValue())),
					"confirm_stop"),
			new Option(lang("OptionVolume", code(getSetting("volume")
					.getDefaultValue())), "volume"),
			new Option(lang("OptionEmptyDropDelay",
					code(getSetting("empty_drop_delay").getDefaultValue())),
					"empty_drop_delay"),
			new Option(lang("OptionAloneDropDelay",
					code(getSetting("alone_drop_delay").getDefaultValue())),
					"alone_drop_delay"),
			new Option(lang("OptionDefault"), false, "d", "default"),
			new Option(lang("OptionGuild"), false, "g", "guild"),
		};
	}
	
}
