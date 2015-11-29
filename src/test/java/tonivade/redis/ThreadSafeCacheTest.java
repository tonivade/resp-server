/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThreadSafeCacheTest {

    private static final String KEY = "key";

    private ThreadSafeCache<String, String> cache;

    @Mock
    private Consumer<String> consumer;

    @Before
    public void setUp() {
        cache = new ThreadSafeCache<>();
    }

    @Test
    public void getOrCreate() throws Exception {
        String result = cache.get(KEY, Function.identity(), System.out::println);

        assertThat(result, equalTo(KEY));
    }

    @Test
    public void callbackIsCalled() throws Exception {
        cache.get(KEY, Function.identity(), consumer);

        verify(consumer, times(1)).accept(KEY);
    }

    @Test
    public void callbackIsCalledOnlyOnce() throws Exception {
        cache.get(KEY, Function.identity(), consumer);
        cache.get(KEY, Function.identity(), consumer);

        verify(consumer, times(1)).accept(KEY);
    }

}
