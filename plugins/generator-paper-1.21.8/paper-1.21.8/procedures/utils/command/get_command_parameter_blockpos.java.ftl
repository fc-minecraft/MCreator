private static BlockPosition commandParameterBlockPos(CommandContext<CommandSourceStack> arguments, String parameter) {
	try {
		return arguments.getArgument(parameter, BlockPositionResolver.class).resolve(arguments.getSource());
	} catch (CommandSyntaxException e) {
		e.printStackTrace();
		return BlockPosition.BLOCK_ZERO;
	}
}