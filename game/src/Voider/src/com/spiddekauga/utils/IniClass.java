package com.spiddekauga.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * Base class for all classes that are set from an ini-file. If the derived class has an
 * inner class (also derived from IniClass) and a variable of that inner-class, this
 * variable will be automatically set.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class IniClass {
	/**
	 * Constructor which takes a section that represents the class to be loaded.
	 * @param ini the ini file
	 * @param classSection section for this class
	 */
	protected IniClass(Ini ini, Section classSection) {
		set(ini, classSection);
	}

	/**
	 * Sets the variables from a section
	 * @param ini the ini file
	 * @param classSection section for this class
	 */
	public void set(Ini ini, Section classSection) {
		Map<String, Field> vars = new HashMap<>();
		ArrayList<Field> children = new ArrayList<>();
		getDeclaredVariables(vars, children);

		// Set all variables from this section
		setSectionVars(classSection, vars);

		// Not all variables have been set, check super-classes in ini-file
		if (!vars.isEmpty()) {
			setDefaultVars(ini, getSuperClass(getClass()), vars);
		}

		// Still not empty print all that weren't set!
		if (!vars.isEmpty()) {
			printUnsetVars(vars);
		}

		// Set children/inner class variables
		setChildren(ini, classSection, children);
	}

	/**
	 * Set all children variables
	 * @param ini the ini file
	 * @param classSection this class's section
	 * @param children all children to create
	 */
	private void setChildren(Ini ini, Section classSection, ArrayList<Field> children) {
		for (Field child : children) {
			String childClassName = child.getType().getSimpleName();
			@SuppressWarnings("unchecked")
			String childName = getSimpleSectionName((Class<? extends IniClass>) child.getType());
			Section childSection = classSection.getChild(childName);

			// Get constructor
			try {
				Constructor<?> constructor = child.getType().getDeclaredConstructor(Ini.class, Section.class);
				constructor.setAccessible(true);
				try {
					IniClass childInstance = (IniClass) constructor.newInstance(ini, childSection);
					child.set(this, childInstance);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				mLogger.severe("Could not find constructor for " + childClassName);
			} catch (SecurityException e) {
				mLogger.severe("Cannot access constructor for " + childClassName);
			}
		}
	}

	/**
	 * Print error for all variables that haven't been set
	 * @param vars all variables that haven't been set
	 */
	private void printUnsetVars(Map<String, Field> vars) {
		HashMap<String, Set<String>> unsetVars = new HashMap<>();
		for (Field var : vars.values()) {
			String className = var.getDeclaringClass().getSimpleName();
			Set<String> unsetClass = unsetVars.get(className);
			if (unsetClass == null) {
				unsetClass = new HashSet<>();
				unsetVars.put(className, unsetClass);
			}

			unsetClass.add(var.getName());
		}

		StringBuilder message = new StringBuilder();
		message.append(getClass().getSimpleName()).append(" has ").append(vars.size()).append(" variables:\n");

		for (Entry<String, Set<String>> classVars : unsetVars.entrySet()) {
			message.append(classVars.getKey()).append("\n");

			for (String varName : classVars.getValue()) {
				message.append("\t").append(varName).append("\n");
			}
		}
	}

	/**
	 * Set default variables from super classes
	 * @param ini the ini-file
	 * @param superClass the super class to get default variables from
	 * @param vars all class variables that haven't been set yet
	 */
	private void setDefaultVars(Ini ini, Class<? extends IniClass> superClass, Map<String, Field> vars) {
		if (superClass == null) {
			return;
		}

		Section section = ini.get(getFullSectionName(superClass));
		Iterator<Entry<String, Field>> varIt = vars.entrySet().iterator();
		while (varIt.hasNext()) {
			Entry<String, Field> entry = varIt.next();

			// Set variable if it exists in this section
			if (section.containsKey(entry.getKey())) {
				setVar(entry.getValue(), section, entry.getKey());
				varIt.remove();
			}
		}

		if (!vars.isEmpty()) {
			setDefaultVars(ini, getSuperClass(superClass), vars);
		}
	}

	/**
	 * Set section variables
	 * @param classSection section for this class (and super classes)
	 * @param vars all class variables (including super classes)
	 */
	private void setSectionVars(Section classSection, Map<String, Field> vars) {
		for (String varName : classSection.keySet()) {
			// Skip sections
			if (classSection.getChild(varName) == null) {
				Field field = vars.get(varName);

				if (field != null) {
					setVar(field, classSection, varName);
					vars.remove(varName);
				} else {
					mLogger.warning("No field '" + varName + "' found in " + getClass().getSimpleName());
				}
			}
		}
	}

	/**
	 * Sets the specified class variable
	 * @param field the variable to set
	 * @param section the section to fetch from
	 * @param varName variable name in the section
	 */
	private void setVar(Field field, Section section, String varName) {
		Object value = section.get(varName, field.getType());
		try {
			field.set(this, value);
		} catch (IllegalArgumentException e) {
			mLogger.severe("Could not set field '" + varName + "' as '" + field.getType() + "' in " + getClass().getSimpleName());
		} catch (IllegalAccessException e) {
			mLogger.severe("No access to field '" + varName + "' in " + getClass().getSimpleName());
		}
	}

	/**
	 * Get all declared variables and children
	 * @param vars all declared variables
	 * @param children all inner classes
	 */
	private void getDeclaredVariables(Map<String, Field> vars, ArrayList<Field> children) {
		ArrayList<Field> fields = new ArrayList<>();
		getDeclaredFields(getClass(), fields);

		Set<Class<? extends IniClass>> innerClasses = new HashSet<>();
		getInnerClasses(getClass(), innerClasses);

		// Split into children and vars
		for (Field field : fields) {
			field.setAccessible(true);

			// Inner class
			if (IniClass.class.isAssignableFrom(field.getType())) {
				// Check for inner class
				if (innerClasses.contains(field.getType())) {
					children.add(field);
				} else {
					mLogger.warning("Field (" + field.getName() + ") seems to be an inner class variable, but no inner class of type "
							+ field.getType().getSimpleName() + " was found.");
				}
			}
			// Variable
			else {
				vars.put(field.getName(), field);
			}
		}
	}

	/**
	 * Get all declared inner classes for the specified class and all super classes, not
	 * including IniClass. Only inner classes that are derived from the IniClass.
	 * @param clazz the class to get all inner classes for
	 * @param innerClasses all found inner classes
	 */
	@SuppressWarnings("unchecked")
	private static void getInnerClasses(Class<? extends IniClass> clazz, Set<Class<? extends IniClass>> innerClasses) {
		if (clazz == null) {
			return;
		}

		for (Class<?> innerClass : clazz.getDeclaredClasses()) {
			if (IniClass.class.isAssignableFrom(innerClass)) {
				innerClasses.add((Class<? extends IniClass>) innerClass);
			}
		}
		getInnerClasses(getSuperClass(clazz), innerClasses);
	}

	/**
	 * Get all declared field from this class and all super-classes to and not including
	 * IniClass.
	 * @param clazz the class to get all field for
	 * @param fields all found fields
	 */
	private static void getDeclaredFields(Class<? extends IniClass> clazz, ArrayList<Field> fields) {
		if (clazz == null) {
			return;
		}

		Collections.addAll(clazz.getDeclaredFields(), fields);
		getDeclaredFields(getSuperClass(clazz), fields);
	}

	/**
	 * Get super class for the specified class
	 * @param clazz the class which we want to know the base class of
	 * @return super class if it's derived from IniClass. null if super class is the
	 *         IniClass itself
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends IniClass> getSuperClass(Class<? extends IniClass> clazz) {
		Class<?> superClass = clazz.getSuperclass();

		if (superClass != IniClass.class) {
			return (Class<? extends IniClass>) superClass;
		}

		return null;
	}

	/**
	 * Get simple section name for the class
	 * @param clazz the class to get the simple section name for
	 * @return simple section name
	 */
	private static String getSimpleSectionName(Class<? extends IniClass> clazz) {
		String className = clazz.getSimpleName();

		// Remove prefix
		if (!mClassPrefix.isEmpty()) {
			if (className.indexOf(mClassPrefix) == 0) {
				className = className.substring(mClassPrefix.length());
			} else {
				mLogger.warning("Didn't find class prefix for class: " + className + ", full: " + clazz.getCanonicalName());
			}
		}

		return className;
	}

	/**
	 * Get the correct section name for the specified class
	 * @param clazz the class to get the fully qualified section name for
	 * @return fully qualified section name
	 */
	private static String getFullSectionName(Class<? extends IniClass> clazz) {
		Class<? extends IniClass> currentClass = clazz;
		StringBuilder sectionBuilder = new StringBuilder();

		do {
			String className = getSimpleSectionName(currentClass);
			sectionBuilder.insert(0, className);

			currentClass = getSuperClass(currentClass);

			if (currentClass != null) {
				sectionBuilder.insert(0, "/");
			}

		} while (currentClass != null);

		return sectionBuilder.toString();
	}

	/**
	 * Set java class prefix. I.e. if any java class differ from the ini-file
	 * representation. E.g. If a section is name [TextSettings] but the Java class name is
	 * IC_TextSettings the class prefix should be set to "IC_".
	 * @param classPrefix Java class prefixes of all IniClasses to load.
	 */
	public static void setJavaClassPrefix(String classPrefix) {
		mClassPrefix = classPrefix;
	}

	/** Logger */
	private static Logger mLogger = Logger.getLogger("IniClass");
	/** If Java classes has any prefix */
	private static String mClassPrefix = "";
}
