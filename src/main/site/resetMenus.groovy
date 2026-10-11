// Zurücksetzen aller Menüeinstellungen

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.Disjunction;

DynamicQuery dq = PortletPreferencesLocalServiceUtil.dynamicQuery();
// die portletId erlaubt es die Portletpreferences auf Basis des Typs und der ID aus der Konfiguration zu finden
Disjunction dis = RestrictionsFactoryUtil.disjunction();
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_publicMenuGlobal"));
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_publicMenuBreadcrumb"));
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_publicMenuSide"));
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_privateMenuGlobal"));
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_privateMenuBreadcrumb"));
dis.add(RestrictionsFactoryUtil.eq("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet_publicMenuSide"));
dq.add(dis);
PortletPreferencesLocalServiceUtil
    .dynamicQuery(dq)
    .forEach({pp ->
            // Entferne Preferences
            PortletPreferencesLocalServiceUtil.deletePortletPreferences(pp);
            out.println(String.valueOf(pp));
        });
