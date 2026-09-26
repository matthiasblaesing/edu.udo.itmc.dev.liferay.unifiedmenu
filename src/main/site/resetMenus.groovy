// Zurücksetzen aller Menüeinstellungen

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;

DynamicQuery dq = PortletPreferencesLocalServiceUtil.dynamicQuery();
// die portletId erlaubt es die Portletpreferences auf Basis des Typs zu finden
dq.add(RestrictionsFactoryUtil.like("portletId", "com_liferay_site_navigation_menu_web_portlet_SiteNavigationMenuPortlet%"));
PortletPreferencesLocalServiceUtil
    .dynamicQuery(dq)
    .forEach({pp ->
            // Entferne Preferences
            PortletPreferencesLocalServiceUtil.deletePortletPreferences(pp);
            out.println(String.valueOf(pp));
        });
