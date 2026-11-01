package edu.udo.itmc.dev.liferay.unifiedmenu.pubpriv;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
@Component(
        property = {
            "com.liferay.portlet.display-category=category.hidden",
            "javax.portlet.display-name=Move to Public",
            "javax.portlet.init-param.view-template=/view.jsp",
            "javax.portlet.name=" + MoveToPublicPortlet.PORTLET_NAME,
            "javax.portlet.resource-bundle=content.Language",
            "javax.portlet.security-role-ref=power-user,user",
            "javax.portlet.supported-locale=de,en",
            "javax.portlet.version=3.0"
        },
        service = Portlet.class
)
public class MoveToPublicPortlet extends MVCPortlet {

    public static final String PORTLET_NAME
            = "edu_udo_itmc_dev_liferay_unifiedmenu_pubpriv_MoveToPublic";

    private static final String PLID_PARAMETER = "plid";
    private static final String REMOVE_SOURCE_PARAMETER = "removeSource";
    private static final String LAYOUT_TREE_ATTRIBUTE = "moveToPublicLayoutTree";
    private static final String PARENT_LAYOUT = "moveToPublicParentLayout";

    @Reference
    private LayoutLocalService layoutLocalService;

    @ProcessAction(name = "execute")
    public void list(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
        
        ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
        long plid = ParamUtil.getLong(actionRequest, PLID_PARAMETER);
        boolean removeSource = ParamUtil.getBoolean(actionRequest, REMOVE_SOURCE_PARAMETER);

        if (plid > 0) {
            Layout rootLayout = layoutLocalService.fetchLayout(plid);

            if (rootLayout != null) {
                try {
                    LayoutNode layoutTree = getDescendants(rootLayout, themeDisplay.getLocale());
                    
                    if(layoutTree.isFriendlyNameConflictRecursive()) {
                        SessionErrors.add(actionRequest, "");
                        return;
                    }

                    Layout parent = findPublicParent(rootLayout);

                    Layout newLayout = copyLayoutTreeToPublic(parent == null ? 0 : parent.getPlid(), layoutTree);

                    if (removeSource) {
                        deleteTree(layoutTree);
                    }

                    actionResponse.sendRedirect("/web" + newLayout.getGroup().getFriendlyURL() + newLayout.getFriendlyURL());
                } catch (Exception ex) {
                    System.getLogger(MoveToPublicPortlet.class.getName()).log(Level.ERROR, (String) null, ex);
                }
            }
        }

    }

    private Layout copyLayoutTreeToPublic(long parentPlid, LayoutNode ln) throws Exception {
        Layout sourceLayout = layoutLocalService.getLayout(ln.getPlId());

        Layout newLayout = layoutLocalService.copyLayout(
                sourceLayout.getUserId(),
                sourceLayout.getGroupId(),
                false,
                sourceLayout.getNameMap(),
                sourceLayout.isHidden(),
                sourceLayout.isSystem(),
                true,
                sourceLayout.getPlid(),
                ServiceContextThreadLocal.getServiceContext());

        newLayout = layoutLocalService.copyLayoutContent(sourceLayout, newLayout);

        for(Entry<Locale, String> urlEntry: sourceLayout.getFriendlyURLMap().entrySet()) {
            newLayout = layoutLocalService.updateFriendlyURL(sourceLayout.getUserId(), newLayout.getPlid(), urlEntry.getValue(), LocaleUtil.toLanguageId(urlEntry.getKey()));
        }

        newLayout = layoutLocalService.updateParentLayoutId(newLayout.getPlid(), parentPlid);

        UnicodeProperties targetUnicodeProperties =
			newLayout.getTypeSettingsProperties();

        // Liferay markiert Content Seiten als noch nicht publiziert
		targetUnicodeProperties.put("published", Boolean.TRUE.toString());

        newLayout = layoutLocalService.updateLayout(newLayout);

        // Update the draft page
        if ("content".equals(newLayout.getType())) {
            Layout draftLayout = layoutLocalService.fetchDraftLayout(newLayout.getPlid());
            draftLayout = layoutLocalService.copyLayoutContent(newLayout, draftLayout);
            layoutLocalService.updateStatus(
				sourceLayout.getUserId(), draftLayout.getPlid(),
				WorkflowConstants.STATUS_APPROVED, ServiceContextThreadLocal.getServiceContext());
        }

        for(LayoutNode child: ln.getChildren()) {
            copyLayoutTreeToPublic(newLayout.getPlid(), child);
        }

        return newLayout;
    }

    private void deleteTree(LayoutNode ln) throws PortalException {
        for (LayoutNode child : ln.getChildren()) {
            deleteTree(child);
        }

        layoutLocalService.deleteLayout(ln.getPlId());
    }

    @Override
    public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {
        long plid = ParamUtil.getLong(renderRequest, PLID_PARAMETER);

        if (plid > 0) {
            Layout rootLayout = layoutLocalService.fetchLayout(plid);

            if (rootLayout != null) {
                try {
                    ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

                    LayoutNode rootLayoutNode = getDescendants(rootLayout, themeDisplay.getLocale());
                    
                    renderRequest.setAttribute(LAYOUT_TREE_ATTRIBUTE, List.of(rootLayoutNode));

                    Layout newParent = findPublicParent(rootLayout);

                    renderRequest.setAttribute(PARENT_LAYOUT, newParent);
                } catch (PortalException ex) {
                    throw new PortletException(ex);
                }

            }
        }

        super.doView(renderRequest, renderResponse);
    }

    private Layout findPublicParent(Layout rootLayout) throws PortalException {
        Layout newParent = null;
        if (rootLayout.getParentLayoutId() != 0) {
            String parentLayoutFriendlyUrl = layoutLocalService.getLayout(rootLayout.getParentPlid()).getFriendlyURL();
            newParent = layoutLocalService.fetchLayoutByFriendlyURL(rootLayout.getGroupId(), false, parentLayoutFriendlyUrl);
        }
        return newParent;
    }

    private LayoutNode getDescendants(Layout rootLayout, Locale locale) {
        Map<Long, List<Layout>> childrenByParentPlid = new HashMap<>();
        Set<String> otherLayoutSetFriendlyUrls = new HashSet<>();

        for (Layout layout : layoutLocalService.getLayouts(
                rootLayout.getGroupId(), !rootLayout.isPrivateLayout())) {

            otherLayoutSetFriendlyUrls.add(layout.getFriendlyURL());
        }

        for (Layout layout : layoutLocalService.getLayouts(
                rootLayout.getGroupId(), rootLayout.isPrivateLayout())) {

            childrenByParentPlid.computeIfAbsent(
                    layout.getParentPlid(), key -> new ArrayList<>()).add(layout);
        }

        Comparator<Layout> comparator = Comparator
                .comparingInt(Layout::getPriority)
                .thenComparing(layout -> layout.getTitle(locale));

        childrenByParentPlid.values().forEach(children -> children.sort(comparator));

        LayoutNode node = new LayoutNode(
                rootLayout, locale,
                otherLayoutSetFriendlyUrls.contains(rootLayout.getFriendlyURL()));

        getDescendants(rootLayout.getPlid(), childrenByParentPlid, node, locale, otherLayoutSetFriendlyUrls);

        return node;
    }

    private void getDescendants(
            long parentPlid, Map<Long, List<Layout>> childrenByParentPlid,
            LayoutNode parentNode, Locale locale,
            Set<String> otherLayoutSetFriendlyUrls) {

        for (Layout child : childrenByParentPlid.getOrDefault(parentPlid, List.of())) {
            LayoutNode ln  = new LayoutNode(
                    child, locale,
                    otherLayoutSetFriendlyUrls.contains(child.getFriendlyURL()));
            parentNode.appendChild(ln);
            getDescendants(
                    child.getPlid(), childrenByParentPlid, ln,
                    locale, otherLayoutSetFriendlyUrls);
        }
    }

    public static class LayoutNode {

        public LayoutNode(
                Layout layout, Locale locale,
                boolean friendlyNameConflict) {
            _plId = layout.getPlid();
            _title = layout.getName(locale);
            _friendlyUrl = layout.getFriendlyURL();
            _friendlyNameConflict = friendlyNameConflict;
        }

        public long getPlId() {
            return _plId;
        }

        public String getFriendlyUrl() {
            return _friendlyUrl;
        }

        public String getTitle() {
            return _title;
        }

        public boolean isFriendlyNameConflict() {
            return _friendlyNameConflict;
        }

        public void appendChild(LayoutNode node) {
            this.children.add(node);
        }

        public List<LayoutNode> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public boolean isFriendlyNameConflictRecursive() {
            return _friendlyNameConflict || children.stream().anyMatch(ln -> ln.isFriendlyNameConflictRecursive());
        }

        private final long _plId;
        private final boolean _friendlyNameConflict;
        private final String _title;
        private final String _friendlyUrl;
        private final ArrayList<LayoutNode> children = new ArrayList<>();
    }

}
