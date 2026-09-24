
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
import java.util.HashMap;
import java.util.Iterator;
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
        executor = Executors.newScheduledThreadPool(0);
        rebuildMenu();
    }

    @Deactivate
    public void deactivate() {
        executor.shutdownNow();
        executor = null;
    }

    public void rebuildMenu() {
        future.updateAndGet(currentValue -> {
           if(currentValue == null) {
               return executor.schedule(() -> rebuildMenu0(), 5, TimeUnit.SECONDS);
           } else {
               return currentValue;
           }
        });
    }

    public void rebuildMenu0() {
        future.set(null);
        System.out.println("Menu rebuild 2");
        for(Company c : CompanyLocalServiceUtil.getCompanies()) {
            for (Group group: GroupLocalServiceUtil.getGroups(c.getCompanyId(), -1, true) ) {
                try {
                SiteNavigationMenu menu = SiteNavigationMenuLocalServiceUtil
                        .getSiteNavigationMenus(group.getGroupId())
                        .stream()
                        .filter(snm -> "UnifiedMenu".equals(snm.getName()))
                        .findFirst()
                        .orElse(null);

                if(menu == null) {
                    continue;
                }

                Map<Long, Node> nodeMap = new HashMap<>();
                List<Layout> publicLayouts = LayoutLocalServiceUtil.getLayouts(group.getGroupId(), false);
                publicLayouts.get(0).isFirstChild();
                List<Layout> privateLayouts = LayoutLocalServiceUtil.getLayouts(group.getGroupId(), true);
                Stream.concat(publicLayouts.stream(), privateLayouts.stream())
                        .forEach(l -> nodeMap.put(l.getPlid(), new Node(l)));
                List<Node> nodesWithoutParent = new ArrayList<>(nodeMap.values());
                Iterator<Node> nodesWithoutParentIterator = nodesWithoutParent.iterator();
                while(nodesWithoutParentIterator.hasNext()) {
                    Node node = nodesWithoutParentIterator.next();
                    if(nodeMap.containsKey(node.layout().getParentPlid())) {
                        nodesWithoutParentIterator.remove();
                        nodeMap.get(node.layout().getParentPlid()).children().add(node);
                    }
                }

                List<SiteNavigationMenuItem> existingEntries = new ArrayList<>(SiteNavigationMenuItemLocalServiceUtil.getSiteNavigationMenuItems(menu.getSiteNavigationMenuId()));
                Map<Long, SiteNavigationMenuItem> navigationEntries = new HashMap<>();

                toSiteNavigationMenuItem(menu, nodesWithoutParent, nodeMap, navigationEntries, existingEntries);
                } catch (PortalException | RuntimeException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    private void toSiteNavigationMenuItem(SiteNavigationMenu menu, List<Node> toProcess, Map<Long, Node> input, Map<Long, SiteNavigationMenuItem> snmi, List<SiteNavigationMenuItem> existingEntries) throws PortalException {
        int counter = 0;
        List<Node> toProcessSorted = new ArrayList<>(toProcess);
        toProcessSorted.sort((n1, n2) -> {
            int priv1 = n1.layout().isPrivateLayout() ? 1 : -1;
            int priv2 = n2.layout().isPrivateLayout() ? 1 : -1;
            if(priv1 != priv2) {
                return priv1 - priv2;
            }
            return Integer.compare(n1.layout().getPriority(), n2.layout().getPriority());
        });
        for (Node n : toProcessSorted) {
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
            if(snmi.containsKey(n.layout().getParentPlid())) {
                item.setParentSiteNavigationMenuItemId(snmi.get(n.layout().getParentPlid()).getSiteNavigationMenuItemId());
            } else {
                item.setParentSiteNavigationMenuItemId(0);
            }
            item.setOrder(counter);
            counter++;
            SiteNavigationMenuItemLocalServiceUtil.updateSiteNavigationMenuItem(item);
            snmi.put(n.layout().getPlid(), item);
            toSiteNavigationMenuItem(menu, n.children(), input, snmi, existingEntries);
        }
    }


    private static record Node (Layout layout, List<Node> children) {
        public Node(Layout layout) {
            this(layout, new ArrayList<>());
        }
    }
}
