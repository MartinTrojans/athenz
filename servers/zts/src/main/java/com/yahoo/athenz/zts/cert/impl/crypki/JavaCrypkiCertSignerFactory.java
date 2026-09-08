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
import org.eclipse.jetty.util.StringUtil;

/**
 * Opt-in factory for the in-process Java Crypki backends. Existing
 * {@link HttpCertSignerFactory} deployments are unchanged unless
 * {@code athenz.zts.java_crypki_enabled=true} and this factory is
 * selected via {@code athenz.zts.cert_signer_factory_class}.
 */
public class JavaCrypkiCertSignerFactory implements CertSignerFactory {

    @Override
    public CertSigner create() {
        if (!Boolean.parseBoolean(System.getProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED, "false"))) {
            throw new ResourceException(ResourceException.INTERNAL_SERVER_ERROR,
                    "Java Crypki is disabled; set " + ZTSConsts.ZTS_PROP_JAVA_CRYPKI_ENABLED + "=true");
        }
        final String factoryClass = System.getProperty(ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS);
        if (StringUtil.isEmpty(factoryClass)) {
            throw new ResourceException(ResourceException.INTERNAL_SERVER_ERROR,
                    "Missing " + ZTSConsts.ZTS_PROP_JAVA_CRYPKI_FACTORY_CLASS);
        }
        try {
            CertSignerFactory factory = (CertSignerFactory) Class.forName(factoryClass)
                    .getDeclaredConstructor().newInstance();
            return factory.create();
        } catch (ResourceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceException(ResourceException.INTERNAL_SERVER_ERROR,
                    "Unable to create Java Crypki signer: " + ex.getMessage());
        }
    }
}
