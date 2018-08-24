package vendor.utilities.settings;

import vendor.abstracts.Translatable;
import vendor.exceptions.BadFormatException;
import vendor.modules.Environment;
import vendor.modules.Logger;
import vendor.modules.Logger.LogType;

import java.util.IllegalFormatException;
import java.util.function.Consumer;

public abstract class SettingField<E> extends Translatable {
	
	protected E value;
	private E defaultValue;
	
	private String name;
	private String env;
	
	public SettingField(String name, String env, E defaultValue){
		
		this.name = name;
		this.defaultValue = defaultValue;
		this.env = env;
		
	}
	
	public E getValue(){
		if(this.value != null){
			return this.value;
		}
		
		try{
			E envValue = null;
			
			try{
				envValue = (E)Environment.getVar(this.env);
			}
			catch(NullPointerException e){}
			
			if(envValue == null){
				this.value = this.getDefaultValue();
			}
			else{
				this.value = this.formatEnvironment(envValue);
			}
		}
		catch(ClassCastException | IllegalFormatException e){
			
			Logger.log(
					"Environment variable is not formatted correctly for its data type! Using default value.",
					LogType.WARNING);
			
			this.value = this.getDefaultValue();
			
		}
		
		return this.value;
	}
	
	public E getDefaultValue(){
		return this.defaultValue;
	}
	
	public final void setValue(E value) throws BadFormatException{
		this.setValue(value, null);
	}
	
	public final void setToDefaultValue(){
		this.setToDefaultValue(null);
	}
	
	public final void setToDefaultValue(Consumer<E> onChange){
		this.setValue(getDefaultValue(), onChange);
	}
	
	public final void setValue(E value, Consumer<E> onChange)
			throws BadFormatException{
		
		if(value == null){
			throw new BadFormatException("Value cannot be null!", 0);
		}
		
		this.value = this.sanitizeValue(value);
		
		if(onChange != null)
			onChange.accept(this.value);
		
	}
	
	public String getName(){
		return this.name;
	}
	
	protected abstract E sanitizeValue(Object value)
			throws IllegalArgumentException;
	
	protected E formatEnvironment(E envValue) throws IllegalFormatException{
		return envValue;
	}
	
}
