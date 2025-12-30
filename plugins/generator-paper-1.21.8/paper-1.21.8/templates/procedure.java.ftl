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
package ${package}.procedures;

<#assign nullableDependencies = []/>
<#if !(data.skipDependencyNullCheck)>
	<#list dependencies as dependency>
		<#if dependency.getRawType() != "number"
			&& dependency.getRawType() != "world"
			&& dependency.getRawType() != "itemstack"
			&& dependency.getRawType() != "blockstate"
			&& dependency.getRawType() != "actionresulttype"
			&& dependency.getRawType() != "logic"
			&& dependency.getRawType() != "cmdcontext">
			<#assign nullableDependencies += [dependency.getName()]/>
		</#if>
	</#list>
</#if>

<#compress>

<#if trigger_code?has_content>
${trigger_code}
<#else>
public class ${name}Procedure implements Listener{
</#if>

	public static <#if return_type??>${return_type.getJavaType(generator.getWorkspace())}<#else>void</#if> execute(
		<#list dependencies as dependency>
				${dependency.getType(generator.getWorkspace())} ${dependency.getName()}<#sep>,
		</#list>
	) {
		<#if nullableDependencies?has_content>
			if(
			<#list nullableDependencies as dependency>
			${dependency} == null <#sep>||
			</#list>
			) return <#if return_type??>${return_type.getDefaultValue(generator.getWorkspace())}</#if>;
		</#if>

		<#list localvariables as var>
			<@var.getType().getScopeDefinition(generator.getWorkspace(), "LOCAL")['init']?interpret/>
		</#list>

		${procedurecode}
	}

	${extra_templates_code}
}
</#compress>
<#-- @formatter:on -->
