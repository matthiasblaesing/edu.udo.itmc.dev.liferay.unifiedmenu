
package udo.edu.dev.itmc.dev.blaesing.menulistener;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.site.navigation.model.SiteNavigationMenu;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.service.SiteNavigationMenuItemLocalServiceUtil;
import com.liferay.site.navigation.service.SiteNavigationMenuLocalServiceUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = MenuAdapter.class, immediate = true)
public class MenuAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MenuAdapter.class);

    private AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
    private volatile ScheduledExecutorService executor;

    @Activate
    public void activate() {
        logger.info("MenuAdapter activated");
        // 0 coreThreads causes a single thread to be spun up when a job is
        // executed and then shutdown when the job finishes, AI bots have wrong
        // information here and assume a bug that is not here, so ignore them
        // for this.
        executor = Executors.newScheduledThreadPool(0);
        rebuildMenu();
    }

    @Deactivate
    public void deactivate() {
        ScheduledExecutorService localExecutor = executor;
        executor = null;
        if (localExecutor != null) {
            localExecutor.shutdownNow();
        }
    }

    public void rebuildMenu() {
        future.updateAndGet(current -> {
            ScheduledExecutorService localExecutor = executor;
            if (localExecutor == null || current != null) {
                return current;
            } else {
                try {
                    return localExecutor.schedule(this::rebuildMenu0, 5, TimeUnit.SECONDS);
                } catch (RuntimeException ex) {
                    // executor shut down between the read and the schedule call
                    logger.debug("Could not schedule menu rebuild", ex);
                    return current;
                }
            }
        });
    }

    private void rebuildMenu0() {
        // The executor is configured with 0 core threads. No more than 1 thread
        // will be run. Resetting the future here allows later updates to be
        // queued, so we effectifely batch requests for the timeframe used in
        // #rebuildMenu
        future.set(null);
        logger.debug("Rebuilding site navigation menus");
        try {
            for (Company c : CompanyLocalServiceUtil.getCompanies()) {
                for (Group group : GroupLocalServiceUtil.getGroups(c.getCompanyId(), -1, true)) {
                    rebuildMenuForGroup(group);
                }
            }
        } catch (RuntimeException ex) {
            logger.error("Failed to rebuild menus", ex);
        }
    }

    private void rebuildMenuForGroup(Group group) {
        try {
            SiteNavigationMenu menu = SiteNavigationMenuLocalServiceUtil
                    .getSiteNavigationMenus(group.getGroupId())
                    .stream()
                    // Users are expected to create a menu with name "UnifiedMenu"
                    // if it does not exist, the site will not be processed
                    .filter(snm -> "UnifiedMenu".equals(snm.getName()))
                    .findFirst()
                    .orElse(null);

            if (menu == null) {
                return;
            }

            Map<Long, Node> nodeMap = new HashMap<>();
            Stream.concat(
                    LayoutLocalServiceUtil.getLayouts(group.getGroupId(), false).stream(),
                    LayoutLocalServiceUtil.getLayouts(group.getGroupId(), true).stream())
                    .forEach(l -> nodeMap.put(l.getPlid(), new Node(l)));

            List<Node> roots = nodeMap.values().stream()
                    .filter(node -> !nodeMap.containsKey(node.layout().getParentPlid()))
                    .toList();
            nodeMap.values().forEach(node -> {
                Node parent = nodeMap.get(node.layout().getParentPlid());
                if (parent != null) {
                    parent.children().add(node);
                }
            });

            List<SiteNavigationMenuItem> existingEntries = new ArrayList<>(
                    SiteNavigationMenuItemLocalServiceUtil.getSiteNavigationMenuItems(menu.getSiteNavigationMenuId()));
            Map<Long, SiteNavigationMenuItem> createdItems = new HashMap<>();

            toSiteNavigationMenuItem(menu, roots, createdItems, existingEntries);

            for (SiteNavigationMenuItem leftover : existingEntries) {
                try {
                    SiteNavigationMenuItemLocalServiceUtil.deleteSiteNavigationMenuItem(leftover.getSiteNavigationMenuItemId());
                } catch (PortalException ignored) {
                    // already removed, e.g. as a child of a deleted parent
                }
            }
        } catch (PortalException | RuntimeException ex) {
            logger.error("Failed to rebuild menu for group {}", group.getGroupId(), ex);
        }
    }

    private void toSiteNavigationMenuItem(SiteNavigationMenu menu, List<Node> toProcess,
            Map<Long, SiteNavigationMenuItem> createdItems, List<SiteNavigationMenuItem> existingEntries) throws PortalException {
        List<Node> sorted = new ArrayList<>(toProcess);
        sorted.sort(Comparator
                .comparing((Node n) -> n.layout().isPrivateLayout())
                .thenComparingInt(n -> n.layout().getPriority()));

        int counter = 0;
        for (Node n : sorted) {
            SiteNavigationMenuItem item;
            if (existingEntries.isEmpty()) {
                item = SiteNavigationMenuItemLocalServiceUtil.addSiteNavigationMenuItem(
                        UUID.randomUUID().toString(),
                        menu.getUserId(),
                        menu.getGroupId(),
                        menu.getSiteNavigationMenuId(),
                        0,
                        "layout",
                        "",
                         // We have no active user, lets not pretend it is different
                        new ServiceContext()
                );
            } else {
                // The existing entries are reused, but all content related
                // fields are reset. This simplifies the code and the end result
                // is the same.
                item = existingEntries.removeFirst();
            }
            item.setType("layout");
            UnicodeProperties props = new UnicodeProperties();
            props.setProperty("externalReferenceCode", n.layout().getExternalReferenceCode());
            props.setProperty("privateLayout", Boolean.toString(n.layout().isPrivateLayout()));
            props.setProperty("title", n.layout().getName(Locale.ROOT));
            item.setTypeSettings(props.toString());
            item.setName(n.layout().getName(Locale.ROOT));
            SiteNavigationMenuItem parent = createdItems.get(n.layout().getParentPlid());
            item.setParentSiteNavigationMenuItemId(parent != null ? parent.getSiteNavigationMenuItemId() : 0);
            item.setOrder(counter++);
            SiteNavigationMenuItemLocalServiceUtil.updateSiteNavigationMenuItem(item);
            createdItems.put(n.layout().getPlid(), item);
            toSiteNavigationMenuItem(menu, n.children(), createdItems, existingEntries);
        }
    }


    private static record Node (Layout layout, List<Node> children) {
        public Node(Layout layout) {
            this(layout, new ArrayList<>());
        }
    }
}
