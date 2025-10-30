package co.com.bancolombia.ecs.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractMiddlewareEcsLogTest {
    @Test
    void testShouldCallProcessAndNext() {
        Object mockRequest = new Object();
        String service = "test-service";

        TestAbstractMiddleware first = spy(new TestAbstractMiddleware());
        TestAbstractMiddleware second = spy(new TestAbstractMiddleware());

        first.setNext(second);

        first.handler(mockRequest, service);

        verify(first, times(1)).process(mockRequest, service);
        verify(second, times(1)).process(mockRequest, service);
    }

    @Test
    void testShouldOnlyCallFirstWhenNoNext() {
        Object mockRequest = new Object();
        String service = "test-service";

        TestAbstractMiddleware only = spy(new TestAbstractMiddleware());

        only.handler(mockRequest, service);

        verify(only, times(1)).process(mockRequest, service);
    }

    static class TestAbstractMiddleware extends AbstractMiddlewareEcsLog {
        @Override
        protected void process(Object request, String service) {
            // Mockito Test
        }
    }
}
