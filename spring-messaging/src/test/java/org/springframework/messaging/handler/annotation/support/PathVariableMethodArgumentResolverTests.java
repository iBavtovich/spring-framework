/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.handler.annotation.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.PathVariable;
import org.springframework.messaging.simp.handler.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.Assert.*;

/**
 * Test fixture for {@link PathVariableMethodArgumentResolver} tests.
 * @author Brian Clozel
 */
public class PathVariableMethodArgumentResolverTests {

	private PathVariableMethodArgumentResolver resolver;

	private MethodParameter paramAnnotated;
	private MethodParameter paramAnnotatedValue;
	private MethodParameter paramNotAnnotated;

	@Before
	public void setup() throws Exception {
		this.resolver = new PathVariableMethodArgumentResolver(new DefaultConversionService());

		Method method = getClass().getDeclaredMethod("handleMessage",
				String.class, String.class, String.class);
		this.paramAnnotated = new MethodParameter(method, 0);
		this.paramAnnotatedValue = new MethodParameter(method, 1);
		this.paramNotAnnotated = new MethodParameter(method, 2);

		this.paramAnnotated.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
		GenericTypeResolver.resolveParameterType(this.paramAnnotated, PathVariableMethodArgumentResolver.class);
		this.paramAnnotatedValue.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
		GenericTypeResolver.resolveParameterType(this.paramAnnotatedValue, PathVariableMethodArgumentResolver.class);
	}

	@Test
	public void supportsParameter() {
		assertTrue(resolver.supportsParameter(paramAnnotated));
		assertTrue(resolver.supportsParameter(paramAnnotatedValue));
		assertFalse(resolver.supportsParameter(paramNotAnnotated));
	}

	@Test
	public void resolveArgument() throws Exception {
		Map<String,Object> pathParams = new HashMap<String,Object>();
		pathParams.put("foo","bar");
		pathParams.put("name","value");
		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
				.setHeader(PathVariableMethodArgumentResolver.PATH_TEMPLATE_VARIABLES_HEADER, pathParams).build();
		Object result = this.resolver.resolveArgument(this.paramAnnotated, message);
		assertEquals("bar",result);
		result = this.resolver.resolveArgument(this.paramAnnotatedValue, message);
		assertEquals("value",result);
	}

	@Test(expected = MessageHandlingException.class)
	public void resolveArgumentNotFound() throws Exception {
		Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();
		this.resolver.resolveArgument(this.paramAnnotated, message);
	}

	@SuppressWarnings("unused")
	private void handleMessage(
			@PathVariable String foo,
			@PathVariable(value = "name") String param1,
			String param3) {
	}
}