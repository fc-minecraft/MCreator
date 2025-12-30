private static String commandParameterMessage(CommandContext<CommandSourceStack> arguments, String parameter) {
	try {
		return arguments.getArgument(parameter, SignedMessageResolver.class).resolveSignedMessage(parameter, arguments).get().message();
	} catch (CommandSyntaxException | InterruptedException | ExecutionException e) {
		e.printStackTrace();
		return "";
	}
}