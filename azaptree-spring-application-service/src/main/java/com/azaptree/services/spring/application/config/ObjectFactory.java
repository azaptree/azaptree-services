//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.02 at 06:38:20 PM EST 
//


package com.azaptree.services.spring.application.config;

/*
 * #%L
 * AZAPTREE-SPRING-APPLICATION-SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
 * %%
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
 * #L%
 */

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.azaptree.services.spring.application.config package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.azaptree.services.spring.application.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SpringApplicationService }
     * 
     */
    public SpringApplicationService createSpringApplicationService() {
        return new SpringApplicationService();
    }

    /**
     * Create an instance of {@link SpringApplicationService.JvmSystemProperties }
     * 
     */
    public SpringApplicationService.JvmSystemProperties createSpringApplicationServiceJvmSystemProperties() {
        return new SpringApplicationService.JvmSystemProperties();
    }

    /**
     * Create an instance of {@link SpringApplicationService.ConfigurationClasses }
     * 
     */
    public SpringApplicationService.ConfigurationClasses createSpringApplicationServiceConfigurationClasses() {
        return new SpringApplicationService.ConfigurationClasses();
    }

    /**
     * Create an instance of {@link SpringApplicationService.ConfigurationPackages }
     * 
     */
    public SpringApplicationService.ConfigurationPackages createSpringApplicationServiceConfigurationPackages() {
        return new SpringApplicationService.ConfigurationPackages();
    }

    /**
     * Create an instance of {@link SpringApplicationService.JvmSystemProperties.Prop }
     * 
     */
    public SpringApplicationService.JvmSystemProperties.Prop createSpringApplicationServiceJvmSystemPropertiesProp() {
        return new SpringApplicationService.JvmSystemProperties.Prop();
    }

}
