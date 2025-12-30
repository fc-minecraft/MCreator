<#include "procedures.java.ftl">
.executes(arguments -> {
    World world = arguments.getSource().getLocation().getWorld();

    double x = arguments.getSource().getLocation().x();
    double y = arguments.getSource().getLocation().y();
    double z = arguments.getSource().getLocation().z();

    Entity entity = arguments.getSource().getExecutor();

    BlockFace direction = BlockFace.DOWN;
    if (entity != null)
    	direction = entity.getFacing();

    <@procedureToCode name=procedure dependencies=dependencies/>
    return 0;
})