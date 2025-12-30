try {
	for (Entity entityiterator : arguments.getArgument("${field$param}", EntitySelectorArgumentResolver.class).resolve(arguments.getSource())) {
		${statement$foreach}
	}
} catch (CommandSyntaxException e) {
	e.printStackTrace();
}