package com.azaptree.services.security.domain;

/*
 * #%L
 * AZAPTREE SECURITY SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.azaptree.services.domain.entity.VersionedEntity;

/**
 * The Subject's entity id is used as the Subject's primary principal.
 * 
 * Applications will need to map the subject to an application specific principal.
 * For example, when a new user is created by an application, the application may use the user's e-mail address as the primary principal. The application will
 * then need to create a new Subject that is mapped to the user.
 * 
 * @author alfio
 * 
 */
public interface Subject extends VersionedEntity {

}
