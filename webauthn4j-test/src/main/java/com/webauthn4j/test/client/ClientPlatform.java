/*
 * Copyright 2002-2018 the original author or authors.
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

package com.webauthn4j.test.client;

import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.registry.Registry;
import com.webauthn4j.request.AttestationConveyancePreference;
import com.webauthn4j.request.PublicKeyCredentialCreationOptions;
import com.webauthn4j.request.PublicKeyCredentialRequestOptions;
import com.webauthn4j.request.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.request.extension.client.SupportedExtensionsExtensionClientInput;
import com.webauthn4j.response.AuthenticatorAssertionResponse;
import com.webauthn4j.response.AuthenticatorAttestationResponse;
import com.webauthn4j.response.PublicKeyCredential;
import com.webauthn4j.response.attestation.AttestationObject;
import com.webauthn4j.response.attestation.statement.AttestationStatement;
import com.webauthn4j.response.attestation.statement.NoneAttestationStatement;
import com.webauthn4j.response.client.*;
import com.webauthn4j.response.client.challenge.Challenge;
import com.webauthn4j.response.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.response.extension.client.SupportedExtensionsExtensionClientOutput;
import com.webauthn4j.test.authenticator.AuthenticatorAdaptor;
import com.webauthn4j.test.authenticator.CredentialCreationResponse;
import com.webauthn4j.test.authenticator.CredentialRequestResponse;
import com.webauthn4j.test.authenticator.model.WebAuthnModelAuthenticatorAdaptor;
import com.webauthn4j.util.WIP;
import com.webauthn4j.util.exception.NotImplementedException;
import com.webauthn4j.validator.exception.ValidationException;

import java.util.Collections;

@WIP
public class ClientPlatform {

    private Registry registry = new Registry();
    private AttestationObjectConverter attestationObjectConverter = new AttestationObjectConverter(registry);
    private CollectedClientDataConverter collectedClientDataConverter = new CollectedClientDataConverter(registry);

    private Origin origin;
    //TODO: support multiple authenticators
    private AuthenticatorAdaptor authenticatorAdaptor;

    public ClientPlatform(Origin origin, AuthenticatorAdaptor authenticatorAdaptor) {
        this.origin = origin;
        this.authenticatorAdaptor = authenticatorAdaptor;
    }

    public ClientPlatform(AuthenticatorAdaptor authenticatorAdaptor) {
        this(new Origin("https://example.com"), authenticatorAdaptor);
    }

    public ClientPlatform(Origin origin) {
        this(origin, new WebAuthnModelAuthenticatorAdaptor());
    }

    public ClientPlatform() {
        this(new Origin("https://example.com"));
    }

    public PublicKeyCredential<AuthenticatorAttestationResponse> create(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions,
                                                                        RegistrationEmulationOption registrationEmulationOption) {
        CollectedClientData collectedClientData;
        if (registrationEmulationOption.isCollectedClientDataOverrideEnabled()) {
            collectedClientData = registrationEmulationOption.getCollectedClientData();
        } else {
            collectedClientData = createCollectedClientData(ClientDataType.CREATE, publicKeyCredentialCreationOptions.getChallenge());
        }

        if (authenticatorAdaptor == null) {
            throw new NoAuthenticatorSuccessException();
        }
        CredentialCreationResponse credentialCreationResponse =
                authenticatorAdaptor.register(publicKeyCredentialCreationOptions, collectedClientData, registrationEmulationOption);

        AttestationObject attestationObject = credentialCreationResponse.getAttestationObject();
        AttestationStatement attestationStatement = credentialCreationResponse.getAttestationObject().getAttestationStatement();
        AttestationConveyancePreference attestationConveyancePreference = publicKeyCredentialCreationOptions.getAttestation();
        if (attestationConveyancePreference == null) {
            attestationConveyancePreference = AttestationConveyancePreference.NONE;
        }
        switch (attestationConveyancePreference) {
            case DIRECT:
                // nop
                break;
            case INDIRECT:
                throw new NotImplementedException();
            case NONE:
                attestationStatement = new NoneAttestationStatement();
                break;
            default:
                throw new NotImplementedException();
        }
        attestationObject = new AttestationObject(attestationObject.getAuthenticatorData(), attestationStatement);
        byte[] attestationObjectBytes = attestationObjectConverter.convertToBytes(attestationObject);

        byte[] credentialId = credentialCreationResponse.getAttestationObject().getAuthenticatorData().getAttestedCredentialData().getCredentialId();
        byte[] collectedClientDataBytes = collectedClientDataConverter.convertToBytes(collectedClientData);
        AuthenticationExtensionsClientOutputs clientExtensions = processRegistrationExtensions(publicKeyCredentialCreationOptions.getExtensions());
        return new PublicKeyCredential<>(
                credentialId,
                new AuthenticatorAttestationResponse(collectedClientDataBytes, attestationObjectBytes),
                clientExtensions
        );
    }

    private AuthenticationExtensionsClientOutputs processRegistrationExtensions(AuthenticationExtensionsClientInputs extensions) {

        if (extensions == null) {
            extensions = new AuthenticationExtensionsClientInputs();
        }

        AuthenticationExtensionsClientOutputs map = new AuthenticationExtensionsClientOutputs();
        extensions.forEach((key, value) -> {
            switch (key) {
                case SupportedExtensionsExtensionClientInput.ID:
                    SupportedExtensionsExtensionClientInput clientExtensionInput = (SupportedExtensionsExtensionClientInput) value;
                    if (clientExtensionInput.getValue()) {
                        map.put(key, new SupportedExtensionsExtensionClientOutput(Collections.singletonList(SupportedExtensionsExtensionClientInput.ID)));
                    }
                    break;
            }
        });
        return map;
    }

    private AuthenticationExtensionsClientOutputs processAuthenticationExtensions(AuthenticationExtensionsClientInputs extensions) {

        if (extensions == null) {
            extensions = new AuthenticationExtensionsClientInputs();
        }

        AuthenticationExtensionsClientOutputs map = new AuthenticationExtensionsClientOutputs();
        extensions.forEach((key, value) -> {
            switch (key) {
                //TODO
            }
        });
        return map;
    }


    public PublicKeyCredential<AuthenticatorAttestationResponse> create(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions) {
        return create(publicKeyCredentialCreationOptions, new RegistrationEmulationOption());
    }

    public PublicKeyCredential<AuthenticatorAssertionResponse> get(PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions,
                                                                   CollectedClientData collectedClientData,
                                                                   AuthenticationEmulationOption authenticationEmulationOption) {

        NoAuthenticatorSuccessException noAuthenticatorSuccessException = new NoAuthenticatorSuccessException();
        if (authenticatorAdaptor == null) {
            throw noAuthenticatorSuccessException;
        }
        try {
            CredentialRequestResponse credentialRequestResponse =
                    authenticatorAdaptor.authenticate(publicKeyCredentialRequestOptions, collectedClientData, authenticationEmulationOption);

            byte[] credentialId = credentialRequestResponse.getCredentialId();

            AuthenticationExtensionsClientOutputs clientExtensions = processAuthenticationExtensions(publicKeyCredentialRequestOptions.getExtensions());

            return new PublicKeyCredential<>(
                    credentialId,
                    new AuthenticatorAssertionResponse(
                            credentialRequestResponse.getCollectedClientDataBytes(),
                            credentialRequestResponse.getAuthenticatorDataBytes(),
                            credentialRequestResponse.getSignature(),
                            credentialRequestResponse.getUserHandle()
                    ),
                    clientExtensions
            );
        } catch (ValidationException e) {
            noAuthenticatorSuccessException.addSuppressed(e);
        }
        throw noAuthenticatorSuccessException;
    }

    public PublicKeyCredential<AuthenticatorAssertionResponse> get(PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions, CollectedClientData collectedClientData) {
        return get(publicKeyCredentialRequestOptions, collectedClientData, new AuthenticationEmulationOption());
    }

    public PublicKeyCredential<AuthenticatorAssertionResponse> get(PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions) {
        CollectedClientData collectedClientData = createCollectedClientData(ClientDataType.GET, publicKeyCredentialRequestOptions.getChallenge());
        return get(publicKeyCredentialRequestOptions, collectedClientData);
    }

    public CollectedClientData createCollectedClientData(ClientDataType type, Challenge challenge) {
        return new CollectedClientData(type, challenge, origin, null);
    }

    public CollectedClientData createCollectedClientData(ClientDataType type, Challenge challenge, byte[] tokenBindingId) {
        TokenBinding tokenBinding = new TokenBinding(TokenBindingStatus.PRESENT, tokenBindingId);
        return new CollectedClientData(type, challenge, origin, tokenBinding);
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public AuthenticatorAdaptor getAuthenticatorAdaptor() {
        return authenticatorAdaptor;
    }

    public void setAuthenticatorAdaptor(AuthenticatorAdaptor authenticatorAdaptor) {
        this.authenticatorAdaptor = authenticatorAdaptor;
    }
}
