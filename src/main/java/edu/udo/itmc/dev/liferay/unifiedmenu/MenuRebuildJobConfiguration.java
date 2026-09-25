
package edu.udo.itmc.dev.liferay.unifiedmenu;

import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Job für die periodische Regenerierung der Menüstruktur
 */
@Component(service = SchedulerJobConfiguration.class)
public class MenuRebuildJobConfiguration implements SchedulerJobConfiguration {

    @Reference
    private MenuAdapter menuAdapter;

    @Override
    public UnsafeRunnable<Exception> getJobExecutorUnsafeRunnable() {
        return () -> {
            menuAdapter.rebuildMenu();
        };
    }

    @Override
    public TriggerConfiguration getTriggerConfiguration() {
        return TriggerConfiguration.createTriggerConfiguration(6, TimeUnit.HOUR);
    }
}
