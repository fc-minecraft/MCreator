<#--
 # This file is part of Paper-Generator-MCreator.
 # Copyright (C) 2020-2025, Goldorion, opensource contributors
 #
 # Paper-Generator-MCreator is free software: you can redistribute it and/or modify
 # it under the terms of the GNU Lesser General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 # Paper-Generator-MCreator is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 # GNU Lesser General Public License for more details.
 #
 # You should have received a copy of the GNU Lesser General Public License
 # along with Paper-Generator-MCreator.  If not, see <https://www.gnu.org/licenses/>.
-->
<#-- @formatter:off -->
package ${package}.init;

public class ${JavaModName}Procedures {

	public static void register(JavaPlugin plugin) {
	    <#list procedures as procedure>
			plugin.getServer().getPluginManager().registerEvents(new ${procedure.getModElement().getName()}Procedure(), plugin);
	    </#list>
	}
}
<#-- @formatter:on -->
