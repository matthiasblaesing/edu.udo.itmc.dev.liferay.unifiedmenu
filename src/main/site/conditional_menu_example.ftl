<#if has_navigation && is_setup_complete>
        <#assign mainNavigationPreferencesMap = {"displayDepth": "0", "displayStyle": "ddmTemplate_NAVBAR-LINKS-FTL", "portletSetupPortletDecoratorId": "barebone", "siteNavigationMenuType": "6", "siteNavigationMenuId": "0"} />
        <#assign mainNavigationId = "publicMenu" />
        <#if is_signed_in>
            <#assign mainNavigationPreferencesMap = {"displayDepth": "0", "displayStyle": "ddmTemplate_NAVBAR-LINKS-FTL", "portletSetupPortletDecoratorId": "barebone", "siteNavigationMenuType": "1", "siteNavigationMenuId": "0"} />
            <#assign mainNavigationId = "privateMenu" />
        </#if>

	<button aria-controls="navigationCollapse" aria-expanded="false" aria-label="Toggle navigation" class="navbar-toggler navbar-toggler-right" data-target="#navigationCollapse" data-toggle="liferay-collapse" type="button">
		<span class="navbar-toggler-icon"></span>
	</button>

	<div class="collapse navbar-collapse" id="navigationCollapse">
		<@liferay.navigation_menu  instance_id=mainNavigationId default_preferences=freeMarkerPortletPreferences.getPreferences(mainNavigationPreferencesMap) />
	</div>
</#if>
