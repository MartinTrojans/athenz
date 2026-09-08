/*
 * Copyright The Athenz Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahoo.athenz.zts.cert.impl.crypki;

import com.yahoo.athenz.common.server.cert.CertSigner;
import com.yahoo.athenz.common.server.cert.CertSignerFactory;
import com.yahoo.athenz.zts.ResourceException;
import com.yahoo.athenz.zts.ZTSConsts;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

public class JavaCrypkiCertSignerFactoryTest {

    @AfterMethod
    public void cleanup() {
        System.clearProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED);
        System.clearProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS);
    }

    @Test
    public void testDisabledByDefault() {
        expectThrows(ResourceException.class, () -> new JavaCrypkiCertSignerFactory().create());
    }

    @Test
    public void testEnabledRequiresFactoryClass() {
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED, "true");
        expectThrows(ResourceException.class, () -> new JavaCrypkiCertSignerFactory().create());
    }

    @Test
    public void testEnabledCreatesBackendFactory() {
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED, "true");
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS, DummyCertSignerFactory.class.getName());
        assertNotNull(new JavaCrypkiCertSignerFactory().create());
    }

    @Test
    public void testInvalidFactoryClass() {
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED, "true");
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS, "invalid.Factory");
        expectThrows(ResourceException.class, () -> new JavaCrypkiCertSignerFactory().create());
    }

    @Test
    public void testBackendResourceExceptionIsRethrown() {
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED, "true");
        System.setProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS, FailingCertSignerFactory.class.getName());
        expectThrows(ResourceException.class, () -> new JavaCrypkiCertSignerFactory().create());
    }

    public static final class DummyCertSignerFactory implements CertSignerFactory {
        @Override
        public CertSigner create() {
            return Mockito.mock(CertSigner.class);
        }
    }

    public static final class FailingCertSignerFactory implements CertSignerFactory {
        @Override
        public CertSigner create() {
            throw new ResourceException(ResourceException.INTERNAL_SERVER_ERROR, "backend failed");
        }
    }
}
