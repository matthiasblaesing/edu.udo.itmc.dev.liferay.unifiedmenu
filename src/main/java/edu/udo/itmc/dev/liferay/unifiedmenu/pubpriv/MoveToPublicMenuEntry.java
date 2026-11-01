package edu.udo.itmc.dev.liferay.unifiedmenu.pubpriv;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutServiceUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.product.navigation.control.menu.BaseProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.ProductNavigationControlMenuEntry;
import com.liferay.product.navigation.control.menu.constants.ProductNavigationControlMenuCategoryKeys;
import com.liferay.segments.service.SegmentsExperienceLocalServiceUtil;
import com.liferay.translation.url.provider.TranslationURLProvider;
import java.util.Locale;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration eines Icons zum Aufruf der Kopier/Verschiebefunktion für Seiten.
 *
 * @see <a href="https://github.com/liferay/liferay-portal/blob/2dced1012abe6ba44542ac6e84e69ddbd63ca841/modules/apps/layout/layout-admin-web/src/main/java/com/liferay/layout/admin/web/internal/servlet/taglib/util/LayoutActionDropdownItemsProvider.java#L50">Integration into admin interface</a>
 */
@Component(
	property = {
		"product.navigation.control.menu.category.key=" + ProductNavigationControlMenuCategoryKeys.USER,
		"product.navigation.control.menu.entry.order:Integer=350"
	},
	service = ProductNavigationControlMenuEntry.class
)
public class MoveToPublicMenuEntry extends BaseProductNavigationControlMenuEntry {

    private static final Logger logger = LoggerFactory.getLogger(MoveToPublicMenuEntry.class);

	@Reference
	private TranslationURLProvider translationURLProvider;

	@Reference
	private LayoutLocalService layoutLocalService;

    @Override
    public String getLabel(Locale locale) {
        // Wird als Tooltip angezeigt, ein Bundle wäre für einen String Overkill
        if("de".equals(locale.getLanguage())) {
            return "Kopiere/Verschiebe in Public-Bereich";
        } else {
            return "Copy/move to public space";
        }
    }

    @Override
    public String getURL(HttpServletRequest httpServletRequest) {
        try {
            // Baue URL zum Übersetzungs Portlet

            ThemeDisplay themeDisplay =
                    (ThemeDisplay)httpServletRequest.getAttribute(
                            WebKeys.THEME_DISPLAY);

            Layout layout = themeDisplay.getLayout();

            return PortletURLBuilder
                    .create(
                            PortalUtil
                                    .getControlPanelPortletURL(
                                            httpServletRequest,
                                            layout.getGroup(),
                                            MoveToPublicPortlet.PORTLET_NAME,
                                            layout.getGroupId(),
                                            layout.getPlid(),
                                            PortletRequest.RENDER_PHASE
                                    )
                    )
                    .setParameter("groupId", layout.getGroupId())
                    .setParameter("plid", layout.getPlid())
                    .setWindowState(WindowState.MAXIMIZED)
                    .buildString();
        } catch (Exception ex) {
            logger.warn("Failed to generate translation URL", ex);
            return null;
        }
    }

    @Override
    public String getIcon(HttpServletRequest httpServletRequest) {
        // Layout mit Pfeil nach oben rechts
        return "export";
    }

    @Override
    public boolean isShow(HttpServletRequest httpServletRequest) throws PortalException {
        // Prüfung: Sollte Übersetzungs Icon angezeigt werden?
        ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

        switch (layout.getType()) {
            case "content", "portlet" -> {
                // ignore
            }
            default -> {
                return false;
            }
        }

        // Soweit erkennbar werden "Draft"-Einträge mit isSystem == true erstellt
        // hier ist ist Interaktion unklar und entsprechend wird hier die
        // Darstellung auch übersprungen
        if (layout.isSystem()) {
            return false;
        }

        // Verschieben/Kopieren nach Publik is nur für PrivatePages relevant
        if (! layout.isPrivateLayout()) {
            return false;
        }

        // Nur wenn der Nutzende Änderungsberechtigung hat soll das Icon angezeigt
        // werden
        return themeDisplay.getPermissionChecker().hasPermission(
                layout.getGroup(),
                Layout.class.getName(),
                layout.getPlid(),
                ActionKeys.UPDATE);
    }

}

