package laser.ddg;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.persist.DBWriter;

/**
 * Keeps track of the classes that implement language-specific parts of the DDG. 
 * @author Barbara Lerner
 * @version Jul 3, 2013
 *
 */
public class LanguageConfigurator {
	// Map from a language name to the name of a class to construct to be the DDGBuiler.
	private static Map<String, String> languageBuilders = new HashMap<String, String>();

	// Map from a language name to the name of a class to parse programs written in that language
	private static Map<String, String> parsers= new HashMap<String, String>();

	/**
	 * Creates a language-specific DDG builder for the given language.  The class implementing that 
	 * builder must have been previously registered by calling addLanguageBuilder
	 * @param language the language we want a builder for
	 * @param scrpt the program that created the ddg being built
	 * @param provData 
	 * @param dbWriter an object that can write DDGs to a database.  If null, the DDGs created will
	 *    not be saved in the DB.
	 * @return a DDG builder to construct the DDG
	 */
	public static DDGBuilder createDDGBuilder(String language, String scrpt,
			ProvenanceData provData, DBWriter dbWriter)  {
		try {
			Class<DDGBuilder> builderClass = getDDGBuilder(language);
			Class<?> stringClass = Class.forName("java.lang.String");
			Class<?> dbWriterClass = Class.forName("laser.ddg.persist.JenaWriter");
			Constructor<DDGBuilder> builderConstructor;
			builderConstructor = builderClass.getConstructor(stringClass, ProvenanceData.class, dbWriterClass);
			return builderConstructor.newInstance(scrpt, provData, dbWriter);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create DB loader for " + language, e);
		}
	}
	
	/**
	 * Returns the class that knows how to build DDGs for a specific language
	 * @param language the name of the language.
	 * @return the class used to build ddgs for this language
	 */
	public static Class<DDGBuilder> getDDGBuilder(String language) {
		String builderClassName = languageBuilders.get(language);
		if (builderClassName == null) {
			throw new IllegalArgumentException("No DDG builder for " + language);
		}

		try {
			return (Class<DDGBuilder>) Class.forName(builderClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("DDG builder class for " + language + " (" + builderClassName + ") not on classpath");
		}
	}
	
	/**
	 * Records the name of the class to use as the DDGBuilder for the language
	 * @param language
	 * @param builderClass
	 */
	public static void addLanguageBuilder(String language, String builderClass) {
		languageBuilders.put(language, builderClass);
	}

	/**
	 * Create and return a parser a programming language that was used to create a ddg
	 * @param language the language of the program that wrote the DDG
	 * @return the parser
	 */
	public static LanguageParser createParser(String language)  {
		String parserClassName = parsers.get(language);
		if (parserClassName == null) {
			throw new IllegalArgumentException("No parser for " + language);
		}
		
		try {
			Class<LanguageParser> parserClass = (Class<LanguageParser>) Class.forName(parserClassName);
			return parserClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create parser for " + language, e);
		}
	}
	
	/**
	 * Register a parser for a language
	 * @param language the language the parser is for
	 * @param parserClass the class that can parse this language
	 */
	public static void addParser (String language, String parserClass) {
		parsers.put(language, parserClass);
	}
}
