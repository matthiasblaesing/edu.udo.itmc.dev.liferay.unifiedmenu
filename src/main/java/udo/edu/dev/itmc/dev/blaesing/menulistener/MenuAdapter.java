
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

@Component(service = MenuAdapter.class, immediate = true)
public class MenuAdapter {
    private AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
    private ScheduledExecutorService executor;

    @Activate
    public void activate() {
        System.out.println("Activation");
        // 0 coreThreads causes a single thread to be spun up when a job is
        // executed and then shutdown when the job finishes
        executor = Executors.newScheduledThreadPool(0);
        rebuildMenu();
    }

    @Deactivate
    public void deactivate() {
        executor.shutdownNow();
        executor = null;
    }

    public void rebuildMenu() {
        future.updateAndGet(current -> current != null
                ? current
                : executor.schedule(this::rebuildMenu0, 5, TimeUnit.SECONDS));
    }

    private void rebuildMenu0() {
        future.set(null);
        System.out.println("Menu rebuild 2");
        for (Company c : CompanyLocalServiceUtil.getCompanies()) {
            for (Group group : GroupLocalServiceUtil.getGroups(c.getCompanyId(), -1, true)) {
                rebuildMenuForGroup(group);
            }
        }
    }

    private void rebuildMenuForGroup(Group group) {
        try {
            SiteNavigationMenu menu = SiteNavigationMenuLocalServiceUtil
                    .getSiteNavigationMenus(group.getGroupId())
                    .stream()
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
            System.out.println(ex);
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
                        new ServiceContext()
                );
            } else {
                item = existingEntries.removeFirst();
            }
            item.setType("layout");
            UnicodeProperties props = new UnicodeProperties();
            props.setProperty("externalReferenceCode", n.layout().getExternalReferenceCode());
            props.setProperty("privateLayout", Boolean.toString(n.layout().isPrivateLayout()));
            props.setProperty("title", n.layout().getNameCurrentValue());
            item.setTypeSettings(props.toString());
            item.setName(n.layout().getNameCurrentValue());
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
