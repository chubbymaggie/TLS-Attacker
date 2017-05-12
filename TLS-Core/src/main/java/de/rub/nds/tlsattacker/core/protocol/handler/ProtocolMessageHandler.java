/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.exceptions.AdjustmentException;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.handler.ParserResult;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.parser.Parser;
import de.rub.nds.tlsattacker.core.protocol.parser.ProtocolMessageParser;
import de.rub.nds.tlsattacker.core.protocol.parser.ServerHelloParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.Preparator;
import de.rub.nds.tlsattacker.core.protocol.preparator.ProtocolMessagePreparator;
import de.rub.nds.tlsattacker.core.protocol.preparator.ServerHelloMessagePreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.ProtocolMessageSerializer;
import de.rub.nds.tlsattacker.core.protocol.serializer.Serializer;
import de.rub.nds.tlsattacker.core.protocol.serializer.ServerHelloMessageSerializer;
import de.rub.nds.tlsattacker.core.workflow.TlsConfig;
import de.rub.nds.tlsattacker.core.workflow.TlsContext;
import de.rub.nds.modifiablevariable.util.ArrayConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 * @param <Message>
 */
public abstract class ProtocolMessageHandler<Message extends ProtocolMessage> {

    protected static final Logger LOGGER = LogManager.getLogger("Handler");

    /**
     * tls context
     */
    protected final TlsContext tlsContext;

    /**
     *
     * @param tlsContext
     */
    public ProtocolMessageHandler(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        if (tlsContext == null) {
            throw new ConfigurationException("TLS Context is not configured yet");
        }
    }

    /**
     * Prepare message for sending. This method invokes before and after method
     * hooks.
     *
     * @return message in bytes
     */
    public byte[] prepareMessage(Message message) {
        Preparator preparator = getPreparator(message);
        preparator.prepare();
        try {
            adjustTLSContext(message);
        } catch (AdjustmentException E) {
            LOGGER.warn("Could not adjust TLSContext");
            LOGGER.debug(E);
        }
        Serializer serializer = getSerializer(message);
        byte[] completeMessage = serializer.serialize();
        message.setCompleteResultingMessage(completeMessage);
        if (message instanceof HandshakeMessage) {
            if (((HandshakeMessage) message).getIncludeInDigest()) {
                tlsContext.getDigest().update(message.getCompleteResultingMessage().getValue());
            }
        }
        return message.getCompleteResultingMessage().getValue();
    }

    /**
     * Parses a byteArray from a Position into a MessageObject and returns the
     * parsed MessageObjet and parser position in a parser result. The current
     * TlsContext is adjusted as
     *
     * @param message
     * @param pointer
     * @return
     */
    public ParserResult parseMessage(byte[] message, int pointer) {
        Parser<Message> parser = getParser(message, pointer);
        Message parsedMessage = parser.parse();
        try {
            adjustTLSContext(parsedMessage);
        } catch (AdjustmentException E) {
            LOGGER.warn("Could not adjust TLSContext");
            LOGGER.debug(E);
        }
        if (parsedMessage instanceof HandshakeMessage) {
            if (((HandshakeMessage) parsedMessage).getIncludeInDigest()) {
                tlsContext.getDigest().update(parsedMessage.getCompleteResultingMessage().getValue());
            }
        }
        return new ParserResult(parsedMessage, parser.getPointer());
    }

    public abstract ProtocolMessageParser getParser(byte[] message, int pointer);

    public abstract ProtocolMessagePreparator getPreparator(Message message);

    public abstract ProtocolMessageSerializer getSerializer(Message message);

    /**
     * Adjusts the TLS Context according to the received or sending
     * ProtocolMessage
     *
     * @param message
     */
    protected abstract void adjustTLSContext(Message message);
}