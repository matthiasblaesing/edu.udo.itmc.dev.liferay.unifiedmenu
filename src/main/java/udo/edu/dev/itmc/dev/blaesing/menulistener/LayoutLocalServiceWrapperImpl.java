package udo.edu.dev.itmc.dev.blaesing.menulistener;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalServiceWrapper;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ServiceWrapper.class, immediate = true)
public class LayoutLocalServiceWrapperImpl extends LayoutLocalServiceWrapper {

    @Reference
    private MenuAdapter menuAdapter;

    @Override
    public Layout updatePriority(long plid, int priority) throws PortalException {
        try {
            return super.updatePriority(plid, priority);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updatePriority(long groupId, boolean privateLayout, long layoutId, long nextLayoutId, long previousLayoutId) throws PortalException {
        try {
            return super.updatePriority(groupId, privateLayout, layoutId, nextLayoutId, previousLayoutId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updatePriority(long groupId, boolean privateLayout, long layoutId, int priority) throws PortalException {
        try {
            return super.updatePriority(groupId, privateLayout, layoutId, priority);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updatePriority(Layout layout, int priority) throws PortalException {
        try {
            return super.updatePriority(layout, priority);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateParentLayoutIdAndPriority(long plid, long parentPlid, int priority) throws PortalException {
        try {
            return super.updateParentLayoutIdAndPriority(plid, parentPlid, priority);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateParentLayoutId(long plid, long parentPlid) throws PortalException {
        try {
            return super.updateParentLayoutId(plid, parentPlid);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateParentLayoutId(long groupId, boolean privateLayout, long layoutId, long parentLayoutId) throws PortalException {
        try {
            return super.updateParentLayoutId(groupId, privateLayout, layoutId, parentLayoutId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void updateLayoutContent(String data, Layout layout, long segmentsExperienceId) throws Exception {
        try {
            super.updateLayoutContent(data, layout, segmentsExperienceId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateLayout(long groupId, boolean privateLayout, long layoutId, String typeSettings, byte[] iconBytes, String themeId, String colorSchemeId, String styleBookEntryERC, String css, String faviconFileEntryERC, String faviconFileEntryScopeERC, String masterLayoutPageTemplateEntryERC) throws PortalException {
        try {
            return super.updateLayout(groupId, privateLayout, layoutId, typeSettings, iconBytes, themeId, colorSchemeId, styleBookEntryERC, css, faviconFileEntryERC, faviconFileEntryScopeERC, masterLayoutPageTemplateEntryERC);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateLayout(long groupId, boolean privateLayout, long layoutId, long parentLayoutId, Map<Locale, String> nameMap, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, Map<Locale, String> keywordsMap, Map<Locale, String> robotsMap, String type, boolean hidden, Map<Locale, String> friendlyURLMap, boolean hasIconImage, byte[] iconBytes, String styleBookEntryERC, String faviconFileEntryERC, String faviconFileEntryScopeERC, String masterLayoutPageTemplateEntryERC, ServiceContext serviceContext) throws PortalException {
        try {
            return super.updateLayout(groupId, privateLayout, layoutId, parentLayoutId, nameMap, titleMap, descriptionMap, keywordsMap, robotsMap, type, hidden, friendlyURLMap, hasIconImage, iconBytes, styleBookEntryERC, faviconFileEntryERC, faviconFileEntryScopeERC, masterLayoutPageTemplateEntryERC, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateLayout(long groupId, boolean privateLayout, long layoutId, long classNameId, long classPK) throws PortalException {
        try {
            return super.updateLayout(groupId, privateLayout, layoutId, classNameId, classPK);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateLayout(long groupId, boolean privateLayout, long layoutId, Date publishDate) throws PortalException {
        try {
            return super.updateLayout(groupId, privateLayout, layoutId, publishDate);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateLayout(Layout layout) {
        try {
            return super.updateLayout(layout);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateFriendlyURL(long userId, long plid, String friendlyURL, String languageId) throws PortalException {
        try {
            return super.updateFriendlyURL(userId, plid, friendlyURL, languageId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void deleteLayouts(long groupId, boolean privateLayout, ServiceContext serviceContext) throws PortalException {
        try {
            super.deleteLayouts(groupId, privateLayout, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void deleteLayout(String externalReferenceCode, long groupId) throws PortalException {
        try {
            super.deleteLayout(externalReferenceCode, groupId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void deleteLayout(long plid, ServiceContext serviceContext) throws PortalException {
        try {
            super.deleteLayout(plid, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void deleteLayout(long groupId, boolean privateLayout, long layoutId, ServiceContext serviceContext) throws PortalException {
        try {
            super.deleteLayout(groupId, privateLayout, layoutId, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout deleteLayout(long plid) throws PortalException {
        try {
            return super.deleteLayout(plid);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void deleteLayout(Layout layout, ServiceContext serviceContext) throws PortalException {
        try {
            super.deleteLayout(layout, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout deleteLayout(Layout layout) throws PortalException {
        try {
            return super.deleteLayout(layout);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout createLayout(long plid) {
        try {
            return super.createLayout(plid);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout copyLayout(long userId, long groupId, boolean privateLayout, Map<Locale, String> nameMap, boolean hidden, boolean system, boolean copyPermissions, long sourcePlid, ServiceContext serviceContext) throws PortalException {
        try {
            return super.copyLayout(userId, groupId, privateLayout, nameMap, hidden, system, copyPermissions, sourcePlid, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(String externalReferenceCode, long userId, long groupId, boolean privateLayout, long parentLayoutId, String name, String title, String description, String type, boolean hidden, String friendlyURL, ServiceContext serviceContext) throws PortalException {
        try {
            return super.addLayout(externalReferenceCode, userId, groupId, privateLayout, parentLayoutId, name, title, description, type, hidden, friendlyURL, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(String externalReferenceCode, long userId, long groupId, boolean privateLayout, long parentLayoutId, String name, String title, String description, String type, boolean hidden, boolean system, String friendlyURL, ServiceContext serviceContext) throws PortalException {
        try {
            return super.addLayout(externalReferenceCode, userId, groupId, privateLayout, parentLayoutId, name, title, description, type, hidden, system, friendlyURL, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(String externalReferenceCode, long userId, long groupId, boolean privateLayout, long parentLayoutId, Map<Locale, String> nameMap, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, Map<Locale, String> keywordsMap, Map<Locale, String> robotsMap, String type, String typeSettings, boolean hidden, Map<Locale, String> friendlyURLMap, ServiceContext serviceContext) throws PortalException {
        try {
            return super.addLayout(externalReferenceCode, userId, groupId, privateLayout, parentLayoutId, nameMap, titleMap, descriptionMap, keywordsMap, robotsMap, type, typeSettings, hidden, friendlyURLMap, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(String externalReferenceCode, long userId, long groupId, boolean privateLayout, long parentLayoutId, Map<Locale, String> nameMap, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, Map<Locale, String> keywordsMap, Map<Locale, String> robotsMap, String type, String typeSettings, boolean hidden, boolean system, Map<Locale, String> friendlyURLMap, ServiceContext serviceContext) throws PortalException {
        try {
            return super.addLayout(externalReferenceCode, userId, groupId, privateLayout, parentLayoutId, nameMap, titleMap, descriptionMap, keywordsMap, robotsMap, type, typeSettings, hidden, system, friendlyURLMap, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(String externalReferenceCode, long userId, long groupId, boolean privateLayout, long parentLayoutId, long classNameId, long classPK, Map<Locale, String> nameMap, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, Map<Locale, String> keywordsMap, Map<Locale, String> robotsMap, String type, String typeSettings, boolean hidden, boolean system, Map<Locale, String> friendlyURLMap, String masterLayoutPageTemplateEntryERC, ServiceContext serviceContext) throws PortalException {
        try {
            return super.addLayout(externalReferenceCode, userId, groupId, privateLayout, parentLayoutId, classNameId, classPK, nameMap, titleMap, descriptionMap, keywordsMap, robotsMap, type, typeSettings, hidden, system, friendlyURLMap, masterLayoutPageTemplateEntryERC, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout addLayout(Layout layout) {
        try {
            return super.addLayout(layout);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateName(Layout layout, String name, String languageId) throws PortalException {
        try {
            return super.updateName(layout, name, languageId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateName(long groupId, boolean privateLayout, long layoutId, String name, String languageId) throws PortalException {
        try {
            return super.updateName(groupId, privateLayout, layoutId, name, languageId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public Layout updateName(long plid, String name, String languageId) throws PortalException {
        try {
            return super.updateName(plid, name, languageId);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void updatePriorities(long groupId, boolean privateLayout) throws PortalException {
        try {
            super.updatePriorities(groupId, privateLayout);
        } finally {
            rebuildMenu();
        }
    }

    @Override
    public void setLayouts(long groupId, boolean privateLayout, long parentLayoutId, long[] layoutIds, ServiceContext serviceContext) throws PortalException {
        try {
            super.setLayouts(groupId, privateLayout, parentLayoutId, layoutIds, serviceContext);
        } finally {
            rebuildMenu();
        }
    }

    private void rebuildMenu() {
        menuAdapter.rebuildMenu();
    }
}
