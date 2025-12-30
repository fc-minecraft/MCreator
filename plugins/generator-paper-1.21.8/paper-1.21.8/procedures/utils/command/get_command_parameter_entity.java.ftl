private static Entity commandParameterEntity(CommandContext<CommandSourceStack> arguments, String parameter) {
	try {
		return arguments.getArgument(parameter, EntitySelectorArgumentResolver.class).resolve(arguments.getSource()).getFirst();
	} catch (CommandSyntaxException e) {
		e.printStackTrace();
		return null;
	}
}