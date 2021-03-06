/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.config.delegate;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import java.security.Provider;
import java.security.Security;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class GeneralAttackDelegate extends GeneralDelegate {

    public GeneralAttackDelegate() {
    }

    @Override
    public void applyDelegate(Config config) {
        Security.addProvider(new BouncyCastleProvider());
        if (isDebug()) {
            setLogLevel(Level.DEBUG);
        }
        Configurator.setRootLevel(getLogLevel());
        Configurator.setAllLevels("de.rub.nds.modifiablevariable", Level.FATAL);
        if (getLogLevel() == Level.ALL) {
            Configurator.setAllLevels("de.rub.nds.tlsattacker.core", Level.ALL);
            Configurator.setAllLevels("de.rub.nds.tlsattacker.transport", Level.DEBUG);
        } else if (getLogLevel() == Level.TRACE) {
            Configurator.setAllLevels("de.rub.nds.tlsattacker.core", Level.DEBUG);
            Configurator.setAllLevels("de.rub.nds.tlsattacker.transport", Level.DEBUG);
        } else {
            Configurator.setAllLevels("de.rub.nds.tlsattacker.core", Level.OFF);
        }
        LOGGER.debug("Using the following security providers");
        for (Provider p : Security.getProviders()) {
            LOGGER.debug("Provider {}, version, {}", p.getName(), p.getVersion());
        }

        // remove stupid Oracle JDK security restriction (otherwise, it is not
        // possible to use strong crypto with Oracle JDK)
        UnlimitedStrengthEnabler.enable();
    }
}
