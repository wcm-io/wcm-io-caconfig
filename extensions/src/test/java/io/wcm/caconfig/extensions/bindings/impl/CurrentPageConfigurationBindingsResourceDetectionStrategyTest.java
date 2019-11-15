package io.wcm.caconfig.extensions.bindings.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.adobe.cq.sightly.WCMBindings;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import javax.script.Bindings;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class CurrentPageConfigurationBindingsResourceDetectionStrategyTest {

    final AemContext context = new AemContext();

    @Mock
    private Page page;

    @Mock
    private Resource contentResource;

    @Mock
    private Bindings bindings;

    private CurrentPageConfigurationBindingsResourceDetectionStrategy underTest;

    @BeforeEach
    void before() {
        underTest = context.registerService(new CurrentPageConfigurationBindingsResourceDetectionStrategy());
    }

    @Test
    void returnsNullWhenNoPagePresent() {
        when(bindings.containsKey(WCMBindings.CURRENT_PAGE)).thenReturn(false);
        assertNull(underTest.detectResource(bindings));
    }

    @Test
    void returnsContentResourceWhenPageIsPresent() {
        when(bindings.containsKey(WCMBindings.CURRENT_PAGE)).thenReturn(true);
        when(bindings.get(WCMBindings.CURRENT_PAGE)).thenReturn(page);
        when(page.getContentResource()).thenReturn(contentResource);
        assertSame(contentResource, underTest.detectResource(bindings));
    }
}
