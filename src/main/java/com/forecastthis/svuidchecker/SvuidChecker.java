package com.forecastthis.svuidchecker;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;

import org.reflections.Reflections;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SvuidChecker {

	public static List<Class<?>> checkClasspath() {

		Reflections reflections = new Reflections("");

		return check(reflections);
	}

	public static List<Class<?>> checkPackage(String packageName) {

		Reflections reflections = new Reflections(packageName);

		return check(reflections);
	}

	public static List<Class<?>> check(Reflections reflections) {

		Set<Class<?>> allClasses = new HashSet<>();

		Stream.of(Serializable.class, Externalizable.class)
				.flatMap(cls -> reflections.getSubTypesOf(cls).stream())
				.filter(cls -> !cls.isInterface())
				.filter(cls -> !cls.isEnum())
				.forEach(allClasses::add);

		List<Class<?>> classes = new ArrayList<>();

		allClasses.stream()
				.filter(cls -> serialVersionUidMissing(cls))
				.sorted(Comparator.comparing(Class::getName))
				.forEach(classes::add);

		return classes;
	}

	private static boolean serialVersionUidMissing(Class<?> cls) {

		return Arrays.stream(cls.getDeclaredFields())
				.filter(f -> "serialVersionUID".equals(f.getName()))
				.filter(f -> Modifier.isStatic(f.getModifiers()))
				.count() == 0;
	}


	public static void main(String[] args) {

		CliCommand.from(args).execute();
	}

	static class CliCommand {

		@Arg boolean classpath = false;

		@Arg(dest = "package") String packageName = null;

		void execute() {

			List<Class<?>> classes;

			if(classpath) {
				classes = checkClasspath();
			}
			else {
				classes = checkPackage(packageName);
			}

			for(Class<?> cls : classes) {
				System.out.println(cls.getName());
			}

		}

		static CliCommand from(String[] args) {

			ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("serial-version-uid-checker")
					.defaultHelp(true)
					.description("Scans classes and looks for Externalizable or Serializable subtypes missing a static " +
							"serialVersionUID field.");

			MutuallyExclusiveGroup target = argumentParser.addMutuallyExclusiveGroup("target")
					.required(true);

			target.addArgument("--classpath")
					.help("all classes")
					.action(Arguments.storeTrue());

			target.addArgument("--package")
					.help("just this package")
					.type(String.class);

			CliCommand params = new CliCommand();

			try {
				argumentParser.parseArgs(args, params);
			}
			catch(ArgumentParserException e) {
				argumentParser.handleError(e);
				System.exit(1);
			}

			return params;
		}

	}

}