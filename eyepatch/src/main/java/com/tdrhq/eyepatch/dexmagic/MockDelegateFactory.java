// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.LinkedList;
import static org.mockito.Mockito.*;

public class MockDelegateFactory {
    static int MAX_DELEGATES = 1000;

    LinkedList<MockDelegate> queue = new LinkedList<>();

    public MockDelegateFactory() {
        for (int i = 0; i < MAX_DELEGATES; i++) {
            queue.push(mock(MockDelegate.class));
        }
    }

    public void init(Class klass) {

    }

    public MockDelegate create() {
        return queue.pop();
    }

    private static MockDelegateFactory sInstance;
    public synchronized static MockDelegateFactory getInstance() {

        if (sInstance == null) {
            sInstance = new MockDelegateFactory();
        }

        return sInstance;
    }
}
