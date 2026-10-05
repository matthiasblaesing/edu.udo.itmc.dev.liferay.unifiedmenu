package edu.udo.itmc.dev.liferay.unifiedmenu;

import com.liferay.layout.utility.page.kernel.LayoutUtilityPageEntryViewRenderer;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.osgi.service.component.annotations.Component;

@Component(
        property = {
            "service.ranking:Integer=100",
            "utility.page.type=" + LayoutUtilityPageEntryConstants.TYPE_SC_NOT_FOUND
        },
        service = LayoutUtilityPageEntryViewRenderer.class
)
public class NotFoundInfoRenderer implements LayoutUtilityPageEntryViewRenderer {

    @Override
    public String getLabel(Locale locale) {
        return "UnifiedMenu";
    }

    @Override
    public String getType() {
        return LayoutUtilityPageEntryConstants.TYPE_SC_NOT_FOUND;
    }

    @Override
    public void renderHTML(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        try {
            ThemeDisplay themeDisplay = (ThemeDisplay) httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY);
            ResourceBundle rb = ResourceBundle.getBundle("messages", themeDisplay.getLocale());

            // We should only ever reach this place when the system "knows"
            // the "right" group (Site). This is required to fetch layouts
            // based on their friendly URL.
            long scopedGroupUid = PortalUtil.getScopeGroupId(httpServletRequest);

            // Prepare the URL for analysis the expected cases are:
            //
            // /<friendlyUrlOfLayout>
            // /web/<site>/<friendlyUrlOfLayout>
            // /group/<site>/<friendlyUrlOfLayout>
            // /<locale>/<friendlyUrlOfLayout>
            // /<locale>/web/<site>/<friendlyUrlOfLayout>
            // /<locale>/group/<site>/<friendlyUrlOfLayout>
            //
            // It was checked that liferay rejects the prefixes (web, group,
            // de, en) as friendly URLs for layouts
            URI currentUrl = URI.create((String) httpServletRequest.getAttribute("CURRENT_URL"));
            String path = currentUrl.getPath();
            List<String> urlCandiates = new ArrayList<>();
            urlCandiates.add(path);
            int secondSlash = path.length() < 2 ? -1 : path.indexOf("/", 1);
            if (secondSlash >= 0) {
                // the locale must be stripped from the URL to be able to
                // analyse it using getGroupFriendlyURLIndex. To make this
                // easier both variants (with and without the prefix) are
                // checked with getGroupFriendlyURLIndex.
                String localeStripped = path.substring(secondSlash);
                urlCandiates.add(localeStripped);
            }

            // extraCssClass is added as a wrapper around the content generated
            // here. The top margin is always intended, the addition of the
            // container class is conditional This was tested with the classic
            // theme and that holds a special casing for some of the layouts.
            // For some layouts the container class is already added by the
            // theme (class-theme.war/templates/init_custom.ftl):
            //
            // <#if wrapWidgetPageContent && ((layout.isTypeContent() && themeDisplay.isStateMaximized()) || (layout.getType() == "portlet"))>
            //
            // in this case we don't want to add a second wrapper
            String extraCssClass = " mt-4 ";
            if(
                    !(GetterUtil.getBoolean(themeDisplay.getThemeSetting("wrap-widget-page-content"))
                    && ((themeDisplay.getLayout().isTypeContent() && themeDisplay.isStateMaximized())
                    || "portlet".equals(themeDisplay.getLayout().getType())))
            ) {
                extraCssClass += " container ";
            }

            // Generate the response, start with the general "Not found" info
            PrintWriter out = httpServletResponse.getWriter();
            out.write("<div class='");
            out.write(extraCssClass);
            out.write("'>");

            out.write("<h1>");
            out.write(rb.getString("http404.header"));
            out.write("</h1>");
            out.write(rb.getString("http404.text"));

            // Check if we can find a layout with same friendly URL, but in
            // the "other" section of the site (section here means public or
            // private page
            for (String url : urlCandiates) {
                boolean requestedPublic = !url.startsWith("/group/");
                User user = PortalUtil.getUser(httpServletRequest);
                if (requestedPublic && (user == null || user.isGuestUser())) {
                    continue;
                }
                // Strip away the public/private indicators and the site name
                // It is assumed, that only the friendly URL of the layout
                // remains
                if (url.startsWith("/web/") || url.startsWith("/group/")) {
                    int[] indices = PortalUtil.getGroupFriendlyURLIndex(url);
                    url = url.substring(indices[1]);
                }
                Layout l = LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(scopedGroupUid, requestedPublic, url);
                if (l != null) {
                    // Build the new URL and add query string again
                    String fullUrl = PortalUtil.getAbsoluteURL(httpServletRequest, PortalUtil.getLayoutFullURL(l, themeDisplay));
                    if(currentUrl.getRawQuery() != null) {
                        fullUrl += "?" + currentUrl.getRawQuery();
                    }
                    out.write(rb.getString("http404.alt"));
                    out.write("<p>");
                    out.write(rb.getString("http404.altUrl"));
                    out.write("<br />");
                    out.write("<a href='");
                    out.write(HtmlUtil.escape(fullUrl));
                    out.write("'>");
                    out.write(HtmlUtil.escape(fullUrl));
                    out.write("</a>");
                    out.write("</p>");
                    break;
                }
            }

            out.write("</div>");
        } catch (PortalException ex) {
            throw new ServletException(ex);
        }
    }

}
